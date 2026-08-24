package com.sim.chatserver.web.admin.salesforce;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    public void doGet_nonAdmin_returnsUnauthorized() throws Exception {
        SalesforceOAuthCallbackServlet servlet = new SalesforceOAuthCallbackServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getSession(false)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin authentication required.");
    }

    @Test
    public void doGet_adminWithoutCodeState_returnsBadRequest() throws Exception {
        SalesforceOAuthCallbackServlet servlet = new SalesforceOAuthCallbackServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin-user");
        when(session.getAttribute("role")).thenReturn("ADMIN");

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing OAuth code/state.");
    }

    @Test
    public void doGet_errorParam_forwardsWithSessionMessage() throws Exception {
        SalesforceOAuthCallbackServlet servlet = new SalesforceOAuthCallbackServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("admin-user");
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(req.getParameterValues("error")).thenReturn(new String[]{"access_denied"});
        when(req.getParameterValues("error_description")).thenReturn(new String[]{"denied by policy"});
        when(req.getRequestDispatcher("/admin")).thenReturn(dispatcher);

        servlet.doGet(req, resp);

        verify(session).setAttribute("salesforceOAuthStatus", "error");
        verify(session).setAttribute("salesforceOAuthMessage", "Salesforce authorization failed: access_denied");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    public void helper_isValidState_and_constantTimeEquals_coverSuccessAndExpiry() throws Throwable {
        SalesforceOAuthCallbackServlet servlet = new SalesforceOAuthCallbackServlet();
        HttpSession session = mock(HttpSession.class);
        long now = System.currentTimeMillis();

        when(session.getAttribute("sf_oauth_state")).thenReturn("state-1");
        when(session.getAttribute("sf_oauth_state_ts")).thenReturn(Long.toString(now));

        boolean valid = (boolean) invokePrivate(servlet,
                "isValidState",
                new Class<?>[]{HttpSession.class, String.class},
                session,
                "state-1");

        when(session.getAttribute("sf_oauth_state_ts")).thenReturn(Long.toString(now - (11 * 60 * 1000L)));
        boolean expired = (boolean) invokePrivate(servlet,
                "isValidState",
                new Class<?>[]{HttpSession.class, String.class},
                session,
                "state-1");

        boolean equal = (boolean) invokePrivate(servlet,
                "constantTimeEquals",
                new Class<?>[]{String.class, String.class},
                "abc",
                "abc");
        boolean notEqual = (boolean) invokePrivate(servlet,
                "constantTimeEquals",
                new Class<?>[]{String.class, String.class},
                "abc",
                "abd");

        assertTrue(valid);
        assertTrue(!expired);
        assertTrue(equal);
        assertTrue(!notEqual);
    }

    @Test
    public void helper_parseTokenPayload_redaction_and_redirectUri() throws Throwable {
        SalesforceOAuthCallbackServlet servlet = new SalesforceOAuthCallbackServlet();

        Object parsed = invokePrivate(servlet,
                "parseTokenPayload",
                new Class<?>[]{String.class},
                "{\"access_token\":\"a1\",\"refresh_token\":\"r1\",\"instance_url\":\"https://inst\"}");
        Object invalid = invokePrivate(servlet,
                "parseTokenPayload",
                new Class<?>[]{String.class},
                "not-json");

        assertNotNull(parsed);
        assertNull(invalid);

        Field accessTokenField = parsed.getClass().getDeclaredField("accessToken");
        Field refreshTokenField = parsed.getClass().getDeclaredField("refreshToken");
        Field instanceUrlField = parsed.getClass().getDeclaredField("instanceUrl");
        accessTokenField.setAccessible(true);
        refreshTokenField.setAccessible(true);
        instanceUrlField.setAccessible(true);
        assertEquals("a1", accessTokenField.get(parsed));
        assertEquals("r1", refreshTokenField.get(parsed));
        assertEquals("https://inst", instanceUrlField.get(parsed));

        String redacted = (String) invokePrivate(servlet,
                "redactOauthPayload",
                new Class<?>[]{String.class},
                "{\"access_token\":\"secret\",\"refresh_token\":\"refresh\"}");
        assertTrue(redacted.contains("[REDACTED]"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        ServletContext sc = mock(ServletContext.class);
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(req.getHeader("X-Forwarded-Host")).thenReturn("ida.parasoft.com:8443");
        when(req.getHeader("X-Forwarded-Port")).thenReturn("8443");
        when(req.isSecure()).thenReturn(false);
        when(req.getServletContext()).thenReturn(sc);
        when(sc.getContextPath()).thenReturn("/chat-server");

        String redirect = (String) invokePrivate(servlet,
                "buildExternalRedirectUri",
                new Class<?>[]{HttpServletRequest.class},
                req);
        assertEquals("https://ida.parasoft.com:8443/chat-server/admin/salesforce/oauth/callback", redirect);
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
