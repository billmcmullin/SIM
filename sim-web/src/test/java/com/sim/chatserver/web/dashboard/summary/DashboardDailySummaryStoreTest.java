package com.sim.chatserver.web.dashboard.summary;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.json.JsonObject;

@ExtendWith(MockitoExtension.class)
class DashboardDailySummaryStoreTest {

    @Mock
    DataSource dataSource;

    @Mock
    Connection connection;

    @Mock
    Statement statement;

    @Mock
    PreparedStatement preparedStatement;

    @Mock
    PreparedStatement exactPreparedStatement;

    @Mock
    PreparedStatement latestPreparedStatement;

    @Mock
    ResultSet exactResultSet;

    @Mock
    ResultSet latestResultSet;

    private DashboardDailySummaryStore store;

    @BeforeEach
    void setUp() {
        store = new DashboardDailySummaryStore(dataSource);
    }

    @Test
    void ensureTable_executesCreateSql() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        store.ensureTable();

        verify(preparedStatement, atLeastOnce()).execute();
    }

    @Test
    void ensureTable_throwsIllegalStateOnFailure() throws Exception {
        when(dataSource.getConnection()).thenThrow(new RuntimeException("db down"));

        assertThrows(IllegalStateException.class, () -> store.ensureTable());
    }

    @Test
    void upsertProgress_bindsNormalizedClampedAndDefaultValues() throws Exception {
        LocalDate day = LocalDate.of(2026, 1, 10);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        store.upsertProgress(day, 2, " BAD_STATUS ", 999, null, -5, false, false);

        verify(preparedStatement).setDate(1, Date.valueOf(day));
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setString(3, "idle"); // normalized unknown status
        verify(preparedStatement).setInt(4, 100);       // clamped
        verify(preparedStatement).setString(5, "");     // null message -> ""
        verify(preparedStatement).setString(6, null);
        verify(preparedStatement).setString(7, null);
        verify(preparedStatement).setString(8, null);
        verify(preparedStatement).setString(9, null);
        verify(preparedStatement).setString(10, null);
        verify(preparedStatement).setInt(11, 0);        // max(0, entryCount)
        verify(preparedStatement).setTimestamp(12, null);
        verify(preparedStatement).setTimestamp(13, null);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void upsertSummary_setsStartedAndGeneratedTimestamps_andTrimsBlankToNull() throws Exception {
        LocalDate day = LocalDate.of(2026, 2, 2);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        store.upsertSummary(day, 1, "running", -3, "msg", "  ", "quality", " ", "usage", 3, true, true);

        verify(preparedStatement).setString(3, "running");
        verify(preparedStatement).setInt(4, 0); // clamped
        verify(preparedStatement).setString(6, null);       // blank -> null
        verify(preparedStatement).setString(7, "quality");
        verify(preparedStatement).setString(8, null);       // blank -> null
        verify(preparedStatement).setString(9, "usage");

        // started_at / generated_at should be non-null when flags are true
        verify(preparedStatement).setTimestamp(org.mockito.ArgumentMatchers.eq(12), org.mockito.ArgumentMatchers.notNull());
        verify(preparedStatement).setTimestamp(org.mockito.ArgumentMatchers.eq(13), org.mockito.ArgumentMatchers.notNull());
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void upsert_throwsIllegalStateOnSqlFailure() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new RuntimeException("sql fail"));

        assertThrows(IllegalStateException.class, ()
                -> store.upsertProgress(LocalDate.now(), 0, "running", 1, "x", 1, false, false));
    }

    @Test
    void fetchExactOrLatest_returnsExactMatch_whenExactRowExists() throws Exception {
        LocalDate day = LocalDate.of(2026, 3, 3);
        Timestamp started = Timestamp.from(java.time.Instant.parse("2026-03-03T10:00:00Z"));
        Timestamp generated = Timestamp.from(java.time.Instant.parse("2026-03-03T10:05:00Z"));
        Timestamp updated = Timestamp.from(java.time.Instant.parse("2026-03-03T10:06:00Z"));

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.startsWith("SELECT summary_day, slot, status")))
                .thenReturn(exactPreparedStatement, latestPreparedStatement);

        when(exactPreparedStatement.executeQuery()).thenReturn(exactResultSet);
        when(exactResultSet.next()).thenReturn(true);

        when(exactResultSet.getString("status")).thenReturn("running");
        when(exactResultSet.getInt("progress_pct")).thenReturn(55);
        when(exactResultSet.getString("message")).thenReturn("Working...");
        when(exactResultSet.getString("summary_overall")).thenReturn("");
        when(exactResultSet.getString("summary_quality")).thenReturn("");
        when(exactResultSet.getString("summary_response")).thenReturn("");
        when(exactResultSet.getString("summary_usage")).thenReturn("");
        when(exactResultSet.getInt("entry_count")).thenReturn(42);
        when(exactResultSet.getTimestamp("started_at")).thenReturn(started);
        when(exactResultSet.getTimestamp("generated_at")).thenReturn(generated);
        when(exactResultSet.getTimestamp("updated_at")).thenReturn(updated);
        when(exactResultSet.getDate("summary_day")).thenReturn(Date.valueOf(day));
        when(exactResultSet.getInt("slot")).thenReturn(2);

        JsonObject out = store.fetchExactOrLatest(day, 2);

        assertEquals("ok", out.getString("status"));
        JsonObject summary = out.getJsonObject("summary");
        JsonObject meta = out.getJsonObject("meta");

        assertEquals("Working...", summary.getString("overall")); // derived from message because overall blank + running
        assertEquals("—", summary.getString("quality"));
        assertEquals("—", summary.getString("response"));
        assertEquals("—", summary.getString("usage"));
        assertEquals(42, summary.getInt("entryCount"));

        assertEquals(true, meta.getBoolean("inProgress"));
        assertEquals(55, meta.getInt("progressPct"));
        assertEquals("running", meta.getString("statusText"));
        assertEquals(false, meta.getBoolean("fromFallback"));
    }

    @Test
    void fetchExactOrLatest_fallsBackToLatest_whenExactMissing() throws Exception {
        LocalDate requestedDay = LocalDate.of(2026, 4, 1);
        LocalDate latestDay = LocalDate.of(2026, 3, 31);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.startsWith("SELECT summary_day, slot, status")))
                .thenReturn(exactPreparedStatement, latestPreparedStatement);

        when(exactPreparedStatement.executeQuery()).thenReturn(exactResultSet);
        when(exactResultSet.next()).thenReturn(false);

        when(latestPreparedStatement.executeQuery()).thenReturn(latestResultSet);
        when(latestResultSet.next()).thenReturn(true);

        when(latestResultSet.getString("status")).thenReturn("success");
        when(latestResultSet.getInt("progress_pct")).thenReturn(100);
        when(latestResultSet.getString("message")).thenReturn("done");
        when(latestResultSet.getString("summary_overall")).thenReturn("Overall text");
        when(latestResultSet.getString("summary_quality")).thenReturn("Quality text");
        when(latestResultSet.getString("summary_response")).thenReturn("Response text");
        when(latestResultSet.getString("summary_usage")).thenReturn("Usage text");
        when(latestResultSet.getInt("entry_count")).thenReturn(7);
        when(latestResultSet.getTimestamp("started_at")).thenReturn(null);
        when(latestResultSet.getTimestamp("generated_at")).thenReturn(null);
        when(latestResultSet.getTimestamp("updated_at")).thenReturn(null);
        when(latestResultSet.getDate("summary_day")).thenReturn(Date.valueOf(latestDay));
        when(latestResultSet.getInt("slot")).thenReturn(3);

        JsonObject out = store.fetchExactOrLatest(requestedDay, 1);

        assertEquals("ok", out.getString("status"));
        assertEquals("Overall text", out.getJsonObject("summary").getString("overall"));
        assertEquals(true, out.getJsonObject("meta").getBoolean("fromFallback"));
    }

    @Test
    void fetchExactOrLatest_returnsDefault_whenNoRowsAnywhere() throws Exception {
        LocalDate day = LocalDate.of(2026, 5, 5);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.startsWith("SELECT summary_day, slot, status")))
                .thenReturn(exactPreparedStatement, latestPreparedStatement);

        when(exactPreparedStatement.executeQuery()).thenReturn(exactResultSet);
        when(exactResultSet.next()).thenReturn(false);

        when(latestPreparedStatement.executeQuery()).thenReturn(latestResultSet);
        when(latestResultSet.next()).thenReturn(false);

        JsonObject out = store.fetchExactOrLatest(day, 0);

        assertEquals("ok", out.getString("status"));
        assertEquals("No summary has been generated yet.", out.getJsonObject("summary").getString("overall"));
        assertEquals(false, out.getJsonObject("meta").getBoolean("fromFallback"));
        assertEquals(day.toString(), out.getJsonObject("meta").getString("day"));
    }

    @Test
    void fetchExactOrLatest_returnsErrorPayload_whenExceptionOccurs() throws Exception {
        when(dataSource.getConnection()).thenThrow(new RuntimeException("db error"));

        JsonObject out = store.fetchExactOrLatest(LocalDate.now(), 0);

        assertEquals("error", out.getString("status"));
        assertEquals("Unable to load summary.", out.getString("message"));
    }

    @Test
    void fetchExactOrLatest_executesExactThenLatestInOrder_whenExactMisses() throws Exception {
        LocalDate day = LocalDate.of(2026, 6, 1);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.startsWith("SELECT summary_day, slot, status")))
                .thenReturn(exactPreparedStatement, latestPreparedStatement);

        when(exactPreparedStatement.executeQuery()).thenReturn(exactResultSet);
        when(exactResultSet.next()).thenReturn(false);

        when(latestPreparedStatement.executeQuery()).thenReturn(latestResultSet);
        when(latestResultSet.next()).thenReturn(false);

        store.fetchExactOrLatest(day, 2);

        InOrder order = inOrder(exactPreparedStatement, latestPreparedStatement);
        order.verify(exactPreparedStatement).setDate(1, Date.valueOf(day));
        order.verify(exactPreparedStatement).setInt(2, 2);
        order.verify(exactPreparedStatement).executeQuery();
        order.verify(latestPreparedStatement).executeQuery();
    }

    @Test
    void fetchExactOrLatest_successWithBlankStatus_defaultsToIdle() throws Exception {
        LocalDate day = LocalDate.of(2026, 7, 7);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.startsWith("SELECT summary_day, slot, status")))
                .thenReturn(exactPreparedStatement, latestPreparedStatement);

        when(exactPreparedStatement.executeQuery()).thenReturn(exactResultSet);
        when(exactResultSet.next()).thenReturn(true);

        when(exactResultSet.getString("status")).thenReturn(" ");
        when(exactResultSet.getInt("progress_pct")).thenReturn(10);
        when(exactResultSet.getString("message")).thenReturn("");
        when(exactResultSet.getString("summary_overall")).thenReturn("");
        when(exactResultSet.getString("summary_quality")).thenReturn("");
        when(exactResultSet.getString("summary_response")).thenReturn("");
        when(exactResultSet.getString("summary_usage")).thenReturn("");
        when(exactResultSet.getInt("entry_count")).thenReturn(1);
        when(exactResultSet.getTimestamp("started_at")).thenReturn(null);
        when(exactResultSet.getTimestamp("generated_at")).thenReturn(null);
        when(exactResultSet.getTimestamp("updated_at")).thenReturn(null);
        when(exactResultSet.getDate("summary_day")).thenReturn(Date.valueOf(day));
        when(exactResultSet.getInt("slot")).thenReturn(1);

        JsonObject out = store.fetchExactOrLatest(day, 1);

        assertEquals("idle", out.getJsonObject("meta").getString("statusText"));
        assertEquals("Summary generation in progress.", out.getJsonObject("summary").getString("overall"));
        assertEquals(false, out.getJsonObject("meta").getBoolean("inProgress"));
    }
}
