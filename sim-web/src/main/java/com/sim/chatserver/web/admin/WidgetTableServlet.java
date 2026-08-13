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
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
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

    private boolean authorizeAdmin(HttpServletRequest req, HttpServletResponse resp) {
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
    private void handleCheck(HttpServletRequest req, HttpServletResponse resp) {
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
        try (Connection conn = openConnectionSafe()) {
            TableStatus status = determineTableStatus(conn, tableName);
            Long count = null;
            if (status.exists) {
                count = Long.valueOf(countRows(conn, tableName));
            }
            writeSingleResponse(resp,
                    HttpServletResponse.SC_OK,
                    widgetId,
                    tableName,
                    status.exists,
                    count,
                    status.message,
                    false);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to check widget table", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to inspect the database.");
        }
    }

    // Handle bulk checks: return JSON array of statuses
    private void handleBulkCheck(HttpServletResponse resp, List<String> widgetIds) {
        JsonArrayBuilder statuses = Json.createArrayBuilder();

        try (Connection conn = openConnectionSafe()) {
            for (String wid : widgetIds) {
                String tableName = sanitizeWidgetId(wid);
                if (!isSafeIdentifier(tableName)) {
                    JsonObjectBuilder statusBody = Json.createObjectBuilder()
                            .add("widgetId", wid == null ? "" : wid)
                            .add("tableName", tableName)
                            .add("tableExists", false)
                            .addNull("count")
                            .add("message", "Invalid widget identifier.");
                    statuses.add(statusBody);
                    continue;
                }

                boolean exists = false;
                Long count = null;
                String message = "";

                exists = tableExists(conn, tableName);
                if (exists) {
                    count = Long.valueOf(countRows(conn, tableName));
                }

                JsonObjectBuilder statusBody = Json.createObjectBuilder()
                        .add("widgetId", wid == null ? "" : wid)
                        .add("tableName", tableName)
                        .add("tableExists", exists)
                        .add("message", message == null ? "" : message);
                if (count == null) {
                    statusBody.addNull("count");
                } else {
                    statusBody.add("count", count.longValue());
                }
                statuses.add(statusBody);
            }

                JsonObjectBuilder payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("statuses", statuses);
                writeJson(resp, HttpServletResponse.SC_OK, payload.build());
        } catch (SQLException | IllegalStateException e) {
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
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) {
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
        try (Connection conn = openConnectionSafe()) {
            TableStatus status = determineTableStatus(conn, tableName);
            if (status.exists) {
                writeSingleResponse(resp,
                        HttpServletResponse.SC_OK,
                        widgetId,
                        tableName,
                        true,
                        Long.valueOf(countRowsSafe(conn, tableName)),
                        "Table already exists.",
                        false);
                return;
            }

            // create table
            createTable(conn, tableName);

            // get count after creation (should be 0)
            Long count = countRowsSafe(conn, tableName);

                writeSingleResponse(resp,
                    HttpServletResponse.SC_OK,
                    widgetId,
                    tableName,
                    true,
                    count,
                    "Table created successfully.",
                    true);
        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to create widget table", e);
            jsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create the table.");
        }
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to open database connection", e);
        }
    }

    private long countRowsSafe(Connection conn, String tableName) {
        return countRows(conn, tableName);
    }

    private TableStatus determineTableStatus(Connection conn, String tableName) {
        if (tableExists(conn, tableName)) {
            return new TableStatus(true, "Table is accessible.");
        }
        return new TableStatus(false, "Table does not exist.");
    }

    private boolean tableExists(Connection conn, String tableName) {
        if (tableExistsByMetadata(conn, tableName)) {
            return true;
        }
        return tableExistsByProbe(conn, tableName);
    }

    private boolean tableExistsByMetadata(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Metadata table check failed for " + tableName, ex);
        }
        return false;
    }

    private boolean tableExistsByProbe(Connection conn, String tableName) {
        String sql = "SELECT 1 FROM " + quoteIdentifier(tableName) + " WHERE 1 = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            if (isMissingRelationSqlState(ex)) {
                return false;
            }
            log.log(Level.FINE, "Probe table check failed for " + tableName, ex);
            return false;
        }
    }

    private boolean isMissingRelationSqlState(SQLException ex) {
        String sqlState = ex.getSQLState();
        return "42P01".equals(sqlState) || "42S02".equals(sqlState);
    }

    private long countRows(Connection conn, String tableName) {
        // Quote identifier to avoid SQL injection; tableName is sanitized earlier.
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException ex) {
            log.log(Level.FINE, "Count rows failed for " + tableName, ex);
            return 0L;
        }
    }

    private void createTable(Connection conn, String tableName) {
        String sql = "CREATE TABLE " + quoteIdentifier(tableName)
                + " (id BIGSERIAL PRIMARY KEY, payload TEXT, created_at TIMESTAMPTZ DEFAULT now())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to create table " + tableName, ex);
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
        String value = ServletRequestParamUtil.firstParam(
                req,
                name,
                maxLen > 0 ? maxLen : DEFAULT_PARAM_MAX_LEN,
                true,
                false);
        return value == null ? "" : value;
    }

    protected AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private void jsonError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write JSON error response", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? "Request failed." : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, jakarta.json.JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write JSON response", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
    }

    private void writeSingleResponse(HttpServletResponse resp,
            int status,
            String widgetId,
            String tableName,
            boolean exists,
            Long count,
            String message,
            boolean created) {
        JsonObjectBuilder body = Json.createObjectBuilder()
                .add("status", "ok")
                .add("widgetId", widgetId == null ? "" : widgetId)
                .add("tableName", tableName == null ? "" : tableName)
                .add("tableExists", exists)
                .add("created", created)
                .add("message", message == null ? "" : message);
        if (count == null) {
            body.addNull("count");
        } else {
            body.add("count", count.longValue());
        }
        writeJson(resp, status, body.build());
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
