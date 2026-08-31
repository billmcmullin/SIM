package com.sim.chatserver.web.dashboard.trends;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardTrendsSelectServletTest {

    @Test
    void doPost_unauthenticated_returnsUnauthorized() {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            servlet.doPost(req, resp);
            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required."));
        }
    }

    @Test
    void doPost_invalidContentLength_returnsBadRequest() {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getContentLengthLong()).thenReturn(-1L);

        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            servlet.doPost(req, resp);
            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload."));
        }
    }

    @Test
    void doPost_nullOrMissingDayPayload_returnsBadRequest() {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getContentLengthLong()).thenReturn(20L);

        try (MockedStatic<JsonRequestParserUtil> parser = Mockito.mockStatic(JsonRequestParserUtil.class);
             MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024)).thenReturn(null);
            servlet.doPost(req, resp);

            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024)).thenReturn(Json.createObjectBuilder().build());
            servlet.doPost(req, resp);

            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024))
                    .thenReturn(Json.createObjectBuilder().add("day", " ").build());
            servlet.doPost(req, resp);

            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload."), Mockito.times(2));
            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "day is required (yyyy-MM-dd)."));
        }
    }

    @Test
    void doPost_invalidDayFormat_returnsBadRequest() {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getContentLengthLong()).thenReturn(32L);

        try (MockedStatic<JsonRequestParserUtil> parser = Mockito.mockStatic(JsonRequestParserUtil.class);
             MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024))
                    .thenReturn(Json.createObjectBuilder().add("day", "not-a-date").build());

            servlet.doPost(req, resp);

            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid day format. Use yyyy-MM-dd."));
        }
    }

    @Test
    void doPost_queryFailure_returnsInternalServerError() {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getContentLengthLong()).thenReturn(64L);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenThrow(new IllegalStateException("no cdi"));

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
             MockedStatic<JsonRequestParserUtil> parser = Mockito.mockStatic(JsonRequestParserUtil.class);
             MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024))
                    .thenReturn(Json.createObjectBuilder().add("day", "2026-08-27").build());

            servlet.doPost(req, resp);

            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to collect chats for day."));
        }
    }

    @Test
    void doPost_noChatsFound_returnsNotFound() throws Exception {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getContentLengthLong()).thenReturn(64L);

        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        when(holder.getDataSource()).thenReturn(ds);

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(holder);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
             MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<JsonRequestParserUtil> parser = Mockito.mockStatic(JsonRequestParserUtil.class);
             MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            widgetStoreStatic.when(() -> WidgetStore.list(null)).thenReturn(List.of());
            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024))
                    .thenReturn(Json.createObjectBuilder().add("day", "2026-08-27").build());

            servlet.doPost(req, resp);

            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for selected day."));
        }
    }

    @Test
    void doPost_success_returnsSelectionJson() throws Exception {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getContentLengthLong()).thenReturn(64L);
        when(req.getContextPath()).thenReturn("/ctx");
        when(session.getAttribute("widgetReviewSelectionMap")).thenReturn(null);

        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("widget_chat_id")).thenReturn("chat-1");
        when(rs.getString("prompt")).thenReturn("p");
        when(rs.getString("response_text")).thenReturn("r");
        when(rs.getString("session_id")).thenReturn("s1");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-08-27T12:00:00Z")));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        when(holder.getDataSource()).thenReturn(ds);

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(holder);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        WidgetEntry widget = com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(1, "w1", "Widget One", Instant.parse("2026-08-27T00:00:00Z"));

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
             MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtilStatic = Mockito.mockStatic(DashboardDbUtil.class);
             MockedStatic<JsonRequestParserUtil> parser = Mockito.mockStatic(JsonRequestParserUtil.class);
             MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            widgetStoreStatic.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget));
            dbUtilStatic.when(DashboardDbUtil::newRequestTableCache).thenReturn(new java.util.HashMap<>());
            dbUtilStatic.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("widget_w1");
            dbUtilStatic.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class))).thenReturn(true);
            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024))
                    .thenReturn(Json.createObjectBuilder().add("day", "2026-08-27").add("widgetId", "").build());

            servlet.doPost(req, resp);

            jsonUtil.verify(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp), Mockito.eq(HttpServletResponse.SC_OK), Mockito.any(JsonObject.class)));
        }
    }

    @Test
    void doPost_selectionFailure_returnsInternalServerError() throws Exception {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getContentLengthLong()).thenReturn(64L);
        when(req.getContextPath()).thenReturn("/ctx");

        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(ds.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("widget_chat_id")).thenReturn("chat-1");
        when(rs.getString("prompt")).thenReturn("p");
        when(rs.getString("response_text")).thenReturn("r");
        when(rs.getString("session_id")).thenReturn("s1");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-08-27T12:00:00Z")));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        when(holder.getDataSource()).thenReturn(ds);

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(holder);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        WidgetEntry widget = com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(1, "w1", "Widget One", Instant.parse("2026-08-27T00:00:00Z"));

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
             MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtilStatic = Mockito.mockStatic(DashboardDbUtil.class);
             MockedStatic<JsonRequestParserUtil> parser = Mockito.mockStatic(JsonRequestParserUtil.class);
             MockedStatic<WidgetReviewStartServlet> reviewStart = Mockito.mockStatic(WidgetReviewStartServlet.class);
             MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            widgetStoreStatic.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget));
            dbUtilStatic.when(DashboardDbUtil::newRequestTableCache).thenReturn(new java.util.HashMap<>());
            dbUtilStatic.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("widget_w1");
            dbUtilStatic.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class))).thenReturn(true);
            parser.when(() -> JsonRequestParserUtil.parseObject(req, 64 * 1024))
                    .thenReturn(Json.createObjectBuilder().add("day", "2026-08-27").build());
            reviewStart.when(() -> WidgetReviewStartServlet.createSnapshotSelection(
                    Mockito.eq(session), Mockito.anyString(), Mockito.anyList(), Mockito.anyString()))
                    .thenReturn(" ");

            servlet.doPost(req, resp);

            jsonUtil.verify(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection."));
        }
    }

    @Test
    void writeHelpers_andSanitizeForLog_privateCoverage() throws Exception {
        DashboardTrendsSelectServlet servlet = new DashboardTrendsSelectServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonUtil.when(() -> ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "bad"))
                    .thenThrow(new java.io.IOException("io"));
            when(resp.isCommitted()).thenReturn(false);

            invoke(servlet, "writeError", new Class<?>[] {HttpServletResponse.class, int.class, String.class},
                    resp, HttpServletResponse.SC_BAD_REQUEST, "bad");
            verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

        HttpServletResponse resp2 = mock(HttpServletResponse.class);
        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonUtil.when(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp2), Mockito.eq(HttpServletResponse.SC_OK), Mockito.any(JsonObject.class)))
                    .thenThrow(new java.io.IOException("io"));
            when(resp2.isCommitted()).thenReturn(false);

            invoke(servlet, "writeJson", new Class<?>[] {HttpServletResponse.class, int.class, JsonObject.class},
                    resp2, HttpServletResponse.SC_OK, Json.createObjectBuilder().add("status", "ok").build());
            verify(resp2).sendError(HttpServletResponse.SC_OK);
        }

        assertEquals("", invoke(servlet, "sanitizeForLog", new Class<?>[] {String.class}, new Object[] {null}));
        assertEquals("a_b", invoke(servlet, "sanitizeForLog", new Class<?>[] {String.class}, "a\nb"));
        String longText = "x".repeat(200);
        String sanitized = (String) invoke(servlet, "sanitizeForLog", new Class<?>[] {String.class}, longText);
        assertEquals(120, sanitized.length());

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        try (MockedStatic<ServletJsonResponseUtil> jsonUtil = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonUtil.when(() -> ServletJsonResponseUtil.writeError(committed, HttpServletResponse.SC_BAD_REQUEST, "bad"))
                    .thenThrow(new java.io.IOException("io"));
            invoke(servlet, "writeError", new Class<?>[] {HttpServletResponse.class, int.class, String.class},
                    committed, HttpServletResponse.SC_BAD_REQUEST, "bad");
            verify(committed, never()).sendError(anyInt());
        }
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
