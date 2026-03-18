package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

@WebServlet(name = "DashboardLatestChatsServlet", urlPatterns = {"/dashboard/latest-chats"})
public class DashboardLatestChatsServlet extends HttpServlet {

    private static final String REVIEW_TEMPLATE = "/WEB-INF/views/widget_review.html";

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int limit = parseLimit(req.getParameter("limit"), 200);

        List<TermChatSnapshot> snapshots = collectLatestChats(limit);
        if (snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found.");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Latest Chats",
                snapshots,
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create latest-chats selection.");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/dashboard/widgets/drilldown/review?selectionId=" + selectionId);
    }

    private List<TermChatSnapshot> collectLatestChats(int limit) {
        List<TermChatSnapshot> all = new ArrayList<>();
        List<WidgetEntry> widgets;
        try {
            widgets = WidgetStore.list(null);
        } catch (Exception e) {
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
                            Timestamp createdAt = rs.getTimestamp("created_at");
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
        } catch (Exception ignored) {
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
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
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
