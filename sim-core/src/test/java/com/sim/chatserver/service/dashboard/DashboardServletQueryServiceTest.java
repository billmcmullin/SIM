package com.sim.chatserver.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;

class DashboardServletQueryServiceTest {

    private MockedStatic<CDI> cdiMock;

    @AfterEach
    void tearDown() {
        if (cdiMock != null) {
            cdiMock.close();
            cdiMock = null;
        }
    }

    @Test
    void quoteIdentifier_allowsSimpleNamesAndRejectsInvalidNames() throws Exception {
        DashboardServletQueryService service = new DashboardServletQueryService(Logger.getLogger("test"));

        assertEquals("\"widget_1\"", invokeQuoteIdentifier(service, "widget_1"));

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> invokePrivate(service, "quoteIdentifier", new Class<?>[] { String.class }, "bad-name"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void loadTermSummary_returnsDelegateValue() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        mockDataSourceHolderCdi(dataSource);

        DashboardServletQueryService service = new DashboardServletQueryService(Logger.getLogger("test"));
        DashboardTermService termService = mock(DashboardTermService.class);

        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 24);
        List<WidgetEntry> widgets = List.of(new WidgetEntry(1, "widget_1", "Widget 1", Instant.now()));
        List<TermDefinition> terms = List.of(mock(TermDefinition.class));
        TermSummary expected = new TermSummary();

        when(termService.loadAllTerms()).thenReturn(terms);
        when(termService.buildTermSummary(conn, widgets, terms, start, end)).thenReturn(expected);

        TermSummary actual = service.loadTermSummary(termService, widgets, start, end);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void loadTermSummary_returnsNullOnFailure() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        mockDataSourceHolderCdi(dataSource);

        DashboardServletQueryService service = new DashboardServletQueryService(Logger.getLogger("test"));
        DashboardTermService termService = mock(DashboardTermService.class);
        when(termService.loadAllTerms()).thenThrow(new IllegalStateException("db down"));

        TermSummary actual = service.loadTermSummary(termService, List.of(), LocalDate.now(), LocalDate.now());
        assertNull(actual);
    }

    @Test
    void loadSessionOverview_returnsDelegateValue() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        mockDataSourceHolderCdi(dataSource);

        DashboardServletQueryService service = new DashboardServletQueryService(Logger.getLogger("test"));
        DashboardSessionService sessionService = mock(DashboardSessionService.class);

        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 24);
        int activeDays = 7;
        List<WidgetEntry> widgets = List.of(new WidgetEntry(1, "widget_1", "Widget 1", Instant.now()));

        SessionOverview expected = new SessionOverview(
                List.of(),
                new SessionTimeline(List.of(), Map.of()),
                0,
                0,
                0,
                activeDays,
                0,
                0,
                new ProgressStat(0, 0));

        when(sessionService.buildSessionOverview(conn, widgets, start, end, activeDays)).thenReturn(expected);

        SessionOverview actual = service.loadSessionOverview(sessionService, widgets, start, end, activeDays);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void buildLastFiveDaysTrendJson_returnsZeroValuesWhenConnectionFails() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("no conn"));
        mockDataSourceHolderCdi(dataSource);

        DashboardServletQueryService service = new DashboardServletQueryService(Logger.getLogger("test"));
        String json = service.buildLastFiveDaysTrendJson(List.of());

        JsonObject parsed = Json.createReader(new StringReader(json)).readObject();
        assertEquals(5, parsed.getInt("days"));
        assertEquals(5, parsed.getJsonArray("labels").size());
        assertEquals(5, parsed.getJsonArray("values").size());
    }

    @Test
    void buildLastFiveDaysTrendJson_countsRowsForExistingWidgetTable() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet dataRs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getCatalog()).thenReturn("chat");
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(any(), any(), anyString(), any(String[].class))).thenReturn(tableRs, tableRs, tableRs);
        when(tableRs.next()).thenReturn(true);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(dataRs);
        when(dataRs.next()).thenReturn(true, false);
        when(dataRs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        mockDataSourceHolderCdi(dataSource);

        DashboardServletQueryService service = new DashboardServletQueryService(Logger.getLogger("test"));
        String json = service.buildLastFiveDaysTrendJson(
                List.of(new WidgetEntry(1, "widget-1", "Widget 1", Instant.now())));

        JsonObject parsed = Json.createReader(new StringReader(json)).readObject();
        assertEquals(5, parsed.getInt("days"));
        int total = parsed.getJsonArray("values").stream().mapToInt(v -> Integer.parseInt(v.toString())).sum();
        assertTrue(total >= 1);
    }

    private void mockDataSourceHolderCdi(DataSource dataSource) {
        if (cdiMock != null) {
            cdiMock.close();
        }
        cdiMock = Mockito.mockStatic(CDI.class);

        CDI<Object> cdi = mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> dsHolderInstance = mock(Instance.class);
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);

        when(holder.getDataSource()).thenReturn(dataSource);
        when(dsHolderInstance.get()).thenReturn(holder);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(dsHolderInstance);
        cdiMock.when(CDI::current).thenReturn(cdi);
    }

    private static String invokeQuoteIdentifier(DashboardServletQueryService service, String identifier)
            throws Exception {
        return (String) invokePrivate(service, "quoteIdentifier", new Class<?>[] { String.class }, identifier);
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] signature, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
