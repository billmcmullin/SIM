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
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

final class DatabaseImportService {

    private static final Logger log = Logger.getLogger(DatabaseImportServlet.class.getCanonicalName());
    private static final String LS = System.lineSeparator();
    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,62}");
    private static final Pattern SAFE_WIDGET_ID = Pattern.compile("^[A-Za-z0-9_:-]{1,80}$");
    private static final Pattern SAFE_HOST = Pattern.compile("^[A-Za-z0-9.-]{1,253}$");
    private static final Pattern SAFE_SYNC_URL = Pattern.compile("^https?://[A-Za-z0-9.-]+(?::\\d{1,5})?(?:/[-A-Za-z0-9._~%!$&'()*+,;=:@/]*)?(?:\\?[-A-Za-z0-9._~%!$&'()*+,;=:@/?]*)?$");
    private static final Pattern SAFE_DB_TEXT = Pattern.compile("^[A-Za-z0-9_ .:/#@,;\\-]*$");

    private static final String SESSION_USER = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String SESSION_ROLE = com.sim.chatserver.util.CommonLiteralConstants.ROLE;
    private static final String ROLE_ADMIN = com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN;
    private static final String JSON_KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String JSON_KEY_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
    private static final String JSON_KEY_ACTION = "action";
    private static final String JSON_KEY_ROWS_TO_IMPORT = "rowsToImport";
    private static final String JSON_KEY_CREATED_BASELINE_TABLES = "createdBaselineTables";
    private static final String JSON_KEY_CREATED_WIDGET_TABLES = "createdWidgetTables";
    private static final String JSON_KEY_ZIP_TABLE_COUNT = "zipTableCount";
    private static final String JSON_KEY_WIDGET_TABLES = "widgetTables";
    private static final String JSON_KEY_POST_IMPORT_SYNC_TRIGGERED = "postImportSyncTriggered";
    private static final String JSON_KEY_POST_IMPORT_SYNC_OK = "postImportSyncOk";
    private static final String JSON_KEY_POST_IMPORT_SYNC_STATUS_CODE = "postImportSyncStatusCode";
    private static final String JSON_KEY_POST_IMPORT_SYNC_MESSAGE = "postImportSyncMessage";
    private static final String JSON_KEY_ROW_NUMBER = "rowNumber";
    private static final String JSON_KEY_COLUMN = "column";
    private static final String JSON_STATUS_OK = "ok";
    private static final String JSON_STATUS_ERROR = com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
    private static final String DIAG_COMPONENT = "database-import-servlet";
    private static final String ACTION_PRECHECK = "precheck";
    private static final String ACTION_RUN = "run";
    private static final String PART_FILE = "file";
    private static final String KEY_CONNECTION_OK = "connectionOk";
    private static final String KEY_READY_FOR_IMPORT = "readyForImport";
    private static final String KEY_IMPORTED_TABLES = "importedTables";
    private static final String KEY_MESSAGE_KV = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_MESSAGE;
    private static final String EVENT_POST_IMPORT_SYNC_ERROR = "post-import-sync-error";
    private static final String TABLES_PATH_PREFIX = "tables/";
    private static final String LOG_IMPORT_RUN_FAILED = "Import run failed";
    private static final String LOG_IMPORT_PRECHECK_FAILED = "Import precheck failed";
    private static final String LOG_UNABLE_READ_UPLOAD_PRECHECK = "Unable to read upload payload for precheck";
    private static final String LOG_UNABLE_READ_UPLOAD_IMPORT = "Unable to read upload payload for import";
    private static final String LOG_ROLLBACK_FAILED = "Rollback failed";
    private static final String LOG_INVALID_POST_IMPORT_SYNC_URL = "Invalid post-import widget sync URL";
    private static final String LOG_REJECTED_UNSAFE_POST_IMPORT_SYNC_URL = "Rejected unsafe post-import widget sync URL";
    private static final String LOG_POST_IMPORT_SYNC_HTTP_ERROR = "Post-import widget sync returned HTTP {0}: {1}";
    private static final String LOG_POST_IMPORT_SYNC_TRIGGER_FAILED = "Post-import widget sync trigger failed";
    private static final String LOG_POST_IMPORT_SYNC_TRIGGER_INTERRUPTED = "Post-import widget sync trigger interrupted";
    private static final String METADATA_TABLE_TYPE = com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE;
    private static final String MSG_UPLOAD_ZIP = "Upload backup ZIP file.";
    private static final String MSG_ADMIN_AUTH_REQUIRED = "Admin authentication required.";
    private static final String MSG_INVALID_ACTION = "Invalid action. Use action=precheck or action=run";
    private static final String MSG_PRECHECK_SUCCESS = "Precheck successful. Database is ready for import.";
    private static final String MSG_PRECHECK_FAILED = "Precheck failed.";
    private static final String MSG_IMPORT_COMPLETED = "Import completed.";
    private static final String MSG_IMPORT_FAILED_INVALID_DATA = "Import failed due to invalid or incompatible data.";
    private static final String MSG_IMPORT_FAILED = "Import failed.";
    private static final String MSG_WIDGET_SYNC_SKIPPED = "Widget sync skipped.";
    private static final String MSG_WIDGET_SYNC_URL_INVALID = "Widget sync URL is invalid.";
    private static final String MSG_WIDGET_SYNC_URL_NOT_ALLOWED = "Widget sync URL is not allowed.";
    private static final String MSG_WIDGET_SYNC_EXECUTED = "Widget sync executed after import.";
    private static final String MSG_WIDGET_SYNC_CALL_FAILED_PREFIX = "Widget sync call failed with HTTP ";
    private static final String MSG_WIDGET_SYNC_TRIGGER_FAILED = "Widget sync trigger failed.";
    private static final String MSG_WIDGET_SYNC_TRIGGER_INTERRUPTED = "Widget sync trigger interrupted.";
    private static final String MSG_UNABLE_TO_OBTAIN_DB_CONNECTION = "Unable to obtain database connection";
    private static final String EVENT_POST_IMPORT_SYNC_REQUEST = "post-import-sync-request";
    private static final String EVENT_POST_IMPORT_SYNC_RESPONSE = "post-import-sync-response";
    private static final String LOG_KV_METHOD_POST = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_METHOD_POST;
    private static final String LOG_KV_STATUS = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_STATUS;
    private static final String LOG_KV_BODY = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_BODY;
    private static final String HEADER_CONTENT_TYPE = com.sim.chatserver.util.CommonLiteralConstants.HEADER_CONTENT_TYPE;
    private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String LOG_KV_URL = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL;
    private static final String JSON_KEY_TABLE = "table";
    private static final String TABLE_SERVER_CONFIG = "server_config";
    private static final String TABLE_WIDGET_ENTRIES = "widget_entries";
    private static final String TABLE_TERM_DEFINITION = "term_definition";
    private static final String TABLE_CUSTOMER_IDENTITY = "customer_identity";
    private static final String TABLE_CUSTOMER_IDENTITY_SESSION = "customer_identity_session";
    private static final String META_COLUMN_NAME_LOWER = "column_name";
    private static final String META_SEQ_NAME = "seq_name";
    private static final String META_COLUMN_NAME_UPPER = "COLUMN_NAME";
    private static final String META_DATA_TYPE = "DATA_TYPE";
    private static final String META_NULLABLE = "NULLABLE";
    private static final String TERM_MATCH_PATTERN = "match_pattern";
    private static final String TERM_MATCH_TYPE = "match_type";
    private static final String MSG_UNABLE_INSPECT_TABLE_METADATA_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;
    private static final String MSG_UNABLE_PREPARE_SQL_STATEMENT = "Unable to prepare SQL statement";
    private static final String SQL_CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
    private static final String SQL_OPEN_PAREN = " (";
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String MSG_TABLE_PREFIX = "table '";
    private static final String TABLE_NAME_SUFFIX_PERIOD = "'.";
    private static final String MSG_ROW_SEPARATOR = "', row ";
    private static final String MSG_COLUMN_SEPARATOR = ", column '";
    private static final String MSG_NEAR_CSV_ROW_SEPARATOR = "' near CSV row ";
    private static final String MSG_COLON_SPACE = ": ";
    private static final String SQL_COMMA_SPACE = ", ";
    private static final String SQL_QMARK = "?";
    private static final String SQL_OPEN_COLUMNS = " (";
    private static final String SQL_INSERT_INTO = "INSERT INTO ";
    private static final String SQL_VALUES_OPEN = ") VALUES (";
    private static final String SQL_DEFAULT_ID_COLUMN = " (id BIGSERIAL PRIMARY KEY)";
    private static final String SQL_TEXT_TYPE = " TEXT";
    private static final String CSV_EXT = ".csv";
    private static final String ELLIPSIS = com.sim.chatserver.util.CommonLiteralConstants.ELLIPSIS;
    private static final String REGEX_INT_10 = "^-?\\d{1,10}$";
    private static final String VALUE_WILDCARD = com.sim.chatserver.util.CommonLiteralConstants.MATCH_TYPE_WILDCARD;
    private static final String VALUE_TRUE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE;
    private static final String VALUE_T_TEXT = "t";
    private static final String VALUE_ONE_TEXT = "1";
    private static final String VALUE_HTTP = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP;
    private static final String VALUE_HTTPS = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS;
    private static final String VALUE_NUL = com.sim.chatserver.util.CommonLiteralConstants.NULL_CHAR_STRING;
    private static final String LOGGER_OWASP = com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP;
    private static final String MSG_HANDLED_EXCEPTION = com.sim.chatserver.util.CommonLiteralConstants.LOG_HANDLED_EXCEPTION;
    private static final String MSG_UPLOAD_FILE_MISSING_PHASE_PREFIX = "Upload file is missing for phase ";
    private static final String MSG_UNABLE_OPEN_UPLOAD_STREAM_PHASE_PREFIX = "Unable to open upload stream for phase ";
    private static final String MSG_UNABLE_READ_ZIP_TABLE_DATA = "Unable to read ZIP table data";
    private static final String MSG_UNABLE_READ_CSV_TABLE_PAYLOAD = "Unable to read CSV table payload";
    private static final String MSG_UNABLE_PARSE_CSV_PAYLOAD = "Unable to parse CSV payload";
    private static final String MSG_NO_MATCHING_COLUMNS_PREFIX = "No matching columns between CSV and ";
    private static final String MSG_IMPORT_FAILED_AT_PREFIX = "Import failed at ";
    private static final String MSG_UNABLE_REPLACE_TABLE_DATA_PREFIX = "Unable to replace table data for ";
    private static final String MSG_UNABLE_REALIGN_SEQUENCE_COLUMNS_PREFIX = "Unable to realign sequence-backed columns for ";
    private static final String MSG_UNABLE_LOAD_COLUMN_METADATA_PREFIX = "Unable to load column metadata for ";
    private static final String MSG_TYPED_METADATA_INT_READ_FAILED_PREFIX = "Typed metadata integer read failed for column ";
    private static final String MSG_INVALID_METADATA_INT_VALUE = "Invalid metadata integer value";
    private static final String MSG_BATCH_INSERT_FAILED_AT_PREFIX = "Batch insert failed at ";
    private static final String MSG_INVALID_VALUE_PREFIX = "Invalid value '";
    private static final String MSG_INVALID_VALUE_SQL_TYPE_INFIX = "' for SQL type ";
    private static final String MSG_INVALID_TIMESTAMP_FORMAT_PREFIX = "Invalid timestamp format '";
    private static final String MSG_UNABLE_BIND_TIMESTAMP = "Unable to bind timestamp value";
    private static final String MSG_INVALID_DATE_FORMAT_PREFIX = "Invalid date format '";
    private static final String MSG_UNABLE_BIND_DATE = "Unable to bind date value";
    private static final String MSG_UNABLE_CREATE_REQUIRED_TABLE_PREFIX = "Unable to create required table ";
    private static final String MSG_UNABLE_CREATE_FALLBACK_TABLE_PREFIX = "Unable to create fallback table ";
    private static final String MSG_UNABLE_CREATE_TABLE_FROM_HEADER_PREFIX = "Unable to create table from CSV header for ";
    private static final String SQL_SETVAL_PREFIX = "SELECT setval(?::regclass, COALESCE((SELECT MAX(";
    private static final String SQL_SETVAL_MIDDLE = ") FROM ";
    private static final String SQL_SETVAL_SUFFIX = "), 0) + 1, false)";
    private static final String SQL_DDL_CUSTOMER_IDENTITY_BODY = "identity_id BIGSERIAL PRIMARY KEY, canonical_email VARCHAR(320), canonical_name VARCHAR(256), salesforce_contact_id VARCHAR(64), salesforce_account_id VARCHAR(64), email_enc TEXT, phone_enc TEXT, title_enc TEXT, department_enc TEXT, raw_json_enc TEXT, confidence VARCHAR(24) NOT NULL DEFAULT 'high', created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), last_synced_at TIMESTAMPTZ";
    private static final String SQL_DDL_CUSTOMER_IDENTITY_SESSION_BODY = "session_id TEXT PRIMARY KEY, identity_id BIGINT NOT NULL REFERENCES customer_identity(identity_id) ON DELETE CASCADE, display_name_snapshot VARCHAR(256), contact_email_snapshot VARCHAR(320), linked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()";
    private static final String SQL_SELECT_WIDGET_ID_FROM = "SELECT widget_id FROM ";
    private static final String DB_COL_WIDGET_ID = "widget_id";
    private static final String MSG_UNABLE_LOAD_WIDGET_ID_MAP = "Unable to load widget ID table map";
    private static final String MSG_UNABLE_WRITE_JSON_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_RESPONSE;
    private static final String MSG_UNABLE_WRITE_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE;
    private static final String MSG_FALLBACK_SEND_ERROR_FAILED = com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED;
    private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
    private static final String MSG_DB_TEXT_READ_FAILED_PREFIX = "DB text read failed for column ";
    static final String MSG_DESERIALIZATION_NOT_ALLOWED = com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED;

    private static final List<String> REQUIRED_TABLES = List.of(
            TABLE_SERVER_CONFIG,
            TABLE_WIDGET_ENTRIES,
            TABLE_TERM_DEFINITION,
            TABLE_CUSTOMER_IDENTITY,
            TABLE_CUSTOMER_IDENTITY_SESSION
    );

    private static final List<String> IMPORT_ORDER = List.of(
            TABLE_SERVER_CONFIG,
            TABLE_WIDGET_ENTRIES,
            TABLE_TERM_DEFINITION,
            TABLE_CUSTOMER_IDENTITY,
            TABLE_CUSTOMER_IDENTITY_SESSION
    );

    // Enhancement: post-import sync trigger (best-effort)
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        private static final String POST_IMPORT_SYNC_URL = readEnv("SIM_POST_IMPORT_SYNC_URL");
        private static final PostImportSyncResult SYNC_RESULT_SKIPPED = new PostImportSyncResult(false, true, 0, MSG_WIDGET_SYNC_SKIPPED);
        private static final PostImportSyncResult SYNC_RESULT_URL_INVALID = new PostImportSyncResult(false, false, 0, MSG_WIDGET_SYNC_URL_INVALID);
        private static final PostImportSyncResult SYNC_RESULT_URL_NOT_ALLOWED = new PostImportSyncResult(false, false, 0, MSG_WIDGET_SYNC_URL_NOT_ALLOWED);
        private static final PostImportSyncResult SYNC_RESULT_TRIGGER_FAILED = new PostImportSyncResult(true, false, 0, MSG_WIDGET_SYNC_TRIGGER_FAILED);
        private static final PostImportSyncResult SYNC_RESULT_TRIGGER_INTERRUPTED = new PostImportSyncResult(true, false, 0, MSG_WIDGET_SYNC_TRIGGER_INTERRUPTED);

        void handlePost(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req)) {
            json(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(JSON_KEY_MESSAGE, MSG_ADMIN_AUTH_REQUIRED)
                    .build());
            return;
        }

        String action = Optional.ofNullable(ServletRequestParamUtil.firstParam(req, JSON_KEY_ACTION, 128, true, false)).orElse(EMPTY).trim();
        if (ACTION_PRECHECK.equalsIgnoreCase(action)) {
            handlePrecheck(req, resp);
        } else if (ACTION_RUN.equalsIgnoreCase(action)) {
            handleImportRun(req, resp);
        } else {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(JSON_KEY_MESSAGE, MSG_INVALID_ACTION)
                    .build());
        }
    }

    private void handlePrecheck(HttpServletRequest req, HttpServletResponse resp) {
        Part file;
        try {
            file = req.getPart(PART_FILE);
        } catch (IOException | ServletException e) {
            log.log(Level.WARNING, LOG_UNABLE_READ_UPLOAD_PRECHECK, e);
            json(resp, HttpServletResponse.SC_BAD_REQUEST, err(MSG_UPLOAD_ZIP));
            return;
        }
        if (file == null || file.getSize() == 0) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, err(MSG_UPLOAD_ZIP));
            return;
        }

        try (Connection conn = getConnectionSafe()) {
            Map<String, CsvTableData> zipTablesRaw = readZipTables(openPartInputStream(file, ACTION_PRECHECK));
            Map<String, CsvTableData> zipTables = normalizeZipWidgetTables(conn, zipTablesRaw);

            List<String> createdBaseline = ensureRequiredTables(conn);
            List<String> widgetTables = findWidgetTables(zipTables.keySet());
            List<String> createdWidgetTables = ensureWidgetTables(conn, widgetTables, zipTables);

            JsonArrayBuilder widgetSummary = Json.createArrayBuilder();
            for (String wt : widgetTables) {
                CsvTableData d = zipTables.get(wt);
                widgetSummary.add(Json.createObjectBuilder()
                    .add(JSON_KEY_TABLE, wt)
                        .add(JSON_KEY_ROWS_TO_IMPORT, d == null ? 0 : d.rows.size()));
            }

            JsonObjectBuilder result = Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_OK)
                    .add(KEY_CONNECTION_OK, true)
                    .add(KEY_READY_FOR_IMPORT, true)
                    .add(JSON_KEY_MESSAGE, MSG_PRECHECK_SUCCESS)
                    .add(JSON_KEY_CREATED_BASELINE_TABLES, toJsonArray(createdBaseline))
                    .add(JSON_KEY_CREATED_WIDGET_TABLES, toJsonArray(createdWidgetTables))
                    .add(JSON_KEY_ZIP_TABLE_COUNT, zipTables.size())
                    .add(JSON_KEY_WIDGET_TABLES, widgetSummary);

            json(resp, HttpServletResponse.SC_OK, result.build());

        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
                log.log(Level.SEVERE, LOG_IMPORT_PRECHECK_FAILED, e);
            json(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(KEY_CONNECTION_OK, false)
                    .add(KEY_READY_FOR_IMPORT, false)
                    .add(JSON_KEY_MESSAGE, MSG_PRECHECK_FAILED)
                    .build());
        }
    }

    private void handleImportRun(HttpServletRequest req, HttpServletResponse resp) {
        Part file;
        try {
            file = req.getPart(PART_FILE);
        } catch (IOException | ServletException e) {
            log.log(Level.WARNING, LOG_UNABLE_READ_UPLOAD_IMPORT, e);
            json(resp, HttpServletResponse.SC_BAD_REQUEST, err(MSG_UPLOAD_ZIP));
            return;
        }
        if (file == null || file.getSize() == 0) {
            json(resp, HttpServletResponse.SC_BAD_REQUEST, err(MSG_UPLOAD_ZIP));
            return;
        }

        try (Connection conn = getConnectionSafe()) {
            conn.setAutoCommit(false);

            try {
            Map<String, CsvTableData> zipTablesRaw = readZipTables(openPartInputStream(file, ACTION_RUN));
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
                    importedCounts.put(table, Integer.valueOf(cnt));
                }

                for (Map.Entry<String, CsvTableData> e : zipTables.entrySet()) {
                    String table = e.getKey();
                    if (importedCounts.containsKey(table)) {
                        continue;
                    }
                    int cnt = replaceTableData(conn, table, e.getValue());
                    realignSequenceBackedColumns(conn, table);
                    importedCounts.put(table, Integer.valueOf(cnt));
                }

                conn.commit();

                PostImportSyncResult syncResult = triggerPostImportSync();

                JsonObjectBuilder out = Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_OK)
                    .add(JSON_KEY_MESSAGE, MSG_IMPORT_COMPLETED)
                        .add(KEY_IMPORTED_TABLES, toJsonObject(importedCounts))
                        .add(JSON_KEY_POST_IMPORT_SYNC_TRIGGERED, syncResult.triggered)
                        .add(JSON_KEY_POST_IMPORT_SYNC_OK, syncResult.ok)
                        .add(JSON_KEY_POST_IMPORT_SYNC_STATUS_CODE, syncResult.statusCode)
                        .add(JSON_KEY_POST_IMPORT_SYNC_MESSAGE, safe(syncResult.message));

                json(resp, HttpServletResponse.SC_OK, out.build());

            } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
                try {
                    conn.rollback();
                } catch (SQLException rb) {
                    log.log(Level.WARNING, LOG_ROLLBACK_FAILED, rb);
                }
                throw e;
            }
        } catch (ImportException ie) {
            log.log(Level.SEVERE, LOG_IMPORT_RUN_FAILED, ie);
            json(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(JSON_KEY_MESSAGE, MSG_IMPORT_FAILED_INVALID_DATA)
                .add(JSON_KEY_TABLE, ie.getTable() == null ? EMPTY : ie.getTable())
                .add(JSON_KEY_ROW_NUMBER, ie.getRowNumber())
                .add(JSON_KEY_COLUMN, ie.getColumn() == null ? EMPTY : ie.getColumn())
                    .build());
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
                    log.log(Level.SEVERE, LOG_IMPORT_RUN_FAILED, e);
            json(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
                    .add(JSON_KEY_MESSAGE, MSG_IMPORT_FAILED)
                    .build());
        }
    }

    private PostImportSyncResult triggerPostImportSync() {
        if (POST_IMPORT_SYNC_URL.isEmpty()) {
            return SYNC_RESULT_SKIPPED;
        }

        String requestId = UUID.randomUUID().toString();

        URI endpointUri;
        try {
            endpointUri = URI.create(POST_IMPORT_SYNC_URL);
        } catch (IllegalArgumentException ex) {
            log.log(Level.WARNING, LOG_INVALID_POST_IMPORT_SYNC_URL, ex);
            return SYNC_RESULT_URL_INVALID;
        }

        if (!isSafeSyncEndpoint(endpointUri)) {
            log.warning(LOG_REJECTED_UNSAFE_POST_IMPORT_SYNC_URL);
            return SYNC_RESULT_URL_NOT_ALLOWED;
        }

        try {
            ServerDiagnosticsLog.write(
                DIAG_COMPONENT,
                requestId,
                EVENT_POST_IMPORT_SYNC_REQUEST,
                LOG_KV_METHOD_POST + LS + LOG_KV_URL + endpointUri
            );

            HttpRequest httpReq = HttpRequest.newBuilder(endpointUri)
                    .timeout(Duration.ofSeconds(120))
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED)
                    .POST(HttpRequest.BodyPublishers.ofString(EMPTY))
                    .build();

            HttpResponse<String> response = HTTP.send(httpReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = response.statusCode();
            boolean ok = code >= 200 && code < 300;

            ServerDiagnosticsLog.write(
                DIAG_COMPONENT,
                requestId,
                EVENT_POST_IMPORT_SYNC_RESPONSE,
                LOG_KV_STATUS + code + LS + LOG_KV_BODY + truncate(response.body())
            );

            if (!ok) {
                log.log(Level.WARNING, LOG_POST_IMPORT_SYNC_HTTP_ERROR,
                        new Object[]{code, truncate(response.body())});
            }

            return new PostImportSyncResult(true, ok, code, ok
                    ? MSG_WIDGET_SYNC_EXECUTED
                    : MSG_WIDGET_SYNC_CALL_FAILED_PREFIX + code);
        } catch (IOException e) {
            log.log(Level.WARNING, LOG_POST_IMPORT_SYNC_TRIGGER_FAILED, e);
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_POST_IMPORT_SYNC_ERROR,
                    LOG_KV_URL + endpointUri + LS + KEY_MESSAGE_KV + safe(e.getMessage()),
                    e
            );
                return SYNC_RESULT_TRIGGER_FAILED;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, LOG_POST_IMPORT_SYNC_TRIGGER_INTERRUPTED, e);
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_POST_IMPORT_SYNC_ERROR,
                    LOG_KV_URL + endpointUri + LS + KEY_MESSAGE_KV + safe(e.getMessage()),
                    e
            );
            return SYNC_RESULT_TRIGGER_INTERRUPTED;
        }
    }

    private Connection getConnectionSafe() {
        try {
            return Database.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_TO_OBTAIN_DB_CONNECTION, e);
        }
    }

    private InputStream openPartInputStream(Part file, String phase) {
        if (file == null) {
            throw new IllegalStateException(MSG_UPLOAD_FILE_MISSING_PHASE_PREFIX + safe(phase));
        }
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(MSG_UNABLE_OPEN_UPLOAD_STREAM_PHASE_PREFIX + safe(phase), e);
        }
    }

    private String truncate(String s) {
        if (s == null) {
            return EMPTY;
        }
        return s.length() > 512 ? TextIoSanitizerUtil.safeSlice(s, 0, 512) + ELLIPSIS : s;
    }

    private Map<String, CsvTableData> readZipTables(InputStream input) {
        Map<String, CsvTableData> out = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(input, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name == null || !name.startsWith(TABLES_PATH_PREFIX) || !name.endsWith(CSV_EXT)) {
                    continue;
                }

                String table = TextIoSanitizerUtil.safeSlice(name, TABLES_PATH_PREFIX.length(), name.length() - CSV_EXT.length());

                byte[] csvBytes = zis.readAllBytes();
                CsvTableData data = readCsv(new ByteArrayInputStream(csvBytes));
                out.put(table, data);
            }
            return out;
        } catch (IOException e) {
            throw new IllegalStateException(MSG_UNABLE_READ_ZIP_TABLE_DATA, e);
        }
    }

    private CsvTableData readCsv(InputStream in) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setQuote('"')
            .get();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            CSVParser parser = parseCsvSafe(format, br)) {

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
        } catch (IOException e) {
            throw new IllegalStateException(MSG_UNABLE_READ_CSV_TABLE_PAYLOAD, e);
        }
    }

    private CSVParser parseCsvSafe(CSVFormat format, BufferedReader reader) {
        try {
            return format.parse(reader);
        } catch (IOException ex) {
            throw new IllegalStateException(MSG_UNABLE_PARSE_CSV_PAYLOAD, ex);
        }
    }

    private List<String> ensureRequiredTables(Connection conn) {
        List<String> created = new ArrayList<>();
        for (String t : REQUIRED_TABLES) {
            if (!tableExists(conn, t)) {
                createKnownTable(conn, t);
                created.add(t);
            }
        }
        return created;
    }

    private List<String> ensureWidgetTables(Connection conn, List<String> widgetTables, Map<String, CsvTableData> zipTables) {
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

    private int replaceTableData(Connection conn, String table, CsvTableData data) {
        if (data.headers.isEmpty()) {
            return 0;
        }

        try {
            try (PreparedStatement ps = conn.prepareStatement("TRUNCATE TABLE " + q(table) + " RESTART IDENTITY CASCADE")) {
                ps.execute();
            }

            Map<String, ColumnInfo> columnInfo = loadColumnInfo(conn, table);

            List<String> insertHeaders = data.headers.stream()
                    .filter(h -> columnInfo.containsKey(h.toLowerCase()))
                    .toList();

            if (insertHeaders.isEmpty()) {
                throw new ImportException(
                        MSG_NO_MATCHING_COLUMNS_PREFIX + MSG_TABLE_PREFIX + table + TABLE_NAME_SUFFIX_PERIOD,
                        table, 1, null, null
                );
            }

            String cols = String.join(SQL_COMMA_SPACE, insertHeaders.stream().map(this::q).toList());
            String qmarks = String.join(SQL_COMMA_SPACE, Collections.nCopies(insertHeaders.size(), SQL_QMARK));
            String sql = SQL_INSERT_INTO + q(table) + SQL_OPEN_COLUMNS + cols + SQL_VALUES_OPEN + qmarks + ')';

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
                        } catch (IllegalStateException bindErr) {
                            throw new ImportException(
                                MSG_IMPORT_FAILED_AT_PREFIX + MSG_TABLE_PREFIX + table + MSG_ROW_SEPARATOR + csvRowNumber
                                            + MSG_COLUMN_SEPARATOR + column + '\'' + MSG_COLON_SPACE + safe(bindErr.getMessage()),
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
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_REPLACE_TABLE_DATA_PREFIX + table, ex);
        }
    }

    private void realignSequenceBackedColumns(Connection conn, String table) {
        String sequenceLookupSql = """
                SELECT column_name,
                       pg_get_serial_sequence(format('%I.%I', table_schema, table_name), column_name) AS seq_name
                  FROM information_schema.columns
                 WHERE table_schema = 'public'
                   AND table_name = ?
                 ORDER BY ordinal_position
                """;

        try {
            try (PreparedStatement ps = conn.prepareStatement(sequenceLookupSql)) {
                ps.setString(1, table);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String column = readMetadataIdentifier(rs, META_COLUMN_NAME_LOWER);
                        String sequenceName = readSafeDbText(rs, META_SEQ_NAME, 256);
                        if (column == null || sequenceName == null || sequenceName.isBlank()) {
                            continue;
                        }

                        if (!SQL_IDENTIFIER.matcher(column).matches()) {
                            continue;
                        }

                        String setSequenceSql = SQL_SETVAL_PREFIX + q(column) + SQL_SETVAL_MIDDLE + q(table) + SQL_SETVAL_SUFFIX;
                        try (PreparedStatement setPs = conn.prepareStatement(setSequenceSql)) {
                            setPs.setString(1, sequenceName);
                            setPs.execute();
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_REALIGN_SEQUENCE_COLUMNS_PREFIX + table, ex);
        }
    }

    private Map<String, ColumnInfo> loadColumnInfo(Connection conn, String table) {
        Map<String, ColumnInfo> info = new LinkedHashMap<>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, "public", table, null)) {
                while (rs.next()) {
                    String rawName = readMetadataIdentifier(rs, META_COLUMN_NAME_UPPER);
                    if (rawName == null) {
                        continue;
                    }
                    String name = rawName.toLowerCase(Locale.ROOT);
                    Object typeValue = readMetadataInt(rs, META_DATA_TYPE);
                    if (typeValue == null) {
                        continue;
                    }
                    int type = sanitizeSqlType(Integer.parseInt(typeValue.toString()));

                    Object nullableValue = readMetadataInt(rs, META_NULLABLE);
                    boolean nullable = true;
                    if (nullableValue != null) {
                        nullable = Integer.parseInt(nullableValue.toString()) != ResultSetMetaData.columnNoNulls;
                    }
                    info.put(name, new ColumnInfo(type, nullable));
                }
            }
            return info;
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_LOAD_COLUMN_METADATA_PREFIX + table, ex);
        }
    }

    private Integer readMetadataInt(ResultSet rs, String columnName) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return null;
        }
        int direct;
        try {
            direct = rs.getInt(columnName);
            if (!rs.wasNull()) {
                return Integer.valueOf(direct);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_TYPED_METADATA_INT_READ_FAILED_PREFIX + columnName, ex);
        }

        String text = readSafeDbText(rs, columnName, 32);
        if (text == null) {
            return null;
        }
        if (text.isEmpty() || !text.matches(REGEX_INT_10)) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_INVALID_METADATA_INT_VALUE, ex);
            return null;
        }
    }

    private String normalizeValueForColumn(String table, String column, String raw, ColumnInfo ci) {
        String t = table == null ? EMPTY : table.toLowerCase();
        String c = column == null ? EMPTY : column.toLowerCase();

        if (TABLE_TERM_DEFINITION.equals(t) && TERM_MATCH_PATTERN.equals(c)) {
            return raw == null ? EMPTY : raw;
        }

        if (TABLE_TERM_DEFINITION.equals(t) && TERM_MATCH_TYPE.equals(c)) {
            if (raw == null || raw.trim().isEmpty()) {
                return VALUE_WILDCARD;
            }
        }

        if ((raw == null || raw.trim().isEmpty())
                && ci != null
                && !ci.nullable
                && isTextType(ci.sqlType)) {
            return EMPTY;
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

    private void executeBatchWithContext(PreparedStatement ps, String table, int csvRowNumber) {
        try {
            ps.executeBatch();
        } catch (BatchUpdateException bue) {
            SQLException root = bue.getNextException();
            if (root == null) {
                root = bue;
            }
            throw new ImportException(
                    MSG_BATCH_INSERT_FAILED_AT_PREFIX + MSG_TABLE_PREFIX + table + MSG_NEAR_CSV_ROW_SEPARATOR + csvRowNumber + MSG_COLON_SPACE + safe(root.getMessage()),
                    table, csvRowNumber, null, bue
            );
        } catch (SQLException ex) {
            throw new ImportException(
                    MSG_BATCH_INSERT_FAILED_AT_PREFIX + MSG_TABLE_PREFIX + table + MSG_NEAR_CSV_ROW_SEPARATOR + csvRowNumber + MSG_COLON_SPACE + safe(ex.getMessage()),
                    table, csvRowNumber, null, ex
            );
        }
    }

    private void bindTyped(PreparedStatement ps, int idx, String raw, int sqlType) {
        String v = raw == null ? null : raw.trim();

        try {
            if (v == null) {
                ps.setNull(idx, sqlType);
                return;
            }
            if (v.isEmpty()) {
                if (isTextType(sqlType)) {
                    ps.setString(idx, EMPTY);
                } else {
                    ps.setNull(idx, sqlType);
                }
                return;
            }

            switch (sqlType) {
                case Types.BIGINT ->
                    ps.setLong(idx, Long.parseLong(v));
                case Types.INTEGER, Types.SMALLINT, Types.TINYINT ->
                    ps.setInt(idx, Integer.parseInt(v));
                case Types.BOOLEAN, Types.BIT ->
                    ps.setBoolean(idx, VALUE_TRUE_TEXT.equalsIgnoreCase(v) || VALUE_T_TEXT.equalsIgnoreCase(v) || VALUE_ONE_TEXT.equals(v));
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
        } catch (SQLException | IllegalArgumentException ex) {
            throw new IllegalStateException(MSG_INVALID_VALUE_PREFIX + String.valueOf(v) + MSG_INVALID_VALUE_SQL_TYPE_INFIX + sqlType, ex);
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

    private void bindTimestamp(PreparedStatement ps, int idx, String v) {
        Timestamp ts = parseTimestampStrict(v);
        if (ts == null) {
            throw new IllegalStateException(MSG_INVALID_TIMESTAMP_FORMAT_PREFIX + v + '\'');
        }
        try {
            ps.setTimestamp(idx, ts);
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_BIND_TIMESTAMP, ex);
        }
    }

    private LocalDate parseDateStrict(String v) {
        try {
            return LocalDate.parse(v);
        } catch (IllegalArgumentException ignore) {
        }

        try {
            return OffsetDateTime.parse(v).toLocalDate();
        } catch (DateTimeParseException ignore) {
        }

        try {
            return Instant.parse(v).atOffset(java.time.ZoneOffset.UTC).toLocalDate();
        } catch (DateTimeParseException ignore) {
        }

        try {
            return LocalDateTime.parse(v).toLocalDate();
        } catch (DateTimeParseException ignore) {
        }

        if (v.length() >= 10) {
            try {
                return LocalDate.parse(TextIoSanitizerUtil.safeSlice(v, 0, 10));
            } catch (DateTimeParseException | IllegalArgumentException ignore) {
            }
        }

        return null;
    }

    private void bindDate(PreparedStatement ps, int idx, String v) {
        LocalDate parsedDate = parseDateStrict(v);
        if (parsedDate == null) {
            throw new IllegalStateException(MSG_INVALID_DATE_FORMAT_PREFIX + v + '\'');
        }
        try {
            ps.setObject(idx, parsedDate);
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_BIND_DATE, ex);
        }
    }

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, "public", tableName, new String[]{METADATA_TABLE_TYPE})) {
                return rs.next();
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_UNABLE_INSPECT_TABLE_METADATA_PREFIX + tableName, ex);
            return false;
        }
    }

    private void createKnownTable(Connection conn, String table) {
        String ddl = switch (table) {
                case TABLE_CUSTOMER_IDENTITY -> SQL_CREATE_TABLE_IF_NOT_EXISTS + q(table) + SQL_OPEN_PAREN + SQL_DDL_CUSTOMER_IDENTITY_BODY + ')';
                case TABLE_CUSTOMER_IDENTITY_SESSION -> SQL_CREATE_TABLE_IF_NOT_EXISTS + q(table) + SQL_OPEN_PAREN + SQL_DDL_CUSTOMER_IDENTITY_SESSION_BODY + ')';
                default -> SQL_CREATE_TABLE_IF_NOT_EXISTS + q(table) + SQL_DEFAULT_ID_COLUMN;
        };

        PreparedStatement ps = prepareStatementSafe(conn, ddl);
        try (ps) {
            ps.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_CREATE_REQUIRED_TABLE_PREFIX + table, ex);
        }
    }

    private void createTableFromCsvHeader(Connection conn, String table, List<String> headers) {
        if (headers == null || headers.isEmpty()) {
            PreparedStatement ps = prepareStatementSafe(conn, SQL_CREATE_TABLE_IF_NOT_EXISTS + q(table) + SQL_DEFAULT_ID_COLUMN);
            try (ps) {
                ps.execute();
            } catch (SQLException ex) {
                throw new IllegalStateException(MSG_UNABLE_CREATE_FALLBACK_TABLE_PREFIX + table, ex);
            }
            return;
        }

        StringBuilder ddl = new StringBuilder(SQL_CREATE_TABLE_IF_NOT_EXISTS).append(q(table)).append(SQL_OPEN_PAREN);
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) {
                ddl.append(SQL_COMMA_SPACE);
            }
            ddl.append(q(headers.get(i))).append(SQL_TEXT_TYPE);
        }
        ddl.append(')');

        PreparedStatement ps = prepareStatementSafe(conn, ddl.toString());
        try (ps) {
            ps.execute();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_CREATE_TABLE_FROM_HEADER_PREFIX + table, ex);
        }
    }

    private String sanitizeWidgetTableName(String widgetId) {
        return DashboardDbUtil.sanitizeWidgetTableName(widgetId);
    }

    private Map<String, String> buildWidgetIdToTableMap(Connection conn) {
        Map<String, String> out = new LinkedHashMap<>();
        if (!tableExists(conn, TABLE_WIDGET_ENTRIES)) {
            return out;
        }

        String sql = SQL_SELECT_WIDGET_ID_FROM + TABLE_WIDGET_ENTRIES;
        PreparedStatement ps = prepareStatementSafe(conn, sql);
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            while (rs.next()) {
                String widgetId = sanitizeWidgetId(readSafeDbText(rs, DB_COL_WIDGET_ID, 80));
                if (widgetId == null || widgetId.isBlank()) {
                    continue;
                }
                out.put(widgetId, sanitizeWidgetTableName(widgetId));
            }
            return out;
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_LOAD_WIDGET_ID_MAP, ex);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
                java.util.logging.Logger.getLogger(LOGGER_OWASP).log(java.util.logging.Level.FINE, MSG_HANDLED_EXCEPTION, ignore);
                // ignore close failure
            }
        }
    }

    private PreparedStatement prepareStatementSafe(Connection conn, String sql) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_UNABLE_PREPARE_SQL_STATEMENT, ex);
        }
    }

    private Map<String, CsvTableData> normalizeZipWidgetTables(Connection conn, Map<String, CsvTableData> zipTables) {
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
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + ident + '"';
    }

    static String sanitizeRequestValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = stripControlChars(value).trim();
        if (trimmed.isEmpty()) {
            return EMPTY;
        }
        return trimmed.length() > 128 ? TextIoSanitizerUtil.safeSlice(trimmed, 0, 128) : trimmed;
    }

    private static String readEnv(String name) {
        if (name == null || name.isBlank()) {
            return EMPTY;
        }
        String value = new ProcessBuilder().environment().get(name);
        if (value == null || value.isBlank()) {
            return EMPTY;
        }
        String normalized = com.sim.chatserver.util.LineSeparatorUtil.stripCrLf(
            value.replace(VALUE_NUL, SPACE)).trim();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC);
        String bounded = normalized.length() > 512 ? TextIoSanitizerUtil.safeSlice(normalized, 0, 512) : normalized;
        return sanitizeSyncUrlValue(bounded);
    }

    private static String sanitizeSyncUrlValue(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return EMPTY;
        }
        String trimmed = stripControlChars(candidate).trim();
        if (trimmed.length() > 512) {
            return EMPTY;
        }
        return SAFE_SYNC_URL.matcher(trimmed).matches() ? trimmed : EMPTY;
    }

    private static String stripControlChars(String value) {
        if (value == null) {
            return EMPTY;
        }
        return com.sim.chatserver.util.LineSeparatorUtil.stripCrLf(
            value.replace(VALUE_NUL, EMPTY));
    }

    private String readMetadataIdentifier(ResultSet rs, String columnName) {
        String raw = readSafeDbText(rs, columnName, 63);
        if (raw == null) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        return SQL_IDENTIFIER.matcher(normalized).matches() ? normalized : null;
    }

    private String readSafeDbText(ResultSet rs, String columnName, int maxLen) {
        String raw = null;
        try {
            raw = rs.getString(columnName);
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_DB_TEXT_READ_FAILED_PREFIX + columnName, ex);
        }
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(stripControlChars(raw), Normalizer.Form.NFKC).trim();
        if (normalized.isEmpty()) {
            return null;
        }
        String bounded = normalized.length() > maxLen ? TextIoSanitizerUtil.safeSlice(normalized, 0, maxLen) : normalized;
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
            trimmed = TextIoSanitizerUtil.safeSlice(trimmed, 0, 80);
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
        if (!VALUE_HTTP.equals(lowered) && !VALUE_HTTPS.equals(lowered)) {
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
        String role = roleObj == null ? EMPTY : String.valueOf(roleObj);
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    private JsonObject err(String msg) {
        return Json.createObjectBuilder()
            .add(JSON_KEY_STATUS, JSON_STATUS_ERROR)
            .add(JSON_KEY_MESSAGE, safe(msg))
                .build();
    }

    private void json(HttpServletResponse resp, int status, JsonObject obj) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, obj);
        } catch (IOException e) {
            log.log(Level.WARNING, MSG_UNABLE_WRITE_JSON_RESPONSE, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MSG_UNABLE_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private String safe(String s) {
        return s == null ? EMPTY : s;
    }

    private jakarta.json.JsonArray toJsonArray(List<String> vals) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        for (String v : vals) {
            b.add(v == null ? EMPTY : v);
        }
        return b.build();
    }

    private JsonObject toJsonObject(Map<String, Integer> m) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            Object value = e.getValue();
            int safeValue = 0;
            if (value != null) {
                safeValue = Integer.parseInt(value.toString());
            }
            b.add(e.getKey(), safeValue);
        }
        return b.build();
    }

}

