package com.sim.chatserver.web.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.PrintWriter;
import java.io.StringWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class RuntimeHealthServletTest {
    @Test
    void responseWriteFailureIsHandledWithoutEscapingTheServlet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getServletPath()).thenReturn("/health/live");
        when(response.getOutputStream()).thenThrow(new java.io.IOException("response unavailable"));
        new RuntimeHealthServlet().doGet(request, response);
        verify(response).setStatus(503);
    }

    @Test
    void livenessIsIndependentOfDatabaseReadiness() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getServletPath()).thenReturn("/health/live");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        new RuntimeHealthServlet().doGet(request, response);
        verify(response).setStatus(200);
    }

    @Test
    void readinessFailsBeforeWorkflowInitialization() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getServletPath()).thenReturn("/health/ready");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        new RuntimeHealthServlet().doGet(request, response);
        verify(response).setStatus(503);
    }
}