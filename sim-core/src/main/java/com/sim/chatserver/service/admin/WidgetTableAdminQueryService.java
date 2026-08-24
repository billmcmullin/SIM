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

import jakarta.enterprise.inject.spi.CDI;

public final class WidgetTableAdminQueryService {

    private final Logger log;

    public WidgetTableAdminQueryService(Logger log) {
        this.log = log;
    }

    public CheckResult checkTable(String tableName) {
        try (Connection conn = openConnectionSafe()) {
            boolean exists = tableExists(conn, tableName);
            Long count = exists ? Long.valueOf(countRows(conn, tableName)) : null;
            String message = exists ? "Table is accessible." : "Table does not exist.";
            return new CheckResult(exists, count, message);
        } catch (SQLException | IllegalStateException ex) {
            throw new IllegalStateException("Unable to inspect widget table", ex);
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
                out.put(tableName, new CheckResult(exists, count, ""));
            }
            return out;
        } catch (SQLException | IllegalStateException ex) {
            throw new IllegalStateException("Unable to inspect widget table statuses", ex);
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
                    exists ? "Table already exists." : "Table created successfully.",
                    !exists
            );
        } catch (SQLException | IllegalStateException ex) {
            throw new IllegalStateException("Unable to create widget table", ex);
        }
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to open database connection", e);
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
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            String sqlState = ex.getSQLState();
            if ("42P01".equals(sqlState) || "42S02".equals(sqlState)) {
                return false;
            }
            log.log(Level.FINE, "Probe table check failed for " + tableName, ex);
            return false;
        }
    }

    private long countRows(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName);
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
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
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to create table " + tableName, ex);
        }
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("^[A-Za-z_][A-Za-z0-9_]{0,62}$")) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    public static final class CheckResult {
        public final boolean exists;
        public final Long count;
        public final String message;
        public final boolean created;

        private CheckResult(boolean exists, Long count, String message) {
            this(exists, count, message, false);
        }

        private CheckResult(boolean exists, Long count, String message, boolean created) {
            this.exists = exists;
            this.count = count;
            this.message = message;
            this.created = created;
        }
    }
}