package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class DatabaseImportServletTest {

    @Test
    void doPost_withoutAdminSession_returnsUnauthorizedJson() throws Exception {
        DatabaseImportServlet servlet = new DatabaseImportServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(null);
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(out.toString().contains("Admin authentication required"));
    }

    @Test
    void doPost_withAdminAndInvalidAction_returnsBadRequestJson() throws Exception {
        DatabaseImportServlet servlet = new DatabaseImportServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter out = new StringWriter();

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin-user");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameter("action")).thenReturn("invalid");
        when(resp.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(out.toString().contains("Invalid action"));
    }
}
