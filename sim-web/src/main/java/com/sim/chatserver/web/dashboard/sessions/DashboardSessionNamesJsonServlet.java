package com.sim.chatserver.web.dashboard.sessions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardSessionNamesJsonServlet", urlPatterns = {"/dashboard/session-names.json"})
public class DashboardSessionNamesJsonServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionNamesJsonServlet.class.getName());
    private static final int DEFAULT_LIMIT = 10;
    private static final String JSON_UTF8 = "application/json; charset=UTF-8";
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextPath = safeContextPath(req.getServletContext().getContextPath());
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add("status", "unauthorized").build());
            return;
        }

        String query = firstParam(req, "q");
        if (query == null || query.isBlank()) {
            query = firstParam(req, "search");
        }

        boolean labeledOnly = "true".equalsIgnoreCase(firstParam(req, "labeledOnly"));

        int limit = parsePositiveInteger(firstParam(req, "limit"), DEFAULT_LIMIT);
        int page = parsePositiveInteger(firstParam(req, "page"), 1);
        int offset = parseNonNegativeInteger(firstParam(req, "offset"), -1);

        if (offset >= 0) {
            page = (offset / limit) + 1;
        }

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list widgets for session catalog", e);
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            Map<String, SessionAccumulator> accumulators = collectSessionAccumulators(conn, widgets, query);

            Map<String, SessionLabelStore.SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());

            List<Map.Entry<String, SessionAccumulator>> sorted = new ArrayList<>(accumulators.entrySet());
            sorted.sort(
                    Comparator.<Map.Entry<String, SessionAccumulator>>comparingInt(e -> e.getValue().count).reversed()
                            .thenComparing(Map.Entry::getKey));

            if (labeledOnly) {
                sorted.removeIf(entry -> {
                    if (entry == null || entry.getKey() == null) {
                        return true;
                    }
                    SessionLabelStore.SessionLabel label = labels.get(entry.getKey());
                    if (label == null) {
                        return true;
                    }
                    boolean hasName = label.getDisplayName() != null && !label.getDisplayName().isBlank();
                    boolean hasEmail = label.getEmail() != null && !label.getEmail().isBlank();
                    return !(hasName || hasEmail);
                });
            }

            int totalSessions = sorted.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalSessions / (double) limit));

            if (page > totalPages) {
                page = totalPages;
            }

            int start = Math.max(0, (page - 1) * limit);
            int end = Math.min(start + limit, totalSessions);

            List<Map.Entry<String, SessionAccumulator>> pageSlice = sorted.subList(start, end);

            JsonArrayBuilder sessions = Json.createArrayBuilder();
            for (Map.Entry<String, SessionAccumulator> entry : pageSlice) {
                SessionAccumulator acc = entry.getValue();
                SessionLabelStore.SessionLabel label = labels.get(entry.getKey());
                String displayLabel = SessionLabelStore.resolveDisplayLabel(entry.getKey(), label);

                JsonObjectBuilder builder = Json.createObjectBuilder()
                        .add("sessionId", entry.getKey())
                        .add("displayLabel", displayLabel)
                        .add("count", acc.count)
                    .add("lastEntry", formatTimestamp(acc.lastEntry))
                    .add("reviewUrl", buildReviewUrl(contextPath, entry.getKey()))
                        .add("displayName", label == null ? "" : nullSafe(label.getDisplayName()))
                        .add("email", label == null ? "" : nullSafe(label.getEmail()));
                sessions.add(builder);
            }

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("labeledOnly", labeledOnly)
                    .add("total", totalSessions)
                    .add("totalSessions", totalSessions)
                    .add("page", page)
                    .add("limit", limit)
                    .add("totalPages", totalPages)
                    .add("sessions", sessions)
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, payload);

        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to collect session catalog", e);
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load session catalog")
                    .build();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    }

    private String nullSafe(String v) {
        return v == null ? "" : v;
    }

    private String buildReviewUrl(String contextPath, String sessionId) {
        return contextPath + "/dashboard/sessions/drilldown/session-review?sessionId="
                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? "—" : ISO_INSTANT_FMT.format(value.toInstant());
    }

    private int parsePositiveInteger(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private int parseNonNegativeInteger(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String firstParam(HttpServletRequest req, String name) {
        Map<String, String[]> params = req.getParameterMap();
        if (params == null) {
            return null;
        }
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    private Map<String, SessionAccumulator> collectSessionAccumulators(Connection conn, List<WidgetEntry> widgets, String filter)
            throws SQLException {
        Map<String, SessionAccumulator> accumulators = new LinkedHashMap<>();
        if (widgets == null || widgets.isEmpty()) {
            return accumulators;
        }

        for (WidgetEntry widget : widgets) {
            if (widget == null || widget.getWidgetId() == null) {
                continue;
            }

            String tableName = sanitizeWidgetTableName(widget.getWidgetId());
            if (!tableExists(conn, tableName)) {
                continue;
            }

            StringBuilder sql = new StringBuilder("SELECT session_id, COUNT(*) AS total, MAX(created_at) AS last_entry FROM ")
                    .append(quoteIdentifier(tableName))
                    .append(" WHERE session_id IS NOT NULL");

            if (filter != null && !filter.isBlank()) {
                sql.append(" AND session_id ILIKE ?");
            }

            sql.append(" GROUP BY session_id");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                if (filter != null && !filter.isBlank()) {
                    ps.setString(1, "%" + filter.trim() + "%");
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sessionId = rs.getString("session_id");
                        if (sessionId == null || sessionId.isBlank()) {
                            continue;
                        }

                        sessionId = sessionId.trim();
                        SessionAccumulator acc = accumulators.computeIfAbsent(sessionId, k -> new SessionAccumulator());
                        acc.count += rs.getInt("total");

                        Timestamp lastEntry = SqlTimeUtil.safeTimestamp(rs, "last_entry");
                        if (lastEntry != null && (acc.lastEntry == null || lastEntry.after(acc.lastEntry))) {
                            acc.lastEntry = lastEntry;
                        }
                    }
                }
            }
        }

        return accumulators;
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
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        resp.setContentType(JSON_UTF8);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
            writer.writeObject(body);
        }
    }

    private String safeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (!trimmed.startsWith("/") || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return "";
        }
        return trimmed;
    }

    private static final class SessionAccumulator {

        private int count = 0;
        private Timestamp lastEntry = null;
    }
}