final class CsvTableData {

    final List<String> headers;
    final List<List<String>> rows;

    CsvTableData(List<String> headers, List<List<String>> rows) {
        this.headers = headers == null ? List.of() : headers;
        this.rows = rows == null ? List.of() : rows;
    }
}

final class ColumnInfo {

    final int sqlType;
    final boolean nullable;

    ColumnInfo(int sqlType, boolean nullable) {
        this.sqlType = sqlType;
        this.nullable = nullable;
    }
}

final class ImportException extends RuntimeException {

    private final transient String table;
    private final transient int rowNumber;
    private final transient String column;

    ImportException(String message, String table, int rowNumber, String column, Throwable cause) {
        super(message, cause);
        this.table = table;
        this.rowNumber = rowNumber;
        this.column = column;
    }

    String getTable() {
        return table;
    }

    int getRowNumber() {
        return rowNumber;
    }

    String getColumn() {
        return column;
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidObjectException(DatabaseImportService.MSG_DESERIALIZATION_NOT_ALLOWED);
    }
}

final class PostImportSyncResult {

    final boolean triggered;
    final boolean ok;
    final int statusCode;
    final String message;

    PostImportSyncResult(boolean triggered, boolean ok, int statusCode, String message) {
        this.triggered = triggered;
        this.ok = ok;
        this.statusCode = statusCode;
        this.message = message;
    }
}

