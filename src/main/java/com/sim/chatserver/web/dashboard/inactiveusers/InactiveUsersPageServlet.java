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

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "InactiveUsersPageServlet", urlPatterns = {"/dashboard/inactive-users"})
public class InactiveUsersPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(InactiveUsersPageServlet.class.getName());
    private static final int DEFAULT_DAYS = 7;
    private static final int TOP_N = 5;
    private static final String TEMPLATE_PATH = "/WEB-INF/views/inactive_users.html";

    @Inject
    AppDataSourceHolder dsHolder;

    private static final class InactiveRow {

        String sessionId;
        String displayLabel;     // friendly session label if present
        String widgetId;
        String widgetLabel;      // friendly widget name
        Timestamp lastEntry;
        long chats;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int days = parseInt(req.getParameter("days"), DEFAULT_DAYS);
        if (days < 1) {
            days = DEFAULT_DAYS;
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
            String friendly = (w.getDisplayName() == null || w.getDisplayName().isBlank()) ? id : w.getDisplayName().trim();
            widgetNameById.put(id, friendly);
        }

        Map<String, List<InactiveRow>> byWidget = new LinkedHashMap<>();
        Map<String, InactiveRow> allAgg = new LinkedHashMap<>();

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String wid = w.getWidgetId().trim();
                String widgetLabel = widgetNameById.getOrDefault(wid, wid);
                String table = sanitizeWidgetTableName(wid);
                if (!tableExists(conn, table)) {
                    continue;
                }

                Map<String, InactiveRow> perWidgetAgg = querySessionAggregateForTable(conn, table, wid, widgetLabel);

                Set<String> widgetSessionIds = perWidgetAgg.keySet();
                Map<String, SessionLabelStore.SessionLabel> widgetLabels = mapLabelsSafe(widgetSessionIds);

                List<InactiveRow> inactiveRows = new ArrayList<>();
                for (InactiveRow row : perWidgetAgg.values()) {
                    if (!isInactive(row.lastEntry, cutoff)) {
                        continue;
                    }
                    // Friendly session name if available
                    row.displayLabel = SessionLabelStore.resolveDisplayLabel(row.sessionId, widgetLabels.get(row.sessionId));
                    inactiveRows.add(row);
                }

                inactiveRows.sort(Comparator.comparing((InactiveRow r) -> r.lastEntry, Comparator.nullsLast(Comparator.reverseOrder())));
                if (inactiveRows.size() > TOP_N) {
                    inactiveRows = inactiveRows.subList(0, TOP_N);
                }

                // key by widget ID, label included in row payload
                byWidget.put(wid, inactiveRows);

                for (InactiveRow wr : perWidgetAgg.values()) {
                    InactiveRow ar = allAgg.computeIfAbsent(wr.sessionId, k -> {
                        InactiveRow x = new InactiveRow();
                        x.sessionId = k;
                        x.widgetId = "ALL";
                        x.widgetLabel = "All Widgets";
                        x.chats = 0;
                        x.lastEntry = null;
                        return x;
                    });
                    ar.chats += wr.chats;
                    if (ar.lastEntry == null || (wr.lastEntry != null && wr.lastEntry.after(ar.lastEntry))) {
                        ar.lastEntry = wr.lastEntry;
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to compute inactive users", e);
        }

        List<InactiveRow> allRows = new ArrayList<>();
        Map<String, SessionLabelStore.SessionLabel> allLabels = mapLabelsSafe(allAgg.keySet());
        for (InactiveRow row : allAgg.values()) {
            if (!isInactive(row.lastEntry, cutoff)) {
                continue;
            }
            row.displayLabel = SessionLabelStore.resolveDisplayLabel(row.sessionId, allLabels.get(row.sessionId));
            allRows.add(row);
        }

        allRows.sort(Comparator.comparing((InactiveRow r) -> r.lastEntry, Comparator.nullsLast(Comparator.reverseOrder())));
        if (allRows.size() > TOP_N) {
            allRows = allRows.subList(0, TOP_N);
        }

        Map<String, List<InactiveRow>> payload = new LinkedHashMap<>();
        payload.put("ALL", allRows);
        payload.putAll(byWidget);

        String jsonData = buildInactiveUsersJson(payload, widgetNameById);
        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String user = String.valueOf(httpSession.getAttribute("user"));

        String rendered = template
                .replace("${contextPath}", req.getContextPath())
                .replace("${user}", escapeHtml(user))
                .replace("${defaultDays}", String.valueOf(days))
                .replace("${inactiveUsersData}", jsonData);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private Map<String, SessionLabelStore.SessionLabel> mapLabelsSafe(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        try {
            return SessionLabelStore.mapDisplayNames(ids);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load session labels", e);
            return Map.of();
        }
    }

    private boolean isInactive(Timestamp lastEntry, Instant cutoff) {
        if (lastEntry == null) {
            return false;
        }
        return lastEntry.toInstant().toEpochMilli() < cutoff.toEpochMilli();
    }

    private Map<String, InactiveRow> querySessionAggregateForTable(Connection conn, String table, String widgetId, String widgetLabel) throws Exception {
        Map<String, InactiveRow> out = new LinkedHashMap<>();
        String sql = "SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM "
                + quoteIdentifier(table)
                + " WHERE session_id IS NOT NULL AND session_id <> '' GROUP BY session_id";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sid = rs.getString("session_id");
                Timestamp last = rs.getTimestamp("last_entry");
                if (sid == null || sid.isBlank()) {
                    continue;
                }

                InactiveRow r = new InactiveRow();
                r.sessionId = sid.trim();
                r.displayLabel = r.sessionId;
                r.widgetId = widgetId;
                r.widgetLabel = widgetLabel;
                r.lastEntry = last;
                r.chats = rs.getLong("total");
                out.put(r.sessionId, r);
            }
        }
        return out;
    }

    private String buildInactiveUsersJson(Map<String, List<InactiveRow>> byWidget, Map<String, String> widgetNameById) {
        JsonArrayBuilder allArr = Json.createArrayBuilder();
        JsonObjectBuilder widgetsObj = Json.createObjectBuilder();
        JsonObjectBuilder widgetNamesObj = Json.createObjectBuilder();

        for (Map.Entry<String, String> e : widgetNameById.entrySet()) {
            widgetNamesObj.add(e.getKey(), e.getValue() == null ? e.getKey() : e.getValue());
        }

        for (InactiveRow r : byWidget.getOrDefault("ALL", List.of())) {
            allArr.add(toJson(r));
        }

        for (Map.Entry<String, List<InactiveRow>> e : byWidget.entrySet()) {
            if ("ALL".equals(e.getKey())) {
                continue;
            }
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (InactiveRow r : e.getValue()) {
                arr.add(toJson(r));
            }
            widgetsObj.add(e.getKey(), arr);
        }

        JsonObject payload = Json.createObjectBuilder()
                .add("all", allArr)
                .add("widgets", widgetsObj)
                .add("widgetNames", widgetNamesObj)
                .build();

        return payload.toString();
    }

    private JsonObject toJson(InactiveRow r) {
        return Json.createObjectBuilder()
                .add("sessionId", nvl(r.sessionId))
                .add("displayLabel", nvl(r.displayLabel))
                .add("widgetId", nvl(r.widgetId))
                .add("widgetLabel", nvl(r.widgetLabel))
                .add("chatCount", r.chats)
                .add("lastEntry", r.lastEntry == null ? "" : r.lastEntry.toInstant().toString())
                .build();
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

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
