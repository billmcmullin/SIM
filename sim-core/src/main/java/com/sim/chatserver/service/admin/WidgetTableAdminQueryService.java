package com.sim.chatserver.service.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.enterprise.inject.spi.CDI;

public final class WidgetTableAdminQueryService {

    private static final String MSG_TABLE_ACCESSIBLE = "Table is accessible.";
    private static final String MSG_TABLE_MISSING = "Table does not exist.";
    private static final String MSG_INSPECT_TABLE_FAILED = "Unable to inspect widget table";
    private static final String EMPTY = "";
    private static final String MSG_INSPECT_TABLE_STATUSES_FAILED = "Unable to inspect widget table statuses";
    private static final String MSG_TABLE_ALREADY_EXISTS = "Table already exists.";
    private static final String MSG_TABLE_CREATED = "Table created successfully.";
    private static final String MSG_CREATE_WIDGET_TABLE_FAILED = "Unable to create widget table";
    private static final String MSG_OPEN_DB_CONNECTION_FAILED = "Unable to open database connection";
    private static final String MSG_METADATA_CHECK_FAILED_PREFIX = "Metadata table check failed for ";
    private static final String SQL_EXISTS_PROBE_PREFIX = "SELECT 1 FROM ";
    private static final String SQL_EXISTS_PROBE_SUFFIX = " WHERE 1 = 0";
    private static final String SQLSTATE_UNDEFINED_TABLE_POSTGRES = "42P01";
    private static final String SQLSTATE_UNDEFINED_TABLE_ANSI = "42S02";
    private static final String MSG_PROBE_CHECK_FAILED_PREFIX = "Probe table check failed for ";
    private static final String SQL_COUNT_ROWS_PREFIX = "SELECT COUNT(*) FROM ";
    private static final String MSG_COUNT_ROWS_FAILED_PREFIX = "Count rows failed for ";
    private static final String SQL_CREATE_TABLE_PREFIX = "CREATE TABLE ";
    private static final String SQL_CREATE_TABLE_BODY = " (id BIGSERIAL PRIMARY KEY, payload TEXT, created_at TIMESTAMPTZ DEFAULT now())";
    private static final String MSG_CREATE_TABLE_FAILED_PREFIX = "Unable to create table ";
    private static final String IDENTIFIER_REGEX = CommonLiteralConstants.SQL_IDENTIFIER_REGEX;
    private static final String MSG_INVALID_SQL_IDENTIFIER = CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;

    private final Logger log;

    public WidgetTableAdminQueryService(Logger log) {
        this.log = log;
    }

    public CheckResult checkTable(String tableName) {
        try (Connection conn = openConnectionSafe()) {
            boolean exists = tableExists(conn, tableName);
            Long count = exists ? Long.valueOf(countRows(conn, tableName)) : null;
            String message = exists ? MSG_TABLE_ACCESSIBLE : MSG_TABLE_MISSING;
            return new CheckResult(exists, count, message);
        } catch (SQLException | IllegalStateException ex) {
            throw new IllegalStateException(MSG_INSPECT_TABLE_FAILED, ex);
        }
    }

    public Map<String, CheckResult> checkTables(List<String> tableNames) {
        Map<String, CheckResult> out = new LinkedHashMap<>();
        if (tableNames == null || tableNames.isEmpty()) {
            return out;
        }

        try (Connection conn = openConnectionSafe()) {
            for (String tableName : tableNames) {
                boolean exists = tableExists(conn, tableName);
                Long count = exists ? Long.valueOf(countRows(conn, tableName)) : null;
                out.put(tableName, new CheckResult(exists, count, EMPTY));
            }
            return out;
        } catch (SQLException | IllegalStateException ex) {
            throw new IllegalStateException(MSG_INSPECT_TABLE_STATUSES_FAILED, ex);
        }
    }

    public CheckResult createTableIfMissing(String tableName) {
        try (Connection conn = openConnectionSafe()) {
            boolean exists = tableExists(conn, tableName);
            if (!exists) {
                createTable(conn, tableName);
            }
            long count = countRows(conn, tableName);
            return new CheckResult(
                    true,
                    Long.valueOf(count),
                    exists ? MSG_TABLE_ALREADY_EXISTS : MSG_TABLE_CREATED,
                    !exists
            );
        } catch (SQLException | IllegalStateException ex) {
            throw new IllegalStateException(MSG_CREATE_WIDGET_TABLE_FAILED, ex);
        }
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_OPEN_DB_CONNECTION_FAILED, e);
        }
    }

    private boolean tableExists(Connection conn, String tableName) {
        if (tableExistsByMetadata(conn, tableName)) {
            return true;
        }
        return tableExistsByProbe(conn, tableName);
    }

    private boolean tableExistsByMetadata(Connection conn, String tableName) {
        try {
            var meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{CommonLiteralConstants.DB_TABLE_TYPE_TABLE})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_METADATA_CHECK_FAILED_PREFIX + tableName, ex);
        }
        return false;
    }

    private boolean tableExistsByProbe(Connection conn, String tableName) {
        String sql = SQL_EXISTS_PROBE_PREFIX + quoteIdentifier(tableName) + SQL_EXISTS_PROBE_SUFFIX;
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            String sqlState = ex.getSQLState();
            if (SQLSTATE_UNDEFINED_TABLE_POSTGRES.equals(sqlState) || SQLSTATE_UNDEFINED_TABLE_ANSI.equals(sqlState)) {
                return false;
            }
            log.log(Level.FINE, MSG_PROBE_CHECK_FAILED_PREFIX + tableName, ex);
            return false;
        }
    }

    private long countRows(Connection conn, String tableName) {
        String sql = SQL_COUNT_ROWS_PREFIX + quoteIdentifier(tableName);
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_COUNT_ROWS_FAILED_PREFIX + tableName, ex);
            return 0L;
        }
    }

    private void createTable(Connection conn, String tableName) {
        String sql = SQL_CREATE_TABLE_PREFIX + quoteIdentifier(tableName)
                + SQL_CREATE_TABLE_BODY;
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_CREATE_TABLE_FAILED_PREFIX + tableName, ex);
        }
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !identifier.matches(IDENTIFIER_REGEX)) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(QUOTE, DOUBLE_QUOTE) + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }
}

