package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableSelectionServlet", urlPatterns = {"/dashboard/widgets/drilldown/view/select-ids"})
public class WidgetTableSelectionServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetTableSelectionServlet.class.getName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_:-]{1,80}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isLoggedIn(req, resp)) {
            return;
        }
        String widgetId = sanitizeWidgetId(ServletRequestParamUtil.firstParam(req, "widgetId", 256, true, true));
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId required.");
            return;
        }

        WidgetEntry plugin = findWidget(widgetId);
        if (plugin == null) {
            jsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Widget not found.");
            return;
        }

        String tableName = sanitizeWidgetTableName(widgetId);
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            if (!tableExists(conn, tableName)) {
                jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Table for widget does not exist.");
                return;
            }

            FilterState filters = new FilterState(
            ServletRequestParamUtil.firstParam(req, "filterPrompt", 256, true, true),
            ServletRequestParamUtil.firstParam(req, "filterResponse", 256, true, true),
            ServletRequestParamUtil.firstParam(req, "search", 256, true, true)
            );

            String sql = "SELECT widget_chat_id FROM " + quoteIdentifier(tableName) + filters.buildWhereClause()
                    + " ORDER BY created_at DESC";

            List<String> chatIds = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (String param : filters.params()) {
                    ps.setString(idx++, param);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString("widget_chat_id");
                        if (chatId != null && !chatId.isBlank()) {
                            chatIds.add(chatId);
                        }
                    }
                }
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            chatIds.forEach(arrayBuilder::add);

            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("chatIds", arrayBuilder)
                    .add("totalRows", chatIds.size())
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to collect chat ids", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch chat ids.");
        }
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            jsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        return true;
    }

    private WidgetEntry findWidget(String widgetId) {
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            for (WidgetEntry widget : widgets) {
                if (widget != null && widgetId.equals(widget.getWidgetId())) {
                    return widget;
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list widgets", e);
        }
        return null;
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

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String sanitizeWidgetId(String widgetId) {
        if (widgetId == null) {
            return null;
        }
        String trimmed = widgetId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 80) {
            trimmed = trimmed.substring(0, 80);
        }
        return SAFE_WIDGET_ID.matcher(trimmed).matches() ? trimmed : null;
    }

    private void jsonError(HttpServletResponse resp, int status, String message) {
        JsonObject payload = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, payload);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        JsonObject safePayload = payload == null ? Json.createObjectBuilder().build() : payload;
        try {
            ServletJsonResponseUtil.writeJson(resp, status, safePayload);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to write widget table selection response", e);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback widget table selection error", sendErrorFailure);
            }
        }
    }

    static final class FilterState {

        private final String prompt;
        private final String response;
        private final String global;

        private FilterState(String prompt, String response, String global) {
            this.prompt = prompt;
            this.response = response;
            this.global = global;
        }

        private String buildWhereClause() {
            List<String> pieces = new ArrayList<>();
            if (hasValue(prompt)) {
                pieces.add("prompt ILIKE ?");
            }
            if (hasValue(response)) {
                pieces.add("response_text ILIKE ?");
            }
            if (hasValue(global)) {
                pieces.add("(prompt ILIKE ? OR response_text ILIKE ? OR session_id ILIKE ?)");
            }
            if (pieces.isEmpty()) {
                return "";
            }
            return " WHERE " + String.join(" AND ", pieces);
        }

        private List<String> params() {
            List<String> params = new ArrayList<>();
            if (hasValue(prompt)) {
                params.add(pattern(prompt));
            }
            if (hasValue(response)) {
                params.add(pattern(response));
            }
            if (hasValue(global)) {
                String globalPattern = pattern(global);
                for (int i = 0; i < 3; i++) {
                    params.add(globalPattern);
                }
            }
            return params;
        }

        private boolean hasValue(String val) {
            return val != null && !val.isBlank();
        }

        private String pattern(String input) {
            return '%' + input.trim() + '%';
        }
    }
}
