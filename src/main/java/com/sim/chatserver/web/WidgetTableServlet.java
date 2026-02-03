package com.sim.chatserver.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

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
        handleCheck(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }
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

    private void handleCheck(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String widgetId = req.getParameter("widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId is required.");
            return;
        }

        String tableName = sanitizeWidgetId(widgetId);
        try (Connection conn = dsHolder.getDataSource().getConnection()) {
            TableStatus status = determineTableStatus(conn, tableName);
            resp.setContentType("application/json");
            resp.getWriter().write(buildResponse(widgetId, tableName, status.exists, status.message, false));
        } catch (SQLException e) {
            log.severe("Unable to check widget table: " + e.getMessage());
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to inspect the database.");
        }
    }

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
                resp.getWriter().write(buildResponse(widgetId, tableName, true,
                        "Table already exists.", false));
                return;
            }

            createTable(conn, tableName);
            resp.setContentType("application/json");
            resp.getWriter().write(buildResponse(widgetId, tableName, true,
                    "Table created successfully.", true));
        } catch (SQLException e) {
            log.severe("Unable to create widget table: " + e.getMessage());
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create the table.");
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

    private String buildResponse(String widgetId, String tableName, boolean exists, String message,
            boolean created) {
        return "{\"status\":\"ok\",\"widgetId\":\"" + escapeJson(widgetId)
                + "\",\"tableName\":\"" + escapeJson(tableName)
                + "\",\"tableExists\":" + exists
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
