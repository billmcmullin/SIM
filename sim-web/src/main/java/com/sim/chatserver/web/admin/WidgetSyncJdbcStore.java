package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.util.TextIoSanitizerUtil;

import jakarta.json.JsonObject;

final class WidgetSyncJdbcStore {

    private static final Logger log = Logger.getLogger(WidgetSyncJdbcStore.class.getCanonicalName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.SQL_IDENTIFIER_REGEX);
    static final String EMPTY = "";
    private static final String SQL_DELETE_FROM = "DELETE FROM ";
    private static final String SQL_ON = " ON ";
    private static final String SQL_WIDGET_CHAT_ID_COLUMNS = " (widget_chat_id)";
    private static final String SQL_CREATE_UNIQUE_INDEX_IF_NOT_EXISTS = "CREATE UNIQUE INDEX IF NOT EXISTS ";
    private static final String COMMA_SPACE = ", ";
    private static final String VALUE_TRUE = com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE;
    private static final String VALUE_FALSE = com.sim.chatserver.util.CommonLiteralConstants.VALUE_FALSE;
    private static final String MSG_DATASOURCE_UNAVAILABLE = "Data source holder is unavailable.";
    private static final String MSG_INSERT_WIDGET_CHATS_PREFIX = "Unable to insert widget chats into table ";
        private static final String MSG_SYNC_SETTINGS_DEFAULTS_REQUIRED = "Sync settings defaults are required.";
        private static final String MSG_UNABLE_READ_SYNC_SETTINGS = "Unable to read sync settings";
        private static final String MSG_UNABLE_PERSIST_SYNC_SETTINGS = "Unable to persist sync settings";
        private static final String MSG_UNABLE_ENSURE_WIDGET_TABLE_PREFIX = "Unable to ensure widget table ";
        private static final String MSG_UNABLE_LOAD_DAY_ENTRIES = "Unable to load day entries";
        private static final String MSG_UNABLE_BEGIN_WIDGET_CHAT_INSERT_PREFIX = "Unable to begin widget chat insert transaction for table ";
        private static final String MSG_ROLLBACK_FAILURE_DETAILS = "Rollback failure details";
        private static final String MSG_AUTOCOMMIT_RESTORE_ERROR = "Auto-commit restore error";
        private static final String MSG_UNIQUE_INDEX_CREATION_FAILED = "Unique index creation failed, attempting dedupe before retry";
        private static final String MSG_CREATED_AT_INDEX_CREATION_FAILED = "Created_at index creation failed for {0}";
        private static final String MSG_CREATED_AT_INDEX_ERROR = "Created_at index error";
        private static final String MSG_DUPLICATE_ROWS_REMOVED = "Removed {0} duplicate rows from {1}";
        private static final String MSG_UNABLE_INSPECT_TABLE_METADATA_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_INSPECT_TABLE_METADATA_PREFIX;
        private static final String MSG_INVALID_SQL_IDENTIFIER = com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_SQL_IDENTIFIER;
        private static final String MSG_INVALID_PERSISTED_SYNC_INTERVAL = "Invalid persisted sync interval value";
        private static final String MSG_INVALID_PERSISTED_INTEGER = "Invalid persisted integer value";
        private static final String MSG_UNABLE_PARSE_PERSISTED_TIMESTAMP = "Unable to parse persisted timestamp value";
        private static final String MSG_GET_CHARACTER_STREAM_FAILED_PREFIX = "ResultSet#getCharacterStream failed for column ";
        private static final String MSG_GET_BYTES_FAILED_PREFIX = "ResultSet#getBytes failed for column ";
        private static final String MSG_GET_STRING_FAILED_PREFIX = "ResultSet#getString failed for column ";
        private static final String MSG_GET_OBJECT_FAILED_PREFIX = "ResultSet#getObject failed for column ";

