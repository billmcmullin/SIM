package com.sim.chatserver.web.dashboard.widgets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableSelectIdsServlet", urlPatterns = {"/dashboard/widgets/view/select-ids"})
public class WidgetTableSelectIdsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetTableSelectIdsServlet.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        RequestParamContext requestContext = RequestParamContext.from(req);

        String widgetId = requestContext.first("widgetId", 128);
        if (widgetId == null || widgetId.isBlank()) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
            return;
        }

        String search = normalize(requestContext.first("search", 128));
        String filterPrompt = normalize(requestContext.first("filterPrompt", 128));
        String filterResponse = normalize(requestContext.first("filterResponse", 128));

        // NEW: optional date filter (YYYY-MM-DD)
        LocalDate selectedDate = null;
        String dateRaw = requestContext.first("date", 128);
        if (dateRaw != null && !dateRaw.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateRaw.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Invalid date format for widget id selection", ex);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Invalid date. Expected YYYY-MM-DD.")
                        .build());
                return;
            }
        }

        try (Connection conn = resolveDataSourceHolder().getDataSource().getConnection()) {
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName)) {
                writeJson(resp, HttpServletResponse.SC_NOT_FOUND, Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Widget data not found")
                        .build());
                return;
            }

            List<String> conditions = new ArrayList<>();
            List<String> values = new ArrayList<>();
            conditions.add("widget_chat_id IS NOT NULL");

            if (!search.isBlank()) {
                conditions.add("(LOWER(widget_chat_id) LIKE ? OR LOWER(prompt) LIKE ? OR LOWER(response_text) LIKE ?)");
                String pattern = '%' + search + '%';
                values.add(pattern);
                values.add(pattern);
                values.add(pattern);
            }
            if (!filterPrompt.isBlank()) {
                conditions.add("LOWER(prompt) LIKE ?");
                values.add('%' + filterPrompt + '%');
            }
            if (!filterResponse.isBlank()) {
                conditions.add("LOWER(response_text) LIKE ?");
                values.add('%' + filterResponse + '%');
            }

            // NEW: date condition
            Timestamp startTs = null;
            Timestamp endTs = null;
            if (selectedDate != null) {
                conditions.add("created_at >= ? AND created_at < ?");
                startTs = Timestamp.valueOf(selectedDate.atStartOfDay());
                endTs = Timestamp.valueOf(selectedDate.plusDays(1).atStartOfDay());
            }

            StringBuilder sql = new StringBuilder("SELECT widget_chat_id FROM ")
                    .append(quoteIdentifier(tableName));
            if (!conditions.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", conditions));
            }
            sql.append(" ORDER BY created_at DESC");

            List<String> chatIds = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String value : values) {
                    ps.setString(idx, value);
                    idx++;
                }

                // bind date params after string params
                if (selectedDate != null) {
                    ps.setTimestamp(idx, startTs);
                    ps.setTimestamp(idx + 1, endTs);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString("widget_chat_id");
                        if (chatId != null && !chatId.isBlank()) {
                            chatIds.add(chatId.trim());
                        }
                    }
                }
            }

                writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("total", chatIds.size())
                    .add("chatIds", Json.createArrayBuilder(chatIds))
                    .build());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to fetch select-all ids", e);
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to fetch chat IDs")
                    .build());
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to verify widget table existence", ex);
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

    private AppDataSourceHolder resolveDataSourceHolder() {
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private void sendErrorSafe(HttpServletResponse resp, int status) {
        try {
            resp.sendError(status);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send servlet error", ex);
            sendFallbackError(resp, status);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        try {
            resp.sendError(status, message);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send servlet error with message", ex);
            sendFallbackError(resp, status);
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        try {
            try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
                writer.writeObject(payload);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write widget id selection response", ex);
            sendFallbackError(resp, status);
        }
    }

    private void sendFallbackError(HttpServletResponse resp, int status) {
        try {
            if (!resp.isCommitted()) {
                resp.sendError(status);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send fallback widget id selection response", ex);
        }
    }

    private static final class RequestParamContext {
        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        String first(String name, int maxLen) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String[] values = request.getParameterValues(name);
            if (values == null || values.length == 0) {
                return null;
            }
            for (String value : values) {
                String normalized = normalize(value, maxLen);
                if (normalized != null) {
                    return normalized;
                }
            }
            return null;
        }

        private String normalize(String value, int maxLen) {
            if (value == null) {
                return null;
            }
            String trimmed = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            int effectiveMax = maxLen <= 0 ? 128 : maxLen;
            return trimmed.length() > effectiveMax ? trimmed.substring(0, effectiveMax) : trimmed;
        }
    }
}
