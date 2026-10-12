package com.sim.chatserver.web.dashboard.summary;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Persistent store for daily dashboard summary generation status + result
 * payload.
 */
public class DashboardDailySummaryStore {
    private static final Logger log = Logger.getLogger(DashboardDailySummaryStore.class.getCanonicalName());
    private static final DateTimeFormatter UI_TS_FMT = DateTimeFormatter.ofPattern(com.sim.chatserver.util.CommonLiteralConstants.DATETIME_PATTERN_SECONDS);
    private static final String EMPTY = "";
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String PG_UNIQUE_VIOLATION = "23505";
    private static final LocalDate MIN_ALLOWED_DAY = LocalDate.of(1970, 1, 1);
    private static final LocalDate MAX_ALLOWED_DAY = LocalDate.of(3000, 12, 31);
    private static final String KEY_QUALITY = "quality";
    private static final String KEY_USAGE = "usage";
    private static final String KEY_ENTRY_COUNT = CommonLiteralConstants.FIELD_ENTRY_COUNT;
    private static final String KEY_META = CommonLiteralConstants.FIELD_META;
    private static final String KEY_SUMMARY = com.sim.chatserver.util.CommonLiteralConstants.LABEL_SUMMARY;
    private static final String KEY_OVERALL = "overall";
    private static final String KEY_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE;
    private static final String KEY_SUGGESTED_NEXT_ACTION = "suggestedNextAction";
    private static final String KEY_DAY = com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY;
    private static final String KEY_SLOT = com.sim.chatserver.util.CommonLiteralConstants.KEY_SLOT;
    static final String KEY_GENERATED_AT = "generatedAt";
    private static final String KEY_STARTED_AT = "startedAt";
    private static final String KEY_UPDATED_AT = com.sim.chatserver.util.CommonLiteralConstants.FIELD_UPDATED_AT;
    private static final String KEY_IN_PROGRESS = "inProgress";
    private static final String KEY_FROM_FALLBACK = "fromFallback";
    private static final String KEY_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
    private static final String KEY_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String KEY_PROGRESS_PCT = CommonLiteralConstants.FIELD_PROGRESS_PCT;
    private static final String KEY_STATUS_TEXT = CommonLiteralConstants.FIELD_STATUS_TEXT;
    private static final String STATUS_OK = "ok";
    private static final String STATUS_ERROR = com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR;
    private static final String STATUS_IDLE = com.sim.chatserver.util.CommonLiteralConstants.STATUS_IDLE;
    private static final String STATUS_QUEUED = "queued";
    private static final String STATUS_RUNNING = com.sim.chatserver.util.CommonLiteralConstants.STATUS_RUNNING;
    private static final String STATUS_SUCCESS = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SUCCESS;
    private static final String PLACEHOLDER_DASH = com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA;
    private static final String MSG_NO_SUMMARY_YET = "No summary has been generated yet.";
    private static final String MSG_WAITING_FOR_SCHEDULED = "Waiting for scheduled generation.";
    private static final String MSG_SUMMARY_IN_PROGRESS = "Summary generation in progress.";
    static final String MSG_UNABLE_TO_LOAD_SUMMARY = "Unable to load summary.";
    private static final String MSG_UNABLE_TO_ENSURE_TABLE = "Unable to ensure dashboard_daily_summary table";
    private static final String MSG_UNABLE_TO_ENSURE_SUGGESTED_COLUMN = "Unable to ensure suggested_next_action column";
    private static final String MSG_DETECTED_STALE_SEQUENCE_AND_RETRY = "Detected stale dashboard_daily_summary id sequence after import. Realigned sequence and retrying upsert.";
    private static final String MSG_RETRY_AFTER_SEQUENCE_REALIGN_FAILED = "Retry after sequence realignment failed for dashboard_daily_summary upsert";
    private static final String MSG_UNABLE_REALIGN_SEQUENCE = "Unable to realign dashboard_daily_summary id sequence";
    private static final String MSG_UNABLE_FETCH_DAILY_SUMMARY = "Unable to fetch dashboard daily summary";
    private static final String MSG_CHARACTER_STREAM_DECODE_FAILED_COL = "Character stream decode failed for column ";
    private static final String MSG_CHARACTER_STREAM_DECODE_FAILED_COL_INDEX = "Character stream decode failed for column index ";
    private static final String MSG_RS_GET_CHARACTER_STREAM_COL = "ResultSet#getCharacterStream(String) failed for column ";
    private static final String MSG_RS_GET_CHARACTER_STREAM_COL_INDEX = "ResultSet#getCharacterStream(int) failed for column index ";
    private static final String MSG_RS_GET_BYTES_COL = "ResultSet#getBytes(String) failed for column ";
    private static final String MSG_RS_GET_BYTES_COL_INDEX = "ResultSet#getBytes(int) failed for column index ";
    private static final String MSG_RS_GET_STRING_COL = "ResultSet#getString(String) failed for column ";
    private static final String MSG_RS_GET_STRING_COL_INDEX = "ResultSet#getString(int) failed for column index ";
    private static final String MSG_RS_GET_OBJECT_COL = "ResultSet#getObject(String) failed for column ";
    private static final String MSG_RS_GET_OBJECT_COL_INDEX = "ResultSet#getObject(int) failed for column index ";
    private static final String MSG_UNABLE_PARSE_INT_FOR_COL = "Unable to parse integer value for column ";
    private static final String MSG_TS_SQL_PARSE_FALLBACK = "Timestamp SQL parse fallback to Instant parser for column ";
    private static final String MSG_INVALID_TS_FOR_COL = "Invalid timestamp value for column ";
    private static final String MSG_INVALID_DATE_FOR_COL = "Invalid date value for column ";
    private static final String MSG_OUT_OF_RANGE_TS_FOR_COL = "Out-of-range timestamp value for column ";
    private static final String UNKNOWN_COLUMN = "<unknown>";
    private static final String MSG_SUMMARY_STILL_GENERATING = "Summary is still generating. Wait for completion, then review low-performing areas and rerun checks.";
    private static final String MSG_REVIEW_LOW_QUALITY = "Review low-quality conversations first and tighten prompt instructions/guardrails for affected widgets.";
    private static final String MSG_INVESTIGATE_LATENCY = "Investigate response latency by widget and reduce prompt/context size where possible.";
    private static final String MSG_PROMOTE_UNDERUSED = "Promote underused high-value widgets and add clearer in-app guidance for users.";
    private static final String TERM_LOW = com.sim.chatserver.util.CommonLiteralConstants.VALUE_LOW;
    private static final String TERM_SLOW = "slow";
    private static final String TERM_INCORRECT = "incorrect";
    private static final String TERM_POOR = "poor";
    private static final String TERM_INCONSISTENT = "inconsistent";
    private static final String TERM_HALLUCINATION = "hallucination";
    private static final String TERM_DELAYED = "delayed";
    private static final String TERM_TIMEOUT = "timeout";
    private static final String TERM_LATENCY = "latency";
    private static final String TERM_UNDERUSED = "underused";
    private static final String TERM_DECLINE = "decline";
    private static final String TERM_DROP = "drop";
    private static final String PK_DAILY_SUMMARY = "dashboard_daily_summary_pkey";
    private static final String PK_ID_PREFIX = "(id)=";
    private static final String SQL_REALIGN_SEQ = "SELECT setval(?::regclass, COALESCE((SELECT MAX(id) FROM dashboard_daily_summary), 0) + 1, false)";
    private static final String REGEX_SIGNED_INT = "^-?\\d+$";

