package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
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

    private static final int IN_CLAUSE_BATCH_SIZE = 200;

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return;
        }

        JsonObject payload;
        try (JsonReader reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        JsonArray arr = payload.getJsonArray("selectedChatIds");
        if (arr == null || arr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"selectedChatIds required.\"}");
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
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"No valid chat IDs provided.\"}");
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
        } catch (Exception ignore) {
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
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
                            + " WHERE widget_chat_id IN (" + inClause + ")";

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

                                String prompt = rs.getString("prompt");
                                String responseText = rs.getString("response_text");
                                Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                                String sessionId = rs.getString("session_id");

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
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to resolve selected chats.\"}");
            return;
        }

        if (snapshots.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"No matching chats found in widget tables.\"}");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Popular Topics",
                snapshots,
                req.getContextPath() + "/dashboard/topics"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to create selection.\"}");
            return;
        }

        JsonObject ok = Json.createObjectBuilder()
                .add("status", "ok")
                .add("selectionId", selectionId)
                .add("requestedCount", requestedIds.size())
                .add("resolvedCount", foundIds.size())
                .build();

        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(ok.toString());
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
}
