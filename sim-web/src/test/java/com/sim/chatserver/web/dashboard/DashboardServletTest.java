package com.sim.chatserver.web.dashboard;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DashboardServletTest {

    @Test
    void doGet_withoutSession_forwardsToLogin() throws Exception {
        DashboardServlet servlet = new DashboardServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(dispatcher).forward(req, resp);
    }

    @Test
    void doGet_loginForwardFailure_sendsInternalServerError() throws Exception {
        DashboardServlet servlet = new DashboardServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(null);
        when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);
        when(resp.isCommitted()).thenReturn(false);
        doThrow(new ServletException("forward failed")).when(dispatcher).forward(req, resp);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void helper_buildInfoMessageHtml_and_parseLocalDate_and_sanitizeForLog() throws Throwable {
        DashboardServlet servlet = new DashboardServlet();

        String noIncrease = (String) invokePrivate(servlet, "buildInfoMessageHtml", new Class<?>[]{String.class}, "noIncreaseForTerm");
        String noYesterday = (String) invokePrivate(servlet, "buildInfoMessageHtml", new Class<?>[]{String.class}, "noYesterdayForTerm");
        String none = (String) invokePrivate(servlet, "buildInfoMessageHtml", new Class<?>[]{String.class}, "other");

        @SuppressWarnings("unchecked")
        Optional<LocalDate> valid = (Optional<LocalDate>) invokePrivate(servlet, "parseLocalDate", new Class<?>[]{String.class}, "2026-08-24");
        @SuppressWarnings("unchecked")
        Optional<LocalDate> invalid = (Optional<LocalDate>) invokePrivate(servlet, "parseLocalDate", new Class<?>[]{String.class}, "bad-date");

        String sanitized = (String) invokePrivate(servlet, "sanitizeForLog", new Class<?>[]{String.class}, "line1\r\nline2");
        String longInput = "x".repeat(200);
        String truncated = (String) invokePrivate(servlet, "sanitizeForLog", new Class<?>[]{String.class}, longInput);

        assertTrue(noIncrease.contains("No increased chats found"));
        assertTrue(noYesterday.contains("No chats found for that term yesterday"));
        assertEquals("", none);
        assertTrue(valid.isPresent());
        assertTrue(invalid.isEmpty());
        assertEquals("line1__line2", sanitized);
        assertEquals(120, truncated.length());
    }

    @Test
    void helper_formatProgression_and_widgetPieJson() throws Throwable {
        DashboardServlet servlet = new DashboardServlet();
        Object positiveProgress = newLocalProgressStat(10, 5);
        Object negativeProgress = newLocalProgressStat(0, 5);
        Class<?> progressClass = positiveProgress.getClass();

        String positive = (String) invokePrivate(
                servlet,
                "formatProgressionHtml",
            new Class<?>[]{progressClass},
            positiveProgress);
        String negative = (String) invokePrivate(
                servlet,
                "formatProgressionHtml",
            new Class<?>[]{progressClass},
            negativeProgress);
        String nullProgress = (String) invokePrivate(servlet, "formatProgressionHtml", new Class<?>[]{progressClass}, new Object[]{null});

        List<WidgetStat> stats = List.of(
                new WidgetStat("w1", "Label", 3),
                new WidgetStat(null, null, 0)
        );
        String pieJson = (String) invokePrivate(servlet, "buildWidgetPieChartData", new Class<?>[]{List.class}, stats);

        assertTrue(positive.contains("+5"));
        assertTrue(positive.contains("100.0%"));
        assertTrue(negative.contains("-5"));
        assertTrue(nullProgress.contains("0 (0.0%) vs yesterday"));
        assertTrue(pieJson.contains("widgetId"));
        assertTrue(pieJson.contains("\"\""));
    }

    @Test
    void helper_termMaps_and_snapshotStorage_and_safeJoin() throws Throwable {
        DashboardServlet servlet = new DashboardServlet();
        TermSummary summary = new TermSummary();
        summary.getTermCounts().put("termA", Integer.valueOf(-2));
        summary.getTermCounts().put("", Integer.valueOf(10));
        summary.getTermCounts().put("termB", Integer.valueOf(4));
        summary.getTermSnapshots().put("termA", List.of(new TermChatSnapshot(
                "termA",
                "widget-1",
                "chat-1",
                "prompt",
                "response",
                Timestamp.from(java.time.Instant.parse("2026-08-24T12:00:00Z")),
                "session-1")));
        summary.getTermSnapshots().put("  ", new ArrayList<>());

        @SuppressWarnings("unchecked")
        Map<String, Integer> totalMap = (Map<String, Integer>) invokePrivate(servlet, "buildTermTotalMap", new Class<?>[]{TermSummary.class}, summary);
        String increaseJson = (String) invokePrivate(servlet, "buildTermIncreaseMapJson", new Class<?>[]{Map.class}, totalMap);
        String totalJson = (String) invokePrivate(servlet, "buildTermTotalMapJson", new Class<?>[]{Map.class}, totalMap);

        @SuppressWarnings("unchecked")
        Map<String, List<TermChatSnapshot>> copied = (Map<String, List<TermChatSnapshot>>) invokePrivate(
                servlet,
                "copySnapshots",
                new Class<?>[]{TermSummary.class},
                summary);

        HttpSession session = mock(HttpSession.class);
        invokePrivate(servlet, "storeIncreaseSnapshots", new Class<?>[]{HttpSession.class, Map.class}, session, copied);
        invokePrivate(servlet, "storeIncreaseSnapshots", new Class<?>[]{HttpSession.class, Map.class}, session, Map.of());
        invokePrivate(servlet, "storeYesterdaySnapshots", new Class<?>[]{HttpSession.class, Map.class}, session, copied);
        invokePrivate(servlet, "storeYesterdaySnapshots", new Class<?>[]{HttpSession.class, Map.class}, session, null);

        String fallback = (String) invokePrivate(
                servlet,
                "safeJoin",
                new Class<?>[]{CompletableFuture.class, Object.class, String.class},
                CompletableFuture.failedFuture(new IllegalStateException("boom")),
                "fallback",
                "label");
        String success = (String) invokePrivate(
                servlet,
                "safeJoin",
                new Class<?>[]{CompletableFuture.class, Object.class, String.class},
                CompletableFuture.completedFuture("ok"),
                "fallback",
                "label");

        assertEquals(Integer.valueOf(0), totalMap.get("termA"));
        assertEquals(Integer.valueOf(4), totalMap.get("termB"));
        assertFalse(totalMap.containsKey(""));
        assertTrue(increaseJson.contains("termA"));
        assertTrue(totalJson.contains("termB"));
        assertTrue(copied.containsKey("termA"));
        assertFalse(copied.containsKey("  "));
        verify(session, times(1)).setAttribute(org.mockito.ArgumentMatchers.eq("termDistributionIncreaseSnapshots"), org.mockito.ArgumentMatchers.any());
        verify(session, times(1)).removeAttribute("termDistributionIncreaseSnapshots");
        verify(session, times(1)).setAttribute(org.mockito.ArgumentMatchers.eq("termDistributionYesterdaySnapshots"), org.mockito.ArgumentMatchers.any());
        verify(session, times(1)).removeAttribute("termDistributionYesterdaySnapshots");
        assertEquals("fallback", fallback);
        assertEquals("ok", success);
    }

    @Test
    void helper_loadSessionLabels_and_threadFactory() throws Throwable {
        DashboardServlet servlet = new DashboardServlet();

        @SuppressWarnings("unchecked")
        Map<String, Object> none = (Map<String, Object>) invokePrivate(servlet, "loadSessionLabels", new Class<?>[]{List.class}, List.of());

        DashboardServlet.DashboardThreadFactory tf = new DashboardServlet.DashboardThreadFactory();
        Thread t = tf.newThread(() -> {
            // no-op
        });

        assertTrue(none.isEmpty());
        assertTrue(t.isDaemon());
        assertTrue(t.getName().startsWith("dashboard-worker-"));
    }

    @Test
    void helper_quoteIdentifier_throwsForInvalidIdentifiers() {
        DashboardServlet servlet = new DashboardServlet();

        IllegalArgumentException blank = assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(servlet, "quoteIdentifier", new Class<?>[]{String.class}, " "));
        IllegalArgumentException invalidChars = assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(servlet, "quoteIdentifier", new Class<?>[]{String.class}, "1bad-name"));

        assertEquals("Invalid SQL identifier", blank.getMessage());
        assertEquals("Invalid SQL identifier", invalidChars.getMessage());
    }

    @Test
    void helper_openConnectionSafe_wrapsSQLExceptionInIllegalStateException() {
        DashboardServlet servlet = new DashboardServlet();

        @SuppressWarnings("unchecked")
        CDI<Object> cdi = mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> holderInstance = mock(Instance.class);
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);

        when(cdi.select(AppDataSourceHolder.class)).thenReturn(holderInstance);
        when(holderInstance.get()).thenReturn(holder);
        when(holder.getDataSource()).thenReturn(dataSource);
        try {
            when(dataSource.getConnection()).thenThrow(new SQLException("db down"));
        } catch (SQLException e) {
            throw new AssertionError("Unexpected setup failure", e);
        }

        try (MockedStatic<CDI> cdiStatic = Mockito.mockStatic(CDI.class)) {
            cdiStatic.when(CDI::current).thenReturn(cdi);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> invokePrivate(servlet, "openConnectionSafe", new Class<?>[]{}));

            assertEquals("Unable to open dashboard data connection", ex.getMessage());
            assertInstanceOf(SQLException.class, ex.getCause());
        }
    }

    private static Object newLocalProgressStat(int today, int yesterday) throws ReflectiveOperationException {
        Class<?> progressClass = Class.forName("com.sim.chatserver.web.dashboard.DashboardLocalViewModels$ProgressStat");
        Constructor<?> ctor = progressClass.getDeclaredConstructor(int.class, int.class);
        ctor.setAccessible(true);
        return ctor.newInstance(today, yesterday);
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Throwable {
        try {
            Method m = DashboardServlet.class.getDeclaredMethod(methodName, parameterTypes);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
