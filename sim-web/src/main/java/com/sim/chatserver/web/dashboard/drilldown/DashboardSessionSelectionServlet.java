package com.sim.chatserver.web.dashboard.drilldown;

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
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardSessionSelectionServlet", urlPatterns = {"/dashboard/sessions/drilldown/session-review"})
public class DashboardSessionSelectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionSelectionServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            String loginPath = safeRedirectPath("/login", "/login");
            if (!isAllowedRedirect(loginPath)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect target.");
                return;
            }
            req.getRequestDispatcher(loginPath).forward(req, resp);
            return;
        }

        String rawSessionId = firstQueryParam(req, "sessionId");
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

        String redirectPath = "/dashboard/widgets/drilldown/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8)
                + "&sessionId="
                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
        String forwardPath = safeRedirectPath(redirectPath, "/dashboard");
        if (!isAllowedRedirect(forwardPath)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect target.");
            return;
        }
        req.getRequestDispatcher(forwardPath).forward(req, resp);
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
                    }
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Query failed for widget table " + tableName + ": " + e.getMessage(), e);
                }
            }
        }
        return snapshots;
    }

    private List<WidgetEntry> listWidgets() {
        try {
            return WidgetStore.list(null);
        } catch (SQLException | RuntimeException e) {
            log.log(Level.WARNING, "Unable to list widgets for session review", e);
            return List.of();
        }
    }

    private String firstQueryParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String[] values = req.getParameterValues(name);
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }
        String val = values[0].replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
        if (val.isEmpty()) {
            return null;
        }
        return val.length() > 128 ? val.substring(0, 128) : val;
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

    private boolean isAllowedRedirect(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return false;
        }
        if (redirectUrl.contains("://") || redirectUrl.contains("\r") || redirectUrl.contains("\n")) {
            return false;
        }
        if ("/login".equals(redirectUrl) || "/dashboard".equals(redirectUrl)) {
            return true;
        }
        return redirectUrl.contains("/dashboard/widgets/drilldown/review?selectionId=");
    }

    private String safeRedirectPath(String target, String fallback) {
        if (target == null) {
            return fallback;
        }
        String trimmed = target.trim();
        if (!trimmed.startsWith("/") || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return fallback;
        }
        return trimmed;
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
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}
