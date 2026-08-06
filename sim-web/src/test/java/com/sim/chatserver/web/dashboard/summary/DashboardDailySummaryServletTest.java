package com.sim.chatserver.web.dashboard.summary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class DashboardDailySummaryServletTest {

    @Mock
    AppDataSourceHolder dsHolder;

    @Mock
    DataSource dataSource;

    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse resp;

    @Mock
    HttpSession session;

    private DashboardDailySummaryServlet servlet;

    private ByteArrayOutputStream responseBuffer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new DashboardDailySummaryServlet();
        injectField(servlet, "dsHolder", dsHolder);
        injectField(DashboardDailySummaryServlet.class, "summaryStore", null);

        responseBuffer = new ByteArrayOutputStream();
    }

    @Test
    void init_success_createsStoreAndEnsuresTable() throws Exception {
        when(dsHolder.getDataSource()).thenReturn(dataSource);

        try (MockedConstruction<DashboardDailySummaryStore> mocked
                = Mockito.mockConstruction(DashboardDailySummaryStore.class)) {
            servlet.init();

            assertEquals(1, mocked.constructed().size());
            DashboardDailySummaryStore constructed = mocked.constructed().get(0);
            verify(constructed).ensureTable();
        }
    }

    @Test
    void init_failure_throwsServletException() throws Exception {
        when(dsHolder.getDataSource()).thenReturn(dataSource);

        try (MockedConstruction<DashboardDailySummaryStore> mocked
                = Mockito.mockConstruction(DashboardDailySummaryStore.class,
                        (mock, context) -> Mockito.doThrow(new RuntimeException("boom")).when(mock).ensureTable())) {

            assertThrows(ServletException.class, () -> servlet.init());
            assertEquals(1, mocked.constructed().size());
        }
    }

    @Test
    void init_whenDsHolderNull_resolvesDataSourceHolderFromCdi() throws Exception {
        injectField(servlet, "dsHolder", null);

        CDI<Object> cdi = Mockito.mock(CDI.class);
        @SuppressWarnings("unchecked")
        Instance<AppDataSourceHolder> instance = Mockito.mock(Instance.class);
        when(cdi.select(AppDataSourceHolder.class)).thenReturn(instance);
        when(instance.get()).thenReturn(dsHolder);
        when(dsHolder.getDataSource()).thenReturn(dataSource);

        try (MockedStatic<CDI> cdiMock = Mockito.mockStatic(CDI.class);
                MockedConstruction<DashboardDailySummaryStore> mocked
                = Mockito.mockConstruction(DashboardDailySummaryStore.class)) {
            cdiMock.when(CDI::current).thenReturn(cdi);

            servlet.init();

            assertEquals(1, mocked.constructed().size());
            verify(mocked.constructed().get(0)).ensureTable();
        }
    }

    @Test
    void doGet_unauthorized_whenNoSession() throws Exception {
        mockJsonOutput();
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = responseBody();
        assertEquals(true, json.contains("\"status\":\"error\""));
        assertEquals(true, json.contains("Authentication required"));
    }

    @Test
    void doGet_unauthorized_whenNoUserAttribute() throws Exception {
        mockJsonOutput();
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = responseBody();
        assertEquals(true, json.contains("\"status\":\"error\""));
        assertEquals(true, json.contains("Authentication required"));
    }

    @Test
    void doGet_authorized_usesExistingStore_andWritesPayload() throws Exception {
        mockJsonOutput();
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getParameterValues("day")).thenReturn(new String[] {"2026-01-15"});

        when(storeMock.fetchExactOrLatest(Mockito.eq(java.time.LocalDate.of(2026, 1, 15)), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").add("x", 1).build());

        servlet.doGet(req, resp);

        verify(storeMock, never()).ensureTable();
        verify(resp).setCharacterEncoding("UTF-8");
        verify(resp).setContentType("application/json; charset=UTF-8");
        assertEquals(true, responseBody().contains("\"status\":\"ok\""));
    }

    @Test
    void doGet_authorized_initializesStoreWhenNull_thenFetches() throws Exception {
        mockJsonOutput();
        when(dsHolder.getDataSource()).thenReturn(dataSource);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("bob");
        when(req.getParameterValues("day")).thenReturn(new String[] {"2026-02-02"});

        try (MockedConstruction<DashboardDailySummaryStore> mocked
                = Mockito.mockConstruction(DashboardDailySummaryStore.class, (mock, context) -> {
                    doReturn(Json.createObjectBuilder().add("status", "ok").add("constructed", true).build())
                            .when(mock).fetchExactOrLatest(Mockito.eq(java.time.LocalDate.of(2026, 2, 2)), Mockito.anyInt());
                })) {

            servlet.doGet(req, resp);

            assertEquals(1, mocked.constructed().size());
            DashboardDailySummaryStore constructed = mocked.constructed().get(0);
            verify(constructed).ensureTable();
            verify(constructed).fetchExactOrLatest(Mockito.eq(java.time.LocalDate.of(2026, 2, 2)), Mockito.anyInt());

            assertEquals(true, responseBody().contains("\"status\":\"ok\""));
            assertEquals(true, responseBody().contains("\"constructed\":true"));
        }
    }

    @Test
    void doGet_whenStoreFetchThrows_returnsErrorPayload() throws Exception {
        mockJsonOutput();
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("carol");
        when(req.getParameterValues("day")).thenReturn(new String[] {"2026-03-03"});
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt())).thenThrow(new RuntimeException("db"));

        servlet.doGet(req, resp);

        String out = responseBody();
        assertEquals(true, out.contains("\"status\":\"error\""));
        assertEquals(true, out.contains("Unable to load summary."));
    }

    @Test
    void doGet_invalidDayParam_fallsBackToToday_andStillReturns() throws Exception {
        mockJsonOutput();
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("dave");
        when(req.getParameterValues("day")).thenReturn(new String[] {"not-a-date"});
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").build());

        assertDoesNotThrow(() -> servlet.doGet(req, resp));
        verify(storeMock).fetchExactOrLatest(Mockito.any(), Mockito.anyInt());
        assertEquals(true, responseBody().contains("\"status\":\"ok\""));
    }

    @Test
    void doGet_validSlotParam_isPassedToStore() throws Exception {
        mockJsonOutput();
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("erin");
        when(req.getParameterValues("day")).thenReturn(new String[] {"2026-04-20"});
        when(req.getParameterValues("slot")).thenReturn(new String[] {"3"});
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").build());

        servlet.doGet(req, resp);

        verify(storeMock).fetchExactOrLatest(Mockito.eq(java.time.LocalDate.of(2026, 4, 20)), Mockito.eq(3));
    }

    @Test
    void doGet_invalidSlotParam_fallsBackToComputedSlotRange() throws Exception {
        mockJsonOutput();
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("frank");
        when(req.getParameterValues("day")).thenReturn(new String[] {"2026-04-21"});
        when(req.getParameterValues("slot")).thenReturn(new String[] {"999"});
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").build());

        servlet.doGet(req, resp);

        verify(storeMock).fetchExactOrLatest(
                Mockito.eq(java.time.LocalDate.of(2026, 4, 21)),
                Mockito.intThat(v -> v >= 0 && v <= 3));
    }

    @Test
    void doGet_nonNumericSlotParam_hitsNumberFormatFallbackPath() throws Exception {
        mockJsonOutput();
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("frank");
        when(req.getParameterValues("day")).thenReturn(new String[] {"2026-04-22"});
        when(req.getParameterValues("slot")).thenReturn(new String[] {"abc"});
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").build());

        servlet.doGet(req, resp);

        verify(storeMock).fetchExactOrLatest(
                Mockito.eq(java.time.LocalDate.of(2026, 4, 22)),
                Mockito.intThat(v -> v >= 0 && v <= 3));
    }

    @Test
    void parseSlotOrCurrent_returnsAllTimeBucketValues() throws Exception {
        Method parseSlot = DashboardDailySummaryServlet.class
                .getDeclaredMethod("parseSlotOrCurrent", String.class, ZoneId.class);
        parseSlot.setAccessible(true);

        int slot0 = (int) parseSlot.invoke(servlet, null, findZoneForHourRange(0, 5));
        int slot1 = (int) parseSlot.invoke(servlet, null, findZoneForHourRange(6, 11));
        int slot2 = (int) parseSlot.invoke(servlet, null, findZoneForHourRange(12, 17));
        int slot3 = (int) parseSlot.invoke(servlet, null, findZoneForHourRange(18, 23));

        assertEquals(0, slot0);
        assertEquals(1, slot1);
        assertEquals(2, slot2);
        assertEquals(3, slot3);
    }

    @Test
    void setDataSourceHolder_setsReplacementHolderUsedByInit() throws Exception {
        AppDataSourceHolder replacement = Mockito.mock(AppDataSourceHolder.class);
        DataSource replacementDataSource = Mockito.mock(DataSource.class);
        when(replacement.getDataSource()).thenReturn(replacementDataSource);

        servlet.setDataSourceHolder(replacement);

        try (MockedConstruction<DashboardDailySummaryStore> mocked
                = Mockito.mockConstruction(DashboardDailySummaryStore.class)) {
            servlet.init();

            assertEquals(1, mocked.constructed().size());
            verify(mocked.constructed().get(0)).ensureTable();
        }
    }

    @Test
    void doGet_unhandledException_usesOuterFallbackErrorPath() throws Exception {
        when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }

    @Test
    void doGet_unhandledException_fallbackSendErrorIOException_isSwallowed() throws Exception {
        when(req.getSession(false)).thenThrow(new IllegalStateException("boom"));
        when(resp.isCommitted()).thenReturn(false);
        Mockito.doThrow(new IOException("io")).when(resp)
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");

        assertDoesNotThrow(() -> servlet.doGet(req, resp));
    }

    @Test
    void doGet_writeJsonIOException_triggersFallbackSendError() throws Exception {
        when(req.getSession(false)).thenReturn(null);
        when(resp.getOutputStream()).thenThrow(new IllegalStateException("stream unavailable"));
        when(resp.getWriter()).thenReturn(null);
        when(resp.isCommitted()).thenReturn(false);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void doGet_writeJsonIOException_andFallbackSendErrorIOException_isSwallowed() throws Exception {
        when(req.getSession(false)).thenReturn(null);
        when(resp.getOutputStream()).thenThrow(new IllegalStateException("stream unavailable"));
        when(resp.getWriter()).thenReturn(null);
        when(resp.isCommitted()).thenReturn(false);
        Mockito.doThrow(new IOException("send error failed")).when(resp)
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        assertDoesNotThrow(() -> servlet.doGet(req, resp));
    }

    private void mockJsonOutput() throws IOException {
        when(resp.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // No-op for unit test stream.
            }

            @Override
            public void write(int b) throws IOException {
                responseBuffer.write(b);
            }
        });
    }

    private String responseBody() {
        return responseBuffer.toString(StandardCharsets.UTF_8);
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void injectField(Class<?> targetClass, String fieldName, Object value) throws Exception {
        Field f = targetClass.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    private static ZoneId findZoneForHourRange(int startInclusive, int endInclusive) {
        Instant now = Instant.now();
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        for (String zoneId : zoneIds) {
            ZoneId zone = ZoneId.of(zoneId);
            int hour = LocalTime.ofInstant(now, zone).getHour();
            if (hour >= startInclusive && hour <= endInclusive) {
                return zone;
            }
        }
        return ZoneId.systemDefault();
    }
}
