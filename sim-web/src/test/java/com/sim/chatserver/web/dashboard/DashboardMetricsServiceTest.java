package com.sim.chatserver.web.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.TermDayCount;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermMatcher;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.widget.WidgetEntry;

class DashboardMetricsServiceTest {

    @Test
    void buildWidgetStats_happyPath_and_invalidWidgetSkipped() throws Exception {
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        AtomicInteger betweenCounter = new AtomicInteger(0);
        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            if (sql.contains("COUNT(*)") && !sql.contains("created_at >= ?")) {
                ResultSet rs = mockCountResultSet(7);
                when(ps.executeQuery()).thenReturn(rs);
            } else if (sql.contains("COUNT(*)") && sql.contains("created_at >= ?")) {
                int value = betweenCounter.getAndIncrement() == 0 ? 5 : 3;
                ResultSet rs = mockCountResultSet(value);
                when(ps.executeQuery()).thenReturn(rs);
            }
            return ps;
        });

        TermsStore termsStore = mock(TermsStore.class);
        DashboardMetricsService service = DashboardMetricsService.create(dsHolder, termsStore, 10);

        WidgetEntry good = widget("goodWidget", "Good Widget");
        WidgetEntry bad = widget("badWidget", "Bad Widget");

        try (MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class, Mockito.CALLS_REAL_METHODS)) {
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class)))
                    .thenReturn(true);
            dbUtil.when(() -> DashboardDbUtil.sanitizeWidgetTableName("badWidget"))
                    .thenThrow(new IllegalArgumentException("invalid"));

            List<WidgetStat> result = service.buildWidgetStats(List.of(good, bad));

            assertEquals(1, result.size());
            WidgetStat stat = result.get(0);
            assertEquals("goodWidget", stat.getWidgetId());
            assertEquals("Good Widget", stat.getLabel());
            assertEquals(7, stat.getCount());
            assertEquals(5, stat.getTodayCount());
            assertEquals(3, stat.getYesterdayCount());
            assertEquals("up", stat.getDirection());
        }
    }

    @Test
    void buildWidgetStats_whenConnectionFails_returnsEmptyList() throws Exception {
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource ds = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenThrow(new SQLException("db down"));

        DashboardMetricsService service = DashboardMetricsService.create(dsHolder, mock(TermsStore.class), 10);
        List<WidgetStat> result = service.buildWidgetStats(List.of(widget("w1", "W1")));

        assertTrue(result.isEmpty());
    }

    @Test
    void buildChatProgression_and_buildDashboardProgressMetrics_happyPath() throws Exception {
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        TermDefinition term = mock(TermDefinition.class);
        when(term.isSystemFlag()).thenReturn(false);
        when(term.getName()).thenReturn("Error Term");
        when(term.getMatchType()).thenReturn("WILDCARD");
        when(term.getMatchPattern()).thenReturn("error");
        when(termsStore.listAll()).thenReturn(List.of(term));

        Timestamp todayTs = Timestamp.valueOf(LocalDate.now(ZoneId.systemDefault()).atStartOfDay().plusHours(1));
        Timestamp yesterdayTs = Timestamp.valueOf(LocalDate.now(ZoneId.systemDefault()).minusDays(1).atStartOfDay().plusHours(1));

        AtomicInteger countQueryCall = new AtomicInteger(0);
        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            if (sql.startsWith("SELECT COUNT(*)") && sql.contains("created_at >= ?")) {
                int idx = countQueryCall.getAndIncrement();
                int value = (idx == 0 || idx == 2) ? 5 : 3;
                ResultSet rs = mockCountResultSet(value);
                when(ps.executeQuery()).thenReturn(rs);
            } else if (sql.startsWith("SELECT prompt, created_at")) {
                List<Map<String, Object>> rows = new ArrayList<>();
                rows.add(row("prompt", "error happened", "created_at", todayTs));
                rows.add(row("prompt", "error yesterday", "created_at", yesterdayTs));
                rows.add(row("prompt", "unmatched text", "created_at", todayTs));
                ResultSet rs = mockRowsResultSet(rows);
                when(ps.executeQuery()).thenReturn(rs);
            }
            return ps;
        });

        DashboardMetricsService service = DashboardMetricsService.create(dsHolder, termsStore, 10);
        List<WidgetEntry> widgets = List.of(widget("w1", "Widget One"));

        try (MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class, Mockito.CALLS_REAL_METHODS)) {
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class)))
                    .thenReturn(true);

            DashboardLocalViewModels.ProgressStat chatProgression = service.buildChatProgression(widgets);
            assertEquals(5, chatProgression.getToday());
            assertEquals(3, chatProgression.getYesterday());
            assertEquals(2, chatProgression.getDelta());

            DashboardMetricsService.DashboardProgressMetrics metrics = service.buildDashboardProgressMetrics(widgets);
            assertEquals(5, metrics.getChatsToday());
            assertEquals(3, metrics.getChatsYesterday());
            assertEquals(1, metrics.getTermsToday());
            assertEquals(1, metrics.getTermsYesterday());
            assertEquals(2, metrics.getChatsProgression().getDelta());
            assertEquals(0, metrics.getTermsProgression().getDelta());
        }
    }

    @Test
    void buildChatProgression_and_buildDashboardProgressMetrics_fallbackOnSqlError() throws Exception {
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource ds = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenThrow(new SQLException("db down"));

        DashboardMetricsService service = DashboardMetricsService.create(dsHolder, mock(TermsStore.class), 10);

        DashboardLocalViewModels.ProgressStat progression = service.buildChatProgression(List.of(widget("w1", "W1")));
        assertEquals(0, progression.getToday());
        assertEquals(0, progression.getYesterday());
        assertEquals(0, progression.getDelta());

        DashboardMetricsService.DashboardProgressMetrics metrics = service.buildDashboardProgressMetrics(List.of(widget("w1", "W1")));
        assertEquals(0, metrics.getChatsToday());
        assertEquals(0, metrics.getChatsYesterday());
        assertEquals(0, metrics.getTermsToday());
        assertEquals(0, metrics.getTermsYesterday());
    }

    @Test
    void buildNewUserProgression_happyPath_and_sqlFallback() throws Exception {
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        Timestamp todayTs = Timestamp.valueOf(LocalDate.now(ZoneId.systemDefault()).atStartOfDay().plusHours(2));
        Timestamp yesterdayTs = Timestamp.valueOf(LocalDate.now(ZoneId.systemDefault()).minusDays(1).atStartOfDay().plusHours(2));

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            PreparedStatement ps = mock(PreparedStatement.class);
            List<Map<String, Object>> rows = new ArrayList<>();
            rows.add(row("session_id", "s1", "first_seen", todayTs));
            rows.add(row("session_id", "s2", "first_seen", yesterdayTs));
            ResultSet rs = mockRowsResultSet(rows);
            when(ps.executeQuery()).thenReturn(rs);
            return ps;
        });

        DashboardMetricsService service = DashboardMetricsService.create(dsHolder, mock(TermsStore.class), 10);

        try (MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class, Mockito.CALLS_REAL_METHODS)) {
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class)))
                    .thenReturn(true);

            DashboardLocalViewModels.ProgressStat progression = service.buildNewUserProgression(List.of(widget("w1", "W1")));
            assertEquals(1, progression.getToday());
            assertEquals(1, progression.getYesterday());
        }

        when(ds.getConnection()).thenThrow(new SQLException("db down"));
        DashboardLocalViewModels.ProgressStat fallback = service.buildNewUserProgression(List.of(widget("w1", "W1")));
        assertEquals(0, fallback.getToday());
        assertEquals(0, fallback.getYesterday());
    }

    @Test
    void buildTopTopicsTodayVsYesterday_handlesEarlyReturns_and_success() throws Exception {
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        DashboardMetricsService service = DashboardMetricsService.create(dsHolder, termsStore, 2);

        assertTrue(service.buildTopTopicsTodayVsYesterday(List.of()).isEmpty());

        when(termsStore.listAll()).thenThrow(new SQLException("terms unavailable"));
        assertTrue(service.buildTopTopicsTodayVsYesterday(List.of(widget("w1", "W1"))).isEmpty());

        reset(termsStore);
        when(termsStore.listAll()).thenReturn(List.of());
        assertTrue(service.buildTopTopicsTodayVsYesterday(List.of(widget("w1", "W1"))).isEmpty());

        TermDefinition activeA = mock(TermDefinition.class);
        when(activeA.isSystemFlag()).thenReturn(false);
        when(activeA.getName()).thenReturn("alpha");
        when(activeA.getMatchType()).thenReturn("WILDCARD");
        when(activeA.getMatchPattern()).thenReturn("alpha");

        TermDefinition otherParasoft = mock(TermDefinition.class);
        when(otherParasoft.isSystemFlag()).thenReturn(false);
        when(otherParasoft.getName()).thenReturn("Other Parasoft Match");
        when(otherParasoft.getMatchType()).thenReturn("WILDCARD");
        when(otherParasoft.getMatchPattern()).thenReturn("other");

        when(termsStore.listAll()).thenReturn(List.of(activeA, otherParasoft));

        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        Timestamp todayTs = Timestamp.valueOf(LocalDate.now(ZoneId.systemDefault()).atStartOfDay().plusHours(4));
        Timestamp yesterdayTs = Timestamp.valueOf(LocalDate.now(ZoneId.systemDefault()).minusDays(1).atStartOfDay().plusHours(4));
        Timestamp oldTs = Timestamp.valueOf(LocalDate.now(ZoneId.systemDefault()).minusDays(5).atStartOfDay().plusHours(4));

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            PreparedStatement ps = mock(PreparedStatement.class);
            List<Map<String, Object>> rows = new ArrayList<>();
            rows.add(row("prompt", "alpha data", "created_at", todayTs));
            rows.add(row("prompt", "alpha again", "created_at", yesterdayTs));
            rows.add(row("prompt", "unmatched", "created_at", oldTs));
            rows.add(row("prompt", "alpha null ts", "created_at", null));
            ResultSet rs = mockRowsResultSet(rows);
            when(ps.executeQuery()).thenReturn(rs);
            return ps;
        });

        try (MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class, Mockito.CALLS_REAL_METHODS)) {
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class)))
                    .thenReturn(true);

            List<TopTopic> topics = service.buildTopTopicsTodayVsYesterday(List.of(widget("w1", "W1")));
            assertEquals(1, topics.size());
            assertEquals("alpha", topics.get(0).getLabel());
            assertEquals(1, topics.get(0).getToday());
            assertEquals(1, topics.get(0).getYesterday());
        }

        when(ds.getConnection()).thenThrow(new SQLException("db down"));
        List<TopTopic> fallback = service.buildTopTopicsTodayVsYesterday(List.of(widget("w1", "W1")));
        assertTrue(fallback.isEmpty());
    }

    @Test
    void buildLatestOtherParasoftEntries_handlesGuards_and_success_and_fallback() throws Exception {
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        DashboardMetricsService service = DashboardMetricsService.create(dsHolder, termsStore, 5);

        assertTrue(service.buildLatestOtherParasoftEntries(List.of(), 10).isEmpty());
        assertTrue(service.buildLatestOtherParasoftEntries(List.of(widget("w1", "W1")), 0).isEmpty());

        when(termsStore.listAll()).thenThrow(new SQLException("term load failed"));
        assertTrue(service.buildLatestOtherParasoftEntries(List.of(widget("w1", "W1")), 10).isEmpty());

        reset(termsStore);
        TermDefinition term = mock(TermDefinition.class);
        when(term.isSystemFlag()).thenReturn(false);
        when(term.getName()).thenReturn("alpha");
        when(term.getMatchType()).thenReturn("WILDCARD");
        when(term.getMatchPattern()).thenReturn("alpha");
        when(termsStore.listAll()).thenReturn(List.of(term));

        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dsHolder.getDataSource()).thenReturn(ds);
        when(ds.getConnection()).thenReturn(conn);

        Timestamp t1 = Timestamp.from(Instant.now().minusSeconds(60));
        Timestamp t2 = Timestamp.from(Instant.now().minusSeconds(5));

        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            PreparedStatement ps = mock(PreparedStatement.class);
            List<Map<String, Object>> rows = new ArrayList<>();
            rows.add(row("prompt", "alpha matched", "session_id", "s0", "created_at", t1));
            rows.add(row("prompt", "non matching prompt", "session_id", "s1", "created_at", t2));
            rows.add(row("prompt", "skip null ts", "session_id", "s2", "created_at", null));
            ResultSet rs = mockRowsResultSet(rows);
            when(ps.executeQuery()).thenReturn(rs);
            return ps;
        });

        try (MockedStatic<DashboardDbUtil> dbUtil = Mockito.mockStatic(DashboardDbUtil.class, Mockito.CALLS_REAL_METHODS)) {
            dbUtil.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class)))
                    .thenReturn(true);

            List<OtherParasoftEntry> out = service.buildLatestOtherParasoftEntries(List.of(widget("w1", "")), 10);
            assertEquals(1, out.size());
            assertEquals("w1", out.get(0).getWidgetName());
            assertEquals("non matching prompt", out.get(0).getPrompt());
            assertEquals("s1", out.get(0).getSessionId());
            assertNotNull(out.get(0).getCreatedAt());
        }

        when(ds.getConnection()).thenThrow(new SQLException("db down"));
        List<OtherParasoftEntry> fallback = service.buildLatestOtherParasoftEntries(List.of(widget("w1", "W1")), 10);
        assertTrue(fallback.isEmpty());
    }

    @Test
    void privateMatcherHelpers_handleIllegalStateExceptionGracefully() throws Exception {
        DashboardMetricsService service = DashboardMetricsService.create(mock(AppDataSourceHolder.class), mock(TermsStore.class), 10);

        Pattern badPattern = mock(Pattern.class);
        Matcher badMatcher = mock(Matcher.class);
        when(badPattern.matcher(anyString())).thenReturn(badMatcher);
        when(badMatcher.find()).thenThrow(new IllegalStateException("bad matcher"));

        Method matchesAnyPattern = DashboardMetricsService.class
                .getDeclaredMethod("matchesAnyPattern", String.class, List.class);
        matchesAnyPattern.setAccessible(true);
        boolean matched = (boolean) matchesAnyPattern.invoke(service, "anything", List.of(badPattern));
        assertFalse(matched);

        TermDefinition term = mock(TermDefinition.class);
        when(term.getName()).thenReturn("alpha");

        Method resolveBest = DashboardMetricsService.class
                .getDeclaredMethod("resolveBestMatchingLabel", String.class, List.class, List.class);
        resolveBest.setAccessible(true);
        String best = (String) resolveBest.invoke(service, "anything", List.of(term), List.of(badPattern));
        assertEquals(null, best);
    }

    private static WidgetEntry widget(String id, String displayName) {
        String safeDisplay = displayName == null ? "" : displayName;
        return new WidgetEntry(1, id, safeDisplay, Instant.now());
    }

    private static ResultSet mockCountResultSet(int count) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        AtomicInteger idx = new AtomicInteger(-1);
        when(rs.next()).thenAnswer(invocation -> idx.incrementAndGet() == 0);
        when(rs.getInt(1)).thenReturn(count);
        return rs;
    }

    private static ResultSet mockRowsResultSet(List<Map<String, Object>> rows) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        AtomicInteger idx = new AtomicInteger(-1);

        when(rs.next()).thenAnswer(invocation -> {
            int row = idx.incrementAndGet();
            return row < rows.size();
        });

        when(rs.getString(anyString())).thenAnswer(invocation -> {
            int row = idx.get();
            if (row < 0 || row >= rows.size()) {
                return null;
            }
            Object value = rows.get(row).get(invocation.getArgument(0, String.class));
            return value == null ? null : String.valueOf(value);
        });

        when(rs.getTimestamp(anyString())).thenAnswer(invocation -> {
            int row = idx.get();
            if (row < 0 || row >= rows.size()) {
                return null;
            }
            Object value = rows.get(row).get(invocation.getArgument(0, String.class));
            return value instanceof Timestamp ? (Timestamp) value : null;
        });

        return rs;
    }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }

    private static void reset(Object... mocks) {
        Mockito.reset(mocks);
    }
}
