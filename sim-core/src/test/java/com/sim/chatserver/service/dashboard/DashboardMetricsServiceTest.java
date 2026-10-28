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

import com.sim.chatserver.model.OtherParasoftEntry;
import com.sim.chatserver.model.ProgressStat;
import com.sim.chatserver.model.TopTopic;
import com.sim.chatserver.model.WidgetStat;
import com.sim.chatserver.service.dashboard.DashboardProgressMetrics;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.widget.WidgetEntry;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for DashboardMetricsService
 *
 * @see com.sim.chatserver.service.dashboard.DashboardMetricsService
 * @author bmcmullin
 */
public class DashboardMetricsServiceTest
{
    @Test
    public void sharedDatePredicateSupportsTextAndNativeTimestampColumns() throws Exception {
        try (Connection connection = java.sql.DriverManager.getConnection(
                "jdbc:h2:mem:dashboard_date_range;MODE=PostgreSQL")) {
            for (String columnType : List.of("VARCHAR(80)", "TIMESTAMP")) {
                try (java.sql.Statement statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE widget_dates (created_at " + columnType + ")");
                    statement.execute("INSERT INTO widget_dates VALUES ('2026-10-26 23:59:59'), "
                            + "('2026-10-27 00:00:00'), ('2026-10-27 23:59:59'), ('2026-10-28 00:00:00')");
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM widget_dates"
                                + com.sim.chatserver.util.SqlLiteralConstants.SQL_WHERE_CREATED_AT_RANGE)) {
                    statement.setTimestamp(1, Timestamp.valueOf("2026-10-27 00:00:00"));
                    statement.setTimestamp(2, Timestamp.valueOf("2026-10-28 00:00:00"));
                    try (ResultSet rows = statement.executeQuery()) {
                        assertTrue(rows.next());
                        assertEquals(2, rows.getInt(1), columnType);
                    }
                }
                try (java.sql.Statement statement = connection.createStatement()) {
                    statement.execute("DROP TABLE widget_dates");
                }
            }
        }
    }

    @Test
    public void dateRangeCountSupportsTextTimestampColumns() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rows = mock(ResultSet.class);
        when(connection.prepareStatement(nullable(String.class))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (!sql.contains("CAST(created_at AS TIMESTAMP)")) {
                throw new SQLException("operator does not exist: text >= timestamp without time zone");
            }
            return statement;
        });
        when(statement.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true);
        when(rows.getInt(1)).thenReturn(3);

        DashboardMetricsService service = DashboardMetricsService.createForDashboard(
                mock(AppDataSourceHolder.class), mock(TermsStore.class), 3);
        java.lang.reflect.Method countRowsBetween = DashboardMetricsService.class.getDeclaredMethod(
                "countRowsBetween", Connection.class, String.class, Timestamp.class, Timestamp.class);
        countRowsBetween.setAccessible(true);

        assertEquals(3, countRowsBetween.invoke(service, connection, "legacy_widget",
                Timestamp.valueOf("2026-10-27 00:00:00"), Timestamp.valueOf("2026-10-28 00:00:00")));
    }

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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.collectChatProgression(widgets);

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
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.collectChatProgression(widgets);

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
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression4() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression5() throws Throwable
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
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression6() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        int getIntResult = 0; // UTA: configured value
        when(executeQueryResult.getInt(anyInt())).thenReturn(getIntResult);

        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression7() throws Throwable
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
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression8() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression9() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression10() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression11() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression12() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression13() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult).setTimestamp(anyInt(), nullable(Timestamp.class));
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression14() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        when(executeQueryResult.next()).thenThrow(SQLException.class);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression15() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression16() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult).close();

        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression17() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        doThrow(SQLException.class).when(prepareStatementResult).close();

        ResultSet executeQueryResult = mock(ResultSet.class);
        doThrow(SQLException.class).when(executeQueryResult).close();

        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildChatProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildChatProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildChatProgression18() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        doThrow(SQLException.class).when(getConnectionResult).close();

        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = false; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectChatProgression(widgets);

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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        DashboardProgressMetrics result = underTest.collectDashboardProgressMetrics(widgets);

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
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        DashboardProgressMetrics result = underTest.collectDashboardProgressMetrics(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildDashboardProgressMetrics(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildDashboardProgressMetrics(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildDashboardProgressMetrics3() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        DashboardProgressMetrics result = underTest.collectDashboardProgressMetrics(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildDashboardProgressMetrics(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildDashboardProgressMetrics(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildDashboardProgressMetrics4() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        DashboardProgressMetrics result = underTest.collectDashboardProgressMetrics(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildDashboardProgressMetrics(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildDashboardProgressMetrics(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildDashboardProgressMetrics5() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        DashboardProgressMetrics result = underTest.collectDashboardProgressMetrics(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildDashboardProgressMetrics(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildDashboardProgressMetrics(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildDashboardProgressMetrics6() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        DashboardProgressMetrics result = underTest.collectDashboardProgressMetrics(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildDashboardProgressMetrics(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildDashboardProgressMetrics(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildDashboardProgressMetrics7() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        DashboardProgressMetrics result = underTest.collectDashboardProgressMetrics(widgets);

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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        int limit = 1; // UTA: default value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

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
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        int limit = 1; // UTA: default value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

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
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        int limit = 0; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

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
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

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
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries6() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries7() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries8() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries9() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries10() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries11() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

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
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries12() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

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
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries13() throws Throwable
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
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

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
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

    }

    /**
     * Parasoft Jtest UTA: Test for buildLatestOtherParasoftEntries(List, int)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildLatestOtherParasoftEntries(List, int)
     * @author bmcmullin
     */
    @Test
    public void testBuildLatestOtherParasoftEntries14() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        int limit = 1; // UTA: configured value
        List<OtherParasoftEntry> result = underTest.collectLatestOtherParasoftEntries(widgets, limit);

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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.collectNewUserProgression(widgets);

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
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.collectNewUserProgression(widgets);

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
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectNewUserProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression4() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectNewUserProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression5() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        ProgressStat result = underTest.collectNewUserProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression6() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectNewUserProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression7() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        ProgressStat result = underTest.collectNewUserProgression(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildNewUserProgression(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildNewUserProgression(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildNewUserProgression8() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        ProgressStat result = underTest.collectNewUserProgression(widgets);

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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

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
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

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
        List<TermDefinition> listAllResult = null; // UTA: configured value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday4() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday5() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        TermDefinition item = mock(TermDefinition.class);
        listAllResult.add(item);
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday6() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday7() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday8() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday9() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday10() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday11() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday12() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        String getWidgetIdResult2 = "getWidgetIdResult2"; // UTA: default value
        when(item2.getWidgetId()).thenReturn(getWidgetIdResult2);
        widgets.add(item2);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday13() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday14() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday15() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday16() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        String getWidgetIdResult3 = "getWidgetIdResult3"; // UTA: default value
        when(item2.getWidgetId()).thenReturn(getWidgetIdResult3);
        widgets.add(item2);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday17() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday18() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        String getWidgetIdResult3 = "getWidgetIdResult3"; // UTA: default value
        when(item2.getWidgetId()).thenReturn(getWidgetIdResult3);
        widgets.add(item2);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday19() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        String getStringResult = "getStringResult"; // UTA: default value
        when(executeQueryResult.getString(nullable(String.class))).thenReturn(getStringResult);

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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday20() throws Throwable
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
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday21() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday22() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        String getWidgetIdResult2 = "getWidgetIdResult2"; // UTA: default value
        when(item2.getWidgetId()).thenReturn(getWidgetIdResult2);
        widgets.add(item2);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday23() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday24() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        String getWidgetIdResult3 = "getWidgetIdResult3"; // UTA: default value
        when(item2.getWidgetId()).thenReturn(getWidgetIdResult3);
        widgets.add(item2);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildTopTopicsTodayVsYesterday(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildTopTopicsTodayVsYesterday(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildTopTopicsTodayVsYesterday25() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        List<TermDefinition> listAllResult = new ArrayList<TermDefinition>(); // UTA: default value
        doReturn(listAllResult).when(termsStore).listAll();
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<TopTopic> result = underTest.collectTopTopicsTodayVsYesterday(widgets);

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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = null; // UTA: configured value
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

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
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

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
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats4() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats5() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats6() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats7() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats8() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = null; // UTA: configured value
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getWidgetIdResult = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult);
        widgets.add(item);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats9() throws Throwable
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
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getDisplayNameResult = null; // UTA: configured value
        when(item.getDisplayName()).thenReturn(getDisplayNameResult);

        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats10() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        widgets.add(item);
        WidgetEntry item2 = mock(WidgetEntry.class);
        widgets.add(item2);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats11() throws Throwable
    {
        // Given
        AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
        DataSource getDataSourceResult = mock(DataSource.class);
        Connection getConnectionResult = mock(Connection.class);
        PreparedStatement prepareStatementResult = mock(PreparedStatement.class);
        ResultSet executeQueryResult = mock(ResultSet.class);
        boolean nextResult = true; // UTA: configured value
        when(executeQueryResult.next()).thenReturn(nextResult);
        when(prepareStatementResult.executeQuery()).thenReturn(executeQueryResult);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getDisplayNameResult = null; // UTA: configured value
        when(item.getDisplayName()).thenReturn(getDisplayNameResult);

        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }

    /**
     * Parasoft Jtest UTA: Test for buildWidgetStats(List)
     *
     * @see com.sim.chatserver.service.dashboard.DashboardMetricsService#buildWidgetStats(List)
     * @author bmcmullin
     */
    @Test
    public void testBuildWidgetStats12() throws Throwable
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
        PreparedStatement prepareStatementResult2 = mock(PreparedStatement.class);
        ResultSet executeQueryResult2 = mock(ResultSet.class);
        boolean nextResult2 = true; // UTA: configured value
        when(executeQueryResult2.next()).thenReturn(nextResult2);
        when(prepareStatementResult2.executeQuery()).thenReturn(executeQueryResult2);
        when(getConnectionResult.prepareStatement(nullable(String.class))).thenReturn(prepareStatementResult, prepareStatementResult2);
        when(getDataSourceResult.getConnection()).thenReturn(getConnectionResult);
        when(dsHolder.getDataSource()).thenReturn(getDataSourceResult);
        TermsStore termsStore = mock(TermsStore.class);
        int topTopicLimit = 1; // UTA: default value
        DashboardMetricsService underTest = DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit);

        // When
        List<WidgetEntry> widgets = new ArrayList<WidgetEntry>(); // UTA: default value
        WidgetEntry item = mock(WidgetEntry.class);
        String getDisplayNameResult = null; // UTA: configured value
        when(item.getDisplayName()).thenReturn(getDisplayNameResult);

        String getWidgetIdResult = "getWidgetIdResult"; // UTA: default value
        String getWidgetIdResult2 = null; // UTA: configured value
        when(item.getWidgetId()).thenReturn(getWidgetIdResult, getWidgetIdResult2);
        widgets.add(item);
        List<WidgetStat> result = underTest.collectWidgetStats(widgets);

    }


    /**
     * Consolidated from DashboardMetricsService_DashboardProgressMetricsTest.java to keep one test class per production source file.
     */
    /**
     * Parasoft Jtest UTA: Test for getChatsProgression()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardProgressMetrics#getChatsProgression()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsProgression() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = DashboardProgressMetrics.of(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        ProgressStat result = underTest.getChatsProgression();

        // Then - assertions for result of method getChatsProgression()
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(2, result.getDelta());
        }, () -> {
            assertEquals(-200.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", result.getDirection());
        });

        // Then - assertions for this instance of DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.chatsToday());
        }, () -> {
            assertEquals(-1, underTest.chatsYesterday());
        }, () -> {
            assertEquals(1, underTest.termsToday());
        }, () -> {
            assertEquals(-1, underTest.termsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatsToday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardProgressMetrics#getChatsToday()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsToday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = DashboardProgressMetrics.of(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.chatsToday();

        // Then - assertions for result of method getChatsToday()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(-1, underTest.chatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.termsToday());
        }, () -> {
            assertEquals(-1, underTest.termsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getChatsYesterday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardProgressMetrics#getChatsYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetChatsYesterday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = DashboardProgressMetrics.of(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.chatsYesterday();

        // Then - assertions for result of method getChatsYesterday()
        assertEquals(-1, result);

        // Then - assertions for this instance of DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.chatsToday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.termsToday());
        }, () -> {
            assertEquals(-1, underTest.termsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermsProgression()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardProgressMetrics#getTermsProgression()
     * @author bmcmullin
     */
    @Test
    public void testGetTermsProgression() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = DashboardProgressMetrics.of(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        ProgressStat result = underTest.getTermsProgression();

        // Then - assertions for result of method getTermsProgression()
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals(2, result.getDelta());
        }, () -> {
            assertEquals(-200.0d, result.getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", result.getDirection());
        });

        // Then - assertions for this instance of DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.chatsToday());
        }, () -> {
            assertEquals(-1, underTest.chatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.termsToday());
        }, () -> {
            assertEquals(-1, underTest.termsYesterday());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermsToday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardProgressMetrics#getTermsToday()
     * @author bmcmullin
     */
    @Test
    public void testGetTermsToday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = DashboardProgressMetrics.of(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.termsToday();

        // Then - assertions for result of method getTermsToday()
        assertEquals(1, result);

        // Then - assertions for this instance of DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.chatsToday());
        }, () -> {
            assertEquals(-1, underTest.chatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(-1, underTest.termsYesterday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermsYesterday()
     *
     * @see com.sim.chatserver.service.dashboard.DashboardProgressMetrics#getTermsYesterday()
     * @author bmcmullin
     */
    @Test
    public void testGetTermsYesterday() throws Throwable
    {
        // Given
        int chatsToday = 1; // UTA: configured value
        int chatsYesterday = -1; // UTA: configured value
        int termsToday = 1; // UTA: configured value
        int termsYesterday = -1; // UTA: configured value
        DashboardProgressMetrics underTest = DashboardProgressMetrics.of(chatsToday, chatsYesterday, termsToday, termsYesterday);

        // When
        int result = underTest.termsYesterday();

        // Then - assertions for result of method getTermsYesterday()
        assertEquals(-1, result);

        // Then - assertions for this instance of DashboardProgressMetrics
        assertAll(() -> {
            assertEquals(1, underTest.chatsToday());
        }, () -> {
            assertEquals(-1, underTest.chatsYesterday());
        }, () -> {
            assertNotNull(underTest.getChatsProgression());
        }, () -> {
            assertEquals(2, underTest.getChatsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getChatsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getChatsProgression().getDirection());
        }, () -> {
            assertEquals(1, underTest.termsToday());
        }, () -> {
            assertNotNull(underTest.getTermsProgression());
        }, () -> {
            assertEquals(2, underTest.getTermsProgression().getDelta());
        }, () -> {
            assertEquals(-200.0d, underTest.getTermsProgression().getPctDelta(), 0.0);
        }, () -> {
            assertEquals("up", underTest.getTermsProgression().getDirection());
        });

    }
}




