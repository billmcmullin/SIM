package com.sim.chatserver.web.profile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.model.CustomerIdentity;
import com.sim.chatserver.model.CustomerIdentitySessionLink;
import com.sim.chatserver.model.CustomerProfile;
import com.sim.chatserver.model.CustomerProfileStore;
import com.sim.chatserver.service.CustomerIdentityService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
public class CustomerProfileServletTest {

    @Test
    void doGet_redirectsToLogin_whenSessionMissing() throws Exception {
        CustomerProfileServlet servlet = new CustomerProfileServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getSession(false)).thenReturn(null);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/login");
    }

    @Test
    void doGet_returns400_whenSessionIdAndFriendlyNameMissing() throws Exception {
        CustomerProfileServlet servlet = new CustomerProfileServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getSession(false)).thenReturn(session);
        when(req.getParameterValues("sessionId")).thenReturn(null);
        when(req.getParameterValues("friendlyName")).thenReturn(null);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "sessionId or friendlyName is required.");
    }

    @Test
    void doGet_returns400_whenSessionIdIsInvalid() throws Exception {
        CustomerProfileServlet servlet = new CustomerProfileServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getSession(false)).thenReturn(session);
        when(req.getParameterValues("sessionId")).thenReturn(new String[] { "bad session id" });

        HttpServletResponse resp = mock(HttpServletResponse.class);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid sessionId format.");
    }

    @Test
    void doGet_rendersProfileAndUsesRawSessionId_whenSessionIdProvided() throws Exception {
        CustomerProfileServlet servlet = new CustomerProfileServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getSession(false)).thenReturn(session);
        when(req.getParameterValues("sessionId")).thenReturn(new String[] { "sid-123" });
        when(req.getParameterValues("friendlyName")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/chat-server");
        when(req.getServletContext()).thenReturn(context);

        String template = "User:${user}\nDisplay:${sessionId}\nRaw:${rawSessionId}\nFriendly:${friendlyName}\nRows:${linkedSessionsRows}";
        when(context.getResourceAsStream("/WEB-INF/views/customer_profile.html"))
                .thenReturn(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));

        StringWriter buffer = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(buffer));

        CustomerIdentity identity = new CustomerIdentity();
        identity.setIdentityId(7L);
        identity.setCanonicalName("Identity Name");
        identity.setCanonicalEmail("identity@example.com");

        CustomerIdentitySessionLink link = mock(CustomerIdentitySessionLink.class);
        when(link.getSessionId()).thenReturn("linked-1");
        when(link.getDisplayNameSnapshot()).thenReturn("Linked Name");
        when(link.getContactEmailSnapshot()).thenReturn("linked@example.com");
        when(link.getUpdatedAt()).thenReturn(null);

        CustomerProfile profile = new CustomerProfile();
        profile.setFriendlyName("Profile Name");
        profile.setEmail("profile@example.com");

        try (MockedConstruction<CustomerIdentityService> identityServices = Mockito.mockConstruction(
                CustomerIdentityService.class,
                (mockService, ignored) -> {
                    when(mockService.resolveOrCreateBySessionId("sid-123")).thenReturn(identity);
                    when(mockService.listLinkedSessions(7L)).thenReturn(List.of(link));
                });
                MockedStatic<CustomerProfileStore> profileStore = Mockito.mockStatic(CustomerProfileStore.class)) {
            profileStore.when(() -> CustomerProfileStore.loadBySessionId("sid-123")).thenReturn(profile);

            servlet.doGet(req, resp);
        }

        String html = buffer.toString();
        verify(resp).setContentType("text/html;charset=UTF-8");
        assertTrue(html.contains("Display:sid-123"));
        assertTrue(html.contains("Raw:sid-123"));
        assertTrue(html.contains("Friendly:Profile Name"));
        assertTrue(html.contains("linked-1"));
        assertTrue(html.contains("/chat-server/customer-profile?sessionId=linked-1"));
    }

    @Test
    void doGet_returns500_whenIdentityLookupThrowsSqlException() throws Exception {
        CustomerProfileServlet servlet = new CustomerProfileServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(session.getAttribute("user")).thenReturn("alice");
        when(req.getSession(false)).thenReturn(session);
        when(req.getParameterValues("sessionId")).thenReturn(new String[] { "sid-123" });
        when(req.getParameterValues("friendlyName")).thenReturn(null);
        when(resp.isCommitted()).thenReturn(false);

        try (MockedConstruction<CustomerIdentityService> identityServices = Mockito.mockConstruction(
                CustomerIdentityService.class,
                (mockService, ignored) -> when(mockService.resolveOrCreateBySessionId("sid-123"))
                        .thenThrow(new SQLException("db down")))) {
            servlet.doGet(req, resp);
        }

        verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
    }
}
