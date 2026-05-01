package com.sim.chatserver.web.admin.salesforce;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
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

}
