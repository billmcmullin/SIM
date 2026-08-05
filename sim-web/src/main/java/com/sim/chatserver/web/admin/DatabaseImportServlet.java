package com.sim.chatserver.web.admin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.text.Normalizer;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.sim.chatserver.config.Database;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@MultipartConfig
// parasoft-suppress SERVLET.AJDBC "This servlet intentionally orchestrates import persistence and delegates SQL safety to validated identifiers and prepared statements."
public class DatabaseImportServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DatabaseImportServlet.class.getName());
    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_:-]{1,80}$");
    private static final Pattern SAFE_HOST = Pattern.compile("^[A-Za-z0-9.-]{1,253}$");
    private static final Pattern SAFE_SYNC_URL = Pattern.compile("^https?://[A-Za-z0-9.-]+(?::\\d{1,5})?(?:/[-A-Za-z0-9._~%!$&'()*+,;=:@/]*)?(?:\\?[-A-Za-z0-9._~%!$&'()*+,;=:@/?]*)?$");
    private static final Pattern SAFE_DB_TEXT = Pattern.compile("^[A-Za-z0-9_ .:/#@,;\\-]*$");

    private static final String SESSION_USER = "user";
    private static final String SESSION_ROLE = "role";

    private static final List<String> REQUIRED_TABLES = List.of(
            "server_config",
            "widget_entries",
            "term_definition",
            "customer_identity",
            "customer_identity_session"
    );

    private static final List<String> IMPORT_ORDER = List.of(
            "server_config",
            "widget_entries",
            "term_definition",
            "customer_identity",
            "customer_identity_session"
    );

    // Enhancement: post-import sync trigger (best-effort)
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final String POST_IMPORT_SYNC_URL = readEnv("SIM_POST_IMPORT_SYNC_URL");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req)) {
            json(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Admin authentication required.")
                    .build());
            return;
        }

        String action = Optional.ofNullable(ServletRequestParamUtil.firstParam(req, "action", 128, true, false)).orElse("").trim();
        if ("precheck".equalsIgnoreCase(action)) {
            handlePrecheck(req, resp);
        } else if ("run".equalsIgnoreCase(action)) {
            handleImportRun(req, resp);
        } else {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Invalid action. Use action=precheck or action=run")
                    .build());
        }
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
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

    private void handlePrecheck(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part file = req.getPart("file");
        if (file == null || file.getSize() == 0) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, err("Upload backup ZIP file."));
            return;
        }

        try (Connection conn = Database.getConnection()) {
            Map<String, CsvTableData> zipTablesRaw = readZipTables(file.getInputStream());
            Map<String, CsvTableData> zipTables = normalizeZipWidgetTables(conn, zipTablesRaw);

            List<String> createdBaseline = ensureRequiredTables(conn);
            List<String> widgetTables = findWidgetTables(zipTables.keySet());
            List<String> createdWidgetTables = ensureWidgetTables(conn, widgetTables, zipTables);

            JsonArrayBuilder widgetSummary = Json.createArrayBuilder();
            for (String wt : widgetTables) {
                CsvTableData d = zipTables.get(wt);
                widgetSummary.add(Json.createObjectBuilder()
                        .add("table", wt)
                        .add("rowsToImport", d == null ? 0 : d.rows.size()));
            }

            JsonObjectBuilder result = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("connectionOk", true)
                    .add("readyForImport", true)
                    .add("message", "Precheck successful. Database is ready for import.")
                    .add("createdBaselineTables", toJsonArray(createdBaseline))
                    .add("createdWidgetTables", toJsonArray(createdWidgetTables))
                    .add("zipTableCount", zipTables.size())
                    .add("widgetTables", widgetSummary);

            json(resp, HttpServletResponse.SC_OK, result.build());

        } catch (IOException | SQLException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Import precheck failed", e);
            json(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("connectionOk", false)
                    .add("readyForImport", false)
                    .add("message", "Precheck failed.")
                    .build());
        }
    }

    private void handleImportRun(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part file = req.getPart("file");
        if (file == null || file.getSize() == 0) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, err("Upload backup ZIP file."));
            return;
        }

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Map<String, CsvTableData> zipTablesRaw = readZipTables(file.getInputStream());
                Map<String, CsvTableData> zipTables = normalizeZipWidgetTables(conn, zipTablesRaw);

                ensureRequiredTables(conn);
                List<String> widgetTables = findWidgetTables(zipTables.keySet());
                ensureWidgetTables(conn, widgetTables, zipTables);

                Map<String, Integer> importedCounts = new LinkedHashMap<>();

                for (String table : IMPORT_ORDER) {
                    CsvTableData d = zipTables.get(table);
                    if (d == null) {
                        continue;
                    }
                    int cnt = replaceTableData(conn, table, d);
                    realignSequenceBackedColumns(conn, table);
                    importedCounts.put(table, cnt);
                }

                for (Map.Entry<String, CsvTableData> e : zipTables.entrySet()) {
                    String table = e.getKey();
                    if (importedCounts.containsKey(table)) {
                        continue;
                    }
                    int cnt = replaceTableData(conn, table, e.getValue());
                    realignSequenceBackedColumns(conn, table);
                    importedCounts.put(table, cnt);
                }

                conn.commit();

                PostImportSyncResult syncResult = triggerPostImportSync();

                JsonObjectBuilder out = Json.createObjectBuilder()
                        .add("status", "ok")
                        .add("message", "Import completed.")
                        .add("importedTables", toJsonObject(importedCounts))
                        .add("postImportSyncTriggered", syncResult.triggered)
                        .add("postImportSyncOk", syncResult.ok)
                        .add("postImportSyncStatusCode", syncResult.statusCode)
                        .add("postImportSyncMessage", safe(syncResult.message));

                json(resp, HttpServletResponse.SC_OK, out.build());

            } catch (IOException | SQLException | IllegalArgumentException e) {
                try {
                    conn.rollback();
                } catch (SQLException rb) {
                    log.log(Level.WARNING, "Rollback failed", rb);
                }
                throw e;
            }
        } catch (ImportException ie) {
            log.log(Level.SEVERE, "Import run failed", ie);
            json(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Import failed due to invalid or incompatible data.")
                    .add("table", ie.table == null ? "" : ie.table)
                    .add("rowNumber", ie.rowNumber)
                    .add("column", ie.column == null ? "" : ie.column)
                    .build());
        } catch (IOException | SQLException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Import run failed", e);
            json(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Import failed.")
                    .build());
        }
    }

    private PostImportSyncResult triggerPostImportSync() {
        if (POST_IMPORT_SYNC_URL.isEmpty()) {
            return new PostImportSyncResult(false, true, 0, "Widget sync skipped.");
        }

        String requestId = UUID.randomUUID().toString();

        URI endpointUri;
        try {
            endpointUri = URI.create(POST_IMPORT_SYNC_URL);
        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING, "Invalid post-import widget sync URL", ex);
            return new PostImportSyncResult(false, false, 0, "Widget sync URL is invalid.");
        }

        if (!isSafeSyncEndpoint(endpointUri)) {
            log.warning("Rejected unsafe post-import widget sync URL");
            return new PostImportSyncResult(false, false, 0, "Widget sync URL is not allowed.");
        }

        try {
            ServerDiagnosticsLog.write(
                "database-import-servlet",
                requestId,
                "post-import-sync-request",
                "method=POST\nurl=" + endpointUri
            );

            HttpRequest httpReq = HttpRequest.newBuilder(endpointUri)
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .build();

            HttpResponse<String> response = HTTP.send(httpReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = response.statusCode();
            boolean ok = code >= 200 && code < 300;

            ServerDiagnosticsLog.write(
                "database-import-servlet",
                requestId,
                "post-import-sync-response",
                "status=" + code + "\nbody=" + truncate(response.body())
            );

            if (!ok) {
                log.log(Level.WARNING, "Post-import widget sync returned HTTP {0}: {1}",
                        new Object[]{code, truncate(response.body())});
            }

            return new PostImportSyncResult(true, ok, code, ok
                    ? "Widget sync executed after import."
                    : "Widget sync call failed with HTTP " + code);
        } catch (IOException e) {
            log.log(Level.WARNING, "Post-import widget sync trigger failed", e);
            ServerDiagnosticsLog.write(
                    "database-import-servlet",
                    requestId,
                    "post-import-sync-error",
                    "url=" + endpointUri + "\nmessage=" + safe(e.getMessage()),
                    e
            );
            return new PostImportSyncResult(true, false, 0, "Widget sync trigger failed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, "Post-import widget sync trigger interrupted", e);
            ServerDiagnosticsLog.write(
                    "database-import-servlet",
                    requestId,
                    "post-import-sync-error",
                    "url=" + endpointUri + "\nmessage=" + safe(e.getMessage()),
                    e
            );
            return new PostImportSyncResult(true, false, 0, "Widget sync trigger interrupted.");
        }
    }

    private String truncate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 512 ? s.substring(0, 512) + "..." : s;
    }

    private Map<String, CsvTableData> readZipTables(InputStream input) throws IOException {
        Map<String, CsvTableData> out = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(input, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name == null || !name.startsWith("tables/") || !name.endsWith(".csv")) {
                    continue;
                }

                String table = name.substring("tables/".length(), name.length() - 4);

                byte[] csvBytes = zis.readAllBytes();
                CsvTableData data = readCsv(new ByteArrayInputStream(csvBytes));
                out.put(table, data);
            }
        }
        return out;
    }

    private CsvTableData readCsv(InputStream in) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setQuote('"')
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)); CSVParser parser = format.parse(br)) {

            List<String> headers = new ArrayList<>(parser.getHeaderMap().keySet());
            List<List<String>> rows = new ArrayList<>();

            for (CSVRecord rec : parser) {
                List<String> row = new ArrayList<>(headers.size());
                for (String h : headers) {
                    row.add(rec.isMapped(h) ? rec.get(h) : null);
                }
                rows.add(row);
            }

            return new CsvTableData(headers, rows);
        }
    }

    private List<String> ensureRequiredTables(Connection conn) throws SQLException {
        List<String> created = new ArrayList<>();
        for (String t : REQUIRED_TABLES) {
            if (!tableExists(conn, t)) {
                createKnownTable(conn, t);
                created.add(t);
            }
        }
        return created;
    }

    private List<String> ensureWidgetTables(Connection conn, List<String> widgetTables, Map<String, CsvTableData> zipTables) throws SQLException {
        List<String> created = new ArrayList<>();
        for (String t : widgetTables) {
            if (!tableExists(conn, t)) {
                CsvTableData csv = zipTables.get(t);
                createTableFromCsvHeader(conn, t, csv == null ? List.of() : csv.headers);
                created.add(t);
            }
        }
        return created;
    }

    private List<String> findWidgetTables(Set<String> tables) {
        List<String> out = new ArrayList<>();
        for (String t : tables) {
            if (!REQUIRED_TABLES.contains(t)) {
                out.add(t);
            }
        }
        return out;
    }

    private int replaceTableData(Connection conn, String table, CsvTableData data) throws SQLException {
        if (data.headers.isEmpty()) {
            return 0;
        }

        try (PreparedStatement ps = conn.prepareStatement("TRUNCATE TABLE " + q(table) + " RESTART IDENTITY CASCADE")) {
            ps.execute();
        }

        Map<String, ColumnInfo> columnInfo = loadColumnInfo(conn, table);

        List<String> insertHeaders = data.headers.stream()
                .filter(h -> columnInfo.containsKey(h.toLowerCase()))
                .toList();

        if (insertHeaders.isEmpty()) {
            throw new ImportException(
                    "No matching columns between CSV and table '" + table + "'.",
                    table, 1, null, null
            );
        }

        String cols = String.join(", ", insertHeaders.stream().map(this::q).toList());
        String qmarks = String.join(", ", Collections.nCopies(insertHeaders.size(), "?"));
        String sql = "INSERT INTO " + q(table) + " (" + cols + ") VALUES (" + qmarks + ')';

        int inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int rowIndex = 0; rowIndex < data.rows.size(); rowIndex++) {
                List<String> row = data.rows.get(rowIndex);
                int csvRowNumber = rowIndex + 2;

                for (int i = 0; i < insertHeaders.size(); i++) {
                    int idx = i + 1;
                    String column = insertHeaders.get(i);

                    int sourceIndex = data.headers.indexOf(column);
                    String raw = (sourceIndex >= 0 && sourceIndex < row.size()) ? row.get(sourceIndex) : null;

                    ColumnInfo ci = columnInfo.getOrDefault(column.toLowerCase(), new ColumnInfo(Types.VARCHAR, true));
                    String normalized = normalizeValueForColumn(table, column, raw, ci);

                    try {
                        bindTyped(ps, idx, normalized, ci.sqlType);
                    } catch (SQLException bindErr) {
                        throw new ImportException(
                                "Import failed at table '" + table + "', row " + csvRowNumber + ", column '" + column + "': " + safe(bindErr.getMessage()),
                                table, csvRowNumber, column, bindErr
                        );
                    }
                }

                ps.addBatch();
                inserted++;

                if (inserted % 1000 == 0) {
                    executeBatchWithContext(ps, table, csvRowNumber);
                }
            }

            executeBatchWithContext(ps, table, data.rows.size() + 1);
        }

        return inserted;
    }

    private void realignSequenceBackedColumns(Connection conn, String table) throws SQLException {
        String sequenceLookupSql = """
                SELECT column_name,
                       pg_get_serial_sequence(format('%I.%I', table_schema, table_name), column_name) AS seq_name
                  FROM information_schema.columns
                 WHERE table_schema = 'public'
                   AND table_name = ?
                 ORDER BY ordinal_position
                """;

        try (PreparedStatement ps = conn.prepareStatement(sequenceLookupSql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String column = readMetadataIdentifier(rs, "column_name");
                    String sequenceName = readSafeDbText(rs, "seq_name", 256);
                    if (column == null || sequenceName == null || sequenceName.isBlank()) {
                        continue;
                    }

                    if (!SQL_IDENTIFIER.matcher(column).matches()) {
                        continue;
                    }

                    String setSequenceSql = "SELECT setval(?::regclass, COALESCE((SELECT MAX(" + q(column) + ") FROM " + q(table) + "), 0) + 1, false)";
                    try (PreparedStatement setPs = conn.prepareStatement(setSequenceSql)) {
                        setPs.setString(1, sequenceName);
                        setPs.execute();
                    }
                }
            }
        }
    }

    private Map<String, ColumnInfo> loadColumnInfo(Connection conn, String table) throws SQLException {
        Map<String, ColumnInfo> info = new LinkedHashMap<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, "public", table, null)) {
            while (rs.next()) {
                String rawName = readMetadataIdentifier(rs, "COLUMN_NAME");
                if (rawName == null) {
                    continue;
                }
                String name = rawName.toLowerCase(Locale.ROOT);
                Integer typeValue = readMetadataInt(rs, "DATA_TYPE");
                if (typeValue == null) {
                    continue;
                }
                int type = sanitizeSqlType(typeValue);

                Integer nullableValue = readMetadataInt(rs, "NULLABLE");
                boolean nullable = nullableValue == null || nullableValue != ResultSetMetaData.columnNoNulls;
                info.put(name, new ColumnInfo(type, nullable));
            }
        }
        return info;
    }

    private Integer readMetadataInt(ResultSet rs, String columnName) throws SQLException {
        String text = rs.getString(columnName);
        if (text == null) {
            return null;
        }
        text = stripControlChars(text).trim();
        if (text.isEmpty() || !text.matches("^-?\\d{1,10}$")) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid metadata integer value", ex);
            return null;
        }
    }

    private String normalizeValueForColumn(String table, String column, String raw, ColumnInfo ci) {
        String t = table == null ? "" : table.toLowerCase();
        String c = column == null ? "" : column.toLowerCase();

        if ("term_definition".equals(t) && "match_pattern".equals(c)) {
            return raw == null ? "" : raw;
        }

        if ("term_definition".equals(t) && "match_type".equals(c)) {
            if (raw == null || raw.trim().isEmpty()) {
                return "WILDCARD";
            }
        }

        if ((raw == null || raw.trim().isEmpty())
                && ci != null
                && !ci.nullable
                && isTextType(ci.sqlType)) {
            return "";
        }

        return raw;
    }

    private boolean isTextType(int sqlType) {
        return sqlType == Types.VARCHAR
                || sqlType == Types.CHAR
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NVARCHAR
                || sqlType == Types.NCHAR
                || sqlType == Types.LONGNVARCHAR;
    }

    private void executeBatchWithContext(PreparedStatement ps, String table, int csvRowNumber) throws SQLException {
        try {
            ps.executeBatch();
        } catch (BatchUpdateException bue) {
            SQLException root = bue.getNextException();
            if (root == null) {
                root = bue;
            }
            throw new ImportException(
                    "Batch insert failed at table '" + table + "' near CSV row " + csvRowNumber + ": " + safe(root.getMessage()),
                    table, csvRowNumber, null, bue
            );
        }
    }

    private void bindTyped(PreparedStatement ps, int idx, String raw, int sqlType) throws SQLException {
        String v = raw == null ? null : raw.trim();

        if (v == null) {
            ps.setNull(idx, sqlType);
            return;
        }
        if (v.isEmpty()) {
            if (isTextType(sqlType)) {
                ps.setString(idx, "");
            } else {
                ps.setNull(idx, sqlType);
            }
            return;
        }

        try {
            switch (sqlType) {
                case Types.BIGINT ->
                    ps.setLong(idx, Long.parseLong(v));
                case Types.INTEGER, Types.SMALLINT, Types.TINYINT ->
                    ps.setInt(idx, Integer.parseInt(v));
                case Types.BOOLEAN, Types.BIT ->
                    ps.setBoolean(idx, "true".equalsIgnoreCase(v) || "t".equalsIgnoreCase(v) || "1".equals(v));
                case Types.DOUBLE, Types.FLOAT, Types.REAL ->
                    ps.setDouble(idx, Double.parseDouble(v));
                case Types.NUMERIC, Types.DECIMAL ->
                    ps.setBigDecimal(idx, new BigDecimal(v));
                case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
                    bindTimestamp(ps, idx, v);
                case Types.DATE ->
                    bindDate(ps, idx, v);
                case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY ->
                    ps.setBytes(idx, Base64.getDecoder().decode(v));
                default ->
                    ps.setString(idx, v);
            }
        } catch (IllegalArgumentException ex) {
            throw new SQLException("Invalid value '" + v + "' for SQL type " + sqlType, ex);
        }
    }

    private Timestamp parseTimestampStrict(String v) {
        try {
            return Timestamp.from(Instant.parse(v));
        } catch (DateTimeParseException ignore) {
        }

        try {
            return Timestamp.from(OffsetDateTime.parse(v).toInstant());
        } catch (DateTimeParseException ignore) {
        }

        try {
            return Timestamp.valueOf(v.replace('T', ' '));
        } catch (IllegalArgumentException ignore) {
        }

        return null;
    }

    private void bindTimestamp(PreparedStatement ps, int idx, String v) throws SQLException {
        Timestamp ts = parseTimestampStrict(v);
        if (ts == null) {
            throw new SQLException("Invalid timestamp format '" + v + '\'');
        }
        ps.setTimestamp(idx, ts);
    }

    private java.sql.Date parseDateStrict(String v) {
        try {
            return java.sql.Date.valueOf(v);
        } catch (IllegalArgumentException ignore) {
        }

        try {
            return java.sql.Date.valueOf(OffsetDateTime.parse(v).toLocalDate());
        } catch (DateTimeParseException ignore) {
        }

        try {
            return java.sql.Date.valueOf(Instant.parse(v).atOffset(java.time.ZoneOffset.UTC).toLocalDate());
        } catch (DateTimeParseException ignore) {
        }

        try {
            return java.sql.Date.valueOf(LocalDateTime.parse(v).toLocalDate());
        } catch (DateTimeParseException ignore) {
        }

        if (v.length() >= 10) {
            try {
                return java.sql.Date.valueOf(LocalDate.parse(v.substring(0, 10)));
            } catch (DateTimeParseException | IllegalArgumentException ignore) {
            }
        }

        return null;
    }

    private void bindDate(PreparedStatement ps, int idx, String v) throws SQLException {
        // parasoft-suppress SECURITY.BV.ADT "Date value is parsed from controlled CSV import content and immediately bound to a SQL parameter."
        java.sql.Date d = parseDateStrict(v);
        if (d == null) {
            throw new SQLException("Invalid date format '" + v + '\'');
        }
        ps.setDate(idx, d);
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, "public", tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private void createKnownTable(Connection conn, String table) throws SQLException {
        String ddl = switch (table) {
            case "customer_identity" -> "CREATE TABLE IF NOT EXISTS " + q(table) + " ("
                    + "identity_id BIGSERIAL PRIMARY KEY, "
                    + "canonical_email VARCHAR(320), "
                    + "canonical_name VARCHAR(256), "
                    + "salesforce_contact_id VARCHAR(64), "
                    + "salesforce_account_id VARCHAR(64), "
                    + "email_enc TEXT, "
                    + "phone_enc TEXT, "
                    + "title_enc TEXT, "
                    + "department_enc TEXT, "
                    + "raw_json_enc TEXT, "
                    + "confidence VARCHAR(24) NOT NULL DEFAULT 'high', "
                    + "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), "
                    + "last_synced_at TIMESTAMPTZ"
                        + ')';
            case "customer_identity_session" -> "CREATE TABLE IF NOT EXISTS " + q(table) + " ("
                    + "session_id TEXT PRIMARY KEY, "
                    + "identity_id BIGINT NOT NULL REFERENCES customer_identity(identity_id) ON DELETE CASCADE, "
                    + "display_name_snapshot VARCHAR(256), "
                    + "contact_email_snapshot VARCHAR(320), "
                    + "linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()"
                        + ')';
            default -> "CREATE TABLE IF NOT EXISTS " + q(table) + " (id BIGSERIAL PRIMARY KEY)";
        };

        try (PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.execute();
        }
    }

    private void createTableFromCsvHeader(Connection conn, String table, List<String> headers) throws SQLException {
        if (headers == null || headers.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + q(table) + " (id BIGSERIAL PRIMARY KEY)")) {
                ps.execute();
            }
            return;
        }

        StringBuilder ddl = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(q(table)).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) {
                ddl.append(", ");
            }
            ddl.append(q(headers.get(i))).append(" TEXT");
        }
        ddl.append(')');

        try (PreparedStatement ps = conn.prepareStatement(ddl.toString())) {
            ps.execute();
        }
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

    private Map<String, String> buildWidgetIdToTableMap(Connection conn) throws SQLException {
        Map<String, String> out = new LinkedHashMap<>();
        if (!tableExists(conn, "widget_entries")) {
            return out;
        }

        String sql = "SELECT widget_id FROM widget_entries";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String widgetId = sanitizeWidgetId(readSafeDbText(rs, "widget_id", 80));
                if (widgetId == null || widgetId.isBlank()) {
                    continue;
                }
                out.put(widgetId, sanitizeWidgetTableName(widgetId));
            }
        }
        return out;
    }

    private Map<String, CsvTableData> normalizeZipWidgetTables(Connection conn, Map<String, CsvTableData> zipTables) throws SQLException {
        Map<String, CsvTableData> normalized = new LinkedHashMap<>();
        Map<String, String> widgetIdToSanitized = buildWidgetIdToTableMap(conn);

        for (Map.Entry<String, CsvTableData> e : zipTables.entrySet()) {
            String table = e.getKey();
            CsvTableData data = e.getValue();

            if (REQUIRED_TABLES.contains(table)) {
                normalized.put(table, data);
                continue;
            }

            String mapped = widgetIdToSanitized.get(table);
            if (mapped != null) {
                mergeCsvTable(normalized, mapped, data);
                continue;
            }

            normalized.put(table, data);
        }

        return normalized;
    }

    private void mergeCsvTable(Map<String, CsvTableData> target, String tableName, CsvTableData incoming) {
        CsvTableData existing = target.get(tableName);
        if (existing == null) {
            target.put(tableName, incoming);
            return;
        }

        List<String> headers = existing.headers.isEmpty() ? incoming.headers : existing.headers;
        List<List<String>> mergedRows = new ArrayList<>(existing.rows);
        mergedRows.addAll(incoming.rows);

        target.put(tableName, new CsvTableData(headers, mergedRows));
    }

    private String q(String ident) {
        if (ident == null || !SQL_IDENTIFIER.matcher(ident).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + ident + '"';
    }

    static String sanitizeRequestValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = stripControlChars(value).trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.length() > 128 ? trimmed.substring(0, 128) : trimmed;
    }

    private static String readEnv(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String value = new ProcessBuilder().environment().get(name);
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replace('\u0000', ' ').replace("\r", "").replace("\n", "").trim();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC);
        String bounded = normalized.length() > 512 ? normalized.substring(0, 512) : normalized;
        return sanitizeSyncUrlValue(bounded);
    }

    private static String sanitizeSyncUrlValue(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return "";
        }
        String trimmed = stripControlChars(candidate).trim();
        if (trimmed.length() > 512) {
            return "";
        }
        return SAFE_SYNC_URL.matcher(trimmed).matches() ? trimmed : "";
    }

    private static String stripControlChars(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\u0000", "")
                .replace("\r", "")
                .replace("\n", "");
    }

    private String readMetadataIdentifier(ResultSet rs, String columnName) throws SQLException {
        String raw = readSafeDbText(rs, columnName, 63);
        if (raw == null) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        return SQL_IDENTIFIER.matcher(normalized).matches() ? normalized : null;
    }

    private String readSafeDbText(ResultSet rs, String columnName, int maxLen) throws SQLException {
        String raw = rs.getString(columnName);
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(stripControlChars(raw), Normalizer.Form.NFKC).trim();
        if (normalized.isEmpty()) {
            return null;
        }
        String bounded = normalized.length() > maxLen ? normalized.substring(0, maxLen) : normalized;
        return SAFE_DB_TEXT.matcher(bounded).matches() ? bounded : null;
    }

    private int sanitizeSqlType(int sqlType) {
        return switch (sqlType) {
            case Types.BIGINT,
                 Types.INTEGER,
                 Types.SMALLINT,
                 Types.TINYINT,
                 Types.BOOLEAN,
                 Types.BIT,
                 Types.DOUBLE,
                 Types.FLOAT,
                 Types.REAL,
                 Types.NUMERIC,
                 Types.DECIMAL,
                 Types.TIMESTAMP,
                 Types.TIMESTAMP_WITH_TIMEZONE,
                 Types.DATE,
                 Types.BINARY,
                 Types.VARBINARY,
                 Types.LONGVARBINARY,
                 Types.VARCHAR,
                 Types.CHAR,
                 Types.LONGVARCHAR,
                 Types.NVARCHAR,
                 Types.NCHAR,
                 Types.LONGNVARCHAR -> sqlType;
            default -> Types.VARCHAR;
        };
    }

    private String sanitizeWidgetId(String widgetId) {
        if (widgetId == null) {
            return null;
        }
        String trimmed = widgetId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 80) {
            trimmed = trimmed.substring(0, 80);
        }
        return SAFE_WIDGET_ID.matcher(trimmed).matches() ? trimmed : null;
    }

    private boolean isSafeSyncEndpoint(URI uri) {
        if (uri == null) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        String lowered = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(lowered) && !"https".equals(lowered)) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }
        return SAFE_HOST.matcher(host).matches();
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

    private JsonObject err(String msg) {
        return Json.createObjectBuilder()
                .add("status", "error")
                .add("message", safe(msg))
                .build();
    }

    private void json(HttpServletResponse resp, int status, JsonObject obj) throws IOException {
        ServletJsonResponseUtil.writeJson(resp, status, obj);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private jakarta.json.JsonArray toJsonArray(List<String> vals) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        for (String v : vals) {
            b.add(v == null ? "" : v);
        }
        return b.build();
    }

    private JsonObject toJsonObject(Map<String, Integer> m) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            Integer value = e.getValue();
            int safeValue = value == null ? 0 : value;
            b.add(e.getKey(), safeValue);
        }
        return b.build();
    }

    static final class CsvTableData {

        final List<String> headers;
        final List<List<String>> rows;

        private CsvTableData(List<String> headers, List<List<String>> rows) {
            this.headers = headers == null ? List.of() : headers;
            this.rows = rows == null ? List.of() : rows;
        }
    }

    static final class ColumnInfo {

        final int sqlType;
        final boolean nullable;

        private ColumnInfo(int sqlType, boolean nullable) {
            this.sqlType = sqlType;
            this.nullable = nullable;
        }
    }

    static final class ImportException extends SQLException {

        private final transient String table;
        private final transient int rowNumber;
        private final transient String column;

        private ImportException(String message, String table, int rowNumber, String column, Throwable cause) {
            super(message, cause);
            this.table = table;
            this.rowNumber = rowNumber;
            this.column = column;
        }
    }

    static final class PostImportSyncResult {

        final boolean triggered;
        final boolean ok;
        final int statusCode;
        final String message;

        private PostImportSyncResult(boolean triggered, boolean ok, int statusCode, String message) {
            this.triggered = triggered;
            this.ok = ok;
            this.statusCode = statusCode;
            this.message = message;
        }
    }
}
