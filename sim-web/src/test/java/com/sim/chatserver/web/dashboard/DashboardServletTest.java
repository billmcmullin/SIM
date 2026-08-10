package com.sim.chatserver.web.dashboard;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermsStore;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
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
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.sim.chatserver.model.DashboardViewModels.ProgressStat;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
/**
 * Parasoft Jtest UTA: Test class for DashboardServlet
 *
 * @see com.sim.chatserver.web.dashboard.DashboardServlet
 * @author bmcmullin
 */
public class DashboardServletTest
{

    /**
     * Parasoft Jtest UTA: Test for destroy()
     *
     * @see com.sim.chatserver.web.dashboard.DashboardServlet#destroy()
     * @author bmcmullin
     */
    @Test
    public void testDestroy() throws Throwable
    {
        // Given
        DashboardServlet underTest = new DashboardServlet();

        // When
        underTest.destroy();

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.DashboardServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        DashboardServlet underTest = new DashboardServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.DashboardServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        DashboardServlet underTest = new DashboardServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(dispatcher);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }



    // Merged from DashboardServletCoverageTest
    
    
        private MockedStatic<CDI> cdiMock;
    
        @AfterEach
        void tearDown() {
            if (cdiMock != null) {
                cdiMock.close();
                cdiMock = null;
            }
            DashboardTemplateRenderer.clearTemplateCache();
        }
    
        @Test
        void doGet_whenUnauthenticated_forwardsToLogin() throws Exception {
            DashboardServlet servlet = new DashboardServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = null;
            RequestDispatcher dispatcher = mock(RequestDispatcher.class);
    
            when(req.getSession(false)).thenReturn(session);
            when(req.getRequestDispatcher("/login")).thenReturn(dispatcher);
    
            servlet.doGet(req, resp);
    
            verify(dispatcher).forward(req, resp);
        }
    
        @Test
        void doGet_whenAuthenticated_withMinimalDependencies_rendersDashboard() throws Exception {
            DashboardServlet servlet = new DashboardServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);
            ServletContext servletContext = mock(ServletContext.class);
    
            AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
            TermsStore termsStore = mock(TermsStore.class);
            DataSource dataSource = mock(DataSource.class);
            Connection conn = mock(Connection.class);
    
            when(dsHolder.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(conn);
            when(termsStore.listAll()).thenReturn(List.of());
    
            mockCdi(dsHolder, termsStore);
    
            when(req.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn("alice");
            when(session.getAttribute("role")).thenReturn("ADMIN");
            when(req.getServletContext()).thenReturn(servletContext);
            when(servletContext.getContextPath()).thenReturn("/sim");
            when(req.getContextPath()).thenReturn("/sim");
            when(servletContext.getResourceAsStream("/WEB-INF/views/dashboard.html"))
                    .thenReturn(new ByteArrayInputStream("<html>${user}|${role}|${totalChats}</html>".getBytes()));
    
            StringWriter body = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(body));
    
            try (MockedStatic<WidgetStore> widgetStoreMock = Mockito.mockStatic(WidgetStore.class)) {
                widgetStoreMock.when(() -> WidgetStore.list(null)).thenReturn(List.of());
    
                servlet.doGet(req, resp);
            }
    
            verify(resp).setContentType("text/html;charset=UTF-8");
            assertTrue(body.toString().contains("alice"));
            verify(session).removeAttribute("termDistributionSnapshots");
            verify(session).removeAttribute("termDistributionIncreaseSnapshots");
            verify(session).removeAttribute("termDistributionYesterdaySnapshots");
        }
    
        @Test
        void doGet_whenUnexpectedRuntime_sendsInternalServerError() throws Exception {
            DashboardServlet servlet = new DashboardServlet();
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);
    
