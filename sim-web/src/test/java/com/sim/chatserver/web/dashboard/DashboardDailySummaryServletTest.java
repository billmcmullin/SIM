package com.sim.chatserver.web.dashboard;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

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
import jakarta.servlet.ServletException;
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

    private StringWriter responseBuffer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new DashboardDailySummaryServlet();
        injectField(servlet, "dsHolder", dsHolder);

        responseBuffer = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(responseBuffer));
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
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = responseBuffer.toString();
        assertEquals(true, json.contains("\"status\":\"error\""));
        assertEquals(true, json.contains("Authentication required"));
    }

    @Test
    void doGet_unauthorized_whenNoUserAttribute() throws Exception {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = responseBuffer.toString();
        assertEquals(true, json.contains("\"status\":\"error\""));
        assertEquals(true, json.contains("Authentication required"));
    }

    @Test
    void doGet_authorized_usesExistingStore_andWritesPayload() throws Exception {
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getParameter("day")).thenReturn("2026-01-15");

        when(storeMock.fetchExactOrLatest(Mockito.eq(java.time.LocalDate.of(2026, 1, 15)), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").add("x", 1).build());

        servlet.doGet(req, resp);

        verify(storeMock, never()).ensureTable();
        verify(resp).setCharacterEncoding("UTF-8");
        verify(resp).setContentType("application/json; charset=UTF-8");
        assertEquals(true, responseBuffer.toString().contains("\"status\":\"ok\""));
    }

    @Test
    void doGet_authorized_initializesStoreWhenNull_thenFetches() throws Exception {
        when(dsHolder.getDataSource()).thenReturn(dataSource);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("bob");
        when(req.getParameter("day")).thenReturn("2026-02-02");

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

            assertEquals(true, responseBuffer.toString().contains("\"status\":\"ok\""));
            assertEquals(true, responseBuffer.toString().contains("\"constructed\":true"));
        }
    }

    @Test
    void doGet_whenStoreFetchThrows_returnsErrorPayload() throws Exception {
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("carol");
        when(req.getParameter("day")).thenReturn("2026-03-03");
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt())).thenThrow(new RuntimeException("db"));

        servlet.doGet(req, resp);

        String out = responseBuffer.toString();
        assertEquals(true, out.contains("\"status\":\"error\""));
        assertEquals(true, out.contains("Unable to load summary."));
    }

    @Test
    void doGet_invalidDayParam_fallsBackToToday_andStillReturns() throws Exception {
        DashboardDailySummaryStore storeMock = Mockito.mock(DashboardDailySummaryStore.class);
        injectField(servlet, "summaryStore", storeMock);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("dave");
        when(req.getParameter("day")).thenReturn("not-a-date");
        when(storeMock.fetchExactOrLatest(Mockito.any(), Mockito.anyInt()))
                .thenReturn(Json.createObjectBuilder().add("status", "ok").build());

        assertDoesNotThrow(() -> servlet.doGet(req, resp));
        verify(storeMock).fetchExactOrLatest(Mockito.any(), Mockito.anyInt());
        assertEquals(true, responseBuffer.toString().contains("\"status\":\"ok\""));
    }

    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
