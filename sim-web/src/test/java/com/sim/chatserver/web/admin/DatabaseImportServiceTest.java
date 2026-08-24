package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DatabaseImportServiceTest {

    @Test
    void handlePost_whenUnauthenticated_returnsUnauthorizedJson() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        JsonObject payload = Json.createReader(new StringReader(body.toString(java.nio.charset.StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").contains("Admin authentication required"));
    }

    @Test
    void handlePost_whenActionInvalid_returnsBadRequestJson() throws Exception {
        DatabaseImportService service = new DatabaseImportService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("action")).thenReturn(new String[]{"noop"});

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject payload = Json.createReader(new StringReader(body.toString(java.nio.charset.StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").contains("Invalid action"));
    }

    @Test
    void privateParsers_coverDateAndWidgetIdValidation() throws Exception {
        DatabaseImportService service = new DatabaseImportService();

        Method parseDateStrict = DatabaseImportService.class.getDeclaredMethod("parseDateStrict", String.class);
        parseDateStrict.setAccessible(true);

        assertEquals(LocalDate.of(2026, 8, 24), parseDateStrict.invoke(service, "2026-08-24"));
        InvocationTargetException parseEx = assertThrows(
            InvocationTargetException.class,
            () -> parseDateStrict.invoke(service, "2026-08-24T11:00:00Z"));
        assertTrue(parseEx.getCause() instanceof java.time.format.DateTimeParseException);
        InvocationTargetException invalidDateEx = assertThrows(
            InvocationTargetException.class,
            () -> parseDateStrict.invoke(service, "not-a-date"));
        assertTrue(invalidDateEx.getCause() instanceof java.time.format.DateTimeParseException);

        Method sanitizeWidgetId = DatabaseImportService.class.getDeclaredMethod("sanitizeWidgetId", String.class);
        sanitizeWidgetId.setAccessible(true);

        assertEquals("widget-1", sanitizeWidgetId.invoke(service, "widget-1"));
        assertNull(sanitizeWidgetId.invoke(service, "  "));
        assertNull(sanitizeWidgetId.invoke(service, "bad id"));
    }

    private static final class CapturingOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream delegate;

        private CapturingOutputStream(ByteArrayOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // no-op for unit tests
        }
    }
}
