package com.sim.chatserver.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardSessionSelectionServlet", urlPatterns = {"/dashboard/session-review"})
public class DashboardSessionSelectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionSelectionServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String rawSessionId = req.getParameter("sessionId");
        if (rawSessionId == null || rawSessionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "sessionId parameter is required.");
            return;
        }
        String sessionId = rawSessionId.trim();

        List<TermChatSnapshot> snapshots;
        try {
            snapshots = collectSessionEntries(sessionId);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load sessions for review", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load session chats.");
            return;
        }

        if (snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested session.");
            return;
        }

        // Create a selection for review (assumes WidgetReviewStartServlet.createSnapshotSelection exists)
        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Session " + sessionId,
                snapshots,
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create a review selection.");
            return;
        }

        String redirectUrl = req.getContextPath()
                + "/admin/widgets/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8)
                + "&sessionId="
                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
        resp.sendRedirect(redirectUrl);
    }

    private List<TermChatSnapshot> collectSessionEntries(String sessionId) throws SQLException {
        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return snapshots;
        }
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null) {
                    continue;
                }
                String widgetId = widget.getWidgetId();
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }
                String sql = "SELECT widget_chat_id, prompt, response_text, created_at FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE session_id = ? ORDER BY created_at DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            snapshots.add(new TermChatSnapshot(
                                    sessionId,
                                    widgetId,
                                    chatId == null ? "" : chatId,
                                    rs.getString("prompt"),
                                    rs.getString("response_text"),
                                    createdAt,
                                    sessionId
                            ));
                        }
                    }
                } catch (SQLException e) {
                    // log and continue with other widgets
                    log.log(Level.WARNING, "Query failed for widget table " + tableName + ": " + e.getMessage(), e);
                }
            }
        }
        return snapshots;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets for session review", e);
            return List.of();
        }
    }

    // Proper tableExists implementation using DatabaseMetaData
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        // query possible name forms to be robust to case sensitivity
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
}
