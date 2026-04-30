package com.sim.chatserver.web.dashboard.newuser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardNewUsersDrilldownServlet", urlPatterns = {"/dashboard/new-users/drilldown"})
public class DashboardNewUsersDrilldownServlet extends HttpServlet {

    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_new_users_drilldown.html";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int page = parsePositiveInt(req.getParameter("page")).orElse(1);
        int pageSize = parsePositiveInt(req.getParameter("pageSize")).orElse(10);
        if (pageSize != 10 && pageSize != 25 && pageSize != 50) {
            pageSize = 10;
        }

        Optional<LocalDate> dayFilter = parseDate(req.getParameter("day"));

        List<Row> allRows = new ArrayList<>();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            List<WidgetEntry> widgets;
            try {
                widgets = WidgetStore.list(null);
            } catch (Exception e) {
                widgets = List.of();
            }

            Map<String, Timestamp> earliestBySession = findEarliestBySession(conn, widgets);
            Map<String, Integer> totalChatsBySession = findTotalChatsBySession(conn, widgets);

            List<Map.Entry<String, Timestamp>> sorted = new ArrayList<>(earliestBySession.entrySet());
            sorted.sort(Map.Entry.<String, Timestamp>comparingByValue(Comparator.reverseOrder()));

            Set<String> ids = new LinkedHashSet<>();
            for (Map.Entry<String, Timestamp> e : sorted) {
                ids.add(e.getKey());
            }

            Map<String, SessionLabelStore.SessionLabel> labels;
            try {
                labels = ids.isEmpty() ? Map.of() : SessionLabelStore.mapDisplayNames(ids);
            } catch (Exception ex) {
                labels = Map.of();
            }