        private static final String SQL_LOAD_SYNC_SETTINGS =
            "SELECT interval_seconds, last_synced, summary_interval_seconds, summary_auto_enabled, summary_prompt, summary_max_rows, "
            + "summary_max_upstream_entries, summary_max_message_chars, summary_max_request_bytes, summary_last_run"
            + " FROM widget_sync_settings WHERE id = 1";
        private static final String SQL_SELECT_DAY_ENTRIES_PREFIX = com.sim.chatserver.util.CommonLiteralConstants.SQL_SELECT_WIDGET_CHAT_PREFIX;
        private static final String SQL_SELECT_DAY_ENTRIES_SUFFIX = " WHERE created_at >= ? AND created_at < ? ORDER BY created_at DESC LIMIT ?";
        private static final String SQL_CREATE_SYNC_SETTINGS_TABLE_PREFIX = "CREATE TABLE IF NOT EXISTS widget_sync_settings (";
        private static final String SQL_SYNC_SETTINGS_INTERVAL_LAST_SYNCED =
            "id INTEGER PRIMARY KEY, interval_seconds BIGINT NOT NULL, last_synced TIMESTAMP";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_INTERVAL_DEFAULT = "summary_interval_seconds BIGINT NOT NULL DEFAULT ";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_AUTO_ENABLED_DEFAULT =
            "summary_auto_enabled BOOLEAN NOT NULL DEFAULT TRUE";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_PROMPT = "summary_prompt TEXT";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_MAX_ROWS_DEFAULT = "summary_max_rows INTEGER NOT NULL DEFAULT ";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_MAX_UPSTREAM_ENTRIES_DEFAULT =
            "summary_max_upstream_entries INTEGER NOT NULL DEFAULT ";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_MAX_MESSAGE_CHARS_DEFAULT =
            "summary_max_message_chars INTEGER NOT NULL DEFAULT ";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_MAX_REQUEST_BYTES_DEFAULT =
            "summary_max_request_bytes INTEGER NOT NULL DEFAULT ";
        private static final String SQL_SYNC_SETTINGS_SUMMARY_LAST_RUN = "summary_last_run TIMESTAMP)";
        private static final String SQL_ALTER_ADD_SUMMARY_INTERVAL_SECONDS =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_interval_seconds BIGINT";
        private static final String SQL_ALTER_ADD_SUMMARY_AUTO_ENABLED =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_auto_enabled BOOLEAN";
        private static final String SQL_ALTER_ADD_SUMMARY_PROMPT =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_prompt TEXT";
        private static final String SQL_ALTER_ADD_SUMMARY_MAX_ROWS =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_rows INTEGER";
        private static final String SQL_ALTER_ADD_SUMMARY_MAX_UPSTREAM_ENTRIES =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_upstream_entries INTEGER";
        private static final String SQL_ALTER_ADD_SUMMARY_MAX_MESSAGE_CHARS =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_message_chars INTEGER";
        private static final String SQL_ALTER_ADD_SUMMARY_MAX_REQUEST_BYTES =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_request_bytes INTEGER";
        private static final String SQL_ALTER_ADD_SUMMARY_LAST_RUN =
            "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_last_run TIMESTAMP";
        private static final String SQL_UPSERT_SYNC_SETTINGS =
            "INSERT INTO widget_sync_settings (id, interval_seconds, last_synced, summary_interval_seconds, summary_auto_enabled, summary_prompt, summary_max_rows, "
            + "summary_max_upstream_entries, summary_max_message_chars, summary_max_request_bytes, summary_last_run) "
            + "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT (id) DO UPDATE SET "
            + "interval_seconds = EXCLUDED.interval_seconds, "
            + "last_synced = EXCLUDED.last_synced, "
            + "summary_interval_seconds = EXCLUDED.summary_interval_seconds, "
            + "summary_auto_enabled = EXCLUDED.summary_auto_enabled, "
            + "summary_prompt = EXCLUDED.summary_prompt, "
            + "summary_max_rows = EXCLUDED.summary_max_rows, "
            + "summary_max_upstream_entries = EXCLUDED.summary_max_upstream_entries, "
            + "summary_max_message_chars = EXCLUDED.summary_max_message_chars, "
            + "summary_max_request_bytes = EXCLUDED.summary_max_request_bytes, "
            + "summary_last_run = EXCLUDED.summary_last_run";
        private static final String SQL_CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
        private static final String SQL_WIDGET_TABLE_SCHEMA_PREFIX =
            " (db_id BIGSERIAL PRIMARY KEY, widget_chat_id TEXT, prompt TEXT, response_text TEXT, ";
        private static final String SQL_WIDGET_TABLE_SCHEMA_SUFFIX = "created_at TIMESTAMP, session_id TEXT, username TEXT)";
        private static final String SQL_CREATE_INDEX_IF_NOT_EXISTS = "CREATE INDEX IF NOT EXISTS ";
        private static final String SQL_CREATED_AT_INDEX_SUFFIX = " (created_at DESC)";
        private static final String SQL_DEDUPE_USING = " a USING ";
        private static final String SQL_DEDUPE_NONNULL_CHAT_ID = "AND a.widget_chat_id IS NOT NULL ";
        private static final String SQL_DEDUPE_KEEP_NEWER = "AND (a.created_at < b.created_at ";
        private static final String SQL_DEDUPE_KEEP_HIGHER_ID_ON_TIE = "OR (a.created_at = b.created_at AND a.db_id < b.db_id) ";
        private static final String SQL_DEDUPE_NULL_ORDER = "OR (a.created_at IS NULL AND b.created_at IS NOT NULL) ";
        private static final String SQL_DEDUPE_NULL_TIE = "OR (a.created_at IS NULL AND b.created_at IS NULL AND a.db_id < b.db_id))";
        private static final String SQL_DEDUPE_WIDGET_CHAT_JOIN_WHERE = " b WHERE a.widget_chat_id = b.widget_chat_id ";
        private static final String SQL_INSERT_WIDGET_CHATS_PREFIX = "INSERT INTO ";
        private static final String SQL_INSERT_WIDGET_CHATS_COLUMNS = " (widget_chat_id, prompt, response_text, created_at, session_id, username) ";
        private static final String SQL_INSERT_WIDGET_CHATS_VALUES = "VALUES (?,?,?,?,?,?) ON CONFLICT (widget_chat_id) DO NOTHING";

