package com.sim.chatserver.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableServlet", urlPatterns = {"/admin/widgets/table-check"})
public class WidgetTableServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetTableServlet.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }
        // support either single widgetId or bulk ids (comma-separated widgetIds)
        String idsParam = req.getParameter("ids");
        String widgetId = req.getParameter("widgetId");

        if ((idsParam == null || idsParam.isBlank()) && (widgetId == null || widgetId.isBlank())) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Provide 'widgetId' or 'ids' query parameter.");
            return;
        }

        if (idsParam != null && !idsParam.isBlank()) {
            // bulk mode
            List<String> widgetIds = Arrays.stream(idsParam.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
            handleBulkCheck(req, resp, widgetIds);
        } else {
            handleCheck(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }
        // Support creating a table for a widget via POST widgetId=...
        handleCreate(req, resp);
    }

    private boolean authorizeAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            jsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            jsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    // Handle single widget check (existing behavior, extended to include count when exists)
    private void handleCheck(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String widgetId = req.getParameter("widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId is required.");
            return;
        }

        String tableName = sanitizeWidgetId(widgetId);
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            TableStatus status = determineTableStatus(conn, tableName);
            Long count = null;
            if (status.exists) {
                try {
                    count = countRows(conn, tableName);
                } catch (SQLException e) {
                    log.warning("Unable to count rows for table " + tableName + ": " + e.getMessage());
                    // continue and return exists=true but count=null
                }
            }
            resp.setContentType("application/json");
            resp.getWriter().write(buildSingleResponse(widgetId, tableName, status.exists, count, status.message, false));
        } catch (SQLException e) {
            log.severe("Unable to check widget table: " + e.getMessage());
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to inspect the database.");
        }
    }

    // Handle bulk checks: return JSON array of statuses
    private void handleBulkCheck(HttpServletRequest req, HttpServletResponse resp, List<String> widgetIds) throws IOException {
        resp.setContentType("application/json");
        StringBuilder out = new StringBuilder();
        out.append("{\"status\":\"ok\",\"statuses\":[");
        boolean first = true;

        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            for (String wid : widgetIds) {
                if (!first) {
                    out.append(",");
                }
                first = false;

                String tableName = sanitizeWidgetId(wid);
                boolean exists = false;
                Long count = null;
                String message = "";

                try {
                    exists = tableExists(conn, tableName);
                    if (exists) {
                        try {
                            count = countRows(conn, tableName);
                        } catch (SQLException sqle) {
                            message = "Unable to count rows: " + escapeJson(sqle.getMessage());
                            log.warning("Count rows error for table " + tableName + ": " + sqle.getMessage());
                        }
                    } else {
                        count = null;
                    }
                } catch (SQLException e) {
                    message = "Error checking table: " + escapeJson(e.getMessage());
                    log.warning("Table check error for " + tableName + ": " + e.getMessage());
                }

                out.append("{");
                out.append("\"widgetId\":\"").append(escapeJson(wid)).append("\",");
                out.append("\"tableName\":\"").append(escapeJson(tableName)).append("\",");
                out.append("\"tableExists\":").append(exists ? "true" : "false").append(",");
                out.append("\"count\":").append(count == null ? "null" : count).append(",");
                out.append("\"message\":\"").append(escapeJson(message)).append("\"");
                out.append("}");
            }
            out.append("]}");
            resp.getWriter().write(out.toString());
        } catch (SQLException e) {
            log.severe("Unable to check widget tables (bulk): " + e.getMessage());
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to check widget table statuses.");
        }
    }

    /**
     * Create table for widget (POST). Expects parameter widgetId. If table
     * already exists, returns created=false and message. If created
     * successfully, returns created=true.
     */
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String widgetId = req.getParameter("widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId is required.");
            return;
        }

        String tableName = sanitizeWidgetId(widgetId);
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            TableStatus status = determineTableStatus(conn, tableName);
            if (status.exists) {
                resp.setContentType("application/json");
                resp.getWriter().write(buildSingleResponse(widgetId, tableName, true,
                        // include current count if possible
                        countRowsSafe(conn, tableName),
                        "Table already exists.", false));
                return;
            }

            // create table
            createTable(conn, tableName);

            // get count after creation (should be 0)
            Long count = countRowsSafe(conn, tableName);

            resp.setContentType("application/json");
            resp.getWriter().write(buildSingleResponse(widgetId, tableName, true,
                    count, "Table created successfully.", true));
        } catch (SQLException e) {
            log.severe("Unable to create widget table: " + e.getMessage());
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create the table.");
        }
    }

    private long countRowsSafe(Connection conn, String tableName) {
        try {
            return countRows(conn, tableName);
        } catch (SQLException e) {
            log.warning("countRowsSafe failed for " + tableName + ": " + e.getMessage());
            return 0L;
        }
    }

    private TableStatus determineTableStatus(Connection conn, String tableName) throws SQLException {
        if (tableExists(conn, tableName)) {
            return new TableStatus(true, "Table is accessible.");
        }
        return new TableStatus(false, "Table does not exist.");
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

    private long countRows(Connection conn, String tableName) throws SQLException {
        // Quote identifier to avoid SQL injection; tableName is sanitized earlier.
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        }
    }

    private void createTable(Connection conn, String tableName) throws SQLException {
        String sql = "CREATE TABLE " + quoteIdentifier(tableName)
                + " (id BIGSERIAL PRIMARY KEY, payload TEXT, created_at TIMESTAMPTZ DEFAULT now())";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private String sanitizeWidgetId(String widgetId) {
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

    private void jsonError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String buildSingleResponse(String widgetId, String tableName, boolean exists, Long count, String message,
            boolean created) {
        return "{\"status\":\"ok\",\"widgetId\":\"" + escapeJson(widgetId)
                + "\",\"tableName\":\"" + escapeJson(tableName)
                + "\",\"tableExists\":" + exists
                + ",\"count\":" + (count == null ? "null" : count)
                + ",\"created\":" + created
                + ",\"message\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", " ");
    }

    private static final class TableStatus {

        final boolean exists;
        final String message;

        TableStatus(boolean exists, String message) {
            this.exists = exists;
            this.message = message;
        }
    }
}
