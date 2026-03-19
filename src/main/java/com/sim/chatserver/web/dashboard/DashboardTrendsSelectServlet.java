package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTrendsSelectServlet", urlPatterns = {"/dashboard/trends/select"})
public class DashboardTrendsSelectServlet extends HttpServlet {

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

        JsonObject body;
        try (JsonReader jr = Json.createReader(req.getInputStream())) {
            body = jr.readObject();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        String day = body.getString("day", "").trim();
        String widgetIdFilter = body.getString("widgetId", "").trim(); // optional
        if (day.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"day is required (yyyy-MM-dd).\"}");
            return;
        }

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(day);
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid day format. Use yyyy-MM-dd.\"}");
            return;
        }

        List<TermChatSnapshot> snapshots = new ArrayList<>();
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            List<WidgetEntry> widgets = WidgetStore.list(null);

            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = w.getWidgetId();
                if (!widgetIdFilter.isBlank() && !widgetIdFilter.equals(widgetId)) {
                    continue; // scoped drilldown for widget chart clicks
                }

                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE DATE(created_at) = ? ORDER BY created_at DESC";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDate(1, java.sql.Date.valueOf(targetDate));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            String prompt = rs.getString("prompt");
                            String responseText = rs.getString("response_text");
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            String sessionId = rs.getString("session_id");

                            snapshots.add(new TermChatSnapshot(
                                    "Entry Trends",
                                    widgetId,
                                    chatId == null ? "" : chatId,
                                    prompt,
                                    responseText,
                                    createdAt,
                                    sessionId
                            ));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to collect chats for day.\"}");
            return;
        }

        if (snapshots.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"No chats found for selected day.\"}");
            return;
        }

        String label = widgetIdFilter.isBlank()
                ? ("Entry Trends " + day)
                : ("Entry Trends " + widgetIdFilter + " " + day);

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                label,
                snapshots,
                req.getContextPath() + "/dashboard/trends"
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
                .add("count", snapshots.size())
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
