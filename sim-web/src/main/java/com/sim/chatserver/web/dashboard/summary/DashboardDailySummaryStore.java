package com.sim.chatserver.web.dashboard.summary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Persistent store for daily dashboard summary generation status + result
 * payload.
 */
public class DashboardDailySummaryStore {
    // parasoft-suppress SECURITY.WSC.DSER "Store class is not serialized by application design and is used only as an in-memory DAO wrapper."
    // parasoft-suppress SECURITY.WSC.SER "Store class is not serialized by application design and is used only as an in-memory DAO wrapper."

    private static final Logger log = Logger.getLogger(DashboardDailySummaryStore.class.getName());
    private static final DateTimeFormatter UI_TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PG_UNIQUE_VIOLATION = "23505";

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

    public DashboardDailySummaryStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void ensureTable() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(TABLE_SQL)) {
            ps.execute();
            ensureSuggestedNextActionColumn(conn);
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to ensure dashboard_daily_summary table", e);
            throw new IllegalStateException("Unable to ensure dashboard_daily_summary table", e);
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
            log.log(Level.WARNING, "Unable to ensure suggested_next_action column", e);
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
                log.log(Level.INFO, "Detected stale dashboard_daily_summary id sequence after import. Realigned sequence and retrying upsert.");
                try (Connection retryConn = dataSource.getConnection()) {
                    executeUpsert(retryConn, sql, day, slot, status, progressPct, message, overall, quality, response, usage, suggestedNextAction, entryCount, markStarted, markGenerated);
                    return;
                } catch (SQLException retrySqlError) {
                    log.log(Level.WARNING, "Retry after sequence realignment failed for dashboard_daily_summary upsert", retrySqlError);
                    throw new IllegalStateException("Unable to upsert dashboard daily summary row", retrySqlError);
                }
            }

            log.log(Level.WARNING, "Unable to upsert dashboard daily summary row", firstSqlError);
            throw new IllegalStateException("Unable to upsert dashboard daily summary row", firstSqlError);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to upsert dashboard daily summary row", e);
            throw new IllegalStateException("Unable to upsert dashboard daily summary row", e);
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
            ps.setString(5, message == null ? "" : message);

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
                    && msg.contains("dashboard_daily_summary_pkey")
                    && msg.contains("(id)=")) {
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

            String setSeqSql = "SELECT setval(?::regclass, COALESCE((SELECT MAX(id) FROM dashboard_daily_summary), 0) + 1, false)";
            try (PreparedStatement setPs = conn.prepareStatement(setSeqSql)) {
                setPs.setString(1, sequenceName);
                setPs.execute();
            }
            return true;
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to realign dashboard_daily_summary id sequence", e);
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

            String fallbackSuggested = "Review Top Terms and Latest Chats to identify one repeated issue and apply a focused prompt update.";

            return Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("summary", Json.createObjectBuilder()
                            .add("overall", "No summary has been generated yet.")
                            .add("quality", "—")
                            .add("response", "—")
                            .add("usage", "—")
                            .add("suggestedNextAction", fallbackSuggested)
                            .add("entryCount", 0))
                    .add("meta", Json.createObjectBuilder()
                            .add("day", day.toString())
                            .add("slot", slot)
                            .add("generatedAt", "")
                            .add("startedAt", "")
                            .add("updatedAt", "")
                            .add("inProgress", false)
                            .add("progressPct", 0)
                            .add("statusText", "idle")
                            .add("message", "Waiting for scheduled generation.")
                            .add("fromFallback", false))
                    .build();

        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to fetch dashboard daily summary", e);
            return Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load summary.")
                    .build();
        }
    }

    private JsonObject toPayload(ResultSet rs, boolean fromFallback) throws SQLException {
        String status = value(getSafeString(rs, "status", 20), "idle").toLowerCase();
        int progress = clamp(getSafeInt(rs, "progress_pct", 0, 100));
        String message = value(getSafeString(rs, "message", 2000), "");

        String overall = value(getSafeString(rs, "summary_overall", 8000), "");
        String quality = value(getSafeString(rs, "summary_quality", 4000), "");
        String response = value(getSafeString(rs, "summary_response", 4000), "");
        String usage = value(getSafeString(rs, "summary_usage", 4000), "");
        String suggested = value(getSafeString(rs, "suggested_next_action", 2000), "");
        int entryCount = getSafeInt(rs, "entry_count", 0, Integer.MAX_VALUE);

        Timestamp startedAt = getSafeTimestamp(rs, "started_at");
        Timestamp generatedAt = getSafeTimestamp(rs, "generated_at");
        Timestamp updatedAt = getSafeTimestamp(rs, "updated_at");

        boolean inProgress = "running".equals(status) || "queued".equals(status);

        if (overall.isBlank() && !"success".equals(status)) {
            overall = message.isBlank() ? "Summary generation in progress." : message;
        }
        if (quality.isBlank()) {
            quality = "—";
        }
        if (response.isBlank()) {
            response = "—";
        }
        if (usage.isBlank()) {
            usage = "—";
        }
        if (suggested.isBlank()) {
            suggested = suggestNextAction(status, quality, response, usage);
        }

        return Json.createObjectBuilder()
                .add("status", "ok")
                .add("summary", Json.createObjectBuilder()
                        .add("overall", overall)
                        .add("quality", quality)
                        .add("response", response)
                        .add("usage", usage)
                        .add("suggestedNextAction", suggested)
                        .add("entryCount", entryCount))
                .add("meta", Json.createObjectBuilder()
                        .add("day", getSafeDay(rs, "summary_day"))
                        .add("slot", getSafeInt(rs, "slot", 0, 3))
                        .add("generatedAt", fmtTs(generatedAt))
                        .add("startedAt", fmtTs(startedAt))
                        .add("updatedAt", fmtTs(updatedAt))
                        .add("inProgress", inProgress)
                        .add("progressPct", progress)
                        .add("statusText", status)
                        .add("message", message)
                        .add("fromFallback", fromFallback))
                .build();
    }

    private String getSafeString(ResultSet rs, String column, int maxLen) throws SQLException {
        Object raw = rs.getObject(column);
        String value = raw == null ? null : String.valueOf(raw);
        return sanitizeText(value, maxLen);
    }

    private String getSafeString(ResultSet rs, int column, int maxLen) throws SQLException {
        Object raw = rs.getObject(column);
        String value = raw == null ? null : String.valueOf(raw);
        return sanitizeText(value, maxLen);
    }

    private String sanitizeText(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\u0000', ' ')
                .replace("\r", "")
                .replace("\u2028", " ")
                .replace("\u2029", " ");
        if (normalized.length() > maxLen) {
            return normalized.substring(0, maxLen);
        }
        return normalized;
    }

    private int getSafeInt(ResultSet rs, String column, int min, int max) throws SQLException {
        String raw = getSafeString(rs, column, 64);
        if (raw.isBlank() || !raw.matches("^-?\\d+$")) {
            return min;
        }
        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
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
        String raw = getSafeString(rs, column, 128);
        if (raw.isBlank()) {
            return null;
        }
        try {
            Timestamp value;
            try {
                value = Timestamp.valueOf(raw.replace('T', ' '));
            } catch (IllegalArgumentException ex) {
                log.log(Level.FINE, "Timestamp SQL parse fallback to Instant parser for column " + column, ex);
                value = Timestamp.from(Instant.parse(raw));
            }
            Instant instant = value.toInstant();
            return instant == null ? null : Timestamp.from(instant);
        } catch (IllegalArgumentException | DateTimeException e) {
            log.log(Level.FINE, "Invalid timestamp value for column " + column, e);
            return null;
        }
    }

    private String getSafeDay(ResultSet rs, String column) throws SQLException {
        String raw = getSafeString(rs, column, 32);
        if (raw.isBlank()) {
            return "";
        }
        try {
            LocalDate localDay = LocalDate.parse(raw.trim());
            return localDay.toString();
        } catch (IllegalArgumentException | DateTimeException e) {
            log.log(Level.FINE, "Invalid date value for column " + column, e);
            return "";
        }
    }

    private String suggestNextAction(String status, String quality, String response, String usage) {
        String st = value(status, "idle").toLowerCase();
        String q = value(quality, "").toLowerCase();
        String r = value(response, "").toLowerCase();
        String u = value(usage, "").toLowerCase();

        if ("running".equals(st) || "queued".equals(st)) {
            return "Summary is still generating. Wait for completion, then review low-performing areas and rerun checks.";
        }
        if (containsAny(q, "low", "inconsistent", "hallucination", "incorrect", "poor")) {
            return "Review low-quality conversations first and tighten prompt instructions/guardrails for affected widgets.";
        }
        if (containsAny(r, "slow", "latency", "timeout", "delayed")) {
            return "Investigate response latency by widget and reduce prompt/context size where possible.";
        }
        if (containsAny(u, "low", "drop", "decline", "underused")) {
            return "Promote underused high-value widgets and add clearer in-app guidance for users.";
        }
        return "Review Top Terms and Latest Chats to identify one repeated issue and apply a focused prompt update.";
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
            return "";
        }
        return UI_TS_FMT.format(ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private String normalizeStatus(String status) {
        String s = status == null ? "idle" : status.trim().toLowerCase();
        return switch (s) {
            case "queued", "running", "success", "error", "idle" ->
                s;
            default ->
                "idle";
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