    private static final String DB_COL_STATUS = com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS;
    private static final String DB_COL_PROGRESS_PCT = "progress_pct";
    private static final String DB_COL_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE;
    private static final String DB_COL_SUMMARY_OVERALL = "summary_overall";
    private static final String DB_COL_SUMMARY_QUALITY = "summary_quality";
    private static final String DB_COL_SUMMARY_RESPONSE = "summary_response";
    private static final String DB_COL_SUMMARY_USAGE = "summary_usage";
    private static final String DB_COL_SUGGESTED_NEXT_ACTION = "suggested_next_action";
    private static final String DB_COL_ENTRY_COUNT = "entry_count";
    private static final String DB_COL_STARTED_AT = "started_at";
    private static final String DB_COL_GENERATED_AT = "generated_at";
    private static final String DB_COL_UPDATED_AT = com.sim.chatserver.util.CommonLiteralConstants.SQL_COL_UPDATED_AT;
    private static final String DB_COL_SUMMARY_DAY = "summary_day";
    private static final String DB_COL_SLOT = com.sim.chatserver.util.CommonLiteralConstants.KEY_SLOT;
    private static final String SUGGESTED_NEXT_ACTION_DEFAULT = "Review Top Terms and Latest Chats to identify one repeated issue and apply a focused prompt update.";
    private static final String ERROR_UPSERT_DAILY_SUMMARY = "Unable to upsert dashboard daily summary row";

