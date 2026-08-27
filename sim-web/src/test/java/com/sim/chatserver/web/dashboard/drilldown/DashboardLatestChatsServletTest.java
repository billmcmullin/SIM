package com.sim.chatserver.web.dashboard.drilldown;

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
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardLatestChatsServletTest {

    @Test
    void doGet_unauthenticated_forwardsToLogin() throws Exception {
        DashboardLatestChatsServlet servlet = new DashboardLatestChatsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_authenticated_noChats_forwardsToDashboard() throws Exception {
        DashboardLatestChatsServlet servlet = new DashboardLatestChatsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dashboard = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getParameterValues("limit")).thenReturn(null);
        when(req.getRequestDispatcher("/dashboard")).thenReturn(dashboard);

        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        when(holder.getDataSource()).thenReturn(dataSource);

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(holder);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
             MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            widgetStoreStatic.when(() -> WidgetStore.list(null)).thenReturn(List.of());

            servlet.doGet(req, resp);
        }

        verify(req).setAttribute("latestChats", "empty");
        verify(dashboard).forward(req, resp);
    }

    @Test
    void doGet_authenticated_withChats_forwardsToReview() throws Exception {
        DashboardLatestChatsServlet servlet = new DashboardLatestChatsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher review = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getParameterValues("limit")).thenReturn(new String[] { "1" });
        when(req.getRequestDispatcher("/dashboard/widgets/drilldown/review")).thenReturn(review);
        when(session.getAttribute("widgetReviewSelectionMap")).thenReturn(null);

        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("widget_chat_id")).thenReturn("chat-1");
        when(rs.getString("prompt")).thenReturn("p");
        when(rs.getString("response_text")).thenReturn("r");
        when(rs.getString("session_id")).thenReturn("s1");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-08-27T12:00:00Z")));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        when(holder.getDataSource()).thenReturn(dataSource);

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(holder);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        WidgetEntry widget = new WidgetEntry(1, "w1", "Widget One", Instant.parse("2026-08-27T00:00:00Z"));

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
             MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtilStatic = Mockito.mockStatic(DashboardDbUtil.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            widgetStoreStatic.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget));
            dbUtilStatic.when(DashboardDbUtil::newRequestTableCache).thenReturn(new java.util.HashMap<>());
            dbUtilStatic.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("widget_w1");
            dbUtilStatic.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class)))
                    .thenReturn(true);

            servlet.doGet(req, resp);
        }

        verify(req).setAttribute(Mockito.eq("selectionId"), Mockito.anyString());
        verify(review).forward(req, resp);
    }

    @Test
    void doGet_selectionFailure_sendsInternalServerError() throws Exception {
        DashboardLatestChatsServlet servlet = new DashboardLatestChatsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("user");
        when(req.getParameterValues("limit")).thenReturn(new String[] { "2" });
        when(resp.isCommitted()).thenReturn(false);

        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("widget_chat_id")).thenReturn("chat-1");
        when(rs.getString("prompt")).thenReturn("p");
        when(rs.getString("response_text")).thenReturn("r");
        when(rs.getString("session_id")).thenReturn("s1");
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-08-27T12:00:00Z")));

        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        when(holder.getDataSource()).thenReturn(dataSource);

        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = (Instance<AppDataSourceHolder>) mock(Instance.class);
        when(instance.get()).thenReturn(holder);

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);

        WidgetEntry widget = new WidgetEntry(1, "w1", "Widget One", Instant.parse("2026-08-27T00:00:00Z"));

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class);
             MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class);
             MockedStatic<DashboardDbUtil> dbUtilStatic = Mockito.mockStatic(DashboardDbUtil.class);
             MockedStatic<WidgetReviewStartServlet> reviewStartStatic = Mockito.mockStatic(WidgetReviewStartServlet.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);
            widgetStoreStatic.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget));
            dbUtilStatic.when(DashboardDbUtil::newRequestTableCache).thenReturn(new java.util.HashMap<>());
            dbUtilStatic.when(() -> DashboardDbUtil.sanitizeWidgetTableName("w1")).thenReturn("widget_w1");
            dbUtilStatic.when(() -> DashboardDbUtil.tableExistsCached(any(Connection.class), anyString(), any(Map.class)))
                    .thenReturn(true);
            reviewStartStatic.when(() -> WidgetReviewStartServlet.createSnapshotSelection(
                    Mockito.eq(session), Mockito.eq("Latest Chats"), Mockito.anyList(), Mockito.eq("/dashboard")))
                    .thenReturn(" ");

            servlet.doGet(req, resp);
        }

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to create latest-chats selection.");
    }

    @Test
    void parseLimit_andSendErrorSafe_privateHelpers() throws Exception {
        DashboardLatestChatsServlet servlet = new DashboardLatestChatsServlet();

        assertEquals(200, invoke(servlet, "parseLimit", new Class<?>[] { String.class, int.class }, null, 200));
        assertEquals(200, invoke(servlet, "parseLimit", new Class<?>[] { String.class, int.class }, "", 200));
        assertEquals(200, invoke(servlet, "parseLimit", new Class<?>[] { String.class, int.class }, "abc", 200));
        assertEquals(200, invoke(servlet, "parseLimit", new Class<?>[] { String.class, int.class }, "-1", 200));
        assertEquals(2000, invoke(servlet, "parseLimit", new Class<?>[] { String.class, int.class }, "5000", 200));
        assertEquals(10, invoke(servlet, "parseLimit", new Class<?>[] { String.class, int.class }, "10", 200));

        HttpServletResponse committed = mock(HttpServletResponse.class);
        when(committed.isCommitted()).thenReturn(true);
        invoke(servlet, "sendErrorSafe", new Class<?>[] { HttpServletResponse.class, int.class, String.class },
                committed, HttpServletResponse.SC_BAD_REQUEST, "bad");
        verify(committed, never()).sendError(anyInt(), anyString());

        HttpServletResponse open = mock(HttpServletResponse.class);
        when(open.isCommitted()).thenReturn(false);
        invoke(servlet, "sendErrorSafe", new Class<?>[] { HttpServletResponse.class, int.class, String.class },
                open, HttpServletResponse.SC_BAD_REQUEST, "bad");
        verify(open).sendError(HttpServletResponse.SC_BAD_REQUEST, "bad");
    }

    @Test
    void doGet_unhandledException_usesFallbackSendError() {
        DashboardLatestChatsServlet servlet = new DashboardLatestChatsServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(false);

        servlet.doGet(req, resp);

        try {
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
