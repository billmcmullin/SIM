package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AllSessionsServlet", urlPatterns = {"/dashboard/sessions", "/dashboard/sessions.json"})
public class AllSessionsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AllSessionsServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    private static final long serialVersionUID = 1L;

    private static final class SessionStats {

        long count;
        Timestamp last;

        // per-widget counts keyed by display name
        Map<String, Long> widgetCounts = new HashMap<>();

        // computed fields
        String topWidgetId;
        String topWidgetName;
        long topWidgetCount;
        String widgetNamesCombined;      // e.g. "Widget A, Widget B"
        List<String> widgetNamesOrdered; // ordered by contribution desc, used for cycling sort

        void addCount(long c, String widgetId, String widgetName) {
            this.count += c;
            String nameKey = (widgetName == null || widgetName.isBlank()) ? widgetId : widgetName;
            if (nameKey == null) {
                nameKey = "";
            }
            this.widgetCounts.put(nameKey, this.widgetCounts.getOrDefault(nameKey, 0L) + c);
        }

        void updateLast(Timestamp t) {
            if (t == null) {
                return;
            }
            if (this.last == null || t.after(this.last)) {
                this.last = t;
            }
        }

        void finalizeWidgetNames() {
            // compute ordered list of widget names by contribution desc, tie-break by name
            List<Map.Entry<String, Long>> list = new ArrayList<>(widgetCounts.entrySet());
            Collections.sort(list, (a, b) -> {
                int cmp = Long.compare(b.getValue(), a.getValue()); // desc by count
                if (cmp != 0) {
                    return cmp;
                }
                // tie-break by case-insensitive name
                String an = a.getKey() == null ? "" : a.getKey();
                String bn = b.getKey() == null ? "" : b.getKey();
                return an.compareToIgnoreCase(bn);
            });
            List<String> ordered = new ArrayList<>();
            String bestName = null;
            long bestCount = -1;
            for (Map.Entry<String, Long> en : list) {
                ordered.add(en.getKey());
                if (en.getValue() > bestCount) {
                    bestCount = en.getValue();
                    bestName = en.getKey();
                }
            }
            this.widgetNamesOrdered = ordered;
            this.widgetNamesCombined = String.join(", ", ordered);
            if (this.widgetNamesCombined.isEmpty()) {
                this.widgetNamesCombined = null;
            }
            this.topWidgetName = bestName;
            this.topWidgetCount = bestCount < 0 ? 0 : bestCount;
        }
    }

    // Pagination defaults / limits
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 500;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Determine if JSON requested
        boolean wantsJson = req.getRequestURI().endsWith(".json")
                || "json".equalsIgnoreCase(req.getParameter("format"))
                || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        // Parse pagination parameters
        int page = DEFAULT_PAGE;
        int pageSize = DEFAULT_PAGE_SIZE;
        String pageParam = req.getParameter("page");
        String pageSizeParam = req.getParameter("pageSize");
        try {
            if (pageParam != null) {
                page = Math.max(1, Integer.parseInt(pageParam));
        
            }} catch (NumberFormatException ignored) {
            page = DEFAULT_PAGE;
        }
        try {
            if (pageSizeParam != null) {
                pageSize = Math.max(1, Math.min(MAX_PAGE_SIZE, Integer.parseInt(pageSizeParam)));
        
            }} catch (NumberFormatException ignored) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Read sort parameters (server-side sort applied to full dataset)
        String sortBy = req.getParameter("sortBy");
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "count";
        }
        String sortDir = req.getParameter("sortDir");
        if (sortDir == null || (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir))) {
            sortDir = "desc";
        }
        sortBy = sortBy.trim();

        Map<String, SessionStats> aggregated = new HashMap<>();
        List<WidgetEntry> widgets = listWidgets();

        if (!widgets.isEmpty()) {
            try (Connection conn = dsHolder.getDataSource().getConnection()) {
                for (WidgetEntry w : widgets) {
                    if (w == null || w.getWidgetId() == null) {
                        continue;
                    }
                    String widgetId = w.getWidgetId();
                    String widgetName = (w.getDisplayName() != null && !w.getDisplayName().isBlank()) ? w.getDisplayName() : widgetId;
                    String tableName = sanitizeWidgetTableName(widgetId);
                    try {
                        if (!tableExists(conn, tableName)) {
                            continue;
                        }

                        String sql = "SELECT session_id, COUNT(*) AS cnt, MAX(created_at) AS last_ts FROM "
                                + quoteIdentifier(tableName) + " GROUP BY session_id";
                        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String sessionId = rs.getString("session_id");
                                long cnt = rs.getLong("cnt");
                                Timestamp last = rs.getTimestamp("last_ts");
                                if (sessionId == null || sessionId.isBlank()) {
                                    continue;
                                }
                                SessionStats st = aggregated.get(sessionId);
                                if (st == null) {
                                    st = new SessionStats();
                                    aggregated.put(sessionId, st);
                                }
                                st.addCount(cnt, widgetId, widgetName);
                                st.updateLast(last);
                            }
                        }
                    } catch (SQLException e) {
                        log.log(Level.WARNING, "Failed to query table " + tableName + ": " + e.getMessage(), e);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Unable to obtain DB connection for session aggregation", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to query sessions.");
                return;
            }
        }

        // Finalize widget-name derived fields for each session
        for (SessionStats s : aggregated.values()) {
            s.finalizeWidgetNames();
        }

        // Convert to list
        List<Entry<String, SessionStats>> entries = new ArrayList<>(aggregated.entrySet());

        // Build comparator according to sortBy and sortDir.
        Comparator<Entry<String, SessionStats>> comparator;
        switch (sortBy) {
            case "sessionId":
                comparator = Comparator.comparing(e -> e.getKey().toLowerCase());
                comparator = comparator.thenComparing((e1, e2) -> Long.compare(e2.getValue().count, e1.getValue().count))
                        .thenComparing((e1, e2) -> {
                            Timestamp t1 = e1.getValue().last;
                            Timestamp t2 = e2.getValue().last;
                            if (t1 == null && t2 == null) {
                                return 0;
                            }
                            if (t1 == null) {
                                return 1;
                            }
                            if (t2 == null) {
                                return -1;
                            }
                            return t2.compareTo(t1);
                        });
                break;

            case "widget":
                // Compare sessions by cycling through their widgetNamesOrdered lists (ordered by contribution desc).
                comparator = (a, b) -> {
                    List<String> la = a.getValue().widgetNamesOrdered;
                    List<String> lb = b.getValue().widgetNamesOrdered;
                    int na = la == null ? 0 : la.size();
                    int nb = lb == null ? 0 : lb.size();
                    int max = Math.max(na, nb);
                    for (int i = 0; i < max; i++) {
                        String va  = (i < na) ? (la.get(i) == null ? "" : la.get(i)) : "";
                        String vb = (i < nb) ? (lb.get(i) == null ? "" : lb.get(i)) : "";
                        int cmp = va.compareToIgnoreCase(vb);
                        if (cmp != 0) {
                            return cmp;
                        }
                    }
                    // all equal up to min length; shorter list first
                    return Integer.compare(na, nb);
                };
                // tie-breaker: total count desc
                comparator = comparator.thenComparing((e1, e2) -> Long.compare(e2.getValue().count, e1.getValue().count));
                break;

            case "last":
                comparator = Comparator.comparing((Entry<String, SessionStats> e)
                        -> e.getValue().last == null ? Long.MIN_VALUE : e.getValue().last.getTime());
                comparator = comparator.thenComparing((e1, e2) -> Long.compare(e2.getValue().count, e1.getValue().count));
                break;

            case "count":
            default:
                comparator = Comparator.comparingLong((Entry<String, SessionStats> e) -> e.getValue().count);
                comparator = comparator.thenComparing((e1, e2) -> {
                    Timestamp t1 = e1.getValue().last;
                    Timestamp t2 = e2.getValue().last;
                    if (t1 == null && t2 == null) {
                        return 0;
                    }
                    if (t1 == null) {
                        return 1;
                    }
                    if (t2 == null) {
                        return -1;
                    }
                    return t2.compareTo(t1);
                });
                break;
        }

        // Apply direction
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        entries.sort(comparator);

        final int total = entries.size();
        final int totalPages = (int) Math.max(1, Math.ceil((double) total / pageSize));
        if (page > totalPages) {
            page = totalPages;
        }

        final int startIndex = Math.max(0, (page - 1) * pageSize);
        final int endIndex = Math.min(total, startIndex + pageSize);
        final List<Entry<String, SessionStats>> pageEntries = (startIndex < endIndex) ? entries.subList(startIndex, endIndex)
                : List.of();

        if (wantsJson) {
            resp.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = resp.getWriter()) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"status\":\"ok\",");
                sb.append("\"page\":").append(page).append(",");
                sb.append("\"pageSize\":").append(pageSize).append(",");
                sb.append("\"total\":").append(total).append(",");
                sb.append("\"totalPages\":").append(totalPages).append(",");
                sb.append("\"sortBy\":").append(jsonEscape(sortBy)).append(",");
                sb.append("\"sortDir\":").append(jsonEscape(sortDir)).append(",");
                sb.append("\"sessions\":[");
                boolean first = true;
                String contextPath = req.getContextPath();
                for (Entry<String, SessionStats> e : pageEntries) {
                    if (!first) {
                        sb.append(",");
                    }
                    first = false;
                    String sessionId = e.getKey();
                    SessionStats s = e.getValue();
                    sb.append("{");
                    sb.append("\"sessionId\":").append(jsonEscape(sessionId)).append(",");
                    sb.append("\"count\":").append(s.count).append(",");
                    sb.append("\"last\":").append(s.last == null ? "null" : jsonEscape(s.last.toInstant().toString())).append(",");
                    sb.append("\"topWidgetName\":").append(s.topWidgetName == null ? "null" : jsonEscape(s.topWidgetName)).append(",");
                    sb.append("\"widgetNamesCombined\":").append(s.widgetNamesCombined == null ? "null" : jsonEscape(s.widgetNamesCombined)).append(",");
                    String reviewUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                    sb.append("\"reviewUrl\":").append(jsonEscape(reviewUrl));
                    sb.append("}");
                }
                sb.append("]");
                sb.append("}");
                out.write(sb.toString());
            }
            return;
        }

        // Render HTML (paged) with server-side sortable columns (links)
        resp.setContentType("text/html;charset=UTF-8");
        String contextPath = req.getContextPath();
        String user = (session.getAttribute("user") != null) ? session.getAttribute("user").toString() : "Unknown";

        // Precompute sort links (toggle direction if same column)
        String sessionIdNextDir = "asc".equalsIgnoreCase(sortDir) && "sessionId".equals(sortBy) ? "desc" : "asc";
        String widgetNextDir = "asc".equalsIgnoreCase(sortDir) && "widget".equals(sortBy) ? "desc" : "asc";
        String countNextDir = "asc".equalsIgnoreCase(sortDir) && "count".equals(sortBy) ? "desc" : "asc";
        String lastNextDir = "asc".equalsIgnoreCase(sortDir) && "last".equals(sortBy) ? "desc" : "asc";

        String sIdLink = contextPath + "/dashboard/sessions?sortBy=" + URLEncoder.encode("sessionId", StandardCharsets.UTF_8)
                + "&sortDir=" + URLEncoder.encode(sessionIdNextDir, StandardCharsets.UTF_8)
                + "&pageSize=" + pageSize + "&page=1";
        String widgetLink = contextPath + "/dashboard/sessions?sortBy=" + URLEncoder.encode("widget", StandardCharsets.UTF_8)
                + "&sortDir=" + URLEncoder.encode(widgetNextDir, StandardCharsets.UTF_8)
                + "&pageSize=" + pageSize + "&page=1";
        String countLink = contextPath + "/dashboard/sessions?sortBy=" + URLEncoder.encode("count", StandardCharsets.UTF_8)
                + "&sortDir=" + URLEncoder.encode(countNextDir, StandardCharsets.UTF_8)
                + "&pageSize=" + pageSize + "&page=1";
        String lastLink = contextPath + "/dashboard/sessions?sortBy=" + URLEncoder.encode("last", StandardCharsets.UTF_8)
                + "&sortDir=" + URLEncoder.encode(lastNextDir, StandardCharsets.UTF_8)
                + "&pageSize=" + pageSize + "&page=1";

        try (PrintWriter out = resp.getWriter()) {
            out.write("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><title>All Sessions</title>");
            out.write("<link rel=\"stylesheet\" href=\"" + contextPath + "/assets/css/app.css\">");
            out.write("</head><body>");
            out.write("<div class=\"top-bar\"><div class=\"top-bar-inner\"><div class=\"brand\">Chat Server</div>");
            out.write("<div><span>Signed in as <strong>" + escapeHtml(user) + "</strong></span> ");
            out.write("<a href=\"" + contextPath + "/dashboard\"> Dashboard </a> ");
            out.write("<a href=\"" + contextPath + "/logout\"> Logout</a></div></div></div>");

            out.write("<div class=\"container\"><section class=\"section\">");
            out.write("<h1>All Sessions</h1>");
            out.write("<p>List of all session IDs across all widget tables with total chat counts, top widget, and last entry time.</p>");
            out.write("<p><a href=\"" + contextPath + "/dashboard\">&larr; Back to Dashboard</a></p>");
            out.write("</section>");

            out.write("<section class=\"section\">");
            out.write("<div class=\"table-scroll\">");
            out.write("<table class=\"session-table\" aria-describedby=\"session-table-desc\">");
            out.write("<caption id=\"session-table-desc\" style=\"display:none\">Sessions with counts and last entry; click columns to sort.</caption>");
            out.write("<thead><tr>");

            out.write("<th><a href=\"" + sIdLink + "\">Session ID" + ("sessionId".equals(sortBy) ? ("asc".equalsIgnoreCase(sortDir) ? " ▲" : " ▼") : "") + "</a></th>");
            out.write("<th><a href=\"" + widgetLink + "\">Widget Table" + ("widget".equals(sortBy) ? ("asc".equalsIgnoreCase(sortDir) ? " ▲" : " ▼") : "") + "</a></th>");
            out.write("<th><a href=\"" + countLink + "\">Total Chats" + ("count".equals(sortBy) ? ("asc".equalsIgnoreCase(sortDir) ? " ▲" : " ▼") : "") + "</a></th>");
            out.write("<th><a href=\"" + lastLink + "\">Last Entry" + ("last".equals(sortBy) ? ("asc".equalsIgnoreCase(sortDir) ? " ▲" : " ▼") : "") + "</a></th>");
            out.write("<th>Actions</th>");
            out.write("</tr></thead><tbody>");

            if (pageEntries.isEmpty()) {
                out.write("<tr><td colspan=\"5\" class=\"empty-row\">No sessions found.</td></tr>");
            } else {
                for (Entry<String, SessionStats> e : pageEntries) {
                    String sessionId = e.getKey();
                    SessionStats s = e.getValue();
                    String lastDisplay = (s.last == null) ? "—" : s.last.toString();
                    String lastValue = (s.last == null) ? "" : s.last.toInstant().toString();
                    String reviewUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);

                    out.write("<tr>");
                    out.write("<td data-value=\"" + escapeHtmlAttr(sessionId) + "\">" + escapeHtml(sessionId) + "</td>");
                    out.write("<td data-value=\"" + escapeHtmlAttr(s.widgetNamesCombined) + "\">" + escapeHtml(s.topWidgetName == null ? "—" : s.topWidgetName) + "</td>");
                    out.write("<td data-value=\"" + s.count + "\">" + s.count + "</td>");
                    out.write("<td data-value=\"" + escapeHtmlAttr(lastValue) + "\">" + escapeHtml(lastDisplay) + "</td>");
                    out.write("<td><a class=\"ghost-btn\" href=\"" + reviewUrl + "\">Review</a></td>");
                    out.write("</tr>");
                }
            }

            out.write("</tbody></table></div>");

            // Pagination controls (preserve sort params)
            out.write("<div style=\"margin-top:12px; display:flex; align-items:center; justify-content:space-between;\">");
            out.write("<div>Page " + page + " of " + totalPages + " (Total sessions: " + total + ")</div>");
            out.write("<div>");
            if (page > 1) {
                out.write("<a class=\"ghost-btn\" href=\"" + contextPath + "/dashboard/sessions?page=" + (page - 1)
                        + "&pageSize=" + pageSize + "&sortBy=" + URLEncoder.encode(sortBy, StandardCharsets.UTF_8)
                        + "&sortDir=" + URLEncoder.encode(sortDir, StandardCharsets.UTF_8) + "\">Previous</a>");
            }
            if (page < totalPages) {
                out.write("<a class=\"ghost-btn\" style=\"margin-left:8px;\" href=\"" + contextPath + "/dashboard/sessions?page=" + (page + 1)
                        + "&pageSize=" + pageSize + "&sortBy=" + URLEncoder.encode(sortBy, StandardCharsets.UTF_8)
                        + "&sortDir=" + URLEncoder.encode(sortDir, StandardCharsets.UTF_8) + "\">Next</a>");
            }
            out.write("</div></div>");

            // Page size form (preserve sort params)
            out.write("<div style=\"margin-top:8px;\">");
            out.write("<form method=\"get\" action=\"" + contextPath + "/dashboard/sessions\">");
            out.write("<label for=\"pageSize\">Page size:</label>");
            out.write("<input type=\"number\" id=\"pageSize\" name=\"pageSize\" min=\"1\" max=\"" + MAX_PAGE_SIZE + "\" value=\"" + pageSize + "\" style=\"width:80px; margin-left:8px;\"/>");
            out.write("<input type=\"hidden\" name=\"page\" value=\"1\"/>");
            out.write("<input type=\"hidden\" name=\"sortBy\" value=\"" + escapeHtmlAttr(sortBy) + "\"/>");
            out.write("<input type=\"hidden\" name=\"sortDir\" value=\"" + escapeHtmlAttr(sortDir) + "\"/>");
            out.write("<button type=\"submit\" class=\"ghost-btn\" style=\"margin-left:8px;\">Go</button>");
            out.write("</form>");
            out.write("</div>");

            out.write("</section>");
            out.write("</div></body></html>");
        }
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets for session aggregation", e);
            return List.of();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isEmpty()) {
            normalized = "widget";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private static String escapeHtml(String v) {
        if (v == null) {
            return "";
        }
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String jsonEscape(String v) {
        if (v == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c)); 
                    }else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static String escapeHtmlAttr(String v) {
        if (v == null) {
            return "";
        }
        return v.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
