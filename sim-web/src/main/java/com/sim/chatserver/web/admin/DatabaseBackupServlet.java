package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sim.chatserver.config.Database;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin data export backup endpoint.
 *
 * Exports all PUBLIC schema base tables as CSV files into a ZIP: -
 * tables/<table_name>.csv - manifest.json
 *
 * This includes dynamic widget tables (widget ID tables) automatically.
 *
 * Note: This is a raw data export backup, not a full pg_dump replacement.
 */
public class DatabaseBackupServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DatabaseBackupServlet.class.getName());

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;
        private static final DateTimeFormatter BACKUP_TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)
            .withZone(java.time.ZoneOffset.UTC);
    private static final int MAX_CELL_TEXT_LENGTH = 65535;
    private static final int MAX_BINARY_BYTES = 2 * 1024 * 1024;

    // Optional exclusions from export
    private static final List<String> EXCLUDED_TABLES = List.of(
            "flyway_schema_history"
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req)) {
            try {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
            } catch (IOException ioe) {
                log.log(Level.FINE, "Unable to send unauthorized response", ioe);
            }
            return;
        }

        String ts = BACKUP_TS_FMT.format(Instant.now());
        String fileName = "chatserver-data-backup-" + ts + ".zip";

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/zip");
        resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

        try (Connection conn = Database.getConnection(); ZipOutputStream zip = new ZipOutputStream(resp.getOutputStream(), StandardCharsets.UTF_8)) {

            List<String> exported = new ArrayList<>();
            List<String> skipped = new ArrayList<>();

            List<String> tables = listExportableTables(conn);
            for (String table : tables) {
                try {
                    exportTableAsCsv(conn, zip, table);
                    exported.add(table);
                } catch (IllegalStateException tableErr) {
                    skipped.add(table);
                    log.log(Level.WARNING, "Failed exporting table: " + table, tableErr);
                }
            }

            writeManifest(zip, exported, skipped, ts);

            zip.finish();
            log.info(() -> "Data backup export completed. Exported tables=" + exported.size() + ", skipped=" + skipped.size());

        } catch (SQLException | IOException | IllegalStateException e) {
            log.log(Level.SEVERE, "Data backup export failed", e);
            if (!resp.isCommitted()) {
                resp.reset();
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Data export failed.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Unable to send backup export error response", ioe);
                }
            }
        }
    
        } catch (IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER) == null) {
            return false;
        }
        Object roleObj = session.getAttribute(SESSION_ROLE);
        String role = roleObj == null ? "" : String.valueOf(roleObj);
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Returns all PUBLIC schema base tables, excluding internal/system tables
     * if configured.
     */
    private List<String> listExportableTables(Connection conn) {
        List<String> out = new ArrayList<>();

        String sql = """
                SELECT tablename
                  FROM pg_catalog.pg_tables
                 WHERE schemaname = 'public'
                 ORDER BY tablename
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String table = readValidatedIdentifier(rs, 1);
                    if (table == null || table.isBlank()) {
                        continue;
                    }
                    if (EXCLUDED_TABLES.contains(table)) {
                        continue;
                    }
                    out.add(table);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to list exportable tables.", e);
        }

        return out;
    }

    private void exportTableAsCsv(Connection conn, ZipOutputStream zip, String tableName) {
        try {
            List<String> columns = listTableColumns(conn, tableName);
            if (columns.isEmpty()) {
                return;
            }
            String projected = String.join(", ", columns.stream().map(this::quoteIdent).toList());
            String sql = "SELECT " + projected + " FROM " + quoteIdent(tableName);

            try (PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                ps.setFetchSize(1000);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();

                    zip.putNextEntry(new ZipEntry("tables/" + tableName + ".csv"));
                    OutputStreamWriter w = new OutputStreamWriter(zip, StandardCharsets.UTF_8);

                    // Header row
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) {
                            w.write(',');
                        }
                        w.write(csvEscape(md.getColumnLabel(i)));
                    }
                    w.write('\n');

                    // Data rows
                    while (rs.next()) {
                        for (int i = 1; i <= cols; i++) {
                            if (i > 1) {
                                w.write(',');
                            }
                            w.write(csvEscape(readCellAsText(rs, md, i)));
                        }
                        w.write('\n');
                    }

                    w.flush();
                    zip.closeEntry();
                }
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed exporting table: " + tableName, e);
        }
    }

    private void writeManifest(ZipOutputStream zip, List<String> exported, List<String> skipped, String ts) {
        try {
            zip.putNextEntry(new ZipEntry("manifest.json"));
            OutputStreamWriter w = new OutputStreamWriter(zip, StandardCharsets.UTF_8);

            w.write("{\n");
            w.write("  \"formatVersion\": 1,\n");
            w.write("  \"type\": \"raw-data-export\",\n");
            w.write("  \"generatedAt\": \"" + jsonEscape(ts) + "\",\n");
            w.write("  \"schema\": \"public\",\n");
            w.write("  \"exportedTableCount\": " + exported.size() + ",\n");
            w.write("  \"skippedTableCount\": " + skipped.size() + ",\n");
            w.write("  \"exportedTables\": " + toJsonArray(exported) + ",\n");
            w.write("  \"skippedTables\": " + toJsonArray(skipped));
            w.write('\n');
            w.write("}\n");

            w.flush();
            zip.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write backup manifest.", e);
        }
    }

    private List<String> listTableColumns(Connection conn, String tableName) {
        List<String> out = new ArrayList<>();
        String sql = "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name=? ORDER BY ordinal_position";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = readValidatedIdentifier(rs, 1);
                    if (name != null) {
                        out.add(name);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to list columns for table: " + tableName, e);
        }
        return out;
    }

    private String readCellAsText(ResultSet rs, ResultSetMetaData md, int columnIndex) {
        try {
            int sqlType = md.getColumnType(columnIndex);
            if (sqlType == Types.BINARY || sqlType == Types.VARBINARY || sqlType == Types.LONGVARBINARY) {
                byte[] bytes = readValidatedBinary(rs, columnIndex);
                if (bytes == null) {
                    return "";
                }
                return Base64.getEncoder().encodeToString(bytes);
            }

            if (sqlType == Types.TIMESTAMP || sqlType == Types.TIMESTAMP_WITH_TIMEZONE) {
                Timestamp ts = readValidatedTimestamp(rs, columnIndex);
                if (ts == null) {
                    return "";
                }
                return ISO_INSTANT_FMT.format(ts.toInstant());
            }

            if (sqlType == Types.DATE) {
                java.sql.Date sqlDate = readValidatedDate(rs, columnIndex);
                if (sqlDate == null) {
                    return "";
                }
                LocalDate localDate = sqlDate.toLocalDate();
                return localDate == null ? "" : localDate.toString();
            }

            String value = readValidatedCellText(rs, columnIndex);
            return value == null ? "" : value;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read cell value.", e);
        }
    }

    private String readValidatedIdentifier(ResultSet rs, int columnIndex) {
        return sanitizeIdentifier(readValidatedCellText(rs, columnIndex));
    }

    private String readValidatedCellText(ResultSet rs, int columnIndex) {
        try {
            return sanitizeCellText(rs.getString(columnIndex));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read text cell value.", e);
        }
    }

    private byte[] readValidatedBinary(ResultSet rs, int columnIndex) {
        try {
            byte[] bytes = rs.getBytes(columnIndex);
            if (bytes != null) {
                return sanitizeBinary(bytes);
            }
            String fallback = rs.getString(columnIndex);
            if (fallback == null || fallback.isBlank()) {
                return new byte[0];
            }
            return sanitizeBinary(fallback.getBytes(StandardCharsets.UTF_8));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read binary cell value.", e);
        }
    }

    private Timestamp readValidatedTimestamp(ResultSet rs, int columnIndex) {
        String rawFallback;
        try {
            Timestamp typed = rs.getTimestamp(columnIndex);
            if (typed != null) {
                return normalizeTimestamp(typed);
            }
            rawFallback = rs.getString(columnIndex);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read timestamp cell value.", e);
        }

        String raw = sanitizeCellText(rawFallback);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        Timestamp parsed = parseTimestamp(raw);
        return normalizeTimestamp(parsed);
    }

    private java.sql.Date readValidatedDate(ResultSet rs, int columnIndex) {
        String rawFallback;
        try {
            java.sql.Date typedDate = rs.getDate(columnIndex);
            if (typedDate != null) {
                LocalDate localDate = typedDate.toLocalDate();
                return localDate == null ? null : java.sql.Date.valueOf(localDate);
            }
            rawFallback = rs.getString(columnIndex);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read date cell value.", e);
        }

        String raw = sanitizeCellText(rawFallback);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        java.sql.Date parsedDate = parseDate(raw);
        if (parsedDate == null) {
            return null;
        }
        try {
            LocalDate localDate = parsedDate.toLocalDate();
            return localDate == null ? null : java.sql.Date.valueOf(localDate);
        } catch (DateTimeException | IllegalArgumentException ex) {
            log.log(Level.FINE, "Ignoring invalid date cell", ex);
            return null;
        }
    }

    private Timestamp normalizeTimestamp(Timestamp candidate) {
        if (candidate == null) {
            return null;
        }
        try {
            java.time.Instant instant = candidate.toInstant();
            return instant == null ? null : Timestamp.from(instant);
        } catch (DateTimeException | IllegalArgumentException ex) {
            log.log(Level.FINE, "Ignoring invalid timestamp cell", ex);
            return null;
        }
    }

    private String csvEscape(String s) {
        if (s == null) {
            return "";
        }
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String x = s.replace("\"", "\"\"");
        return needsQuotes ? '"' + x + '"' : x;
    }

    private String quoteIdent(String ident) {
        if (ident == null || !SAFE_SQL_IDENTIFIER.matcher(ident).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + ident.replace("\"", "\"\"") + '"';
    }

    private String toJsonArray(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('"').append(jsonEscape(values.get(i))).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    private String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String sanitizeIdentifier(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!SAFE_SQL_IDENTIFIER.matcher(trimmed).matches()) {
            return null;
        }
        return trimmed;
    }

    private String sanitizeCellText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace('\u0000', ' ').replace("\r", "");
        return normalized.length() > MAX_CELL_TEXT_LENGTH
                ? normalized.substring(0, MAX_CELL_TEXT_LENGTH)
                : normalized;
    }

    private byte[] sanitizeBinary(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        if (bytes.length <= MAX_BINARY_BYTES) {
            return bytes;
        }
        return Arrays.copyOf(bytes, MAX_BINARY_BYTES);
    }

    private Timestamp parseTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim();
        try {
            return Timestamp.from(java.time.Instant.parse(normalized));
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Timestamp parse via Instant failed", ex);
        }

        try {
            return Timestamp.from(java.time.OffsetDateTime.parse(normalized).toInstant());
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Timestamp parse via OffsetDateTime failed", ex);
        }

        try {
            return Timestamp.valueOf(normalized.replace('T', ' '));
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Timestamp parse via Timestamp.valueOf failed", ex);
            return null;
        }
    }

    private java.sql.Date parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim();
        try {
            return java.sql.Date.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Date parse via Date.valueOf failed", ex);
        }

        try {
            return java.sql.Date.valueOf(java.time.OffsetDateTime.parse(normalized).toLocalDate());
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Date parse via OffsetDateTime failed", ex);
        }

        try {
            return java.sql.Date.valueOf(java.time.Instant.parse(normalized).atOffset(java.time.ZoneOffset.UTC).toLocalDate());
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Date parse via Instant failed", ex);
            return null;
        }
    }
}