            when(req.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn("alice");
            when(req.getServletContext()).thenReturn(null);
            when(resp.isCommitted()).thenReturn(false);
    
            servlet.doGet(req, resp);
    
            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    
        @Test
        void buildLastFiveDaysTrendJson_countsInWindow_andSkipsInvalidRows() throws Exception {
            DashboardServlet servlet = new DashboardServlet();
    
            AppDataSourceHolder dsHolder = mock(AppDataSourceHolder.class);
            TermsStore termsStore = mock(TermsStore.class);
            DataSource dataSource = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            DatabaseMetaData metaData = mock(DatabaseMetaData.class);
            ResultSet tableRs = mock(ResultSet.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet dataRs = mock(ResultSet.class);
    
            when(dsHolder.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(conn);
            when(conn.getMetaData()).thenReturn(metaData);
            when(metaData.getTables(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.any()))
                .thenReturn(tableRs);
            when(tableRs.next()).thenReturn(true);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(dataRs);
            when(dataRs.next()).thenReturn(true, true, true, false);
    
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            Timestamp inWindow = Timestamp.from(today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Timestamp outOfWindow = Timestamp.from(today.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
            when(dataRs.getTimestamp("created_at")).thenReturn(inWindow, null, outOfWindow);
    
            mockCdi(dsHolder, termsStore);
    
            String json = (String) invoke(
                    servlet,
                    "buildLastFiveDaysTrendJson",
                    new Class[]{List.class},
                    List.of(new WidgetEntry(1, "Widget", "widget-1", Instant.now()))
            );
    
            JsonObject obj = Json.createReader(new StringReader(json)).readObject();
            JsonArray labels = obj.getJsonArray("labels");
            JsonArray values = obj.getJsonArray("values");
    
            assertEquals(5, obj.getInt("days"));
            assertEquals(5, labels.size());
            assertEquals(5, values.size());
    
            int total = 0;
            for (int i = 0; i < values.size(); i++) {
                total += values.getInt(i);
            }
            assertEquals(1, total);
        }
    
        @Test
        void helperMethods_parseSanitizeAndBuildJson_coverBranches() throws Exception {
            DashboardServlet servlet = new DashboardServlet();
    
            assertEquals("widget", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, " "));
            assertEquals("w_123abc", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "123abc"));
            assertEquals("abc_1", invoke(servlet, "sanitizeWidgetTableName", new Class[]{String.class}, "abc-1"));
    
            assertEquals("\"a\"\"b\"", invoke(servlet, "quoteIdentifier", new Class[]{String.class}, "a\"b"));
    
            @SuppressWarnings("unchecked")
            Optional<LocalDate> parsed = (Optional<LocalDate>) invoke(
                    servlet,
                    "parseLocalDate",
                    new Class[]{String.class},
                    "2026-08-07"
            );
            assertTrue(parsed.isPresent());
    
            @SuppressWarnings("unchecked")
            Optional<LocalDate> invalid = (Optional<LocalDate>) invoke(
                    servlet,
                    "parseLocalDate",
                    new Class[]{String.class},
                    "invalid"
            );
            assertFalse(invalid.isPresent());
    
            assertEquals("a_b", invoke(servlet, "sanitizeForLog", new Class[]{String.class}, "a\nb"));
    
            String longValue = "x".repeat(200);
            String sanitized = (String) invoke(servlet, "sanitizeForLog", new Class[]{String.class}, longValue);
            assertEquals(120, sanitized.length());
    
            assertEquals("", invoke(servlet, "buildInfoMessageHtml", new Class[]{String.class}, "x"));
            assertTrue(((String) invoke(servlet, "buildInfoMessageHtml", new Class[]{String.class}, "noIncreaseForTerm"))
                    .contains("No increased chats found"));
    
            String progression = (String) invoke(
                    servlet,
                    "formatProgressionHtml",
                    new Class[]{ProgressStat.class},
                    new ProgressStat(10, 5)
            );
            assertTrue(progression.contains("+5"));
    
            String emptyProgression = (String) invoke(servlet, "formatProgressionHtml", new Class[]{ProgressStat.class}, new Object[]{null});
            assertTrue(emptyProgression.contains("vs yesterday"));
    
            String pie = (String) invoke(
                    servlet,
                    "buildWidgetPieChartData",
                    new Class[]{List.class},
                    List.of(new WidgetStat(null, null, 3))
            );
            JsonArray pieJson = Json.createReader(new StringReader(pie)).readArray();
            assertEquals(1, pieJson.size());
            assertEquals(3, pieJson.getJsonObject(0).getInt("count"));
    
            Map<String, Integer> values = new LinkedHashMap<>();
            values.put("termA", 4);
            values.put("termB", null);
            String increaseJson = (String) invoke(servlet, "buildTermIncreaseMapJson", new Class[]{Map.class}, values);
            JsonObject increaseObj = Json.createReader(new StringReader(increaseJson)).readObject();
            assertEquals(4, increaseObj.getInt("termA"));
            assertEquals(0, increaseObj.getInt("termB"));
        }
    
        @Test
        void snapshotAndTermMapHelpers_coverEmptyAndPopulatedPaths() throws Exception {
            DashboardServlet servlet = new DashboardServlet();
            HttpSession session = mock(HttpSession.class);
    
            TermSummary summary = new TermSummary();
            summary.getTermCounts().put("termA", Integer.valueOf(3));
            summary.getTermCounts().put("termB", null);
            summary.getTermCounts().put("", Integer.valueOf(9));
            summary.getTermCounts().put(null, Integer.valueOf(5));
    
            TermChatSnapshot snapshot = new TermChatSnapshot(
                    "termA",
                    "widget1",
                    "chat1",
                    "prompt",
                    "response",
                    Timestamp.from(Instant.now()),
                    "s1"
            );
            summary.getTermSnapshots().put("termA", List.of(snapshot));
            summary.getTermSnapshots().put("", List.of(snapshot));
    
            @SuppressWarnings("unchecked")
            Map<String, Integer> totals = (Map<String, Integer>) invoke(
                    servlet,
                    "buildTermTotalMap",
                    new Class[]{TermSummary.class},
                    summary
            );
            assertEquals(2, totals.size());
            assertEquals(Integer.valueOf(3), totals.get("termA"));
            assertEquals(Integer.valueOf(0), totals.get("termB"));
    
            @SuppressWarnings("unchecked")
            Map<String, List<TermChatSnapshot>> copied = (Map<String, List<TermChatSnapshot>>) invoke(
                    servlet,
                    "copySnapshots",
                    new Class[]{TermSummary.class},
                    summary
            );
            assertEquals(1, copied.size());
            assertTrue(copied.containsKey("termA"));
    
            invoke(servlet, "storeIncreaseSnapshots", new Class[]{HttpSession.class, Map.class}, session, copied);
            verify(session).setAttribute(Mockito.eq("termDistributionIncreaseSnapshots"), any());
    
            invoke(servlet, "storeIncreaseSnapshots", new Class[]{HttpSession.class, Map.class}, session, Map.of());
            verify(session).removeAttribute("termDistributionIncreaseSnapshots");
    
            invoke(servlet, "storeYesterdaySnapshots", new Class[]{HttpSession.class, Map.class}, session, copied);
            verify(session).setAttribute(Mockito.eq("termDistributionYesterdaySnapshots"), any());
    
            invoke(servlet, "storeYesterdaySnapshots", new Class[]{HttpSession.class, Map.class}, session, Map.of());
            verify(session).removeAttribute("termDistributionYesterdaySnapshots");
    
            invoke(servlet, "storeTermSnapshots", new Class[]{HttpSession.class, TermSummary.class}, session, summary);
            verify(session).setAttribute(Mockito.eq("termDistributionSnapshots"), any());
        }
    
        @Test
        void safeJoinSendErrorAndThreadFactory_coverFallbackPaths() throws Exception {
            DashboardServlet servlet = new DashboardServlet();
    
            String ok = (String) invoke(
                    servlet,
                    "safeJoin",
                    new Class[]{CompletableFuture.class, Object.class, String.class},
                    CompletableFuture.completedFuture("ok"),
                    "fallback",
                    "ok-label"
            );
            assertEquals("ok", ok);
    
            String fallback = (String) invoke(
                    servlet,
                    "safeJoin",
                    new Class[]{CompletableFuture.class, Object.class, String.class},
                    CompletableFuture.failedFuture(new IllegalStateException("boom")),
                    "fallback",
                    "bad-label"
            );
            assertEquals("fallback", fallback);
    
            @SuppressWarnings("unchecked")
            CompletableFuture<String> badFuture = mock(CompletableFuture.class);
            when(badFuture.join()).thenThrow(new IllegalStateException("invalid state"));
            String fallback2 = (String) invoke(
                    servlet,
                    "safeJoin",
                    new Class[]{CompletableFuture.class, Object.class, String.class},
                    badFuture,
                    "fallback2",
                    "illegal-state"
            );
            assertEquals("fallback2", fallback2);
    
            HttpServletResponse committed = mock(HttpServletResponse.class);
            when(committed.isCommitted()).thenReturn(true);
            invoke(servlet, "sendErrorSafe", new Class[]{HttpServletResponse.class, int.class, String.class}, committed, 500, "x");
    
            HttpServletResponse failingResp = mock(HttpServletResponse.class);
            when(failingResp.isCommitted()).thenReturn(false);
            Mockito.doThrow(new IOException("io")).when(failingResp).sendError(400, "bad");
            invoke(servlet, "sendErrorSafe", new Class[]{HttpServletResponse.class, int.class, String.class}, failingResp, 400, "bad");
    
            DashboardServlet.DashboardThreadFactory factory = new DashboardServlet.DashboardThreadFactory();
            Thread t1 = factory.newThread(() -> { });
            Thread t2 = factory.newThread(() -> { });
            assertTrue(t1.isDaemon());
            assertTrue(t2.getName().startsWith("dashboard-worker-"));
        }
    
        private void mockCdi(AppDataSourceHolder dsHolderValue, TermsStore termsStoreValue) {
            if (cdiMock != null) {
                cdiMock.close();
            }
            cdiMock = Mockito.mockStatic(CDI.class);
    
            CDI<Object> cdi = mock(CDI.class);
            @SuppressWarnings("unchecked")
            Instance<AppDataSourceHolder> dsHolderInstance = mock(Instance.class);
            @SuppressWarnings("unchecked")
            Instance<TermsStore> termsStoreInstance = mock(Instance.class);
    
            when(cdi.select(AppDataSourceHolder.class)).thenReturn(dsHolderInstance);
            when(dsHolderInstance.get()).thenReturn(dsHolderValue);
            when(cdi.select(TermsStore.class)).thenReturn(termsStoreInstance);
            when(termsStoreInstance.get()).thenReturn(termsStoreValue);
    
            cdiMock.when(CDI::current).thenReturn(cdi);
        }
    
        private Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
            Method m = target.getClass().getDeclaredMethod(name, types);
            m.setAccessible(true);
            try {
                return m.invoke(target, args);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof Exception e) {
                    throw e;
                }
                if (cause instanceof Error e) {
                    throw e;
                }
                throw ite;
            }
        }
}
