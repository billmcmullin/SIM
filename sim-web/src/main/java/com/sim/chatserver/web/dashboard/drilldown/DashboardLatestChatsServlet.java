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
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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

@WebServlet(name = "DashboardLatestChatsServlet", urlPatterns = {"/dashboard/latest-chats"})
public class DashboardLatestChatsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardLatestChatsServlet.class.getName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String contextPath = safeContextPath(req.getContextPath());
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(safeRedirectPath(contextPath + "/login", "/login"));
            return;
        }

        int limit = parseLimit(firstParam(req, "limit"), 200);

        List<TermChatSnapshot> snapshots = collectLatestChats(limit);
        if (snapshots.isEmpty()) {
            // Keep UX smooth: redirect back to dashboard instead of 404 page.
            resp.sendRedirect(safeRedirectPath(contextPath + "/dashboard?latestChats=empty", "/dashboard?latestChats=empty"));
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Latest Chats",
                snapshots,
                contextPath + "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create latest-chats selection.");
            return;
        }

        String redirectUrl = contextPath
                + "/dashboard/widgets/drilldown/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8);
        resp.sendRedirect(safeRedirectPath(redirectUrl, "/dashboard"));
    }

    private List<TermChatSnapshot> collectLatestChats(int limit) {
        List<TermChatSnapshot> all = new ArrayList<>();
        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load widgets for latest chats", e);
            widgets = List.of();
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (WidgetEntry w : widgets) {
                if (w == null || w.getWidgetId() == null || w.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = w.getWidgetId();
                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " ORDER BY created_at DESC LIMIT ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Math.max(limit, 1));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            String prompt = rs.getString("prompt");
                            String response = rs.getString("response_text");
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            String sessionId = rs.getString("session_id");

                            all.add(new TermChatSnapshot(
                                    "Latest Chats",
                                    widgetId,
                                    chatId == null ? "" : chatId,
                                    prompt,
                                    response,
                                    createdAt,
                                    sessionId
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to collect latest chats", e);
        }

        all.sort(Comparator.comparing(
                (TermChatSnapshot s) -> s.getCreatedAt() == null ? new Timestamp(0) : s.getCreatedAt()
        ).reversed());

        if (all.size() > limit) {
            return new ArrayList<>(all.subList(0, limit));
        }
        return all;
    }

    private int parseLimit(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            if (v <= 0) {
                return fallback;
            }
            return Math.min(v, 2000);
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid latest chats limit", e);
            return fallback;
        }
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

    private String firstParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        var params = req.getParameterMap();
        if (params == null || params.isEmpty()) {
            return null;
        }
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        String value = values[0];
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > 32 ? trimmed.substring(0, 32) : trimmed;
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
}
