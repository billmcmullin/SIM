package com.sim.chatserver.web.dashboard.inactiveusers;

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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "InactiveUsersListPageServlet", urlPatterns = {"/dashboard/inactive-users/list"})
public class InactiveUsersListPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(InactiveUsersListPageServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/inactive_users_list.html";
    private static final int DEFAULT_DAYS = 7;
    private static final int DEFAULT_LIMIT = 10;

    @Inject
    AppDataSourceHolder dsHolder;

    private static final class Row {

        String sessionId;
        String displayLabel;
        String widgetId;
        String widgetLabel;
        long chatCount;
        Timestamp lastEntry;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String scope = nvl(req.getParameter("scope")).trim();
        if (!"widget".equalsIgnoreCase(scope)) {
            scope = "all";
        }

        String widgetIdFilter = nvl(req.getParameter("widgetId")).trim();
        String search = nvl(req.getParameter("search")).trim();
        String searchLower = search.toLowerCase();
        boolean hasSearch = !searchLower.isBlank();

        int days = parseInt(req.getParameter("days"), DEFAULT_DAYS);
        if (days < 1) {
            days = DEFAULT_DAYS;
        }

        int page = parseInt(req.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        int limit = parseInt(req.getParameter("limit"), DEFAULT_LIMIT);
        if (limit < 1) {
            limit = DEFAULT_LIMIT;
        }

        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load widgets", e);
            widgets = List.of();
        }

        Map<String, String> widgetNameById = new LinkedHashMap<>();
        for (WidgetEntry w : widgets) {
            if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                continue;
            }
            String id = w.getWidgetId().trim();
            String name = (w.getDisplayName() == null || w.getDisplayName().isBlank()) ? id : w.getDisplayName().trim();
            widgetNameById.put(id, name);
        }

        List<Row> allRows = new ArrayList<>();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            if ("widget".equalsIgnoreCase(scope)) {
                if (!widgetIdFilter.isBlank()) {
                    String table = sanitizeWidgetTableName(widgetIdFilter);
                    if (tableExists(conn, table)) {
                        allRows.addAll(loadWidgetRows(
                                conn,
                                widgetIdFilter,
                                widgetNameById.getOrDefault(widgetIdFilter, widgetIdFilter),
                                cutoff
                        ));
                    }
                }
            } else {
                Map<String, Row> agg = new LinkedHashMap<>();
                for (String wid : widgetNameById.keySet()) {
                    String table = sanitizeWidgetTableName(wid);
                    if (!tableExists(conn, table)) {
                        continue;
                    }

                    String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                            + quoteIdentifier(table)
                            + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

                    try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String sid = rs.getString("session_id");
                            Timestamp last = rs.getTimestamp("last_entry");
                            if (sid == null || sid.isBlank() || last == null) {
                                continue;
                            }

                            Row r = agg.computeIfAbsent(sid.trim(), k -> {
                                Row x = new Row();
                                x.sessionId = k;
                                x.widgetId = "ALL";
                                x.widgetLabel = "All Widgets";
                                return x;
                            });
                            r.chatCount += rs.getLong("total");
                            if (r.lastEntry == null || last.after(r.lastEntry)) {
                                r.lastEntry = last;
                            }
                        }
                    }
                }

                for (Row r : agg.values()) {
                    if (r.lastEntry != null && r.lastEntry.toInstant().isBefore(cutoff)) {
                        allRows.add(r);
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to compute inactive users list", e);
        }

        // resolve session friendly names
        Set<String> ids = allRows.stream().map(r -> r.sessionId).collect(Collectors.toSet());
        Map<String, SessionLabelStore.SessionLabel> labels = Map.of();
        try {
            if (!ids.isEmpty()) {
                labels = SessionLabelStore.mapDisplayNames(ids);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load session labels", e);
        }

        for (Row r : allRows) {
            r.displayLabel = SessionLabelStore.resolveDisplayLabel(r.sessionId, labels.get(r.sessionId));
        }

        // search by session id or friendly name
        if (hasSearch) {
            allRows = allRows.stream().filter(r -> {
                String sid = r.sessionId == null ? "" : r.sessionId.toLowerCase();
                String display = r.displayLabel == null ? "" : r.displayLabel.toLowerCase();
                return sid.contains(searchLower) || display.contains(searchLower);
            }).collect(Collectors.toList());
        }

        // most recent inactive first
        allRows.sort(Comparator.comparing((Row r) -> r.lastEntry, Comparator.nullsLast(Comparator.reverseOrder())));

        int total = allRows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / (double) limit));
        if (page > totalPages) {
            page = totalPages;
        }

        int start = Math.min((page - 1) * limit, total);
        int end = Math.min(start + limit, total);
        List<Row> pageRows = allRows.subList(start, end);

        String jsonData = buildJson(pageRows);
        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);

        String title = "all".equals(scope)
                ? "All Inactive Users"
                : "Inactive Users: " + widgetNameById.getOrDefault(widgetIdFilter, widgetIdFilter);

        String rendered = template
                .replace("${contextPath}", req.getContextPath())
                .replace("${user}", escapeHtml(String.valueOf(s.getAttribute("user"))))
                .replace("${title}", escapeHtml(title))
                .replace("${scope}", escapeHtml(scope))
                .replace("${widgetId}", escapeHtml(widgetIdFilter))
                .replace("${days}", String.valueOf(days))
                .replace("${page}", String.valueOf(page))
                .replace("${limit}", String.valueOf(limit))
                .replace("${total}", String.valueOf(total))
                .replace("${totalPages}", String.valueOf(totalPages))
                .replace("${search}", escapeHtml(search))
                .replace("${rowsJson}", jsonData);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private List<Row> loadWidgetRows(Connection conn, String widgetId, String widgetLabel, Instant cutoff) throws Exception {
        List<Row> rows = new ArrayList<>();
        String table = sanitizeWidgetTableName(widgetId);
        String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                + quoteIdentifier(table)
                + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sid = rs.getString("session_id");
                Timestamp last = rs.getTimestamp("last_entry");
                if (sid == null || sid.isBlank() || last == null) {
                    continue;
                }
                if (!last.toInstant().isBefore(cutoff)) {
                    continue;
                }

                Row r = new Row();
                r.sessionId = sid.trim();
                r.widgetId = widgetId;
                r.widgetLabel = widgetLabel;
                r.chatCount = rs.getLong("total");
                r.lastEntry = last;
                rows.add(r);
            }
        }
        return rows;
    }

    private String buildJson(List<Row> rows) {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Row r : rows) {
            arr.add(Json.createObjectBuilder()
                    .add("sessionId", nvl(r.sessionId))
                    .add("displayLabel", nvl(r.displayLabel))
                    .add("widgetId", nvl(r.widgetId))
                    .add("widgetLabel", nvl(r.widgetLabel))
                    .add("chatCount", r.chatCount)
                    .add("lastEntry", r.lastEntry == null ? "" : r.lastEntry.toInstant().toString()));
        }
        JsonObject obj = Json.createObjectBuilder().add("rows", arr).build();
        return obj.toString();
    }

    private String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    b.append(line).append('\n');
                }
                return b.toString();
            }
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
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
        String normalized = widgetId == null ? "widget" : widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
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

    private String quoteIdentifier(String s) {
        return '"' + s.replace("\"", "\"\"") + '"';
    }

    private int parseInt(String v, int fallback) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String escapeHtml(String in) {
        if (in == null) {
            return "";
        }
        return in.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
