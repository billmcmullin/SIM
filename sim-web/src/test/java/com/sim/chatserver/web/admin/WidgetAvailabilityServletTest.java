package com.sim.chatserver.web.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker.WidgetAvailabilityResult;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;

public class WidgetAvailabilityServletTest {

    @Test
    public void doGet_unauthenticated_returnsUnauthorizedJsonContract() throws Exception {
        WidgetAvailabilityServlet underTest = new WidgetAvailabilityServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(null);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        underTest.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(resp).setContentType("application/json; charset=UTF-8");

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertEquals(false, body.getBoolean("available"));
        assertEquals("UNAUTHORIZED", body.getString("status"));
        assertEquals("Authentication required.", body.getString("details"));
    }

    @Test
    public void doGet_authenticatedHealthy_returnsOkJsonContract() throws Exception {
        WidgetAvailabilityChecker checker = mock(WidgetAvailabilityChecker.class);
        WidgetAvailabilityServlet underTest = new WidgetAvailabilityServlet();

        WidgetAvailabilityResult result = new WidgetAvailabilityResult(
                true,
                "UP",
                "2026-08-05T00:00:00Z",
                25L,
                "Synthetic check succeeded");
        when(checker.checkNow(false, false)).thenReturn(result);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("admin");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getSession(false)).thenReturn(session);
        when(req.getParameter("force")).thenReturn(null);
        when(req.getParameter("runWhenDisabled")).thenReturn(null);

        HttpServletResponse resp = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(resp.getOutputStream()).thenReturn(servletOutput(out));

        try (MockedStatic<CDI> cdiStatic = mockStatic(CDI.class)) {
            mockAvailabilityCheckerLookup(cdiStatic, checker);
            underTest.doGet(req, resp);
        }

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        verify(resp).setContentType("application/json; charset=UTF-8");

        JsonObject body = Json.createReader(new StringReader(out.toString(StandardCharsets.UTF_8))).readObject();
        assertTrue(body.getBoolean("available"));
        assertEquals("UP", body.getString("status"));
        assertEquals("2026-08-05T00:00:00Z", body.getString("checkedAt"));
        assertEquals(25L, body.getJsonNumber("latencyMs").longValue());
        assertEquals("Synthetic check succeeded", body.getString("details"));
    }

    private ServletOutputStream servletOutput(ByteArrayOutputStream out) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
                // no-op for tests
            }

            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static void mockAvailabilityCheckerLookup(MockedStatic<CDI> cdiStatic,
            WidgetAvailabilityChecker checker) {
        Instance<WidgetAvailabilityChecker> instance = (Instance<WidgetAvailabilityChecker>) mock(Instance.class);
        when(instance.get()).thenReturn(checker);

        CDI<Object> cdi = (CDI<Object>) mock(CDI.class);
        when(cdi.select(WidgetAvailabilityChecker.class)).thenReturn(instance);
        cdiStatic.when(CDI::current).thenReturn(cdi);
    }
}
