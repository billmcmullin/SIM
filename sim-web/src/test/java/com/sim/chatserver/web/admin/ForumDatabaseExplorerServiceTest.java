package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class ForumDatabaseExplorerServiceTest {

    @Test
    void handlePost_whenNotAdmin_returnsForbiddenJson() throws Exception {
        ForumDatabaseExplorerService service = new ForumDatabaseExplorerService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));
        when(req.getSession(false)).thenReturn(null);

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
        JsonObject payload = Json.createReader(new StringReader(body.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").toLowerCase().contains("admin"));
    }

    @Test
    void handlePost_whenDbTypeInvalid_returnsBadRequestJson() throws Exception {
        ForumDatabaseExplorerService service = new ForumDatabaseExplorerService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("dbType")).thenReturn(new String[]{"oracle"});

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject payload = Json.createReader(new StringReader(body.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").contains("dbType"));
    }

    @Test
    void handlePost_whenPortOutOfRange_returnsBadRequestJson() throws Exception {
        ForumDatabaseExplorerService service = new ForumDatabaseExplorerService();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(new CapturingOutputStream(body));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("dbType")).thenReturn(new String[]{"postgresql"});
        when(req.getParameterValues("host")).thenReturn(new String[]{"example.internal"});
        when(req.getParameterValues("database")).thenReturn(new String[]{"simdb"});
        when(req.getParameterValues("username")).thenReturn(new String[]{"dbuser"});
        when(req.getParameterValues("password")).thenReturn(new String[]{"secret"});
        when(req.getParameterValues("port")).thenReturn(new String[]{"70000"});

        service.handlePost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject payload = Json.createReader(new StringReader(body.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals("error", payload.getString("status"));
        assertTrue(payload.getString("message").toLowerCase().contains("port"));
    }

    @Test
    void normalizationHelpers_trimControlAndLineSeparators() throws Exception {
        String value = "  alpha" + System.lineSeparator() + "beta\u0000  ";

        String normalizedSingle = (String) invokeStatic(
                "normalizeParam",
                new Class<?>[]{String.class},
                value);
        String normalizedMulti = (String) invokeStatic(
                "normalizeMultilineParam",
                new Class<?>[]{String.class},
                value);

        assertEquals("alphabeta", normalizedSingle);
        assertEquals("alphabeta", normalizedMulti);
        assertNull(invokeStatic("normalizeParam", new Class<?>[]{String.class}, (Object) null));
        assertNull(invokeStatic("normalizeMultilineParam", new Class<?>[]{String.class}, (Object) null));
    }

    @Test
    void parseIntWithBounds_acceptsValidAndRejectsInvalid() {
        assertEquals(5, invokeStatic("parseIntWithBounds", new Class<?>[]{String.class, int.class, int.class, String.class}, "5", 1, 10, "limit"));

        IllegalArgumentException invalidNumber = assertThrows(
                IllegalArgumentException.class,
                () -> invokeStatic("parseIntWithBounds", new Class<?>[]{String.class, int.class, int.class, String.class}, "x", 1, 10, "limit"));
        assertTrue(invalidNumber.getMessage().contains("valid number"));

        IllegalArgumentException outOfRange = assertThrows(
                IllegalArgumentException.class,
                () -> invokeStatic("parseIntWithBounds", new Class<?>[]{String.class, int.class, int.class, String.class}, "42", 1, 10, "limit"));
        assertTrue(outOfRange.getMessage().contains("between"));
    }

    @Test
    void addJsonCell_and_safeErrorMessage_coverMultipleValueKinds() {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        invokeStatic("addJsonCell", new Class<?>[]{JsonObjectBuilder.class, String.class, Object.class}, builder, "flag", Boolean.TRUE);
        invokeStatic("addJsonCell", new Class<?>[]{JsonObjectBuilder.class, String.class, Object.class}, builder, "bytes", "abc".getBytes(StandardCharsets.UTF_8));
        invokeStatic("addJsonCell", new Class<?>[]{JsonObjectBuilder.class, String.class, Object.class}, builder, "when", Instant.parse("2026-08-24T10:15:30Z"));
        invokeStatic("addJsonCell", new Class<?>[]{JsonObjectBuilder.class, String.class, Object.class}, builder, "none", null);

        JsonObject payload = builder.build();
        assertTrue(payload.getBoolean("flag"));
        assertEquals("YWJj", payload.getString("bytes"));
        assertEquals("2026-08-24T10:15:30Z", payload.getString("when"));
        assertTrue(payload.isNull("none"));

        String message = (String) invokeStatic(
                "safeErrorMessage",
                new Class<?>[]{Throwable.class},
                new RuntimeException("line1\r\nline2"));
        assertNotNull(message);
        assertFalse(message.contains("\r"));
        assertFalse(message.contains("\n"));
    }

        @Test
        void addJsonCell_preservesFullLengthPostAndReplyText() {
        String text = "Post start " + "x".repeat(43_483) + "\u2019\u2014 complete ending";
        JsonObjectBuilder builder = Json.createObjectBuilder();
        invokeStatic("addJsonCell", new Class<?>[]{JsonObjectBuilder.class, String.class, Object.class},
            builder, "message_text", text);
        invokeStatic("addJsonCell", new Class<?>[]{JsonObjectBuilder.class, String.class, Object.class},
            builder, "reply_text", text + "\u0000");

        JsonObject payload = builder.build();
        assertEquals(text, payload.getString("message_text"));
        assertEquals(text, payload.getString("reply_text"));
        }

        private static Object invokeStatic(String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = ForumDatabaseExplorerService.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
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
