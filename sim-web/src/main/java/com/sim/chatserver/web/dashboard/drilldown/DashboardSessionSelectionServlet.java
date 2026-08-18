package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
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
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardSessionSelectionServlet", urlPatterns = {"/dashboard/sessions/drilldown/session-review"})
public class DashboardSessionSelectionServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardSessionSelectionServlet.class.getName());
    private static final java.util.regex.Pattern SAFE_SQL_IDENTIFIER = java.util.regex.Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        String rawSessionId = ServletRequestParamUtil.firstParam(req, "sessionId", 128, true, true);
        if (rawSessionId == null || rawSessionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "sessionId parameter is required.");
            return;
        }
        String sessionId = rawSessionId.trim();

        List<TermChatSnapshot> snapshots = collectSessionEntries(sessionId);

        if (snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested session.");
            return;
        }

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

        req.setAttribute("selectionId", selectionId);
        req.getRequestDispatcher("/dashboard/widgets/drilldown/review").forward(req, resp);
    
        } catch (IOException | ServletException | RuntimeException e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, "Failed sending fallback server error.", ioe);
        }
    }

    private List<TermChatSnapshot> collectSessionEntries(String sessionId) {
        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return snapshots;
        }
        Connection conn = null;
        try {
            conn = dataSourceHolder().getDataSource().getConnection();
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
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, sessionId);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        String chatId = rs.getString("widget_chat_id");
                        Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
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
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Query failed for widget table " + tableName + ": " + e.getMessage(), e);
                } finally {
                    closeQuietly(rs);
                    closeQuietly(ps);
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to collect session entries for review", e);
        } finally {
            closeQuietly(conn);
        }
        return snapshots;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // ignore close failure
            }
        }
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to list widgets for session review", e);
            return List.of();
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to inspect table metadata for " + tableName, e);
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
}
