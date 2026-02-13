package com.sim.chatserver.web;

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

        void addCount(long c) {
            this.count += c;
        }

        void updateLast(Timestamp t) {
            if (t == null) {
                return;
            }
            if (this.last == null || t.after(this.last)) {
                this.last = t;
            }
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

        // Determine if JSON requested: URL /dashboard/sessions.json OR Accept header includes application/json OR format=json
        boolean wantsJson = req.getRequestURI().endsWith(".json")
                || "json".equalsIgnoreCase(req.getParameter("format"))
                || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        // Parse pagination parameters (optional)
        int page = DEFAULT_PAGE;
        int pageSize = DEFAULT_PAGE_SIZE;
        String pageParam = req.getParameter("page");
        String pageSizeParam = req.getParameter("pageSize");
        try {
            if (pageParam != null) {
                page = Math.max(1, Integer.parseInt(pageParam));
            }
        } catch (NumberFormatException ignored) {
            page = DEFAULT_PAGE;
        }
        try {
            if (pageSizeParam != null) {
                pageSize = Math.max(1, Math.min(MAX_PAGE_SIZE, Integer.parseInt(pageSizeParam)));
            }
        } catch (NumberFormatException ignored) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        Map<String, SessionStats> aggregated = new HashMap<>();
        List<WidgetEntry> widgets = listWidgets();

        if (!widgets.isEmpty()) {
            try (Connection conn = dsHolder.getDataSource().getConnection()) {
                for (WidgetEntry w : widgets) {
                    if (w == null || w.getWidgetId() == null) {
                        continue;
                    }
                    String widgetId = w.getWidgetId();
                    String tableName = sanitizeWidgetTableName(widgetId);
                    try {
                        if (!tableExists(conn, tableName)) {
                            continue;
                        }
                        // Aggregate per-table session statistics
                        String sql = "SELECT session_id, COUNT(*) AS cnt, MAX(created_at) AS last_ts FROM "
                                + quoteIdentifier(tableName) + " GROUP BY session_id";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            try (ResultSet rs = ps.executeQuery()) {
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
                                        st.count = cnt;
                                        st.last = last;
                                        aggregated.put(sessionId, st);
                                    } else {
                                        st.addCount(cnt);
                                        st.updateLast(last);
                                    }
                                }
                            }
                        }
                    } catch (SQLException e) {
                        // Log and continue with other tables
                        log.log(Level.WARNING, "Failed to query table " + tableName + ": " + e.getMessage(), e);
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Unable to obtain DB connection for session aggregation", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to query sessions.");
                return;
            }
        }

        // Convert to list and sort by count desc, then by last desc
        List<Entry<String, SessionStats>> entries = new ArrayList<>(aggregated.entrySet());
        entries.sort(Comparator.<Entry<String, SessionStats>>comparingLong(e -> e.getValue().count).reversed()
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
                }));

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
                // Build JSON
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"status\":\"ok\",");
                sb.append("\"page\":").append(page).append(",");
                sb.append("\"pageSize\":").append(pageSize).append(",");
                sb.append("\"total\":").append(total).append(",");
                sb.append("\"totalPages\":").append(totalPages).append(",");
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
                    String reviewUrl = contextPath + "/dashboard/session-review?sessionId=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                    sb.append("\"reviewUrl\":").append(jsonEscape(reviewUrl));
                    sb.append("}");
                }
                sb.append("]");
                sb.append("}");
                out.write(sb.toString());
            }
            return;
        }

        // Render HTML (paged)
        resp.setContentType("text/html;charset=UTF-8");
        String contextPath = req.getContextPath();
        String user = (session.getAttribute("user") != null) ? session.getAttribute("user").toString() : "Unknown";
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
            out.write("<p>List of all session IDs across all widget tables with total chat counts and last entry time.</p>");
            out.write("<p><a href=\"" + contextPath + "/dashboard\">&larr; Back to Dashboard</a></p>");
            out.write("</section>");

            out.write("<section class=\"section\">");
            out.write("<div class=\"table-scroll\">");
            out.write("<table class=\"widget-table\"><thead><tr>");
            out.write("<th>Session ID</th><th>Total Chats</th><th>Last Entry</th><th>Actions</th>");
            out.write("</tr></thead><tbody>");

            if (pageEntries.isEmpty()) {
                out.write("<tr><td colspan=\"4\" class=\"empty-row\">No sessions found.</td></tr>");
            } else {
                for (Entry<String, SessionStats> e : pageEntries) {
                    String sessionId = e.getKey();
                    SessionStats s = e.getValue();
                    String last = (s.last == null) ? "—" : s.last.toString();
                    String reviewUrl = contextPath + "/dashboard/session-review?sessionId=" + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
                    out.write("<tr>");
                    out.write("<td>" + escapeHtml(sessionId) + "</td>");
                    out.write("<td>" + s.count + "</td>");
                    out.write("<td>" + escapeHtml(last) + "</td>");
                    out.write("<td><a class=\"ghost-btn\" href=\"" + reviewUrl + "\">Review</a></td>");
                    out.write("</tr>");
                }
            }

            out.write("</tbody></table></div>");

            // Pagination controls
            out.write("<div style=\"margin-top:12px; display:flex; align-items:center; justify-content:space-between;\">");
            out.write("<div>Page " + page + " of " + totalPages + " (Total sessions: " + total + ")</div>");
            out.write("<div>");
            // previous link
            if (page > 1) {
                out.write("<a class=\"ghost-btn\" href=\"" + contextPath + "/dashboard/sessions?page=" + (page - 1) + "&pageSize=" + pageSize + "\">Previous</a>");
            }
            // next link
            if (page < totalPages) {
                out.write("<a class=\"ghost-btn\" style=\"margin-left:8px;\" href=\"" + contextPath + "/dashboard/sessions?page=" + (page + 1) + "&pageSize=" + pageSize + "\">Next</a>");
            }
            out.write("</div></div>");

            // Page size form
            out.write("<div style=\"margin-top:8px;\">");
            out.write("<form method=\"get\" action=\"" + contextPath + "/dashboard/sessions\">");
            out.write("<label for=\"pageSize\">Page size:</label>");
            out.write("<input type=\"number\" id=\"pageSize\" name=\"pageSize\" min=\"1\" max=\"" + MAX_PAGE_SIZE + "\" value=\"" + pageSize + "\" style=\"width:80px; margin-left:8px;\"/>");
            out.write("<input type=\"hidden\" name=\"page\" value=\"1\"/>");
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
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