    private static final String TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS dashboard_daily_summary (
                id BIGSERIAL PRIMARY KEY,
                summary_day DATE NOT NULL,
                slot INTEGER NOT NULL,
                status VARCHAR(20) NOT NULL,
                progress_pct INTEGER NOT NULL DEFAULT 0,
                message TEXT,
                summary_overall TEXT,
                summary_quality TEXT,
                summary_response TEXT,
                summary_usage TEXT,
                suggested_next_action TEXT,
                entry_count INTEGER NOT NULL DEFAULT 0,
                started_at TIMESTAMP,
                generated_at TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                UNIQUE (summary_day, slot)
            )
            """;

    private final DataSource dataSource;

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    public DashboardDailySummaryStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void ensureTable() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(TABLE_SQL)) {
            ps.execute();
            ensureSuggestedNextActionColumn(conn);
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.SEVERE, MSG_UNABLE_TO_ENSURE_TABLE, e);
            throw new IllegalStateException(MSG_UNABLE_TO_ENSURE_TABLE, e);
        }
    }

    private void ensureSuggestedNextActionColumn(Connection conn) {
        final String sql = """
                ALTER TABLE dashboard_daily_summary
                ADD COLUMN IF NOT EXISTS suggested_next_action TEXT
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, MSG_UNABLE_TO_ENSURE_SUGGESTED_COLUMN, e);
        }
    }

    public void upsertProgress(
            LocalDate day,
            int slot,
            String status,
            int progressPct,
            String message,
            int entryCount,
            boolean markStarted,
            boolean markGenerated
    ) {
        upsert(day, slot, status, progressPct, message, null, null, null, null, null, entryCount, markStarted, markGenerated);
    }

    public void upsertSummary(
            LocalDate day,
            int slot,
            String status,
            int progressPct,
            String message,
            String overall,
            String quality,
            String response,
            String usage,
            int entryCount,
            boolean markStarted,
            boolean markGenerated
    ) {
        String suggested = suggestNextAction(status, quality, response, usage);
        upsert(day, slot, status, progressPct, message, overall, quality, response, usage, suggested, entryCount, markStarted, markGenerated);
    }

    final void upsertSummary(
            LocalDate day,
            int slot,
            String status,
            int progressPct,
            String message,
            String overall,
            String quality,
            String response,
            String usage,
            String suggestedNextAction,
            int entryCount,
            boolean markStarted,
            boolean markGenerated
    ) {
        String suggested = (suggestedNextAction == null || suggestedNextAction.isBlank())
                ? suggestNextAction(status, quality, response, usage)
                : suggestedNextAction.trim();
        upsert(day, slot, status, progressPct, message, overall, quality, response, usage, suggested, entryCount, markStarted, markGenerated);
    }

    private void upsert(
            LocalDate day,
            int slot,
            String status,
            int progressPct,
            String message,
            String overall,
            String quality,
            String response,
            String usage,
            String suggestedNextAction,
            int entryCount,
            boolean markStarted,
            boolean markGenerated
    ) {
        String sql = """
                INSERT INTO dashboard_daily_summary
                    (summary_day, slot, status, progress_pct, message,
                     summary_overall, summary_quality, summary_response, summary_usage, suggested_next_action,
                     entry_count, started_at, generated_at, updated_at)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                ON CONFLICT (summary_day, slot)
                DO UPDATE SET
                    status = EXCLUDED.status,
                    progress_pct = EXCLUDED.progress_pct,
                    message = EXCLUDED.message,
                    summary_overall = COALESCE(EXCLUDED.summary_overall, dashboard_daily_summary.summary_overall),
                    summary_quality = COALESCE(EXCLUDED.summary_quality, dashboard_daily_summary.summary_quality),
                    summary_response = COALESCE(EXCLUDED.summary_response, dashboard_daily_summary.summary_response),
                    summary_usage = COALESCE(EXCLUDED.summary_usage, dashboard_daily_summary.summary_usage),
                    suggested_next_action = COALESCE(EXCLUDED.suggested_next_action, dashboard_daily_summary.suggested_next_action),
                    entry_count = EXCLUDED.entry_count,
                    started_at = COALESCE(dashboard_daily_summary.started_at, EXCLUDED.started_at),
                    generated_at = CASE
                        WHEN EXCLUDED.generated_at IS NOT NULL THEN EXCLUDED.generated_at
                        ELSE dashboard_daily_summary.generated_at
                    END,
                    updated_at = NOW()
                """;

        try {
            try (Connection conn = dataSource.getConnection()) {
                executeUpsert(conn, sql, day, slot, status, progressPct, message, overall, quality, response, usage, suggestedNextAction, entryCount, markStarted, markGenerated);
            }
        } catch (SQLException firstSqlError) {
            if (isDashboardSummaryIdDuplicate(firstSqlError) && realignDashboardSummaryIdSequence()) {
                log.log(Level.INFO, MSG_DETECTED_STALE_SEQUENCE_AND_RETRY);
                try (Connection retryConn = dataSource.getConnection()) {
                    executeUpsert(retryConn, sql, day, slot, status, progressPct, message, overall, quality, response, usage, suggestedNextAction, entryCount, markStarted, markGenerated);
                    return;
                } catch (SQLException retrySqlError) {
                    log.log(Level.WARNING, MSG_RETRY_AFTER_SEQUENCE_REALIGN_FAILED, retrySqlError);
                    throw new IllegalStateException(ERROR_UPSERT_DAILY_SUMMARY, retrySqlError);
                }
            }

            log.log(Level.WARNING, ERROR_UPSERT_DAILY_SUMMARY, firstSqlError);
            throw new IllegalStateException(ERROR_UPSERT_DAILY_SUMMARY, firstSqlError);
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            log.log(Level.WARNING, ERROR_UPSERT_DAILY_SUMMARY, e);
            throw new IllegalStateException(ERROR_UPSERT_DAILY_SUMMARY, e);
        }
    }

    private void executeUpsert(
            Connection conn,
            String sql,
            LocalDate day,
            int slot,
            String status,
            int progressPct,
            String message,
            String overall,
            String quality,
            String response,
            String usage,
            String suggestedNextAction,
            int entryCount,
            boolean markStarted,
            boolean markGenerated
    ) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Timestamp now = Timestamp.from(Instant.now());
            Timestamp startedAt = markStarted ? now : null;
            Timestamp generatedAt = markGenerated ? now : null;

            ps.setDate(1, java.sql.Date.valueOf(day));
            ps.setInt(2, slot);
            ps.setString(3, normalizeStatus(status));
            ps.setInt(4, clamp(progressPct));
            ps.setString(5, message == null ? EMPTY : message);

            ps.setString(6, blankToNull(overall));
            ps.setString(7, blankToNull(quality));
            ps.setString(8, blankToNull(response));
            ps.setString(9, blankToNull(usage));
            ps.setString(10, blankToNull(suggestedNextAction));

            ps.setInt(11, Math.max(0, entryCount));
            ps.setTimestamp(12, startedAt);
            ps.setTimestamp(13, generatedAt);

            ps.executeUpdate();
        }
    }

    private boolean isDashboardSummaryIdDuplicate(SQLException e) {
        for (SQLException cur = e; cur != null; cur = cur.getNextException()) {
            String sqlState = cur.getSQLState();
            String msg = cur.getMessage();
            if (PG_UNIQUE_VIOLATION.equals(sqlState)
                    && msg != null
                    && msg.contains(PK_DAILY_SUMMARY)
                    && msg.contains(PK_ID_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    private boolean realignDashboardSummaryIdSequence() {
        final String seqLookupSql = "SELECT pg_get_serial_sequence('dashboard_daily_summary', 'id')";

        try (Connection conn = dataSource.getConnection(); PreparedStatement lookupPs = conn.prepareStatement(seqLookupSql); ResultSet rs = lookupPs.executeQuery()) {
            if (!rs.next()) {
                return false;
            }

            String sequenceName = getSafeString(rs, 1, 256);
            if (sequenceName == null || sequenceName.isBlank()) {
                return false;
            }

            String setSeqSql = SQL_REALIGN_SEQ;
            try (PreparedStatement setPs = conn.prepareStatement(setSeqSql)) {
                setPs.setString(1, sequenceName);
                setPs.execute();
            }
            return true;
        } catch (SQLException e) {
            log.log(Level.WARNING, MSG_UNABLE_REALIGN_SEQUENCE, e);
            return false;
        }
    }

    final JsonObject fetchExactOrLatest(LocalDate day, int slot) {
        String exactSql = """
                SELECT summary_day, slot, status, progress_pct, message,
                       summary_overall, summary_quality, summary_response, summary_usage, suggested_next_action,
                       entry_count, started_at, generated_at, updated_at
                FROM dashboard_daily_summary
                WHERE summary_day = ? AND slot = ?
                ORDER BY updated_at DESC
                LIMIT 1
                """;

        String latestSql = """
                SELECT summary_day, slot, status, progress_pct, message,
                       summary_overall, summary_quality, summary_response, summary_usage, suggested_next_action,
                       entry_count, started_at, generated_at, updated_at
                FROM dashboard_daily_summary
                ORDER BY summary_day DESC, slot DESC, updated_at DESC
                LIMIT 1
                """;

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(exactSql)) {
                ps.setDate(1, java.sql.Date.valueOf(day));
                ps.setInt(2, slot);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return toPayload(rs, false);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(latestSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toPayload(rs, true);
                }
            }

            return Json.createObjectBuilder()
                    .add(KEY_STATUS, STATUS_OK)
                    .add(KEY_SUMMARY, Json.createObjectBuilder()
                        .add(KEY_OVERALL, MSG_NO_SUMMARY_YET)
                        .add(KEY_QUALITY, PLACEHOLDER_DASH)
                        .add(KEY_RESPONSE, PLACEHOLDER_DASH)
                        .add(KEY_USAGE, PLACEHOLDER_DASH)
                        .add(KEY_SUGGESTED_NEXT_ACTION, SUGGESTED_NEXT_ACTION_DEFAULT)
                            .add(KEY_ENTRY_COUNT, 0))
                        .add(KEY_META, Json.createObjectBuilder()
                            .add(KEY_DAY, day.toString())
                            .add(KEY_SLOT, slot)
                            .add(KEY_GENERATED_AT, EMPTY)
                            .add(KEY_STARTED_AT, EMPTY)
                            .add(KEY_UPDATED_AT, EMPTY)
                            .add(KEY_IN_PROGRESS, false)
                            .add(KEY_PROGRESS_PCT, 0)
                                .add(KEY_STATUS_TEXT, STATUS_IDLE)
                                .add(KEY_MESSAGE, MSG_WAITING_FOR_SCHEDULED)
                            .add(KEY_FROM_FALLBACK, false))
                    .build();

        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, MSG_UNABLE_FETCH_DAILY_SUMMARY, e);
            return Json.createObjectBuilder()
                    .add(KEY_STATUS, STATUS_ERROR)
                    .add(KEY_MESSAGE, MSG_UNABLE_TO_LOAD_SUMMARY)
                    .build();
        }
    }

    private JsonObject toPayload(ResultSet rs, boolean fromFallback) throws SQLException {
        String status = value(getSafeString(rs, DB_COL_STATUS, 20), STATUS_IDLE).toLowerCase();
        int progress = clamp(getSafeInt(rs, DB_COL_PROGRESS_PCT, 0, 100));
        String message = value(getSafeString(rs, DB_COL_MESSAGE, 2000), EMPTY);

        String overall = value(getSafeString(rs, DB_COL_SUMMARY_OVERALL, 8000), EMPTY);
        String quality = value(getSafeString(rs, DB_COL_SUMMARY_QUALITY, 4000), EMPTY);
        String response = value(getSafeString(rs, DB_COL_SUMMARY_RESPONSE, 4000), EMPTY);
        String usage = value(getSafeString(rs, DB_COL_SUMMARY_USAGE, 4000), EMPTY);
        String suggested = value(getSafeString(rs, DB_COL_SUGGESTED_NEXT_ACTION, 2000), EMPTY);
        int entryCount = getSafeInt(rs, DB_COL_ENTRY_COUNT, 0, Integer.MAX_VALUE);

        Timestamp startedAt = getSafeTimestamp(rs, DB_COL_STARTED_AT);
        Timestamp generatedAt = getSafeTimestamp(rs, DB_COL_GENERATED_AT);
        Timestamp updatedAt = getSafeTimestamp(rs, DB_COL_UPDATED_AT);

        boolean inProgress = STATUS_RUNNING.equals(status) || STATUS_QUEUED.equals(status);

        if (overall.isBlank() && !STATUS_SUCCESS.equals(status)) {
            overall = message.isBlank() ? MSG_SUMMARY_IN_PROGRESS : message;
        }
        if (quality.isBlank()) {
            quality = PLACEHOLDER_DASH;
        }
        if (response.isBlank()) {
            response = PLACEHOLDER_DASH;
        }
        if (usage.isBlank()) {
            usage = PLACEHOLDER_DASH;
        }
        if (suggested.isBlank()) {
            suggested = suggestNextAction(status, quality, response, usage);
        }

        return Json.createObjectBuilder()
            .add(KEY_STATUS, STATUS_OK)
                .add(KEY_SUMMARY, Json.createObjectBuilder()
                        .add(KEY_OVERALL, overall)
                    .add(KEY_QUALITY, quality)
                        .add(KEY_RESPONSE, response)
                    .add(KEY_USAGE, usage)
                        .add(KEY_SUGGESTED_NEXT_ACTION, suggested)
                    .add(KEY_ENTRY_COUNT, entryCount))
                .add(KEY_META, Json.createObjectBuilder()
                    .add(KEY_DAY, getSafeDay(rs, DB_COL_SUMMARY_DAY))
                    .add(KEY_SLOT, getSafeInt(rs, DB_COL_SLOT, 0, 3))
                        .add(KEY_GENERATED_AT, fmtTs(generatedAt))
                        .add(KEY_STARTED_AT, fmtTs(startedAt))
                        .add(KEY_UPDATED_AT, fmtTs(updatedAt))
                        .add(KEY_IN_PROGRESS, inProgress)
                    .add(KEY_PROGRESS_PCT, progress)
                    .add(KEY_STATUS_TEXT, status)
                        .add(KEY_MESSAGE, message)
                        .add(KEY_FROM_FALLBACK, fromFallback))
                .build();
    }

    private String getSafeString(ResultSet rs, String column, int maxLen) throws SQLException {
        String value = readRawText(rs, column);
        return sanitizeText(value, maxLen);
    }

    private String getSafeString(ResultSet rs, int column, int maxLen) throws SQLException {
        String value = readRawText(rs, column);
        return sanitizeText(value, maxLen);
    }

    private String readRawText(ResultSet rs, String column) throws SQLException {
        if (rs == null || column == null || column.isBlank()) {
            return EMPTY;
        }

        try {
            Reader reader = rs.getCharacterStream(column);
            if (reader != null) {
                try (Reader closeable = reader) {
                    String raw = readAtMostChars(closeable, 8192);
                    return validateCanonicalizedDbText(raw, 8192);
                } catch (IOException ioEx) {
                    log.log(Level.FINE, MSG_CHARACTER_STREAM_DECODE_FAILED_COL + safeColumnName(column), ioEx);
                    return EMPTY;
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_CHARACTER_STREAM_COL + safeColumnName(column), ex);
        }

        try {
            byte[] rawBytes = rs.getBytes(column);
            if (rawBytes == null) {
                return EMPTY;
            }
            String raw = new String(rawBytes, StandardCharsets.UTF_8);
            return validateCanonicalizedDbText(raw, 8192);
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_BYTES_COL + safeColumnName(column), ex);
        }

        try {
            String raw = rs.getString(column);
            if (raw != null) {
                return validateCanonicalizedDbText(raw, 8192);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_STRING_COL + safeColumnName(column), ex);
        }

        try {
            Object raw = rs.getObject(column);
            if (raw != null) {
                return validateCanonicalizedDbText(String.valueOf(raw), 8192);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_OBJECT_COL + safeColumnName(column), ex);
        }

        return EMPTY;
    }

    private String readRawText(ResultSet rs, int column) throws SQLException {
        if (rs == null || column <= 0) {
            return EMPTY;
        }

        try {
            Reader reader = rs.getCharacterStream(column);
            if (reader != null) {
                try (Reader closeable = reader) {
                    String raw = readAtMostChars(closeable, 8192);
                    return validateCanonicalizedDbText(raw, 8192);
                } catch (IOException ioEx) {
                    log.log(Level.FINE, MSG_CHARACTER_STREAM_DECODE_FAILED_COL_INDEX + column, ioEx);
                    return EMPTY;
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_CHARACTER_STREAM_COL_INDEX + column, ex);
        }

        try {
            byte[] rawBytes = rs.getBytes(column);
            if (rawBytes == null) {
                return EMPTY;
            }
            String raw = new String(rawBytes, StandardCharsets.UTF_8);
            return validateCanonicalizedDbText(raw, 8192);
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_BYTES_COL_INDEX + column, ex);
        }

        try {
            String raw = rs.getString(column);
            if (raw != null) {
                return validateCanonicalizedDbText(raw, 8192);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_STRING_COL_INDEX + column, ex);
        }

        try {
            Object raw = rs.getObject(column);
            if (raw != null) {
                return validateCanonicalizedDbText(String.valueOf(raw), 8192);
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, MSG_RS_GET_OBJECT_COL_INDEX + column, ex);
        }

        return EMPTY;
    }

    private String sanitizeText(String value, int maxLen) {
        if (value == null) {
            return EMPTY;
        }
        String normalized = validateCanonicalizedDbText(value, maxLen > 0 ? maxLen : 0);
        if (maxLen > 0 && normalized.length() > maxLen) {
            return new String(normalized.toCharArray(), 0, maxLen);
        }
        return normalized;
    }

    private int getSafeInt(ResultSet rs, String column, int min, int max) throws SQLException {
        if (rs == null || column == null || column.isBlank()) {
            return min;
        }

        String raw = sanitizeText(readRawText(rs, column), 64);
        if (raw == null || raw.isBlank() || !raw.matches(REGEX_SIGNED_INT)) {
            return min;
        }
        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, MSG_UNABLE_PARSE_INT_FOR_COL + safeColumnName(column), ex);
            return min;
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private Timestamp getSafeTimestamp(ResultSet rs, String column) throws SQLException {
        if (rs == null || column == null || column.isBlank()) {
            return null;
        }

        String text = sanitizeText(readRawText(rs, column), 128);
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            Timestamp value;
            try {
                value = Timestamp.valueOf(text.replace('T', ' '));
            } catch (IllegalArgumentException ex) {
                log.log(Level.FINE, MSG_TS_SQL_PARSE_FALLBACK + safeColumnName(column), ex);
                value = Timestamp.from(Instant.parse(text));
            }
            Instant instant = validateInstant(value.toInstant(), column);
            return instant == null ? null : Timestamp.from(instant);
        } catch (IllegalArgumentException | DateTimeException e) {
            log.log(Level.FINE, MSG_INVALID_TS_FOR_COL + safeColumnName(column), e);
            return null;
        }
    }

    private String getSafeDay(ResultSet rs, String column) throws SQLException {
        if (rs == null || column == null || column.isBlank()) {
            return EMPTY;
        }

        String text = sanitizeText(getSafeString(rs, column, 64), 64);
        if (text == null || text.isBlank()) {
            return EMPTY;
        }

        String dateText = text.trim();
        if (dateText.length() > 10) {
            dateText = new String(dateText.toCharArray(), 0, 10);
        }

        try {
            LocalDate localDay = LocalDate.parse(dateText);
            if (localDay.isBefore(MIN_ALLOWED_DAY) || localDay.isAfter(MAX_ALLOWED_DAY)) {
                return EMPTY;
            }
            return localDay.toString();
        } catch (IllegalArgumentException | DateTimeException e) {
            log.log(Level.FINE, MSG_INVALID_DATE_FOR_COL + safeColumnName(column), e);
            return EMPTY;
        }
    }

    private String safeColumnName(String column) {
        if (column == null || column.isBlank()) {
            return UNKNOWN_COLUMN;
        }
        return column;
    }

    private String validateCanonicalizedDbText(String raw, int maxLen) {
        String canonical = TextIoSanitizerUtil.canonicalize(raw);
        return validateDbText(canonical, maxLen);
    }

    private String validateDbText(String value, int maxLen) {
        if (value == null || value.isBlank()) {
            return EMPTY;
        }
        String normalized = ServletRequestParamUtil.normalizeBodyText(value, maxLen, false);
        if (normalized.isBlank()) {
            return EMPTY;
        }
        StringBuilder safe = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isISOControl(ch) && ch != System.lineSeparator().charAt(System.lineSeparator().length() - 1) && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }
        return safe.toString();
    }

    private Instant validateInstant(Instant instant, String column) {
        if (instant == null) {
            return null;
        }
        LocalDate day = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        if (day.isBefore(MIN_ALLOWED_DAY) || day.isAfter(MAX_ALLOWED_DAY)) {
            log.log(Level.FINE, MSG_OUT_OF_RANGE_TS_FOR_COL + safeColumnName(column));
            return null;
        }
        return instant;
    }

    private String readAtMostChars(Reader reader, int maxChars) throws IOException {
        if (maxChars <= 0) {
            return EMPTY;
        }
        StringBuilder builder = new StringBuilder(Math.min(maxChars, 256));
        char[] buffer = new char[256];
        int remaining = maxChars;
        while (remaining > 0) {
            int toRead = Math.min(buffer.length, remaining);
            int read = reader.read(buffer, 0, toRead);
            if (read < 0) {
                break;
            }
            builder.append(buffer, 0, read);
            remaining -= read;
        }
        return builder.toString();
    }

    private String suggestNextAction(String status, String quality, String response, String usage) {
        String st = value(status, STATUS_IDLE).toLowerCase();
        String q = value(quality, EMPTY).toLowerCase();
        String r = value(response, EMPTY).toLowerCase();
        String u = value(usage, EMPTY).toLowerCase();

        if (STATUS_RUNNING.equals(st) || STATUS_QUEUED.equals(st)) {
            return MSG_SUMMARY_STILL_GENERATING;
        }
        if (containsAny(q, TERM_LOW, TERM_INCONSISTENT, TERM_HALLUCINATION, TERM_INCORRECT, TERM_POOR)) {
            return MSG_REVIEW_LOW_QUALITY;
        }
        if (containsAny(r, TERM_SLOW, TERM_LATENCY, TERM_TIMEOUT, TERM_DELAYED)) {
            return MSG_INVESTIGATE_LATENCY;
        }
        if (containsAny(u, TERM_LOW, TERM_DROP, TERM_DECLINE, TERM_UNDERUSED)) {
            return MSG_PROMOTE_UNDERUSED;
        }
        return SUGGESTED_NEXT_ACTION_DEFAULT;
    }

    private boolean containsAny(String text, String... terms) {
        if (text == null || text.isBlank() || terms == null) {
            return false;
        }
        for (String t : terms) {
            if (t != null && !t.isBlank() && text.contains(t.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String fmtTs(Timestamp ts) {
        if (ts == null) {
            return EMPTY;
        }
        return UI_TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private String normalizeStatus(String status) {
        String s = status == null ? STATUS_IDLE : status.trim().toLowerCase();
        return switch (s) {
            case STATUS_QUEUED, STATUS_RUNNING, STATUS_SUCCESS, STATUS_ERROR, STATUS_IDLE ->
                s;
            default ->
                STATUS_IDLE;
        };
    }

    private int clamp(int p) {
        if (p < 0) {
            return 0;
        }
        if (p > 100) {
            return 100;
        }
        return p;
    }

    private String value(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
