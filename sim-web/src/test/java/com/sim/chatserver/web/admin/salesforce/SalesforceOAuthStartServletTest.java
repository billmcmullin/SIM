package com.sim.chatserver.web.admin.salesforce;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for SalesforceOAuthStartServlet
 *
 * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet
 * @author bmcmullin
 */
public class SalesforceOAuthStartServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "http"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 80; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "http"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 0; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "http"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 81; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = ""; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 0; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "https"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 443; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet10() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = ""; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "http"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 80; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet11() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = ""; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "http"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 80; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet12() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getHeaderResult = ""; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "http"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        String getServerNameResult = "getServerNameResult"; // UTA: default value
        when(req.getServerName()).thenReturn(getServerNameResult);

        int getServerPortResult = 80; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet13() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = "getHeaderResult"; // UTA: default value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet14() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = "getHeaderResult2"; // UTA: default value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet15() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = "getHeaderResult3"; // UTA: default value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet16() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = ""; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthStartServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet17() throws Throwable
    {
        // Given
        SalesforceOAuthStartServlet underTest = new SalesforceOAuthStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getHeaderResult = null; // UTA: configured value
        String getHeaderResult2 = null; // UTA: configured value
        String getHeaderResult3 = null; // UTA: configured value
        when(req.getHeader(nullable(String.class))).thenReturn(getHeaderResult, getHeaderResult2, getHeaderResult3);

        String getSchemeResult = "https"; // UTA: configured value
        when(req.getScheme()).thenReturn(getSchemeResult);

        int getServerPortResult = 81; // UTA: configured value
        when(req.getServerPort()).thenReturn(getServerPortResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    @Test
    public void doGet_nonAdmin_returnsUnauthorized() throws Exception {
        SalesforceOAuthStartServlet servlet = new SalesforceOAuthStartServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
    }

    @Test
    public void doGet_adminWithMissingOauthConfig_returnsBadRequest() throws Exception {
        SalesforceOAuthStartServlet servlet = new SalesforceOAuthStartServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin-user");
        when(session.getAttribute("role")).thenReturn("ADMIN");

        ServerConfig cfg = new ServerConfig();
        cfg.setSalesforceLoginUrl("https://login.salesforce.com");
        cfg.setSalesforceClientId(null);

        try (MockedStatic<EncryptedDbConfigStore> storeMock = Mockito.mockStatic(EncryptedDbConfigStore.class)) {
            storeMock.when(EncryptedDbConfigStore::load).thenReturn(cfg);

            servlet.doGet(req, resp);

            verify(resp).sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing Salesforce OAuth configuration: login URL and client ID are required.");
        }
    }

    @Test
    public void buildExternalRedirectUri_prefersForwardedHeaders() throws Throwable {
        SalesforceOAuthStartServlet servlet = new SalesforceOAuthStartServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getHeaders("X-Forwarded-Proto")).thenReturn(java.util.Collections.enumeration(List.of("https")));
        when(req.getHeaders("X-Forwarded-Host")).thenReturn(java.util.Collections.enumeration(List.of("ida.parasoft.com:8443")));
        when(req.getHeaders("X-Forwarded-Port")).thenReturn(java.util.Collections.enumeration(List.of("8443")));
        when(req.getContextPath()).thenReturn("/chat-server");

        String redirect = (String) invokePrivate(servlet,
                "buildExternalRedirectUri",
                new Class<?>[]{HttpServletRequest.class},
                req);

        assertEquals("https://ida.parasoft.com:8443/chat-server/admin/salesforce/oauth/callback", redirect);
    }

    @Test
    public void normalizeBaseUri_and_toSafeAuthorizeUrl_validateInputs() throws Throwable {
        SalesforceOAuthStartServlet servlet = new SalesforceOAuthStartServlet();

        URI normalized = (URI) invokePrivate(servlet,
                "normalizeBaseUri",
                new Class<?>[]{String.class},
                "login.salesforce.com///");
        URI invalid = (URI) invokePrivate(servlet,
                "normalizeBaseUri",
                new Class<?>[]{String.class},
                "://bad-url");

        String validAuth = (String) invokePrivate(servlet,
                "toSafeAuthorizeUrl",
                new Class<?>[]{String.class},
                "https://login.salesforce.com/services/oauth2/authorize?x=1");
        String invalidAuth = (String) invokePrivate(servlet,
                "toSafeAuthorizeUrl",
                new Class<?>[]{String.class},
                "https://login.salesforce.com/services/oauth2/other");

        assertNotNull(normalized);
        assertEquals("https://login.salesforce.com", normalized.toString());
        assertNull(invalid);
        assertNotNull(validAuth);
        assertNull(invalidAuth);
    }

    @Test
    public void safeRedirect_acceptsValidAndRejectsUnsafeTarget() throws Throwable {
        SalesforceOAuthStartServlet servlet = new SalesforceOAuthStartServlet();
        HttpServletResponse resp = mock(HttpServletResponse.class);

        invokePrivate(servlet,
                "safeRedirect",
                new Class<?>[]{HttpServletResponse.class, String.class},
                resp,
                "https://login.salesforce.com/services/oauth2/authorize?response_type=code");

        verify(resp).setStatus(HttpServletResponse.SC_FOUND);
        verify(resp).setHeader("Location", "https://login.salesforce.com/services/oauth2/authorize?response_type=code");

        invokePrivate(servlet,
                "safeRedirect",
                new Class<?>[]{HttpServletResponse.class, String.class},
                resp,
                "https://login.salesforce.com/bad\nlocation");

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsafe Salesforce authorize URL.");
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>[] signature, Object... args)
            throws Throwable {
        try {
            Method m = target.getClass().getDeclaredMethod(methodName, signature);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

}
