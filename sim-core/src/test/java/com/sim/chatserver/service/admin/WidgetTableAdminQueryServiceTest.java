package com.sim.chatserver.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

class WidgetTableAdminQueryServiceTest {

    @Test
    void checkTable_usesMetadataAndReturnsCount() throws Exception {
        WidgetTableAdminQueryService service = new WidgetTableAdminQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);

        ResultSet tables = mock(ResultSet.class);
        when(tables.next()).thenReturn(true);
        when(meta.getTables(any(), any(), anyString(), any())).thenReturn(tables);

        PreparedStatement countPs = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(countRs.next()).thenReturn(true);
        when(countRs.getLong(1)).thenReturn(5L);
        when(countPs.executeQuery()).thenReturn(countRs);

        when(conn.getMetaData()).thenReturn(meta);
        when(conn.prepareStatement(anyString())).thenReturn(countPs);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            WidgetTableAdminQueryService.CheckResult result = service.checkTable("widget_table_1");
            assertTrue(result.exists);
            assertEquals(Long.valueOf(5L), result.count);
            assertEquals("Table is accessible.", result.message);
            assertFalse(result.created);
        }
    }

    @Test
    void checkTable_whenMissing_returnsNotExists() throws Exception {
        WidgetTableAdminQueryService service = new WidgetTableAdminQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);

        ResultSet tables = mock(ResultSet.class);
        when(tables.next()).thenReturn(false);
        when(meta.getTables(any(), any(), anyString(), any())).thenReturn(tables);

        PreparedStatement probePs = mock(PreparedStatement.class);
        SQLException missing = new SQLException("missing", "42P01");
        when(probePs.execute()).thenThrow(missing);

        when(conn.getMetaData()).thenReturn(meta);
        when(conn.prepareStatement(anyString())).thenReturn(probePs);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            WidgetTableAdminQueryService.CheckResult result = service.checkTable("widget_table_1");
            assertFalse(result.exists);
            assertEquals(null, result.count);
            assertEquals("Table does not exist.", result.message);
        }
    }

    @Test
    void createTableIfMissing_createsThenCountsRows() throws Exception {
        WidgetTableAdminQueryService service = new WidgetTableAdminQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);

        ResultSet tables = mock(ResultSet.class);
        when(tables.next()).thenReturn(false);
        when(meta.getTables(any(), any(), anyString(), any())).thenReturn(tables);

        PreparedStatement probePs = mock(PreparedStatement.class);
        SQLException missing = new SQLException("missing", "42P01");
        when(probePs.execute()).thenThrow(missing);

        PreparedStatement createPs = mock(PreparedStatement.class);
        when(createPs.execute()).thenReturn(true);

        PreparedStatement countPs = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(countRs.next()).thenReturn(true);
        when(countRs.getLong(1)).thenReturn(0L);
        when(countPs.executeQuery()).thenReturn(countRs);

        when(conn.getMetaData()).thenReturn(meta);
        when(conn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.startsWith("SELECT 1 FROM")) {
                return probePs;
            }
            if (sql.startsWith("CREATE TABLE")) {
                return createPs;
            }
            return countPs;
        });

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            WidgetTableAdminQueryService.CheckResult result = service.createTableIfMissing("widget_table_1");
            assertTrue(result.exists);
            assertTrue(result.created);
            assertEquals(Long.valueOf(0L), result.count);
            assertEquals("Table created successfully.", result.message);
        }
    }

    @Test
    void checkTables_emptyInput_returnsEmptyMap() {
        WidgetTableAdminQueryService service = new WidgetTableAdminQueryService(Logger.getLogger("test"));
        Map<String, WidgetTableAdminQueryService.CheckResult> result = service.checkTables(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void checkTables_wrapsConnectionFailure() throws Exception {
        WidgetTableAdminQueryService service = new WidgetTableAdminQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("boom"));

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            assertThrows(IllegalStateException.class,
                    () -> service.checkTables(List.of("widget_table_1")));
        }
    }

    @Test
    void checkTable_rejectsInvalidIdentifier() throws Exception {
        WidgetTableAdminQueryService service = new WidgetTableAdminQueryService(Logger.getLogger("test"));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tables = mock(ResultSet.class);

        when(tables.next()).thenReturn(false);
        when(meta.getTables(any(), any(), anyString(), any())).thenReturn(tables);
        when(conn.getMetaData()).thenReturn(meta);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);

        try (MockedStatic<CDI> cdi = mockCdi(holder)) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.checkTable("bad-name"));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static MockedStatic<CDI> mockCdi(AppDataSourceHolder holder) {
        MockedStatic<CDI> cdi = Mockito.mockStatic(CDI.class);
        CDI<Object> current = mock(CDI.class);
        Instance<AppDataSourceHolder> instance = mock(Instance.class);
        when(instance.get()).thenReturn(holder);
        when(current.select(AppDataSourceHolder.class)).thenReturn((Instance) instance);
        cdi.when(CDI::current).thenReturn(current);
        return cdi;
    }
}