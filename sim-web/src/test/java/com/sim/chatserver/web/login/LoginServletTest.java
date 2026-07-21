package com.sim.chatserver.web.login;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.UserService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for LoginServlet
 *
 * @see com.sim.chatserver.web.login.LoginServlet
 * @author bmcmullin
 */
public class LoginServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.login.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();
        UserService userServiceValue = mock(UserService.class);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        RequestDispatcher getRequestDispatcherResult = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(getRequestDispatcherResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.login.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();
        UserService userServiceValue = mock(UserService.class);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.login.LoginServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();
        UserService userServiceValue = mock(UserService.class);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        RequestDispatcher getRequestDispatcherResult = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(getRequestDispatcherResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.login.LoginServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.login.LoginServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.login.LoginServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();
        UserService userServiceValue = mock(UserService.class);
        boolean authenticateResult = false; // UTA: configured value
        when(userServiceValue.authenticate(nullable(String.class), nullable(String.class))).thenReturn(authenticateResult);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.login.LoginServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        LoginServlet underTest = new LoginServlet();
        UserService userServiceValue = mock(UserService.class);
        boolean authenticateResult = true; // UTA: configured value
        when(userServiceValue.authenticate(nullable(String.class), nullable(String.class))).thenReturn(authenticateResult);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestDispatcher(nullable(String.class))).thenReturn(mock(jakarta.servlet.RequestDispatcher.class));
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        String getParameterResult = "getParameterResult"; // UTA: default value
        String getParameterResult2 = "getParameterResult2"; // UTA: default value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult, getParameterResult2);

        HttpSession getSessionResult = mock(HttpSession.class);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doPost(req, resp);

    }
}
