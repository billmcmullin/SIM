package com.sim.chatserver.web.admin.salesforce;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for SalesforceOAuthCallbackServlet
 *
 * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet
 * @author bmcmullin
 */
public class SalesforceOAuthCallbackServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet6() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet7() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet8() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet9() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = ""; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet10() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = ""; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        String getParameterResult3 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet11() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet12() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = ""; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet13() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = ""; // UTA: configured value
        String getParameterResult3 = ""; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

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
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet14() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet15() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult3 = null; // UTA: configured value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult3);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet16() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult3 = new Object(); // UTA: default value
        Object getAttributeResult4 = null; // UTA: configured value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult3, getAttributeResult4);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet17() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult3 = new Object(); // UTA: default value
        Number getAttributeResult4 = 1; // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult3, getAttributeResult4);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet18() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult3 = new Object(); // UTA: default value
        Object getAttributeResult4 = new Object(); // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult3, getAttributeResult4);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet19() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "*"; // UTA: configured value
        String getParameterResult3 = "*"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2, getParameterResult3);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        HttpSession getSessionResult2 = mock(HttpSession.class);
        Object getAttributeResult3 = new Object(); // UTA: default value
        Number getAttributeResult4 = 1; // UTA: default value
        when(getSessionResult2.getAttribute(nullable(String.class))).thenReturn(getAttributeResult3, getAttributeResult4);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult, getSessionResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.salesforce.SalesforceOAuthCallbackServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet20() throws Throwable
    {
        // Given
        SalesforceOAuthCallbackServlet underTest = new SalesforceOAuthCallbackServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        Object getAttributeResult2 = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult, getAttributeResult2);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }
}
