package com.sim.chatserver.web.dashboard.topics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsSelectServlet", urlPatterns = {"/dashboard/topics/select"})
public class DashboardTopicsSelectServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsSelectServlet.class.getName());
    private static final int IN_CLAUSE_BATCH_SIZE = 200;
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final String JSON_UTF8 = "application/json; charset=UTF-8";
    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");

    AppDataSourceHolder dsHolder;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        JsonObject payload;
        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }
        try (JsonReader reader = Json.createReader(new StringReader(readRequestBody(req)))) {
            payload = reader.readObject();
        } catch (JsonException ex) {
            log.log(Level.FINE, "Invalid topics selection payload", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        JsonArray arr = payload.getJsonArray("selectedChatIds");
        if (arr == null || arr.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "selectedChatIds required.");
            return;
        }

        Set<String> requestedIds = new LinkedHashSet<>();
        for (int i = 0; i < arr.size(); i++) {
            String id = arr.getString(i, "").trim();
            if (!id.isBlank()) {
                requestedIds.add(id);
            }
        }

        if (requestedIds.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "No valid chat IDs provided.");
            return;
        }

        List<TermChatSnapshot> snapshots = new ArrayList<>();
        Set<String> foundIds = new LinkedHashSet<>();
        Map<String, WidgetEntry> widgetById = new LinkedHashMap<>();

        try {
            for (WidgetEntry w : WidgetStore.list(null)) {
                if (w != null && w.getWidgetId() != null && !w.getWidgetId().isBlank()) {
                    widgetById.put(w.getWidgetId(), w);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to load widget list for topics selection", ex);
        }

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            List<String> idList = new ArrayList<>(requestedIds);

            for (Map.Entry<String, WidgetEntry> e : widgetById.entrySet()) {
                String widgetId = e.getKey();
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                for (int from = 0; from < idList.size(); from += IN_CLAUSE_BATCH_SIZE) {
                    int to = Math.min(from + IN_CLAUSE_BATCH_SIZE, idList.size());
                    List<String> chunk = idList.subList(from, to);

                    String inClause = String.join(",", chunk.stream().map(id -> "?").toList());
                    String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                            + quoteIdentifier(tableName)
                            + " WHERE widget_chat_id IN (" + inClause + ')';

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        int idx = 1;
                        for (String id : chunk) {
                            ps.setString(idx++, id);
                        }

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String chatId = rs.getString("widget_chat_id");
                                if (chatId == null || chatId.isBlank()) {
                                    continue;
                                }

                                String prompt = readDbText(rs, "prompt", 64000);
                                String responseText = readDbText(rs, "response_text", 64000);
                                Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                                String sessionId = readDbText(rs, "session_id", 512);

                                snapshots.add(new TermChatSnapshot(
                                        "Popular Topics",
                                        widgetId,
                                        chatId,
                                        prompt,
                                        responseText,
                                        createdAt,
                                        sessionId
                                ));
                                foundIds.add(chatId);
                            }
                        }
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to resolve selected chats", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to resolve selected chats.");
            return;
        }

        if (snapshots.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "No matching chats found in widget tables.");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Popular Topics",
                snapshots,
            ServletPathUtil.safeContextPathEnsureLeadingSlash(req.getContextPath()) + "/dashboard/topics"
        );

        if (selectionId == null || selectionId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection.");
            return;
        }

        JsonObject ok = Json.createObjectBuilder()
                .add("status", "ok")
                .add("selectionId", selectionId)
                .add("requestedCount", requestedIds.size())
                .add("resolvedCount", foundIds.size())
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, ok);
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
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

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        for (String c : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
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
        if (identifier == null || !SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String readRequestBody(HttpServletRequest req) throws IOException {
        if (req == null) {
            return "";
        }
        try (BufferedReader reader = req.getReader()) {
            return ServletRequestParamUtil.readNormalizedBodyText(reader, MAX_JSON_PAYLOAD_BYTES);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String readDbText(ResultSet rs, String column, int maxChars) throws SQLException {
        Object raw = rs.getObject(column);
        if (raw == null) {
            return null;
        }
        String normalized = ServletRequestParamUtil.normalizeBodyText(String.valueOf(raw), maxChars, true);
        if (normalized == null) {
            return null;
        }
        return normalized;
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        ServletJsonResponseUtil.writeError(resp, status, message);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        ServletJsonResponseUtil.writeJson(resp, status, payload);
    }
}
