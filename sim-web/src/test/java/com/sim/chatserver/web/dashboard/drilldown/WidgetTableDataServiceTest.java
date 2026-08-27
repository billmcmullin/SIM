package com.sim.chatserver.web.dashboard.drilldown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class WidgetTableDataServiceTest {

    @Test
    void handleGet_invalidDate_returnsBadRequest() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();
        HttpServletRequest req = authenticatedRequest();
        HttpServletResponse resp = jsonResponse();

        try (MockedStatic<ServletRequestParamUtil> reqParams = Mockito.mockStatic(ServletRequestParamUtil.class)) {
            stubFirstParam(reqParams, req, "widgetId", "widget_1");
            stubFirstParam(reqParams, req, "date", "not-a-date");

            service.handleGet(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void handleGet_missingWidget_returnsNotFound() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();
        HttpServletRequest req = authenticatedRequest();
        HttpServletResponse resp = jsonResponse();

        try (MockedStatic<ServletRequestParamUtil> reqParams = Mockito.mockStatic(ServletRequestParamUtil.class);
                MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {
            stubFirstParam(reqParams, req, "widgetId", "widget_1");
            stubFirstParam(reqParams, req, "date", null);
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of());

            service.handleGet(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void handleGet_missingWidgetTable_returnsBadRequest() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();
        HttpServletRequest req = authenticatedRequest();
        HttpServletResponse resp = jsonResponse();
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(isNull(), isNull(), anyString(), any(String[].class))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(false);

        try (MockedStatic<ServletRequestParamUtil> reqParams = Mockito.mockStatic(ServletRequestParamUtil.class);
                MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
                MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class)) {
            stubFirstParam(reqParams, req, "widgetId", "widget_1");
            stubFirstParam(reqParams, req, "date", null);
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget("widget_1")));
            bindDataSourceHolder(cdiStatic, dataSource);

            service.handleGet(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void handleGet_dbFailure_returnsInternalServerError() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();
        HttpServletRequest req = authenticatedRequest();
        HttpServletResponse resp = jsonResponse();
        DataSource dataSource = mock(DataSource.class);

        when(dataSource.getConnection()).thenThrow(new SQLException("db down"));

        try (MockedStatic<ServletRequestParamUtil> reqParams = Mockito.mockStatic(ServletRequestParamUtil.class);
                MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
                MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class)) {
            stubFirstParam(reqParams, req, "widgetId", "widget_1");
            stubFirstParam(reqParams, req, "date", null);
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget("widget_1")));
            bindDataSourceHolder(cdiStatic, dataSource);

            service.handleGet(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleGet_happyPath_writesOkJson() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();
        HttpServletRequest req = authenticatedRequest();
        HttpServletResponse resp = jsonResponse();
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement countPs = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(isNull(), isNull(), anyString(), any(String[].class))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);
        when(conn.prepareStatement(anyString())).thenReturn(countPs);
        when(countPs.executeQuery()).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getInt(1)).thenReturn(0);

        try (MockedStatic<ServletRequestParamUtil> reqParams = Mockito.mockStatic(ServletRequestParamUtil.class);
                MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
                MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class)) {
            stubFirstParam(reqParams, req, "widgetId", "widget_1");
            stubFirstParam(reqParams, req, "date", "2026-08-26");
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget("widget_1")));
            bindDataSourceHolder(cdiStatic, dataSource);

            service.handleGet(req, resp);
        }

        verify(conn).prepareStatement(anyString());
        verify(resp).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void handleGet_unhandledException_sendsFallbackServerError() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();
        HttpServletRequest req = authenticatedRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(resp.isCommitted()).thenReturn(false);

        try (MockedStatic<ServletRequestParamUtil> reqParams = Mockito.mockStatic(ServletRequestParamUtil.class)) {
            reqParams.when(() -> ServletRequestParamUtil.firstParam(req, "widgetId", 256, true, true))
                    .thenThrow(new IllegalStateException("boom"));

            service.handleGet(req, resp);
        }

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void parseHelpers_handleLimitsPagesAndSorting() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();

        assertEquals(10, invokeInt(service, "parseLimit", new Class<?>[]{String.class}, (Object) null));
        assertEquals(25, invokeInt(service, "parseLimit", new Class<?>[]{String.class}, "25"));
        assertEquals(10, invokeInt(service, "parseLimit", new Class<?>[]{String.class}, "999"));

        assertEquals(1, invokeInt(service, "parsePage", new Class<?>[]{String.class}, (Object) null));
        assertEquals(3, invokeInt(service, "parsePage", new Class<?>[]{String.class}, "3"));
        assertEquals(1, invokeInt(service, "parsePage", new Class<?>[]{String.class}, "-20"));

        assertEquals("prompt", invokeString(service, "parseSortColumn", new Class<?>[]{String.class}, "PROMPT"));
        assertEquals("created_at", invokeString(service, "parseSortColumn", new Class<?>[]{String.class}, "unknown"));

        assertEquals("ASC", invokeString(service, "parseSortDirection", new Class<?>[]{String.class}, "asc"));
        assertEquals("DESC", invokeString(service, "parseSortDirection", new Class<?>[]{String.class}, "zzz"));
    }

    @Test
    void sqlIdentifierAndTableNameHelpers_areSafe() throws Exception {
        WidgetTableDataService service = new WidgetTableDataService();

        assertEquals("widget", invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, (Object) null));
        String normalized = invokeString(service, "sanitizeWidgetTableName", new Class<?>[]{String.class}, "1 bad-id");
        assertTrue(normalized.startsWith("w_"));

        assertEquals("\"good_name\"",
                invokeString(service, "quoteIdentifier", new Class<?>[]{String.class}, "good_name"));
    }

    @Test
    void filterState_buildsExpectedWhereClauseAndParams() throws Exception {
        Class<?> filterStateClass = Class.forName("com.sim.chatserver.web.dashboard.drilldown.WidgetTableDataService$FilterState");
        Constructor<?> ctor = filterStateClass.getDeclaredConstructor(String.class, String.class, String.class, LocalDate.class);
        ctor.setAccessible(true);

        Object filterState = ctor.newInstance("promptTerm", "responseTerm", "globalTerm", LocalDate.of(2026, 8, 26));

        Method buildWhere = filterStateClass.getDeclaredMethod("buildWhereClause");
        buildWhere.setAccessible(true);
        String where = (String) buildWhere.invoke(filterState);

        assertTrue(where.contains("prompt ILIKE ?"));
        assertTrue(where.contains("response_text ILIKE ?"));
        assertTrue(where.contains("session_id ILIKE ?"));
        assertTrue(where.contains("created_at >= ? AND created_at < ?"));

        Method paramsMethod = filterStateClass.getDeclaredMethod("params");
        paramsMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> params = (List<Object>) paramsMethod.invoke(filterState);

        assertEquals(7, params.size());
        assertEquals("%promptTerm%", params.get(0));
        assertEquals("%responseTerm%", params.get(1));
        assertEquals("%globalTerm%", params.get(2));
        assertTrue(params.get(5) instanceof Timestamp);
        assertTrue(params.get(6) instanceof Timestamp);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void bindDataSourceHolder(MockedStatic<CDI> cdiStatic, DataSource dataSource) {
        CDI<Object> cdi = mock(CDI.class);
        Instance<AppDataSourceHolder> holderInstance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);

        when(cdi.select(AppDataSourceHolder.class)).thenReturn((Instance) holderInstance);
        when(holderInstance.get()).thenReturn(holder);
        when(holder.getDataSource()).thenReturn(dataSource);
        cdiStatic.when(CDI::current).thenReturn(cdi);
    }

    private HttpServletRequest authenticatedRequest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("tester");
        return req;
    }

    private HttpServletResponse jsonResponse() throws Exception {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        return resp;
    }

    private WidgetEntry widget(String widgetId) {
        return new WidgetEntry(1, widgetId, "Widget " + widgetId, Instant.parse("2026-01-01T00:00:00Z"));
    }

    private void stubFirstParam(MockedStatic<ServletRequestParamUtil> reqParams,
            HttpServletRequest req,
            String name,
            String value) {
        reqParams.when(() -> ServletRequestParamUtil.firstParam(req, name, 256, true, true)).thenReturn(value);
    }

    private Object invokeObject(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private int invokeInt(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return ((Integer) invokeObject(target, methodName, paramTypes, args)).intValue();
    }

    private String invokeString(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        return (String) invokeObject(target, methodName, paramTypes, args);
    }
}
