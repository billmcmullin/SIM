package com.sim.chatserver.web.dashboard.summary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sim.chatserver.startup.AppDataSourceHolder;

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
        when(req.getParameterMap()).thenReturn(Map.of("day", new String[] {"2026-01-15"}));

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
        when(req.getParameterMap()).thenReturn(Map.of("day", new String[] {"2026-02-02"}));

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
        when(req.getParameterMap()).thenReturn(Map.of("day", new String[] {"2026-03-03"}));
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
        when(req.getParameterMap()).thenReturn(Map.of("day", new String[] {"not-a-date"}));
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").build());

        assertDoesNotThrow(() -> servlet.doGet(req, resp));
        verify(storeMock).fetchExactOrLatest(Mockito.any(), Mockito.anyInt());
        assertEquals(true, responseBody().contains("\"status\":\"ok\""));
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
}
