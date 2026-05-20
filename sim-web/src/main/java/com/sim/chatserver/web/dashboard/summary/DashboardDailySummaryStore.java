package com.sim.chatserver.web.dashboard.summary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
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

    private static final Logger log = Logger.getLogger(DashboardDailySummaryStore.class.getName());
    private static final DateTimeFormatter UI_TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute(TABLE_SQL);
            ensureSuggestedNextActionColumn(conn);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to ensure dashboard_daily_summary table", e);
            throw new IllegalStateException("Unable to ensure dashboard_daily_summary table", e);
        }
    }

    private void ensureSuggestedNextActionColumn(Connection conn) {
        final String sql = """
                ALTER TABLE dashboard_daily_summary
                ADD COLUMN IF NOT EXISTS suggested_next_action TEXT
                """;
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (Exception e) {
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

        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to upsert dashboard daily summary row", e);
            throw new IllegalStateException("Unable to upsert dashboard daily summary row", e);
        }
    }

    public JsonObject fetchExactOrLatest(LocalDate day, int slot) {
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

        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to fetch dashboard daily summary", e);
            return Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load summary.")
                    .build();
        }
    }

    private JsonObject toPayload(ResultSet rs, boolean fromFallback) throws Exception {
        String status = value(rs.getString("status"), "idle").toLowerCase();
        int progress = clamp(rs.getInt("progress_pct"));
        String message = value(rs.getString("message"), "");

        String overall = value(rs.getString("summary_overall"), "");
        String quality = value(rs.getString("summary_quality"), "");
        String response = value(rs.getString("summary_response"), "");
        String usage = value(rs.getString("summary_usage"), "");
        String suggested = value(rs.getString("suggested_next_action"), "");
        int entryCount = Math.max(0, rs.getInt("entry_count"));

        Timestamp startedAt = rs.getTimestamp("started_at");
        Timestamp generatedAt = rs.getTimestamp("generated_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

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
                        .add("day", rs.getDate("summary_day") == null ? "" : rs.getDate("summary_day").toLocalDate().toString())
                        .add("slot", rs.getInt("slot"))
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