            String contextPath = req.getContextPath();
            for (Map.Entry<String, Timestamp> e : sorted) {
                String sid = e.getKey();
                Timestamp ts = e.getValue();

                LocalDate firstSeenDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (dayFilter.isPresent() && !firstSeenDate.equals(dayFilter.get())) {
                    continue;
                }

                String display = SessionLabelStore.resolveDisplayLabel(sid, labels.get(sid));
                String firstSeen = TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()));
                int totalChats = totalChatsBySession.getOrDefault(sid, 0);
                String chatUrl = contextPath + "/dashboard/sessions/drilldown/session-review?sessionId="
                        + java.net.URLEncoder.encode(sid, StandardCharsets.UTF_8);

                allRows.add(new Row(display, firstSeen, totalChats, chatUrl));
            }
        } catch (Exception e) {
            throw new ServletException("Failed to load newest users drilldown", e);
        }

        int total = allRows.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        if (page > totalPages) {
            page = totalPages;
        }

        int from = Math.max(0, (page - 1) * pageSize);
        int to = Math.min(total, from + pageSize);
        List<Row> pageRows = allRows.subList(from, to);

        String rowsHtml = renderRows(pageRows, from);

        String dayParam = dayFilter.map(d -> "&day=" + urlEncode(d.format(DATE_FMT))).orElse("");
        String prevHref = page > 1
                ? req.getContextPath() + "/dashboard/new-users/drilldown?page=" + (page - 1) + "&pageSize=" + pageSize + dayParam
                : "";
        String nextHref = page < totalPages
                ? req.getContextPath() + "/dashboard/new-users/drilldown?page=" + (page + 1) + "&pageSize=" + pageSize + dayParam
                : "";

        String filterTitle = dayFilter.map(d -> "Users first seen on " + d.format(DATE_FMT))
                .orElse("All Session IDs / Users (Newest First)");

        String template = loadTemplate(req, TEMPLATE_PATH);
        String rendered = template
                .replace("${contextPath}", escapeHtml(req.getContextPath()))
                .replace("${user}", escapeHtml(String.valueOf(session.getAttribute("user"))))
                .replace("${rows}", rowsHtml)
                .replace("${totalUsers}", String.valueOf(total))
                .replace("${page}", String.valueOf(page))
                .replace("${totalPages}", String.valueOf(totalPages))
                .replace("${pageSize}", String.valueOf(pageSize))
                .replace("${prevHref}", escapeHtml(prevHref))
                .replace("${nextHref}", escapeHtml(nextHref))
                .replace("${prevDisabled}", prevHref.isBlank() ? "disabled" : "")
                .replace("${nextDisabled}", nextHref.isBlank() ? "disabled" : "")
                .replace("${selected10}", pageSize == 10 ? "selected" : "")
                .replace("${selected25}", pageSize == 25 ? "selected" : "")
                .replace("${selected50}", pageSize == 50 ? "selected" : "")
                .replace("${filterTitle}", escapeHtml(filterTitle))
                .replace("${dayQuery}", dayFilter.map(d -> "&day=" + escapeHtml(d.format(DATE_FMT))).orElse(""));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private String renderRows(List<Row> rows, int offset) {
        if (rows.isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"empty-row\">No users found for this selection.</td></tr>";
        }
        StringBuilder sb = new StringBuilder();
        int rank = offset + 1;
        for (Row r : rows) {
            sb.append("<tr>")
                    .append("<td>").append(rank++).append("</td>")
                    .append("<td>").append(escapeHtml(r.display)).append("</td>")
                    .append("<td>").append(escapeHtml(r.firstSeen)).append("</td>")
                    .append("<td><a class=\"session-count-link\" href=\"").append(escapeHtml(r.chatEntriesUrl)).append("\">")
                    .append(r.totalChats).append(" chats</a></td>")
                    .append("</tr>");
        }
        return sb.toString();
    }

    private Map<String, Timestamp> findEarliestBySession(Connection conn, List<WidgetEntry> widgets) throws Exception {
        Map<String, Timestamp> earliest = new LinkedHashMap<>();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                continue;
            }
            String table = sanitizeWidgetTableName(w.getWidgetId());
            if (!tableExists(conn, table)) {
                continue;
            }

            String sql = "SELECT session_id, MIN(created_at) AS first_seen FROM " + quoteIdentifier(table)
                    + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("session_id");
                    Timestamp ts = SqlTimeUtil.safeTimestamp(rs, "first_seen");
                    if (sid == null || sid.isBlank() || ts == null) {
                        continue;
                    }
                    sid = sid.trim();

                    Timestamp old = earliest.get(sid);
                    if (old == null || ts.before(old)) {
                        earliest.put(sid, ts);
                    }
                }
            }
        }
        return earliest;
    }

    private Map<String, Integer> findTotalChatsBySession(Connection conn, List<WidgetEntry> widgets) throws Exception {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                continue;
            }
            String table = sanitizeWidgetTableName(w.getWidgetId());
            if (!tableExists(conn, table)) {
                continue;
            }

            String sql = "SELECT session_id, COUNT(*) AS c FROM " + quoteIdentifier(table)
                    + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("session_id");
                    if (sid == null || sid.isBlank()) {
                        continue;
                    }
                    sid = sid.trim();
                    totals.merge(sid, rs.getInt("c"), Integer::sum);
                }
            }
        }
        return totals;
    }

    private Optional<Integer> parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            int n = Integer.parseInt(value.trim());
            return n > 0 ? Optional.of(n) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.trim(), DATE_FMT));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        for (String c : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
            try (ResultSet rs = meta.getTables(null, null, c, new String[]{"TABLE"})) {
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
        String n = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (n.isEmpty()) {
            n = "widget";
        }
        if (!Character.isLetter(n.charAt(0))) {
            n = "w_" + n;
        }
        if (n.length() > 60) {
            n = n.substring(0, 60);
        }
        return n;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String loadTemplate(HttpServletRequest req, String path) throws IOException {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        }
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static final class Row {

        final String display;
        final String firstSeen;
        final int totalChats;
        final String chatEntriesUrl;

        Row(String display, String firstSeen, int totalChats, String chatEntriesUrl) {
            this.display = display;
            this.firstSeen = firstSeen;
            this.totalChats = totalChats;
            this.chatEntriesUrl = chatEntriesUrl;
        }
    }
}
