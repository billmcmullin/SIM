package com.sim.chatserver.web.dashboard.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class DashboardBootstrapServletTest {

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
    void doGet_withoutSession_returnsUnauthorizedJson() throws Exception {
        DashboardBootstrapServlet servlet = new DashboardBootstrapServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("unauthorized"));
    }
}
