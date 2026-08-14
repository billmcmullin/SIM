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

import jakarta.json.JsonObject;

final class WidgetSyncJdbcStore {

    private static final Logger log = Logger.getLogger(WidgetSyncJdbcStore.class.getName());
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,62}$");

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
            throw new IllegalArgumentException("Sync settings defaults are required.");
        }

        try (Connection conn = openConnection(holder)) {
            ensureSyncSettingsTable(conn);

            String sql = "SELECT interval_seconds, last_synced, summary_interval_seconds, summary_auto_enabled, summary_prompt, summary_max_rows, "
                    + "summary_max_upstream_entries, summary_max_message_chars, summary_max_request_bytes, summary_last_run"
                    + " FROM widget_sync_settings WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long persistedInterval = readPersistedIntervalSeconds(rs, "interval_seconds", defaults.intervalSeconds);
                    long persistedSummaryInterval = readPersistedIntervalSeconds(
                            rs,
                            "summary_interval_seconds",
                            defaults.summaryIntervalSeconds
                    );
                    boolean persistedSummaryAutoEnabled = readPersistedBoolean(
                            rs,
                            "summary_auto_enabled",
                            defaults.summaryAutoEnabled
                    );
                    int persistedSummaryMaxRows = readPersistedInt(rs, "summary_max_rows", defaults.summaryMaxRows);
                    int persistedSummaryMaxUpstreamEntries = readPersistedInt(
                            rs,
                            "summary_max_upstream_entries",
                            defaults.summaryMaxUpstreamEntries
                    );
                    int persistedSummaryMaxMessageChars = readPersistedInt(
                            rs,
                            "summary_max_message_chars",
                            defaults.summaryMaxMessageChars
                    );
                    int persistedSummaryMaxRequestBytes = readPersistedInt(
                            rs,
                            "summary_max_request_bytes",
                            defaults.summaryMaxRequestBytes
                    );

                    String persistedSummaryPrompt = readDbText(rs, "summary_prompt", maxSummaryPromptChars);
                    if (persistedSummaryPrompt.isBlank()) {
                        persistedSummaryPrompt = defaults.summaryPrompt;
                    }

                    Timestamp persistedLastSynced = readDbTimestamp(rs, "last_synced");
                    Timestamp persistedSummaryLastRun = readDbTimestamp(rs, "summary_last_run");

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
            throw new IllegalStateException("Unable to read sync settings", e);
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
            throw new IllegalStateException("Unable to persist sync settings", e);
        }
    }

    void ensureWidgetTable(AppDataSourceHolder holder, String tableName, Set<String> ensuredTables) {
        if (tableName == null || tableName.isBlank()) {
            return;
        }
        try (Connection conn = openConnection(holder)) {
            ensureTable(conn, tableName, ensuredTables);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to ensure widget table " + tableName, e);
        }
    }

    int insertWidgetChats(AppDataSourceHolder holder, String tableName, List<ChatUpsertRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        try (Connection conn = openConnection(holder)) {
            return insertWidgetChats(conn, tableName, rows);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to insert widget chats into table " + tableName, e);
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

                String sql = "SELECT widget_chat_id, prompt, response_text, created_at, session_id FROM "
                        + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ? ORDER BY created_at DESC LIMIT ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, start);
                    ps.setTimestamp(2, end);
                    ps.setInt(3, remainingRows);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (out.size() >= maxRows) {
                                return out;
                            }

                            String chatId = readDbText(rs, "widget_chat_id", 256);
                            String prompt = readDbText(rs, "prompt", 8000);
                            String response = readDbText(rs, "response_text", 32000);
                            Timestamp createdAt = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            String sessionId = readDbText(rs, "session_id", 256);

                            out.add(new SelectedEntry(
                                    chatId == null ? "" : chatId,
                                    prompt == null ? "" : prompt,
                                    response == null ? "" : response,
                                    createdAt == null ? "" : createdAt.toInstant().toString(),
                                    sessionId == null ? "" : sessionId
                            ));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load day entries", e);
        }

        return out;
    }

    private DataSource requireDataSource(AppDataSourceHolder holder) {
        if (holder == null || holder.getDataSource() == null) {
            throw new IllegalStateException("Data source holder is unavailable.");
        }
        return holder.getDataSource();
    }

    private Connection openConnection(AppDataSourceHolder holder) throws SQLException {
        return requireDataSource(holder).getConnection();
    }

    private void ensureSyncSettingsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS widget_sync_settings ("
                + "id INTEGER PRIMARY KEY, interval_seconds BIGINT NOT NULL, last_synced TIMESTAMP, "
                + "summary_interval_seconds BIGINT NOT NULL DEFAULT " + defaultSummaryIntervalSeconds + ", "
                + "summary_auto_enabled BOOLEAN NOT NULL DEFAULT TRUE, "
                + "summary_prompt TEXT, "
                + "summary_max_rows INTEGER NOT NULL DEFAULT " + defaultSummaryMaxRows + ", "
                + "summary_max_upstream_entries INTEGER NOT NULL DEFAULT " + defaultSummaryMaxUpstreamEntries + ", "
                + "summary_max_message_chars INTEGER NOT NULL DEFAULT " + defaultSummaryMaxMessageChars + ", "
                + "summary_max_request_bytes INTEGER NOT NULL DEFAULT " + defaultSummaryMaxRequestBytes + ", "
                + "summary_last_run TIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        }

        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_interval_seconds BIGINT");
        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_auto_enabled BOOLEAN");
        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_prompt TEXT");
        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_rows INTEGER");
        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_upstream_entries INTEGER");
        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_message_chars INTEGER");
        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_max_request_bytes INTEGER");
        addColumnIfMissing(conn, "ALTER TABLE widget_sync_settings ADD COLUMN IF NOT EXISTS summary_last_run TIMESTAMP");

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
        String sql = "INSERT INTO widget_sync_settings (id, interval_seconds, last_synced, summary_interval_seconds, summary_auto_enabled, summary_prompt, summary_max_rows, "
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
        String createTableSql = "CREATE TABLE IF NOT EXISTS " + quotedTable
                + " (db_id BIGSERIAL PRIMARY KEY, widget_chat_id TEXT, prompt TEXT, response_text TEXT, "
                + "created_at TIMESTAMP, session_id TEXT, username TEXT)";
        try (PreparedStatement ps = conn.prepareStatement(createTableSql)) {
            ps.execute();
        }

        String idxName = (tableName + "_widget_chat_id_uidx");
        if (idxName.length() > 63) {
            idxName = idxName.substring(0, 63);
        }
        String quotedIdx = quoteIdentifier(idxName);

        try (PreparedStatement ps = conn.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS " + quotedIdx
                + " ON " + quotedTable + " (widget_chat_id)")) {
            ps.execute();
        } catch (SQLException uniqueErr) {
            log.log(Level.FINE, "Unique index creation failed, attempting dedupe before retry", uniqueErr);
            dedupeByWidgetChatId(conn, tableName);
            try (PreparedStatement ps = conn.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS " + quotedIdx
                    + " ON " + quotedTable + " (widget_chat_id)")) {
                ps.execute();
            }
        }

        String createdAtIdxName = (tableName + "_created_at_idx");
        if (createdAtIdxName.length() > 63) {
            createdAtIdxName = createdAtIdxName.substring(0, 63);
        }
        String quotedCreatedAtIdx = quoteIdentifier(createdAtIdxName);

        try (PreparedStatement ps = conn.prepareStatement("CREATE INDEX IF NOT EXISTS " + quotedCreatedAtIdx
                + " ON " + quotedTable + " (created_at DESC)")) {
            ps.execute();
        } catch (SQLException createdAtIndexErr) {
            log.log(Level.FINE, "Created_at index creation failed for {0}", tableName);
            log.log(Level.FINEST, "Created_at index error", createdAtIndexErr);
        }

        if (ensuredTables != null) {
            ensuredTables.add(tableName);
        }
    }

    private void dedupeByWidgetChatId(Connection conn, String tableName) throws SQLException {
        String quoted = quoteIdentifier(tableName);
        String sql = "DELETE FROM " + quoted + " a USING " + quoted + " b WHERE a.widget_chat_id = b.widget_chat_id "
                + "AND a.widget_chat_id IS NOT NULL "
                + "AND (a.created_at < b.created_at "
                + "OR (a.created_at = b.created_at AND a.db_id < b.db_id) "
                + "OR (a.created_at IS NULL AND b.created_at IS NOT NULL) "
                + "OR (a.created_at IS NULL AND b.created_at IS NULL AND a.db_id < b.db_id))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int removed = ps.executeUpdate();
            if (removed > 0) {
                log.log(Level.INFO, "Removed {0} duplicate rows from {1}", new Object[]{removed, tableName});
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

        String sql = "INSERT INTO " + quoteIdentifier(tableName)
                + " (widget_chat_id, prompt, response_text, created_at, session_id, username) "
                + "VALUES (?,?,?,?,?,?) ON CONFLICT (widget_chat_id) DO NOTHING";

        boolean originalAutoCommit;
        int inserted = 0;

        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to begin widget chat insert transaction for table " + tableName, e);
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
                log.log(Level.FINE, "Rollback failure details", rollbackError);
            }
            throw new IllegalStateException("Unable to insert widget chats into table " + tableName, e);
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException resetError) {
                log.log(Level.FINE, "Auto-commit restore error", resetError);
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
                try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        if (ensuredTables != null) {
                            ensuredTables.add(tableName);
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to inspect table metadata for " + tableName, e);
        }
        return false;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private long readPersistedIntervalSeconds(ResultSet rs, String columnName, long fallback) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 64).trim();
        if (trimmed.isBlank()) {
            return fallback;
        }
        if (!trimmed.matches("^\\d{1,12}$")) {
            return fallback;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid persisted sync interval value", ex);
            return fallback;
        }
    }

    private int readPersistedInt(ResultSet rs, String columnName, int fallback) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return fallback;
        }
        String trimmed = readDbText(rs, columnName, 32).trim();
        if (trimmed.isBlank() || !trimmed.matches("^-?\\d{1,10}$")) {
            return fallback;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid persisted integer value", ex);
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
        if ("true".equalsIgnoreCase(trimmed) || "t".equalsIgnoreCase(trimmed) || "1".equals(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed) || "f".equalsIgnoreCase(trimmed) || "0".equals(trimmed)) {
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
                    log.log(Level.FINE, "Unable to parse persisted timestamp value", ex);
                    return null;
                }
            }
        }
    }

    private String readDbText(ResultSet rs, String columnName, int maxLen) {
        if (rs == null || columnName == null || columnName.isBlank()) {
            return "";
        }
        String text = readRawDbText(rs, columnName, Math.max(maxLen, 1));
        if (text.isBlank()) {
            return "";
        }
        text = Normalizer.normalize(text, Normalizer.Form.NFKC);
        text = stripControlCharacters(text);
        if (text.indexOf('\u0000') >= 0) {
            text = text.replace('\u0000', ' ');
        }
        if (maxLen > 0 && text.length() > maxLen) {
            return text.substring(0, maxLen);
        }
        return text;
    }

    private String readRawDbText(ResultSet rs, String columnName, int maxLen) {
        if (rs == null || columnName == null || columnName.isBlank() || maxLen <= 0) {
            return "";
        }

        try {
            Reader reader = rs.getCharacterStream(columnName);
            if (reader != null) {
                try (Reader closeable = reader) {
                    return readAtMostChars(closeable, maxLen);
                }
            }
        } catch (SQLException | IOException ex) {
            log.log(Level.FINE, "ResultSet#getCharacterStream failed for column " + columnName, ex);
        }

        try {
            byte[] bytes = rs.getBytes(columnName);
            return readAtMostBytes(bytes, maxLen);
        } catch (SQLException ex) {
            log.log(Level.FINE, "ResultSet#getBytes failed for column " + columnName, ex);
            return "";
        }
    }

    private String readAtMostChars(Reader reader, int maxChars) throws IOException {
        if (reader == null || maxChars <= 0) {
            return "";
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
            return "";
        }
        int length = Math.min(bytes.length, maxBytes);
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }

    private String stripControlCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder sanitized = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t' || !Character.isISOControl(c)) {
                sanitized.append(c);
            }
        }
        return sanitized.toString();
    }

    private String normalizeSummaryPrompt(String prompt) {
        String normalized = prompt == null ? "" : prompt.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (maxSummaryPromptChars > 0 && normalized.length() > maxSummaryPromptChars) {
            normalized = normalized.substring(0, maxSummaryPromptChars);
        }
        return normalized;
    }

    static final class ChatUpsertRow {
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
            this.chatId = chatId == null ? "" : chatId;
            this.prompt = prompt == null ? "" : prompt;
            this.responseText = responseText == null ? "" : responseText;
            this.createdAt = createdAt;
            this.sessionId = sessionId == null ? "" : sessionId;
            this.username = username == null ? "" : username;
        }
    }

    static final class SyncSettingsData {
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
            this.summaryPrompt = summaryPrompt == null ? "" : summaryPrompt;
            this.summaryMaxRows = summaryMaxRows;
            this.summaryMaxUpstreamEntries = summaryMaxUpstreamEntries;
            this.summaryMaxMessageChars = summaryMaxMessageChars;
            this.summaryMaxRequestBytes = summaryMaxRequestBytes;
            this.summaryLastRun = summaryLastRun;
        }
    }
}