        private static final String COL_INTERVAL_SECONDS = "interval_seconds";
        private static final String COL_SUMMARY_INTERVAL_SECONDS = "summary_interval_seconds";
        private static final String COL_SUMMARY_AUTO_ENABLED = "summary_auto_enabled";
        private static final String COL_SUMMARY_MAX_ROWS = "summary_max_rows";
        private static final String COL_SUMMARY_MAX_UPSTREAM_ENTRIES = "summary_max_upstream_entries";
        private static final String COL_SUMMARY_MAX_MESSAGE_CHARS = "summary_max_message_chars";
        private static final String COL_SUMMARY_MAX_REQUEST_BYTES = "summary_max_request_bytes";
        private static final String COL_SUMMARY_PROMPT = "summary_prompt";
        private static final String COL_LAST_SYNCED = "last_synced";
        private static final String COL_SUMMARY_LAST_RUN = "summary_last_run";
        private static final String COL_WIDGET_CHAT_ID = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_WIDGET_CHAT_ID;
        private static final String COL_PROMPT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT;
        private static final String COL_RESPONSE_TEXT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_RESPONSE_TEXT;
        private static final String COL_CREATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_CREATED_AT;
        private static final String COL_SESSION_ID = com.sim.chatserver.util.SqlLiteralConstants.COL_SESSION_ID;
        private static final String DB_TABLE_TYPE = com.sim.chatserver.util.CommonLiteralConstants.DB_TABLE_TYPE_TABLE;
        private static final String INDEX_SUFFIX_WIDGET_CHAT_ID = "_widget_chat_id_uidx";
        private static final String INDEX_SUFFIX_CREATED_AT = "_created_at_idx";
        private static final String VALUE_T = "t";
        private static final String VALUE_F = "f";
        private static final String VALUE_ONE = "1";
        private static final String VALUE_ZERO = "0";
        private static final String DOUBLE_QUOTE = "\"";
        private static final String DOUBLE_DOUBLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
        private static final Pattern PERSISTED_POSITIVE_DIGITS = Pattern.compile("^\\d{1,12}$");
        private static final Pattern PERSISTED_INT_PATTERN = Pattern.compile("^-?\\d{1,10}$");

    private final long defaultSummaryIntervalSeconds;
    private final boolean defaultSummaryAutoEnabled;
    private final int defaultSummaryMaxRows;
    private final int defaultSummaryMaxUpstreamEntries;
    private final int defaultSummaryMaxMessageChars;
    private final int defaultSummaryMaxRequestBytes;
    private final int maxSummaryPromptChars;

    WidgetSyncJdbcStore(
            long defaultSummaryIntervalSeconds,
            boolean defaultSummaryAutoEnabled,
            int defaultSummaryMaxRows,
            int defaultSummaryMaxUpstreamEntries,
            int defaultSummaryMaxMessageChars,
            int defaultSummaryMaxRequestBytes,
            int maxSummaryPromptChars
    ) {
        this.defaultSummaryIntervalSeconds = defaultSummaryIntervalSeconds;
        this.defaultSummaryAutoEnabled = defaultSummaryAutoEnabled;
        this.defaultSummaryMaxRows = defaultSummaryMaxRows;
        this.defaultSummaryMaxUpstreamEntries = defaultSummaryMaxUpstreamEntries;
        this.defaultSummaryMaxMessageChars = defaultSummaryMaxMessageChars;
        this.defaultSummaryMaxRequestBytes = defaultSummaryMaxRequestBytes;
        this.maxSummaryPromptChars = maxSummaryPromptChars;
    }

