package com.sim.chatserver.web.admin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.web.admin.TestSalesforceConnectionServletTest.TestableServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TestSalesforceConnectionServletTest
{

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;

    private StringWriter bodyWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception
    {
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        bodyWriter = new StringWriter();
        printWriter = new PrintWriter(bodyWriter);
        when(resp.getWriter()).thenReturn(printWriter);
    }

    @Test
    void doPost_whenNoSession_returns401() throws Exception
    {
        when(req.getSession(false)).thenReturn(null);

        TestableServlet servlet = new TestableServlet(null, null);
        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(resp).setContentType("application/json");
        assertTrue(bodyWriter.toString().contains("Authentication required"));
    }

    @Test
    void doPost_whenMissingInstanceUrl_returns400() throws Exception
    {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getParameter("salesforceInstanceUrl")).thenReturn(" ");
        when(req.getParameter("salesforceApiKey")).thenReturn("token");

        TestableServlet servlet = new TestableServlet(null, null);
        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(bodyWriter.toString().contains("Salesforce instance URL is required"));
    }

    @Test
    void doPost_whenMissingApiKey_returns400() throws Exception
    {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getParameter("salesforceInstanceUrl")).thenReturn("https://example.my.salesforce.com");
        when(req.getParameter("salesforceApiKey")).thenReturn(" ");

        TestableServlet servlet = new TestableServlet(null, null);
        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(bodyWriter.toString().contains("Salesforce API key is required"));
    }

    @Test
    void doPost_whenFallsBackToStoredConfig_andSuccess_returns200() throws Exception
    {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getParameter("salesforceInstanceUrl")).thenReturn(" ");
        when(req.getParameter("salesforceApiKey")).thenReturn(" ");

        ServerConfig cfg = new ServerConfig();
        cfg.setSalesforceInstanceUrl("parasoft.my.salesforce.com");
        cfg.setSalesforceApiKey("storedToken");

        @SuppressWarnings("unchecked") HttpResponse<String> sfResp = mock(HttpResponse.class);
        when(sfResp.statusCode()).thenReturn(200);
        when(sfResp.body()).thenReturn("{\"versions\":[]}");

        HttpClient client = mock(HttpClient.class);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(sfResp);

        TestableServlet servlet = new TestableServlet(client, cfg);
        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_OK);
        assertTrue(bodyWriter.toString().contains("\"status\":\"ok\""));
    }

    @Test
    void doPost_whenSalesforceNon2xx_returnsUpstreamStatusAndBody() throws Exception
    {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getParameter("salesforceInstanceUrl")).thenReturn("https://x.my.salesforce.com");
        when(req.getParameter("salesforceApiKey")).thenReturn("badToken");

        @SuppressWarnings("unchecked") HttpResponse<String> sfResp = mock(HttpResponse.class);
        when(sfResp.statusCode()).thenReturn(401);
        when(sfResp.body()).thenReturn("invalid session id");

        HttpClient client = mock(HttpClient.class);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(sfResp);

        TestableServlet servlet = new TestableServlet(client, null);
        servlet.doPost(req, resp);

        verify(resp).setStatus(401);
        assertTrue(bodyWriter.toString().contains("invalid session id"));
    }

    @Test
    void doPost_whenHttpClientThrows_returns502() throws Exception
    {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getParameter("salesforceInstanceUrl")).thenReturn("https://x.my.salesforce.com");
        when(req.getParameter("salesforceApiKey")).thenReturn("token");

        HttpClient client = mock(HttpClient.class);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenThrow(new RuntimeException("network down"));

        TestableServlet servlet = new TestableServlet(client, null);
        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        assertTrue(bodyWriter.toString().contains("network down"));
    }

    @Test
    void doPost_whenStoredConfigLoadFails_returns500() throws Exception
    {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin");
        when(req.getParameter("salesforceInstanceUrl")).thenReturn(" ");
        when(req.getParameter("salesforceApiKey")).thenReturn(" ");

        TestSalesforceConnectionServlet servlet = new TestSalesforceConnectionServlet() {
            @Override
            ServerConfig loadConfig() throws Exception
            {
                throw new RuntimeException("db failure");
            }
        };

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertTrue(bodyWriter.toString().contains("Unable to load stored Salesforce configuration"));
    }

    // test seam via subclass override
    static class TestableServlet extends TestSalesforceConnectionServlet
    {

        private final HttpClient client;
        private final ServerConfig cfg;

        TestableServlet(HttpClient client, ServerConfig cfg) {
            this.client = client;
            this.cfg = cfg;
        }

        @Override
        HttpClient getHttpClient()
        {
            return client != null ? client : super.getHttpClient();
        }

        @Override
        ServerConfig loadConfig()
        {
            return cfg;
        }
    }

    /**
     * Parasoft Jtest UTA: Test for loadConfig()
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#loadConfig()
     * @author bmcmullin
     */
    @Test
    public void testLoadConfig() throws Throwable
    {
        // UTA is unable to resolve the values required to create the requested test case.
        // A test case with default input values has been created.

        // Given
        HttpClient client = mock(HttpClient.class);
        ServerConfig cfg = mock(ServerConfig.class);
        TestableServlet underTest = new TestableServlet(client, cfg);

        // When
        ServerConfig result = underTest.loadConfig();

        // Then - assertions for result of method loadConfig()
        assertNotNull(result);

    }
}
