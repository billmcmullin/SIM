package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import com.sim.chatserver.service.FullAnalysisService;
import jakarta.json.Json;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class FullAnalysisServletTest {
    @Test
    void rejectsUnauthenticatedAndNonAdminActionsBeforeOpeningTheService() throws Exception {
        for (String role : new String[] {"anonymous", "USER"}) {
            FullAnalysisServlet servlet = spy(new FullAnalysisServlet());
            HttpServletRequest request = request(role, "{}");
            HttpServletResponse response = response(new ByteArrayOutputStream());
            servlet.doPost(request, response);
            verify(response).setStatus("anonymous".equals(role) ? 401 : 403);
            verify(servlet, never()).service();
        }
    }

    @Test
    void startReturnsAcceptedWithoutWaitingForInference() throws Exception {
        FullAnalysisServlet servlet = spy(new FullAnalysisServlet());
        FullAnalysisService service = mock(FullAnalysisService.class);
        doReturn(service).when(servlet).service();
        when(service.start(eq("admin"), any(LocalDate.class), any())).thenReturn("00000000-0000-0000-0000-000000000001");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse response = response(out);
        servlet.doPost(request("ADMIN", "{\"action\":\"start\",\"day\":\"2026-01-01\"}"), response);
        verify(response).setStatus(202);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("accepted"));
        verify(service).start(eq("admin"), eq(LocalDate.of(2026, 1, 1)), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void rejectsInvalidLimitsMalformedDatesAndUnconfirmedIdleReset() throws Exception {
        for (String body : new String[] {"{\"requestsPerRun\":9999}", "{\"day\":\"not-a-date\"}",
                "{\"action\":\"confirmIdle\"}"}) {
            FullAnalysisServlet servlet = spy(new FullAnalysisServlet());
            HttpServletResponse response = response(new ByteArrayOutputStream());
            servlet.doPost(request("ADMIN", body), response);
            verify(response).setStatus(400);
            verify(servlet, never()).service();
        }
    }

    @Test
    void ownershipFailureDoesNotRevealOtherUsersResults() throws Exception {
        FullAnalysisServlet servlet = spy(new FullAnalysisServlet());
        FullAnalysisService service = mock(FullAnalysisService.class);
        doReturn(service).when(servlet).service();
        when(service.status(anyString(), eq("admin"))).thenThrow(new SecurityException("private"));
        HttpServletRequest request = request("ADMIN", "{}");
        when(request.getParameterValues("action")).thenReturn(new String[] {"status"});
        when(request.getParameterValues("jobId")).thenReturn(new String[] {"00000000-0000-0000-0000-000000000001"});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse response = response(out);
        servlet.doGet(request, response);
        verify(response).setStatus(404);
        assertFalse(out.toString(StandardCharsets.UTF_8).contains("private"));
    }

    @Test
    void exportStreamsUntruncatedSavedItemsAndMarksSuccessfulCompletion() throws Exception {
        FullAnalysisServlet servlet = spy(new FullAnalysisServlet());
        FullAnalysisService service = mock(FullAnalysisService.class);
        doReturn(service).when(servlet).service();
        String fullText = "Full saved source and findings. ".repeat(500);
        var firstItems = Json.createArrayBuilder();
        for (int index = 0; index < 5; index++) {
            firstItems.add(Json.createObjectBuilder().add("text", fullText));
        }
        var job = Json.createObjectBuilder().add("state", "COMPLETED").build();
        when(service.page(anyString(), eq("admin"), eq(0), eq(5))).thenReturn(Json.createObjectBuilder()
                .add("job", job).add("total", 6).add("items", firstItems).build());
        when(service.page(anyString(), eq("admin"), eq(5), eq(5))).thenReturn(Json.createObjectBuilder()
                .add("job", job).add("total", 6).add("items", Json.createArrayBuilder().add(Json.createObjectBuilder().add("text", fullText))).build());
        HttpServletRequest request = request("ADMIN", "{}");
        when(request.getParameterValues("action")).thenReturn(new String[] {"export"});
        when(request.getParameterValues("jobId")).thenReturn(new String[] {"00000000-0000-0000-0000-000000000001"});
        HttpServletResponse response = mock(HttpServletResponse.class);
        java.io.StringWriter out = new java.io.StringWriter();
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(out));
        servlet.doGet(request, response);
        var lines = out.toString().lines().toList();
        assertEquals(8, lines.size());
        assertEquals(fullText, Json.createReader(new StringReader(lines.get(1))).readObject().getString("text"));
        assertTrue(Json.createReader(new StringReader(lines.get(7))).readObject().getBoolean("exportComplete"));
    }

    private HttpServletRequest request(String role, String body) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        if (!"anonymous".equals(role)) {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn("admin");
            when(session.getAttribute("role")).thenReturn(role);
            when(request.getSession(false)).thenReturn(session);
        }
        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
        return request;
    }

    private HttpServletResponse response(ByteArrayOutputStream out) throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) { }
            @Override public void write(int value) { out.write(value); }
        });
        return response;
    }
}