    SyncSettingsData loadOrCreateSyncSettings(AppDataSourceHolder holder, SyncSettingsData defaults) {
        if (defaults == null) {
            throw new IllegalArgumentException(MSG_SYNC_SETTINGS_DEFAULTS_REQUIRED);
        }

        try (Connection conn = openConnection(holder)) {
            ensureSyncSettingsTable(conn);

                String sql = SQL_LOAD_SYNC_SETTINGS;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                long persistedInterval = readPersistedIntervalSeconds(rs, COL_INTERVAL_SECONDS, defaults.intervalSeconds);
                    long persistedSummaryInterval = readPersistedIntervalSeconds(
                            rs,
                    COL_SUMMARY_INTERVAL_SECONDS,
                            defaults.summaryIntervalSeconds
                    );
                    boolean persistedSummaryAutoEnabled = readPersistedBoolean(
                            rs,
                    COL_SUMMARY_AUTO_ENABLED,
                            defaults.summaryAutoEnabled
                    );
                int persistedSummaryMaxRows = readPersistedInt(rs, COL_SUMMARY_MAX_ROWS, defaults.summaryMaxRows);
                    int persistedSummaryMaxUpstreamEntries = readPersistedInt(
                            rs,
                    COL_SUMMARY_MAX_UPSTREAM_ENTRIES,
                            defaults.summaryMaxUpstreamEntries
                    );
                    int persistedSummaryMaxMessageChars = readPersistedInt(
                            rs,
                    COL_SUMMARY_MAX_MESSAGE_CHARS,
                            defaults.summaryMaxMessageChars
                    );
                    int persistedSummaryMaxRequestBytes = readPersistedInt(
                            rs,
                    COL_SUMMARY_MAX_REQUEST_BYTES,
                            defaults.summaryMaxRequestBytes
                    );

                String persistedSummaryPrompt = readDbText(rs, COL_SUMMARY_PROMPT, maxSummaryPromptChars);
                    if (persistedSummaryPrompt.isBlank()) {
                        persistedSummaryPrompt = defaults.summaryPrompt;
                    }

                Timestamp persistedLastSynced = readDbTimestamp(rs, COL_LAST_SYNCED);
                Timestamp persistedSummaryLastRun = readDbTimestamp(rs, COL_SUMMARY_LAST_RUN);

                    return new SyncSettingsData(
                            persistedInterval,
                            persistedLastSynced,
                            persistedSummaryInterval,
                            persistedSummaryAutoEnabled,
                            persistedSummaryPrompt,
                            persistedSummaryMaxRows,
                            persistedSummaryMaxUpstreamEntries,
                            persistedSummaryMaxMessageChars,
                            persistedSummaryMaxRequestBytes,
                            persistedSummaryLastRun
                    );
                }
            }

