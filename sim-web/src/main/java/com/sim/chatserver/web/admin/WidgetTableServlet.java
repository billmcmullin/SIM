package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableServlet", urlPatterns = {"/admin/widgets/table-check"})
public class WidgetTableServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetTableServlet.class.getName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final int DEFAULT_PARAM_MAX_LEN = 256;
    private static final int BULK_IDS_PARAM_MAX_LEN = 8192;

    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorizeAdmin(req, resp)) {
            return;
        }
        // support either single widgetId or bulk ids (comma-separated widgetIds)
        String idsParam = firstParam(req, "ids", BULK_IDS_PARAM_MAX_LEN);
        String widgetId = firstParam(req, "widgetId");

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
            handleBulkCheck(resp, widgetIds);
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
        String role = session.getAttribute("role") == null ? "" : String.valueOf(session.getAttribute("role"));
        if (!"ADMIN".equalsIgnoreCase(role)) {
            jsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    // Handle single widget check (existing behavior, extended to include count when exists)
    private void handleCheck(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String widgetId = firstParam(req, "widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId is required.");
            return;
        }

        String tableName = sanitizeWidgetId(widgetId);
        if (!isSafeIdentifier(tableName)) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid widgetId.");
            return;
        }
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            TableStatus status = determineTableStatus(conn, tableName);
            String countJson = "null";
            if (status.exists) {
                try {
                    countJson = Long.toString(countRows(conn, tableName));
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Unable to count rows for table {0}: {1}",
                            new Object[]{tableName, e.getMessage()});
                    // continue and return exists=true but count=null
                }
            }
            resp.setContentType("application/json");
            resp.getWriter().write(buildSingleResponse(widgetId, tableName, status.exists, countJson, status.message, false));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to check widget table", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to inspect the database.");
        }
    }

    // Handle bulk checks: return JSON array of statuses
    private void handleBulkCheck(HttpServletResponse resp, List<String> widgetIds) throws IOException {
        resp.setContentType("application/json");
        StringBuilder out = new StringBuilder();
        out.append("{\"status\":\"ok\",\"statuses\":[");
        boolean first = true;

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            for (String wid : widgetIds) {
                if (!first) {
                    out.append(',');
                }
                first = false;

                String tableName = sanitizeWidgetId(wid);
                if (!isSafeIdentifier(tableName)) {
                    out.append('{');
                    out.append("\"widgetId\":\"").append(escapeJson(wid)).append("\",");
                    out.append("\"tableName\":\"").append(escapeJson(tableName)).append("\",");
                    out.append("\"tableExists\":false,");
                    out.append("\"count\":null,");
                    out.append("\"message\":\"Invalid widget identifier.\"");
                    out.append('}');
                    continue;
                }
                boolean exists = false;
                String countJson = "null";
                String message = "";

                try {
                    exists = tableExists(conn, tableName);
                    if (exists) {
                        try {
                            countJson = Long.toString(countRows(conn, tableName));
                        } catch (SQLException sqle) {
                            message = "Unable to count rows: " + sqle.getMessage();
                            log.log(Level.WARNING, "Count rows error for table {0}: {1}",
                                    new Object[]{tableName, sqle.getMessage()});
                        }
                    }
                } catch (SQLException e) {
                    message = "Error checking table: " + e.getMessage();
                    log.log(Level.WARNING, "Table check error for {0}: {1}",
                            new Object[]{tableName, e.getMessage()});
                }

                out.append('{');
                out.append("\"widgetId\":\"").append(escapeJson(wid)).append("\",");
                out.append("\"tableName\":\"").append(escapeJson(tableName)).append("\",");
                out.append("\"tableExists\":").append(exists ? "true" : "false").append(",");
                out.append("\"count\":").append(countJson).append(',');
                out.append("\"message\":\"").append(escapeJson(message)).append("\"");
                out.append('}');
            }
            out.append("]}");
            resp.getWriter().write(out.toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to check widget tables (bulk)", e);
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
        String widgetId = firstParam(req, "widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId is required.");
            return;
        }

        String tableName = sanitizeWidgetId(widgetId);
        if (!isSafeIdentifier(tableName)) {
            jsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid widgetId.");
            return;
        }
        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            TableStatus status = determineTableStatus(conn, tableName);
            if (status.exists) {
                resp.setContentType("application/json");
                resp.getWriter().write(buildSingleResponse(widgetId, tableName, true,
                        // include current count if possible
                    Long.toString(countRowsSafe(conn, tableName)),
                        "Table already exists.", false));
                return;
            }

            // create table
            createTable(conn, tableName);

            // get count after creation (should be 0)
            Long count = countRowsSafe(conn, tableName);

            resp.setContentType("application/json");
            resp.getWriter().write(buildSingleResponse(widgetId, tableName, true,
                    Long.toString(count), "Table created successfully.", true));
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to create widget table", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create the table.");
        }
    }

    private long countRowsSafe(Connection conn, String tableName) {
        try {
            return countRows(conn, tableName);
        } catch (SQLException e) {
            log.log(Level.WARNING, "countRowsSafe failed for {0}: {1}",
                    new Object[]{tableName, e.getMessage()});
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
        if (tableExistsByMetadata(conn, tableName)) {
            return true;
        }
        return tableExistsByProbe(conn, tableName);
    }

    private boolean tableExistsByMetadata(Connection conn, String tableName) throws SQLException {
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

    private boolean tableExistsByProbe(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT 1 FROM " + quoteIdentifier(tableName) + " WHERE 1 = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            if (isMissingRelationSqlState(ex)) {
                return false;
            }
            throw ex;
        }
    }

    private boolean isMissingRelationSqlState(SQLException ex) {
        String sqlState = ex.getSQLState();
        return "42P01".equals(sqlState) || "42S02".equals(sqlState);
    }

    private long countRows(Connection conn, String tableName) throws SQLException {
        // Quote identifier to avoid SQL injection; tableName is sanitized earlier.
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        }
    }

    private void createTable(Connection conn, String tableName) throws SQLException {
        String sql = "CREATE TABLE " + quoteIdentifier(tableName)
                + " (id BIGSERIAL PRIMARY KEY, payload TEXT, created_at TIMESTAMPTZ DEFAULT now())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
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
        if (!isSafeIdentifier(identifier)) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private boolean isSafeIdentifier(String identifier) {
        return identifier != null && SAFE_SQL_IDENTIFIER.matcher(identifier).matches();
    }

    private String firstParam(HttpServletRequest req, String name) {
        return firstParam(req, name, DEFAULT_PARAM_MAX_LEN);
    }

    private String firstParam(HttpServletRequest req, String name, int maxLen) {
        return RequestParamContext.from(req).first(name, maxLen);
    }

    private AppDataSourceHolder dataSourceHolder() {
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private static final class RequestParamContext {

        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        private String first(String name, int maxLen) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String value = request.getParameter(name);
            if (value == null) {
                return null;
            }
            String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            int effectiveMax = maxLen > 0 ? maxLen : DEFAULT_PARAM_MAX_LEN;
            return normalized.length() > effectiveMax ? normalized.substring(0, effectiveMax) : normalized;
        }
    }

    private void jsonError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String buildSingleResponse(String widgetId, String tableName, boolean exists, String countJson, String message,
            boolean created) {
        return "{\"status\":\"ok\",\"widgetId\":\"" + escapeJson(widgetId)
                + "\",\"tableName\":\"" + escapeJson(tableName)
                + "\",\"tableExists\":" + exists
                + ",\"count\":" + (countJson == null ? "null" : countJson)
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

        private TableStatus(boolean exists, String message) {
            this.exists = exists;
            this.message = message;
        }
    }
}
