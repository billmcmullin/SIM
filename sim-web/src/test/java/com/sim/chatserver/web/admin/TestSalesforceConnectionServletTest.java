package com.sim.chatserver.web.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for TestSalesforceConnectionServlet
 *
 * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet
 * @author bmcmullin
 */
public class TestSalesforceConnectionServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        TestSalesforceConnectionServlet underTest = new TestSalesforceConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        TestSalesforceConnectionServlet underTest = new TestSalesforceConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        TestSalesforceConnectionServlet underTest = new TestSalesforceConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        TestSalesforceConnectionServlet underTest = new TestSalesforceConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        String getParameterResult2 = "getParameterResult2"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        TestSalesforceConnectionServlet underTest = new TestSalesforceConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        TestSalesforceConnectionServlet underTest = new TestSalesforceConnectionServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        mockJsonOutput(resp);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for getHttpClient()
     *
     * @see com.sim.chatserver.web.admin.TestSalesforceConnectionServlet#getHttpClient()
     * @author bmcmullin
     */
    @Test
    public void testGetHttpClient() throws Throwable
    {
        // Given
        TestSalesforceConnectionServlet underTest = new TestSalesforceConnectionServlet();

        // When
        var method = TestSalesforceConnectionServlet.class.getDeclaredMethod("getHttpClient");
        method.setAccessible(true);
        HttpClient result = (HttpClient) method.invoke(underTest);

    }

    private static void mockJsonOutput(HttpServletResponse resp) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServletOutputStream servletOut = new ServletOutputStream() {
            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener)
            {
                // No-op for unit test stream.
            }

            @Override
            public void write(int b) throws IOException
            {
                out.write(b);
            }
        };
        when(resp.getOutputStream()).thenReturn(servletOut);
    }

}
