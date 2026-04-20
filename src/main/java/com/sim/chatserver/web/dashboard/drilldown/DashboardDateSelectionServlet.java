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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

/**
 * Builds a date-based chat selection and redirects into the existing
 * WidgetReviewServlet flow: /dashboard/widgets/drilldown/review?selectionId=...
 */
@WebServlet(name = "DashboardDateSelectionServlet", urlPatterns = {"/dashboard/sessions/drilldown/date-review"})
public class DashboardDateSelectionServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardDateSelectionServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String rawDate = req.getParameter("date");
        if (rawDate == null || rawDate.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "date parameter is required (yyyy-MM-dd).");
            return;
        }

        final LocalDate date;
        try {
            date = LocalDate.parse(rawDate.trim());
        } catch (DateTimeParseException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        List<TermChatSnapshot> snapshots;
        try {
            snapshots = collectDateEntries(date);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load date entries for review", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load chats for date.");
            return;
        }

        if (snapshots.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No chats found for the requested date.");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Date " + date,
                snapshots,
                req.getContextPath() + "/dashboard"
        );

        if (selectionId == null || selectionId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create a review selection.");
            return;
        }

        String redirectUrl = req.getContextPath()
                + "/dashboard/widgets/drilldown/review?selectionId="
                + URLEncoder.encode(selectionId, StandardCharsets.UTF_8)
                + "&date="
                + URLEncoder.encode(date.toString(), StandardCharsets.UTF_8);

        resp.sendRedirect(redirectUrl);
    }

    private List<TermChatSnapshot> collectDateEntries(LocalDate date) throws SQLException {
        List<TermChatSnapshot> snapshots = new ArrayList<>();
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return snapshots;
        }

        Timestamp startTs = Timestamp.valueOf(date.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(date.plusDays(1).atStartOfDay());

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

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?"
                        + " ORDER BY created_at DESC";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, startTs);
                    ps.setTimestamp(2, endTs);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String chatId = rs.getString("widget_chat_id");
                            String prompt = rs.getString("prompt");
                            String response = rs.getString("response_text");
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            String sessionId = rs.getString("session_id");

                            // first constructor arg is "term" label in snapshot model; for date drilldown use date label
                            snapshots.add(new TermChatSnapshot(
                                    date.toString(),
                                    widgetId,
                                    chatId == null ? "" : chatId,
                                    prompt == null ? "" : prompt,
                                    response == null ? "" : response,
                                    createdAt,
                                    sessionId == null ? "" : sessionId
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
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to list widgets for date review", e);
            return List.of();
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
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}
