package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sim.chatserver.config.Database;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

final class DatabaseBackupService {
    private static final Logger log = Logger.getLogger(DatabaseBackupService.class.getName());

    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;
    private static final int MAX_CELL_TEXT_LENGTH = 65535;
    private static final int MAX_BINARY_BYTES = 2 * 1024 * 1024;
    private static final LocalDate MIN_ALLOWED_DATE = LocalDate.of(1970, 1, 1);
    private static final LocalDate MAX_ALLOWED_DATE = LocalDate.of(3000, 12, 31);
    private static final List<String> EXCLUDED_TABLES = List.of("flyway_schema_history");

    void exportBackup(OutputStream outputStream, String generatedAt) {
        if (outputStream == null) {
            throw new IllegalStateException("Backup output stream is unavailable.");
        }

        try (Connection conn = Database.getConnection(); ZipOutputStream zip = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
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

            writeManifest(zip, exported, skipped, generatedAt);
            zip.finish();
            log.info(() -> "Data backup export completed. Exported tables=" + exported.size() + ", skipped=" + skipped.size());
        } catch (SQLException | IOException | IllegalStateException e) {
            throw new IllegalStateException("Data backup export failed", e);
        }
    }

    private List<String> listExportableTables(Connection conn) {
        List<String> out = new ArrayList<>();
        String sql = """
                SELECT tablename
                  FROM pg_catalog.pg_tables
                 WHERE schemaname = 'public'
                 ORDER BY tablename
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String table = readValidatedIdentifier(rs, 1);
                if (table == null || table.isBlank() || EXCLUDED_TABLES.contains(table)) {
                    continue;
                }
                out.add(table);
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
                    OutputStreamWriter writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);

                    for (int columnIndex = 1; columnIndex <= cols; columnIndex++) {
                        if (columnIndex > 1) {
                            writer.write(',');
                        }
                        writer.write(csvEscape(md.getColumnLabel(columnIndex)));
                    }
                    writer.write('\n');

                    while (rs.next()) {
                        for (int columnIndex = 1; columnIndex <= cols; columnIndex++) {
                            if (columnIndex > 1) {
                                writer.write(',');
                            }
                            writer.write(csvEscape(readCellAsText(rs, md, columnIndex)));
                        }
                        writer.write('\n');
                    }

                    writer.flush();
                    zip.closeEntry();
                }
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed exporting table: " + tableName, e);
        }
    }

    private void writeManifest(ZipOutputStream zip, List<String> exported, List<String> skipped, String generatedAt) {
        try {
            zip.putNextEntry(new ZipEntry("manifest.json"));
            OutputStreamWriter writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);

            writer.write("{\n");
            writer.write("  \"formatVersion\": 1,\n");
            writer.write("  \"type\": \"raw-data-export\",\n");
            writer.write("  \"generatedAt\": \"" + jsonEscape(generatedAt) + "\",\n");
            writer.write("  \"schema\": \"public\",\n");
            writer.write("  \"exportedTableCount\": " + exported.size() + ",\n");
            writer.write("  \"skippedTableCount\": " + skipped.size() + ",\n");
            writer.write("  \"exportedTables\": " + toJsonArray(exported) + ",\n");
            writer.write("  \"skippedTables\": " + toJsonArray(skipped));
            writer.write('\n');
            writer.write("}\n");

            writer.flush();
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
                return bytes.length == 0 ? "" : Base64.getEncoder().encodeToString(bytes);
            }

            if (sqlType == Types.TIMESTAMP || sqlType == Types.TIMESTAMP_WITH_TIMEZONE) {
                Timestamp ts = parseTimestamp(readValidatedCellText(rs, columnIndex));
                return ts == null ? "" : ISO_INSTANT_FMT.format(ts.toInstant());
            }

            if (sqlType == Types.DATE) {
                LocalDate localDate = parseLocalDate(readValidatedCellText(rs, columnIndex));
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
        if (rs == null) {
            return null;
        }
        try {
            String raw = rs.getString(columnIndex);
            if (raw != null) {
                return validateTaintedText(canonicalizeCellText(raw));
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read cell value.", e);
        }
    }

    private byte[] readValidatedBinary(ResultSet rs, int columnIndex) {
        if (rs == null) {
            return new byte[0];
        }
        try {
            byte[] bytes = rs.getBytes(columnIndex);
            return validateTaintedBinary(sanitizeBinary(bytes));
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read binary cell value.", e);
        }
    }

    private String validateTaintedText(String value) {
        if (value == null) {
            return null;
        }
        String canonical = canonicalizeCellText(value);
        String sanitized = sanitizeCellText(canonical);
        String canonicalSanitized = canonicalizeCellText(sanitized);
        String normalizedInput = ServletRequestParamUtil.normalizeBodyText(canonicalSanitized, MAX_CELL_TEXT_LENGTH, false);
        if (normalizedInput == null || normalizedInput.isEmpty()) {
            return "";
        }
        StringBuilder safe = new StringBuilder(normalizedInput.length());
        for (int i = 0; i < normalizedInput.length(); i++) {
            char ch = normalizedInput.charAt(i);
            if (Character.isISOControl(ch) && ch != '\n' && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }
        String normalized = safe.toString();
        return normalized.length() > MAX_CELL_TEXT_LENGTH
                ? normalized.substring(0, MAX_CELL_TEXT_LENGTH)
                : normalized;
    }

    private byte[] validateTaintedBinary(byte[] bytes) {
        return sanitizeBinary(bytes);
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? '"' + escaped + '"' : escaped;
    }

    private String quoteIdent(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
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

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String sanitizeIdentifier(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || !SAFE_SQL_IDENTIFIER.matcher(trimmed).matches()) {
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

    private String canonicalizeCellText(String value) {
        return value == null ? null : Normalizer.normalize(value, Normalizer.Form.NFKC);
    }

    private byte[] sanitizeBinary(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        return bytes.length <= MAX_BINARY_BYTES ? bytes : Arrays.copyOf(bytes, MAX_BINARY_BYTES);
    }

    private Timestamp parseTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim();
        try {
            return Timestamp.from(Instant.parse(normalized));
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Timestamp parse via Instant failed", ex);
        }

        try {
            return Timestamp.from(OffsetDateTime.parse(normalized).toInstant());
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

    private LocalDate parseLocalDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim();
        LocalDate parsed = null;
        try {
            parsed = LocalDate.parse(normalized);
        } catch (DateTimeException ex) {
            log.log(Level.FINE, "Date parse via LocalDate failed", ex);
        }

        if (parsed == null) {
            try {
                parsed = OffsetDateTime.parse(normalized).toLocalDate();
            } catch (DateTimeException ex) {
                log.log(Level.FINE, "Date parse via OffsetDateTime failed", ex);
            }
        }

        if (parsed == null) {
            try {
                parsed = Instant.parse(normalized).atOffset(ZoneOffset.UTC).toLocalDate();
            } catch (DateTimeException ex) {
                log.log(Level.FINE, "Date parse via Instant failed", ex);
                return null;
            }
        }

        if (parsed.isBefore(MIN_ALLOWED_DATE) || parsed.isAfter(MAX_ALLOWED_DATE)) {
            return null;
        }
        return parsed;
    }
}