            upsertSyncSettings(conn, defaults);
            return defaults;
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_READ_SYNC_SETTINGS, e);
        }
    }

    void persistSyncSettings(AppDataSourceHolder holder, SyncSettingsData settings) {
        if (settings == null) {
            return;
        }

        try (Connection conn = openConnection(holder)) {
            ensureSyncSettingsTable(conn);
            upsertSyncSettings(conn, settings);
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_PERSIST_SYNC_SETTINGS, e);
        }
    }

    void ensureWidgetTable(AppDataSourceHolder holder, String tableName, Set<String> ensuredTables) {
        if (tableName == null || tableName.isBlank()) {
            return;
        }
        try (Connection conn = openConnection(holder)) {
            ensureTable(conn, tableName, ensuredTables);
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_ENSURE_WIDGET_TABLE_PREFIX + tableName, e);
        }
    }

    int insertWidgetChats(AppDataSourceHolder holder, String tableName, List<ChatUpsertRow> rows) {
        if (rows == null || rows.isEmpty() || tableName == null || tableName.isBlank()) {
            return 0;
        }

        try (Connection conn = openConnection(holder)) {
            return insertWidgetChats(conn, tableName, rows);
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_INSERT_WIDGET_CHATS_PREFIX + tableName, e);
        }
    }

    List<SelectedEntry> loadEntriesForDay(
            AppDataSourceHolder holder,
            List<String> tableNames,
            LocalDate day,
            int maxRows,
            Set<String> ensuredTables
    ) {
        List<SelectedEntry> out = new ArrayList<>();
        if (tableNames == null || tableNames.isEmpty() || day == null || maxRows <= 0) {
            return out;
        }

        Timestamp start = Timestamp.valueOf(day.atStartOfDay());
        Timestamp end = Timestamp.valueOf(day.plusDays(1).atStartOfDay());

        try (Connection conn = openConnection(holder)) {
            for (String tableName : tableNames) {
                if (tableName == null || tableName.isBlank()) {
                    continue;
                }

                if (!tableExists(conn, tableName, ensuredTables)) {
                    continue;
                }

                int remainingRows = maxRows - out.size();
                if (remainingRows <= 0) {
                    return out;
                }

                String sql = SQL_SELECT_DAY_ENTRIES_PREFIX
                        + quoteIdentifier(tableName)
                    + SQL_SELECT_DAY_ENTRIES_SUFFIX;

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, start);
                    ps.setTimestamp(2, end);
                    ps.setInt(3, remainingRows);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (out.size() >= maxRows) {
                                return out;
                            }

                            String chatId = readDbText(rs, COL_WIDGET_CHAT_ID, 256);
                            String prompt = readDbText(rs, COL_PROMPT, 8000);
                            String response = readDbText(rs, COL_RESPONSE_TEXT, 32000);
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, COL_CREATED_AT);
                            String sessionId = readDbText(rs, COL_SESSION_ID, 256);

                            out.add(new SelectedEntry(
                                    chatId == null ? EMPTY : chatId,
                                    prompt == null ? EMPTY : prompt,
                                    response == null ? EMPTY : response,
                                    createdAt == null ? EMPTY : createdAt.toInstant().toString(),
                                    sessionId == null ? EMPTY : sessionId
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_LOAD_DAY_ENTRIES, e);
        }

        return out;
    }

    private DataSource requireDataSource(AppDataSourceHolder holder) {
        if (holder == null) {
            throw new IllegalStateException(MSG_DATASOURCE_UNAVAILABLE);
        }
        try {
            return holder.getDataSource();
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException ex) {
            throw new IllegalStateException(MSG_DATASOURCE_UNAVAILABLE, ex);
        }
    }

    private Connection openConnection(AppDataSourceHolder holder) throws SQLException {
        return requireDataSource(holder).getConnection();
    }

    private void ensureSyncSettingsTable(Connection conn) throws SQLException {
        String sql = SQL_CREATE_SYNC_SETTINGS_TABLE_PREFIX
            + SQL_SYNC_SETTINGS_INTERVAL_LAST_SYNCED + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_INTERVAL_DEFAULT + defaultSummaryIntervalSeconds + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_AUTO_ENABLED_DEFAULT + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_PROMPT + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_MAX_ROWS_DEFAULT + defaultSummaryMaxRows + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_MAX_UPSTREAM_ENTRIES_DEFAULT + defaultSummaryMaxUpstreamEntries + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_MAX_MESSAGE_CHARS_DEFAULT + defaultSummaryMaxMessageChars + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_MAX_REQUEST_BYTES_DEFAULT + defaultSummaryMaxRequestBytes + COMMA_SPACE
            + SQL_SYNC_SETTINGS_SUMMARY_LAST_RUN;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        }

        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_INTERVAL_SECONDS);
        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_AUTO_ENABLED);
        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_PROMPT);
        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_MAX_ROWS);
        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_MAX_UPSTREAM_ENTRIES);
        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_MAX_MESSAGE_CHARS);
        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_MAX_REQUEST_BYTES);
        addColumnIfMissing(conn, SQL_ALTER_ADD_SUMMARY_LAST_RUN);

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE widget_sync_settings SET summary_interval_seconds = ? WHERE summary_interval_seconds IS NULL")) {
            ps.setLong(1, defaultSummaryIntervalSeconds);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE widget_sync_settings SET summary_auto_enabled = ? WHERE summary_auto_enabled IS NULL")) {
            ps.setBoolean(1, defaultSummaryAutoEnabled);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE widget_sync_settings SET summary_max_rows = ? WHERE summary_max_rows IS NULL")) {
            ps.setInt(1, defaultSummaryMaxRows);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE widget_sync_settings SET summary_max_upstream_entries = ? WHERE summary_max_upstream_entries IS NULL")) {
            ps.setInt(1, defaultSummaryMaxUpstreamEntries);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE widget_sync_settings SET summary_max_message_chars = ? WHERE summary_max_message_chars IS NULL")) {
            ps.setInt(1, defaultSummaryMaxMessageChars);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE widget_sync_settings SET summary_max_request_bytes = ? WHERE summary_max_request_bytes IS NULL")) {
            ps.setInt(1, defaultSummaryMaxRequestBytes);
            ps.executeUpdate();
        }
    }

    private void addColumnIfMissing(Connection conn, String ddl) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.execute();
        }
    }

    private void upsertSyncSettings(Connection conn, SyncSettingsData settings) throws SQLException {
        String sql = SQL_UPSERT_SYNC_SETTINGS;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, settings.intervalSeconds);
            ps.setTimestamp(2, settings.lastSynced);
            ps.setLong(3, settings.summaryIntervalSeconds);
            ps.setBoolean(4, settings.summaryAutoEnabled);
            ps.setString(5, normalizeSummaryPrompt(settings.summaryPrompt));
            ps.setInt(6, settings.summaryMaxRows);
            ps.setInt(7, settings.summaryMaxUpstreamEntries);
            ps.setInt(8, settings.summaryMaxMessageChars);
            ps.setInt(9, settings.summaryMaxRequestBytes);
            ps.setTimestamp(10, settings.summaryLastRun);
            ps.executeUpdate();
        }
    }

    private void ensureTable(Connection conn, String tableName, Set<String> ensuredTables) throws SQLException {
        if (tableName == null || tableName.isBlank()) {
            return;
        }
        if (ensuredTables != null && ensuredTables.contains(tableName)) {
            return;
        }

        String quotedTable = quoteIdentifier(tableName);
        String createTableSql = SQL_CREATE_TABLE_IF_NOT_EXISTS + quotedTable
            + SQL_WIDGET_TABLE_SCHEMA_PREFIX
            + SQL_WIDGET_TABLE_SCHEMA_SUFFIX;
        try (PreparedStatement ps = conn.prepareStatement(createTableSql)) {
            ps.execute();
        }

        String idxName = (tableName + INDEX_SUFFIX_WIDGET_CHAT_ID);
        if (idxName.length() > 63) {
            idxName = safeSlice(idxName, 0, 63);
        }
        String quotedIdx = quoteIdentifier(idxName);

        try (PreparedStatement ps = conn.prepareStatement(SQL_CREATE_UNIQUE_INDEX_IF_NOT_EXISTS + quotedIdx
                + SQL_ON + quotedTable + SQL_WIDGET_CHAT_ID_COLUMNS)) {
            ps.execute();
        } catch (SQLException uniqueErr) {
            log.log(Level.FINE, MSG_UNIQUE_INDEX_CREATION_FAILED, uniqueErr);
            dedupeByWidgetChatId(conn, tableName);
            try (PreparedStatement ps = conn.prepareStatement(SQL_CREATE_UNIQUE_INDEX_IF_NOT_EXISTS + quotedIdx
                    + SQL_ON + quotedTable + SQL_WIDGET_CHAT_ID_COLUMNS)) {
                ps.execute();
            }
        }

        String createdAtIdxName = (tableName + INDEX_SUFFIX_CREATED_AT);
        if (createdAtIdxName.length() > 63) {
            createdAtIdxName = safeSlice(createdAtIdxName, 0, 63);
        }
        String quotedCreatedAtIdx = quoteIdentifier(createdAtIdxName);

        try (PreparedStatement ps = conn.prepareStatement(SQL_CREATE_INDEX_IF_NOT_EXISTS + quotedCreatedAtIdx
                + SQL_ON + quotedTable + SQL_CREATED_AT_INDEX_SUFFIX)) {
            ps.execute();
        } catch (SQLException createdAtIndexErr) {
            log.log(Level.FINE, MSG_CREATED_AT_INDEX_CREATION_FAILED, tableName);
            log.log(Level.FINEST, MSG_CREATED_AT_INDEX_ERROR, createdAtIndexErr);
        }

        if (ensuredTables != null) {
            ensuredTables.add(tableName);
        }
    }

    private void dedupeByWidgetChatId(Connection conn, String tableName) throws SQLException {
        String quoted = quoteIdentifier(tableName);
        String sql = SQL_DELETE_FROM + quoted + SQL_DEDUPE_USING + quoted + SQL_DEDUPE_WIDGET_CHAT_JOIN_WHERE
                + SQL_DEDUPE_NONNULL_CHAT_ID
                + SQL_DEDUPE_KEEP_NEWER
                + SQL_DEDUPE_KEEP_HIGHER_ID_ON_TIE
                + SQL_DEDUPE_NULL_ORDER
                + SQL_DEDUPE_NULL_TIE;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int removed = ps.executeUpdate();
            if (removed > 0) {
                log.log(Level.INFO, MSG_DUPLICATE_ROWS_REMOVED, new Object[]{removed, tableName});
            }
        }
    }

    private int insertWidgetChats(Connection conn, String tableName, List<ChatUpsertRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        Map<String, ChatUpsertRow> uniqueById = new LinkedHashMap<>();
        for (ChatUpsertRow row : rows) {
            if (row == null || row.chatId == null || row.chatId.isBlank()) {
                continue;
            }
            uniqueById.putIfAbsent(row.chatId, row);
        }

        if (uniqueById.isEmpty()) {
            return 0;
        }

        String sql = SQL_INSERT_WIDGET_CHATS_PREFIX + quoteIdentifier(tableName)
            + SQL_INSERT_WIDGET_CHATS_COLUMNS
            + SQL_INSERT_WIDGET_CHATS_VALUES;

        boolean originalAutoCommit;
        int inserted = 0;

        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException(MSG_UNABLE_BEGIN_WIDGET_CHAT_INSERT_PREFIX + tableName, e);
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ChatUpsertRow row : uniqueById.values()) {
                ps.setString(1, row.chatId);
                ps.setString(2, row.prompt);
                ps.setString(3, row.responseText);
                ps.setTimestamp(4, row.createdAt);
                ps.setString(5, row.sessionId);
                ps.setString(6, row.username);
                ps.addBatch();
            }

            int[] batchResult = ps.executeBatch();
            for (int result : batchResult) {
                if (result > 0 || result == Statement.SUCCESS_NO_INFO) {
                    inserted++;
                }
            }
            conn.commit();
            return inserted;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
                log.log(Level.FINE, MSG_ROLLBACK_FAILURE_DETAILS, rollbackError);
            }
            throw new IllegalStateException(MSG_INSERT_WIDGET_CHATS_PREFIX + tableName, e);
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException resetError) {
                log.log(Level.FINE, MSG_AUTOCOMMIT_RESTORE_ERROR, resetError);
            }
        }
    }

    private boolean tableExists(Connection conn, String tableName, Set<String> ensuredTables) {
        if (tableName == null || tableName.isBlank()) {
            return false;
        }
        if (ensuredTables != null && ensuredTables.contains(tableName)) {
            return true;
        }

        try {
            var meta = conn.getMetaData();
            for (String candidate : new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{DB_TABLE_TYPE})) {
                    if (rs.next()) {
                        if (ensuredTables != null) {
                            ensuredTables.add(tableName);
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_UNABLE_INSPECT_TABLE_METADATA_PREFIX + tableName, e);
        }
        return false;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(MSG_INVALID_SQL_IDENTIFIER);
        }
        return '"' + identifier.replace(DOUBLE_QUOTE, DOUBLE_DOUBLE_QUOTE) + '"';
    }

    private long readPersistedIntervalSeconds(ResultSet rs, String columnName, long fallback) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 64).trim();
        if (trimmed.isBlank()) {
            return fallback;
        }
        if (!PERSISTED_POSITIVE_DIGITS.matcher(trimmed).matches()) {
            return fallback;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_INVALID_PERSISTED_SYNC_INTERVAL, ex);
            return fallback;
        }
    }

    private int readPersistedInt(ResultSet rs, String columnName, int fallback) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 32).trim();
        if (trimmed.isBlank() || !PERSISTED_INT_PATTERN.matcher(trimmed).matches()) {
            return fallback;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_INVALID_PERSISTED_INTEGER, ex);
            return fallback;
        }
    }

    private boolean readPersistedBoolean(ResultSet rs, String columnName, boolean fallback) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 16).trim();
        if (trimmed.isBlank()) {
            return fallback;
        }
        if (VALUE_TRUE.equalsIgnoreCase(trimmed) || VALUE_T.equalsIgnoreCase(trimmed) || VALUE_ONE.equals(trimmed)) {
            return true;
        }
        if (VALUE_FALSE.equalsIgnoreCase(trimmed) || VALUE_F.equalsIgnoreCase(trimmed) || VALUE_ZERO.equals(trimmed)) {
            return false;
        }
        return fallback;
    }

    private Timestamp readDbTimestamp(ResultSet rs, String columnName) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return null;
        }
        String normalized = readDbText(rs, columnName, 128).trim();
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return Timestamp.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            try {
                return Timestamp.from(OffsetDateTime.parse(normalized).toInstant());
            } catch (DateTimeParseException ignored) {
                try {
                    return Timestamp.from(Instant.parse(normalized));
                } catch (DateTimeParseException ignoredToo) {
                    log.log(Level.FINE, MSG_UNABLE_PARSE_PERSISTED_TIMESTAMP, ex);
                    return null;
                }
            }
        }
    }

    private String readDbText(ResultSet rs, String columnName, int maxLen) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return EMPTY;
        }
        String text = readRawDbText(rs, columnName, Math.max(maxLen, 1));
        if (text.isBlank()) {
            return EMPTY;
        }
        text = Normalizer.normalize(text, Normalizer.Form.NFKC);
        text = TextIoSanitizerUtil.stripControlCharacters(text);
        if (text.indexOf('\u0000') >= 0) {
            text = text.replace('\u0000', ' ');
        }
        if (maxLen > 0 && text.length() > maxLen) {
            return safeSlice(text, 0, maxLen);
        }
        return text;
    }

    private String readRawDbText(ResultSet rs, String columnName, int maxLen) {
        if (rs == null || columnName == null || columnName.isBlank() || maxLen <= 0) {
            return EMPTY;
        }

        try {
            Reader reader = rs.getCharacterStream(columnName);
            if (reader != null) {
                try (Reader closeable = reader) {
                    String raw = Normalizer.normalize(readAtMostChars(closeable, maxLen), Normalizer.Form.NFKC);
                    return stripAndBoundTaintedDbText(raw, maxLen);
                }
            }
        } catch (SQLException | IOException ex) {
            log.log(Level.FINE, MSG_GET_CHARACTER_STREAM_FAILED_PREFIX + columnName, ex);
        }

        try {
            byte[] bytes = rs.getBytes(columnName);
            if (bytes != null) {
                String raw = Normalizer.normalize(readAtMostBytes(bytes, maxLen), Normalizer.Form.NFKC);
                return stripAndBoundTaintedDbText(raw, maxLen);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_GET_BYTES_FAILED_PREFIX + columnName, ex);
        }

        try {
            String raw = rs.getString(columnName);
            if (raw != null) {
                return stripAndBoundTaintedDbText(Normalizer.normalize(raw, Normalizer.Form.NFKC), maxLen);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_GET_STRING_FAILED_PREFIX + columnName, ex);
        }

        try {
            Object raw = rs.getObject(columnName);
            if (raw != null) {
                return stripAndBoundTaintedDbText(Normalizer.normalize(String.valueOf(raw), Normalizer.Form.NFKC), maxLen);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_GET_OBJECT_FAILED_PREFIX + columnName, ex);
        }

        return EMPTY;
    }

    private String stripAndBoundTaintedDbText(String value, int maxLen) {
        if (value == null || value.isEmpty()) {
            return EMPTY;
        }
        String sanitized = TextIoSanitizerUtil.stripControlCharacters(value);
        if (maxLen > 0 && sanitized.length() > maxLen) {
            return safeSlice(sanitized, 0, maxLen);
        }
        return sanitized;
    }

    private String readAtMostChars(Reader reader, int maxChars) throws IOException {
        if (reader == null || maxChars <= 0) {
            return EMPTY;
        }
        char[] buf = new char[1024];
        StringBuilder sb = new StringBuilder(Math.min(maxChars, 2048));
        int total = 0;
        int read;
        while ((read = reader.read(buf)) != -1) {
            if (read <= 0) {
                continue;
            }
            int appendLen = Math.min(read, maxChars - total);
            if (appendLen <= 0) {
                break;
            }
            sb.append(buf, 0, appendLen);
            total += appendLen;
            if (total >= maxChars) {
                break;
            }
        }
        return sb.toString();
    }

    private String readAtMostBytes(byte[] bytes, int maxBytes) {
        if (bytes == null || bytes.length == 0 || maxBytes <= 0) {
            return EMPTY;
        }
        int length = Math.min(bytes.length, maxBytes);
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }


    private String normalizeSummaryPrompt(String prompt) {
        String normalized = prompt == null ? EMPTY : com.sim.chatserver.util.LineSeparatorUtil.normalizeToLf(prompt)
            .trim();
        if (maxSummaryPromptChars > 0 && normalized.length() > maxSummaryPromptChars) {
            normalized = safeSlice(normalized, 0, maxSummaryPromptChars);
        }
        return normalized;
    }

    private static String safeSlice(String value, int beginIndex, int endIndex) {
        if (value == null || value.isEmpty()) {
            return EMPTY;
        }
        int safeBegin = Math.max(0, beginIndex);
        int safeEnd = Math.max(safeBegin, Math.min(endIndex, value.length()));
        int length = safeEnd - safeBegin;
        if (length <= 0) {
            return EMPTY;
        }
        char[] chars = value.toCharArray();
        return new String(chars, safeBegin, length);
    }

}

