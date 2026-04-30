package com.sim.chatserver.web.dashboard.widgets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableSelectIdsServlet", urlPatterns = {"/dashboard/widgets/view/select-ids"})
public class WidgetTableSelectIdsServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetTableSelectIdsServlet.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String widgetId = req.getParameter("widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
            return;
        }

        String search = normalize(req.getParameter("search"));
        String filterPrompt = normalize(req.getParameter("filterPrompt"));
        String filterResponse = normalize(req.getParameter("filterResponse"));

        // NEW: optional date filter (YYYY-MM-DD)
        LocalDate selectedDate = null;
        String dateRaw = req.getParameter("date");
        if (dateRaw != null && !dateRaw.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateRaw.trim(), DATE_FMT);
            } catch (Exception ex) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().print(Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Invalid date. Expected YYYY-MM-DD.")
                        .build()
                        .toString());
                return;
            }
        }

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            String tableName = sanitizeWidgetTableName(widgetId);
            if (!tableExists(conn, tableName)) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("application/json");
                resp.getWriter().print(Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Widget data not found")
                        .build()
                        .toString());
                return;
            }

            List<String> conditions = new ArrayList<>();
            List<String> values = new ArrayList<>();
            conditions.add("widget_chat_id IS NOT NULL");

            if (!search.isBlank()) {
                conditions.add("(LOWER(widget_chat_id) LIKE ? OR LOWER(prompt) LIKE ? OR LOWER(response_text) LIKE ?)");
                String pattern = "%" + search + "%";
                values.add(pattern);
                values.add(pattern);
                values.add(pattern);
            }
            if (!filterPrompt.isBlank()) {
                conditions.add("LOWER(prompt) LIKE ?");
                values.add("%" + filterPrompt + "%");
            }
            if (!filterResponse.isBlank()) {
                conditions.add("LOWER(response_text) LIKE ?");
                values.add("%" + filterResponse + "%");
            }

            // NEW: date condition
            Timestamp startTs = null;
            Timestamp endTs = null;
            if (selectedDate != null) {
                conditions.add("created_at >= ? AND created_at < ?");
                startTs = Timestamp.valueOf(selectedDate.atStartOfDay());
                endTs = Timestamp.valueOf(selectedDate.plusDays(1).atStartOfDay());
            }

            StringBuilder sql = new StringBuilder("SELECT widget_chat_id FROM ")
                    .append(quoteIdentifier(tableName));
            if (!conditions.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", conditions));
            }
            sql.append(" ORDER BY created_at DESC");

            List<String> chatIds = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                for (String value : values) {
                    ps.setString(idx++, value);
                }

                // bind date params after string params
                if (selectedDate != null) {
                    ps.setTimestamp(idx++, startTs);
                    ps.setTimestamp(idx++, endTs);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString("widget_chat_id");
                        if (chatId != null && !chatId.isBlank()) {
                            chatIds.add(chatId.trim());
                        }
                    }
                }
            }

            resp.setContentType("application/json");
            resp.getWriter().print(Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("total", chatIds.size())
                    .add("chatIds", Json.createArrayBuilder(chatIds))
                    .build()
                    .toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to fetch select-all ids", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().print(Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to fetch chat IDs")
                    .build()
                    .toString());
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
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
