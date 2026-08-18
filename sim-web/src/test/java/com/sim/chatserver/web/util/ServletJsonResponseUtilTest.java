package com.sim.chatserver.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;

class ServletJsonResponseUtilTest {

    @Test
    void writeJson_writesToOutputStreamWhenAvailable() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ByteArrayServletOutputStream(out));

        JsonObject payload = Json.createObjectBuilder().add("ok", true).build();
        ServletJsonResponseUtil.writeJson(response, 201, payload);

        verify(response).setStatus(201);
        verify(response).setContentType("application/json; charset=UTF-8");

        String body = out.toString(StandardCharsets.UTF_8);
        JsonObject parsed = Json.createReader(new StringReader(body)).readObject();
        assertEquals(true, parsed.getBoolean("ok"));
    }

    @Test
    void writeJson_fallsBackToWriterWhenOutputStreamUnavailable() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IllegalStateException("stream unavailable"));

        StringWriter buffer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(buffer));

        ServletJsonResponseUtil.writeError(response, 400, "bad request");

        JsonObject parsed = Json.createReader(new StringReader(buffer.toString())).readObject();
        assertEquals("error", parsed.getString("status"));
        assertEquals("bad request", parsed.getString("message"));
    }

    @Test
    void writeJson_throwsWhenNoStreamAndNoWriter() throws Exception {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IllegalStateException("stream unavailable"));
        when(response.getWriter()).thenReturn(null);

        assertThrows(IOException.class, () -> ServletJsonResponseUtil.writeJson(response, 200, null));
    }

    private static final class ByteArrayServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream delegate;

        private ByteArrayServletOutputStream(ByteArrayOutputStream delegate) {
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
            // no-op for tests
        }
    }
}