final class ChatUpsertRow {
    final String chatId;
    final String prompt;
    final String responseText;
    final Timestamp createdAt;
    final String sessionId;
    final String username;

    ChatUpsertRow(
            String chatId,
            String prompt,
            String responseText,
            Timestamp createdAt,
            String sessionId,
            String username
    ) {
        this.chatId = chatId == null ? WidgetSyncJdbcStore.EMPTY : chatId;
        this.prompt = prompt == null ? WidgetSyncJdbcStore.EMPTY : prompt;
        this.responseText = responseText == null ? WidgetSyncJdbcStore.EMPTY : responseText;
        this.createdAt = createdAt;
        this.sessionId = sessionId == null ? WidgetSyncJdbcStore.EMPTY : sessionId;
        this.username = username == null ? WidgetSyncJdbcStore.EMPTY : username;
    }
}

final class SyncSettingsData {
    final long intervalSeconds;
    final Timestamp lastSynced;
    final long summaryIntervalSeconds;
    final boolean summaryAutoEnabled;
    final String summaryPrompt;
    final int summaryMaxRows;
    final int summaryMaxUpstreamEntries;
    final int summaryMaxMessageChars;
    final int summaryMaxRequestBytes;
    final Timestamp summaryLastRun;

    SyncSettingsData(
            long intervalSeconds,
            Timestamp lastSynced,
            long summaryIntervalSeconds,
            boolean summaryAutoEnabled,
            String summaryPrompt,
            int summaryMaxRows,
            int summaryMaxUpstreamEntries,
            int summaryMaxMessageChars,
            int summaryMaxRequestBytes,
            Timestamp summaryLastRun
    ) {
        this.intervalSeconds = intervalSeconds;
        this.lastSynced = lastSynced;
        this.summaryIntervalSeconds = summaryIntervalSeconds;
        this.summaryAutoEnabled = summaryAutoEnabled;
        this.summaryPrompt = summaryPrompt == null ? WidgetSyncJdbcStore.EMPTY : summaryPrompt;
        this.summaryMaxRows = summaryMaxRows;
        this.summaryMaxUpstreamEntries = summaryMaxUpstreamEntries;
        this.summaryMaxMessageChars = summaryMaxMessageChars;
        this.summaryMaxRequestBytes = summaryMaxRequestBytes;
        this.summaryLastRun = summaryLastRun;
    }
}


