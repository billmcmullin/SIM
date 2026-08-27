package com.sim.chatserver.web.dashboard.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardBootstrapServletTest {

    @Test
    void init_withExistingSummaryStore_isSuccessful() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();
        DashboardDailySummaryStore store = mock(DashboardDailySummaryStore.class);
        ServletContext context = mock(ServletContext.class);
        ServletConfig config = mock(ServletConfig.class);

        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute(any())).thenReturn(store);

        servlet.init(config);

        verify(context).getAttribute(any());
    }

    @Test
    void doGet_withoutSession_returnsUnauthorizedJson() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        AtomicInteger status = new AtomicInteger(-1);
        AtomicReference<JsonObject> payload = new AtomicReference<>();

        when(req.getSession(false)).thenReturn(null);

        try (MockedStatic<ServletJsonResponseUtil> jsonStatic = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonStatic.when(() -> ServletJsonResponseUtil.writeJson(any(), anyInt(), any(JsonObject.class)))
                    .thenAnswer(invocation -> {
                        status.set(invocation.getArgument(1));
                        payload.set(invocation.getArgument(2));
                        return null;
                    });

            servlet.doGet(req, resp);
        }

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status.get());
        assertEquals("unauthorized", payload.get().getString("status"));
        verify(resp, never()).sendError(anyInt());
    }

    @Test
    void doGet_authenticated_sessionsFallbackAndSummaryOk() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();
        DashboardDailySummaryStore store = mock(DashboardDailySummaryStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);
        ServletConfig config = mock(ServletConfig.class);
        AtomicInteger status = new AtomicInteger(-1);
        AtomicReference<JsonObject> payload = new AtomicReference<>();

        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute(any())).thenReturn(store);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getContextPath()).thenReturn("/chat-server");
        when(store.fetchExactOrLatest(any(), anyInt()))
                .thenReturn(Json.createObjectBuilder().add("summary", "ok").build());

        servlet.init(config);

        try (MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class);
                MockedStatic<ServletJsonResponseUtil> jsonStatic = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            widgetStoreStatic.when(() -> WidgetStore.list(null))
                    .thenReturn(java.util.List.of(new WidgetEntry(1, "w1", "Widget One", Instant.now())));
            jsonStatic.when(() -> ServletJsonResponseUtil.writeJson(any(), anyInt(), any(JsonObject.class)))
                    .thenAnswer(invocation -> {
                        status.set(invocation.getArgument(1));
                        payload.set(invocation.getArgument(2));
                        return null;
                    });

            servlet.doGet(req, resp);
        }

        assertEquals(HttpServletResponse.SC_OK, status.get());
        JsonObject root = payload.get();
        assertEquals("ok", root.getString("status"));
        JsonObject sections = root.getJsonObject("sections");
        assertNotNull(sections);
        assertEquals("ok", sections.getJsonObject("sessions").getString("status"));
        assertEquals("ok", sections.getJsonObject("summary").getString("status"));
    }

    @Test
    void doGet_authenticated_summaryFailure_returnsEmptySummaryPayload() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();
        DashboardDailySummaryStore store = mock(DashboardDailySummaryStore.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);
        ServletConfig config = mock(ServletConfig.class);
        AtomicReference<JsonObject> payload = new AtomicReference<>();

        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute(any())).thenReturn(store);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getContextPath()).thenReturn("/chat-server");
        when(store.fetchExactOrLatest(any(), anyInt())).thenThrow(new IllegalStateException("boom"));

        servlet.init(config);

        try (MockedStatic<WidgetStore> widgetStoreStatic = Mockito.mockStatic(WidgetStore.class);
                MockedStatic<ServletJsonResponseUtil> jsonStatic = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            widgetStoreStatic.when(() -> WidgetStore.list(null))
                    .thenReturn(java.util.List.of(new WidgetEntry(1, "w1", "Widget One", Instant.now())));
            jsonStatic.when(() -> ServletJsonResponseUtil.writeJson(any(), anyInt(), any(JsonObject.class)))
                    .thenAnswer(invocation -> {
                        payload.set(invocation.getArgument(2));
                        return null;
                    });

            servlet.doGet(req, resp);
        }

        JsonObject summary = payload.get().getJsonObject("sections").getJsonObject("summary");
        assertEquals("ok", summary.getString("status"));
        assertEquals(0, summary.getJsonObject("data").size());
    }

    @Test
    void formatTimestamp_handlesNullAndFormatsUsingSystemZone() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();

        Method format = DashboardBootstrapServlet.class.getDeclaredMethod("formatTimestamp", Timestamp.class);
        format.setAccessible(true);

        assertEquals("—", format.invoke(servlet, (Object) null));

        Timestamp ts = Timestamp.from(Instant.parse("2026-08-01T10:15:30Z"));
        String expected = ts.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        assertEquals(expected, format.invoke(servlet, ts));
    }

    @Test
    void ensureSummaryStoreInitialized_returnsExistingStoreFromContext() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();
        DashboardDailySummaryStore store = mock(DashboardDailySummaryStore.class);
        ServletContext context = mock(ServletContext.class);
        ServletConfig config = mock(ServletConfig.class);

        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute(any())).thenReturn(store);

        servlet.init(config);

        Object value = invoke(servlet, "ensureSummaryStoreInitialized", new Class<?>[0]);
        assertSame(store, value);
    }

    @Test
    void resolveCurrentSlot_returnsValidRange() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();
        int slot = (int) invoke(servlet, "resolveCurrentSlot", new Class<?>[] { ZoneId.class }, ZoneId.systemDefault());
        assertTrue(slot >= 0 && slot <= 3);
    }

    @Test
    void writeJson_whenWriterFails_usesFallbackSendError() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        JsonObject payload = Json.createObjectBuilder().add("status", "ok").build();

        when(resp.isCommitted()).thenReturn(false);

        try (MockedStatic<ServletJsonResponseUtil> jsonStatic = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonStatic.when(() -> ServletJsonResponseUtil.writeJson(any(), anyInt(), any(JsonObject.class)))
                    .thenThrow(new IOException("write failed"));

            invoke(servlet, "writeJson", new Class<?>[] { HttpServletResponse.class, int.class, JsonObject.class },
                    resp, HttpServletResponse.SC_BAD_REQUEST, payload);
        }

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void writeJson_whenCommittedAndWriterFails_doesNotSendError() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        JsonObject payload = Json.createObjectBuilder().add("status", "ok").build();

        when(resp.isCommitted()).thenReturn(true);

        try (MockedStatic<ServletJsonResponseUtil> jsonStatic = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            jsonStatic.when(() -> ServletJsonResponseUtil.writeJson(any(), anyInt(), any(JsonObject.class)))
                    .thenThrow(new IOException("write failed"));

            invoke(servlet, "writeJson", new Class<?>[] { HttpServletResponse.class, int.class, JsonObject.class },
                    resp, HttpServletResponse.SC_BAD_REQUEST, payload);
        }

        verify(resp, never()).sendError(anyInt());
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
