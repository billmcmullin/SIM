package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.widget.WidgetEntry;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for DashboardMetricsService
 *
 * @see com.sim.chatserver.service.dashboard.DashboardMetricsService
 * @author bmcmullin
 */
public class DashboardMetricsServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.buildChatProgression(widgets);

        // Then - assertions for result of method buildChatProgression(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression2() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        ProgressStat result = underTest.buildChatProgression(widgets);

        // Then - assertions for result of method buildChatProgression(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression3() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.buildChatProgression(widgets);

        // Then - assertions for result of method buildChatProgression(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildDashboardProgressMetrics(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildDashboardProgressMetrics(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildDashboardProgressMetrics() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        DashboardProgressMetrics result = underTest.buildDashboardProgressMetrics(widgets);

        // Then - assertions for result of method buildDashboardProgressMetrics(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getChatsToday());
        }, () -> {
            assertEquals(0, result.getChatsYesterday());
        }, () -> {
            assertNotNull(result.getChatsProgression());
        }, () -> {
            assertEquals(0, result.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, result.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(0, result.getTermsToday());
        }, () -> {
            assertEquals(0, result.getTermsYesterday());
        }, () -> {
            assertNotNull(result.getTermsProgression());
        }, () -> {
            assertEquals(0, result.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, result.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildDashboardProgressMetrics(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildDashboardProgressMetrics(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildDashboardProgressMetrics2() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        DashboardProgressMetrics result = underTest.buildDashboardProgressMetrics(widgets);

        // Then - assertions for result of method buildDashboardProgressMetrics(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getChatsToday());
        }, () -> {
            assertEquals(0, result.getChatsYesterday());
        }, () -> {
            assertNotNull(result.getChatsProgression());
        }, () -> {
            assertEquals(0, result.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, result.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(0, result.getTermsToday());
        }, () -> {
            assertEquals(0, result.getTermsYesterday());
        }, () -> {
            assertNotNull(result.getTermsProgression());
        }, () -> {
            assertEquals(0, result.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(0.0d, result.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        int limit = 1; // UTA: default value
        List<OtherParasoftEntry> result = underTest.buildLatestOtherParasoftEntries(widgets, limit);

        // Then - assertions for result of method buildLatestOtherParasoftEntries(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries2() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.buildLatestOtherParasoftEntries(widgets, limit);

        // Then - assertions for result of method buildLatestOtherParasoftEntries(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries3() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getDisplayNameResult = null; // UTA: configured value
        when(item.getDisplayName()).thenReturn(getDisplayNameResult);

        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.buildLatestOtherParasoftEntries(widgets, limit);

        // Then - assertions for result of method buildLatestOtherParasoftEntries(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries4() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        Timestamp getTimestampResult = null; // UTA: configured value
        when(executeQueryResult.getTimestamp(nullable(String.class))).thenReturn(getTimestampResult);

        boolean nextResult = true; // UTA: configured value
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult, nextResult2);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getDisplayNameResult = null; // UTA: configured value
        when(item.getDisplayName()).thenReturn(getDisplayNameResult);

        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.buildLatestOtherParasoftEntries(widgets, limit);

        // Then - assertions for result of method buildLatestOtherParasoftEntries(List, int)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries5() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.buildLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.buildNewUserProgression(widgets);

        // Then - assertions for result of method buildNewUserProgression(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression2() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        ProgressStat result = underTest.buildNewUserProgression(widgets);

        // Then - assertions for result of method buildNewUserProgression(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression3() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.buildNewUserProgression(widgets);

        // Then - assertions for result of method buildNewUserProgression(List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(0, result.getDelta());
        }, () -> {
            assertEquals(0.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("flat", result.getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        List<TopTopic> result = underTest.buildTopTopicsTodayVsYesterday(widgets);

        // Then - assertions for result of method buildTopTopicsTodayVsYesterday(List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday2() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = null; // UTA: configured value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TopTopic> result = underTest.buildTopTopicsTodayVsYesterday(widgets);

        // Then - assertions for result of method buildTopTopicsTodayVsYesterday(List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday3() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        List<TopTopic> result = underTest.buildTopTopicsTodayVsYesterday(widgets);

        // Then - assertions for result of method buildTopTopicsTodayVsYesterday(List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        List<WidgetStat> result = underTest.buildWidgetStats(widgets);

        // Then - assertions for result of method buildWidgetStats(List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats2() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<WidgetStat> result = underTest.buildWidgetStats(widgets);

        // Then - assertions for result of method buildWidgetStats(List)
        assertNotNull(result);
        assertEquals(0, result.size());

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats3() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        String getCatalogResult = null; // UTA: configured value
        when(getConnectionResult.getCatalog()).thenReturn(getCatalogResult);

        DatabaseMetaData getMetaDataResult = mock(DatabaseMetaData.class);
        ResultSet getTablesResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(getTablesResult.next()).thenReturn(nextResult);
        when(getMetaDataResult.getTables(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String[].class))).thenReturn(getTablesResult);
        when(getConnectionResult.getMetaData()).thenReturn(getMetaDataResult);

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult2 = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult2);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        ResultSet executeQueryResult2 = mock(ResultSet.class);
        boolean nextResult3 = false; // UTA: configured value
        when(executeQueryResult2.next()).thenReturn(nextResult3);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult2);
        PreparedStatement prepareStatementResult3 = mock(PreparedStatement.class);
        ResultSet executeQueryResult3 = mock(ResultSet.class);
        boolean nextResult4 = false; // UTA: configured value
        when(executeQueryResult3.next()).thenReturn(nextResult4);
        when(prepareStatementResult3.executeQuery()).thenReturn(executeQueryResult3);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult, prepareStatementResult2, prepareStatementResult3);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = new DashboardMetricsService(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getDisplayNameResult = null; // UTA: configured value
        when(item.getDisplayName()).thenReturn(getDisplayNameResult);

        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<WidgetStat> result = underTest.buildWidgetStats(widgets);

        // Then - assertions for result of method buildWidgetStats(List)
        assertNotNull(result);
        assertEquals(1, result.size());

    }

}
