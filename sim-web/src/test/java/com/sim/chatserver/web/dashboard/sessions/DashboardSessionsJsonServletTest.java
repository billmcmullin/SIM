package com.sim.chatserver.web.dashboard.sessions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.web.util.ServletJsonResponseUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DashboardSessionsJsonServletTest {

    @Test
    void doGet_unauthorizedWhenSessionMissing() {
        DashboardSessionsJsonServlet servlet = new DashboardSessionsJsonServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        try (MockedStatic<ServletJsonResponseUtil> json = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            servlet.doGet(req, resp);
            json.verify(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp), Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.any(JsonObject.class)));
        }
    }

    @Test
    void doGet_unauthorizedWhenUserMissing() {
        DashboardSessionsJsonServlet servlet = new DashboardSessionsJsonServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        try (MockedStatic<ServletJsonResponseUtil> json = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            servlet.doGet(req, resp);
            json.verify(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp), Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.any(JsonObject.class)));
        }
    }

    @Test
    void doGet_nullRequest_throwsNullPointerException() {
        DashboardSessionsJsonServlet servlet = new DashboardSessionsJsonServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        assertThrows(NullPointerException.class, () -> servlet.doGet(null, resp));
    }

    @Test
    void formatTimestamp_handlesNullAndValidTimestamp() throws Exception {
        DashboardSessionsJsonServlet servlet = new DashboardSessionsJsonServlet();

        String nullFormatted = (String) invoke(servlet, "formatTimestamp", new Class<?>[]{Timestamp.class}, new Object[]{null});
        Timestamp ts = Timestamp.from(Instant.parse("2026-08-27T10:00:00Z"));
        String formatted = (String) invoke(servlet, "formatTimestamp", new Class<?>[]{Timestamp.class}, ts);

        org.junit.jupiter.api.Assertions.assertTrue(formatted.matches("2026-08-27 \\d{2}:00:00"));
        org.junit.jupiter.api.Assertions.assertFalse(nullFormatted.isBlank());
    }

    @Test
    void writeJson_fallsBackToSendErrorOnIOException() throws Exception {
        DashboardSessionsJsonServlet servlet = new DashboardSessionsJsonServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);
        JsonObject payload = Json.createObjectBuilder().add("status", "ok").build();

        try (MockedStatic<ServletJsonResponseUtil> json = Mockito.mockStatic(ServletJsonResponseUtil.class)) {
            json.when(() -> ServletJsonResponseUtil.writeJson(Mockito.eq(resp), Mockito.eq(HttpServletResponse.SC_OK), Mockito.eq(payload)))
                    .thenThrow(new IOException("io"));

            invoke(servlet, "writeJson", new Class<?>[]{HttpServletResponse.class, int.class, JsonObject.class},
                    resp, HttpServletResponse.SC_OK, payload);

            verify(resp).sendError(HttpServletResponse.SC_OK);
        }
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
