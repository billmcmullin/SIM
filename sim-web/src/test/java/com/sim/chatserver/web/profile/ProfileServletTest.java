package com.sim.chatserver.web.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
/**
 * Parasoft Jtest UTA: Test class for ProfileServlet
 *
 * @see com.sim.chatserver.web.profile.ProfileServlet
 * @author bmcmullin
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProfileServletTest
{

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doGet(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet2() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getContextPathResult = "getContextPathResult"; // UTA: default value
        when(req.getContextPath()).thenReturn(getContextPathResult);

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
     * @see com.sim.chatserver.web.profile.ProfileServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet3() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

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
     * @see com.sim.chatserver.web.profile.ProfileServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet4() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = mock(InputStream.class);
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

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
     * @see com.sim.chatserver.web.profile.ProfileServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoGet5() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        ServletContext getServletContextResult = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = mock(InputStream.class);
        doThrow(IOException.class).when(getResourceAsStreamResult).close();
        when(getServletContextResult.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        when(req.getServletContext()).thenReturn(getServletContextResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        underTest.doGet(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost3() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = null; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost4() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost5() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();
        UserService userServiceValue = mock(UserService.class);
        UserAccount updateCredentialsResult = mock(UserAccount.class);
        String getUsernameResult = "getUsernameResult"; // UTA: default value
        String getUsernameResult2 = null; // UTA: configured value
        when(updateCredentialsResult.getUsername()).thenReturn(getUsernameResult, getUsernameResult2);
        when(userServiceValue.updateCredentials(nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(updateCredentialsResult);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost6() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();
        UserService userServiceValue = mock(UserService.class);
        UserAccount updateCredentialsResult = mock(UserAccount.class);
        String getUsernameResult = "getUsernameResult"; // UTA: default value
        when(updateCredentialsResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.updateCredentials(nullable(String.class), nullable(String.class), nullable(String.class))).thenReturn(updateCredentialsResult);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.profile.ProfileServlet#doPost(HttpServletRequest, HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost7() throws Throwable
    {
        // Given
        ProfileServlet underTest = new ProfileServlet();
        UserService userServiceValue = mock(UserService.class);
        when(userServiceValue.updateCredentials(nullable(String.class), nullable(String.class), nullable(String.class))).thenThrow(PersistenceException.class);
        underTest.userService = userServiceValue;

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        String getParameterResult = "getParameterResult"; // UTA: configured value
        when(req.getParameter(nullable(String.class))).thenReturn(getParameterResult);

        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }



    // Merged from ProfileServletCoverageTest
    
    
        @Mock
        HttpServletRequest req;
    
        @Mock
        HttpServletResponse resp;
    
        @Mock
        HttpSession session;
    
        @Mock
        UserService userService;
    
        @Mock
        UserAccount userAccount;
    
        private ProfileServlet servlet;
        private StringWriter responseWriter;
    
        @BeforeEach
        void setUp() throws Exception {
            servlet = new ProfileServlet();
            servlet.userService = userService;
    
            responseWriter = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(responseWriter, true));
    
            when(req.getSession(false)).thenReturn(session);
            when(session.getAttribute("user")).thenReturn("old-user");
        }
    
        @Test
        void doPost_blankUsername_returnsBadRequestWithoutUpdateCall() {
            when(req.getParameterValues("username")).thenReturn(new String[] {"   "});
            when(req.getParameterValues("password")).thenReturn(new String[] {"pass"});
    
            servlet.doPost(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
            verify(userService, never()).updateCredentials(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            assertEquals(true, responseWriter.toString().contains("Username cannot be empty."));
        }
    
        @Test
        void doPost_success_updatesSessionAndReturnsOkPayload() {
            when(req.getParameterValues("username")).thenReturn(new String[] {" new-user "});
            when(req.getParameterValues("password")).thenReturn(new String[] {" new-pass "});
            when(userService.updateCredentials("old-user", "new-user", "new-pass")).thenReturn(userAccount);
            when(userAccount.getUsername()).thenReturn("new-user");
    
            servlet.doPost(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_OK);
            verify(session).setAttribute("user", "new-user");
            String body = responseWriter.toString();
            assertEquals(true, body.contains("\"status\":\"ok\""));
            assertEquals(true, body.contains("\"username\":\"new-user\""));
        }
    
        @Test
        void doPost_persistenceConflict_returnsConflict() {
            when(req.getParameterValues("username")).thenReturn(new String[] {"new-user"});
            when(req.getParameterValues("password")).thenReturn(new String[] {"new-pass"});
            when(userService.updateCredentials("old-user", "new-user", "new-pass"))
                    .thenThrow(new PersistenceException("duplicate"));
    
            servlet.doPost(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_CONFLICT);
            assertEquals(true, responseWriter.toString().contains("Could not update profile."));
        }
    
        @Test
        void doPost_illegalState_returnsInternalServerError() {
            when(req.getParameterValues("username")).thenReturn(new String[] {"new-user"});
            when(req.getParameterValues("password")).thenReturn(new String[] {"new-pass"});
            when(userService.updateCredentials("old-user", "new-user", "new-pass"))
                    .thenThrow(new IllegalStateException("db unavailable"));
    
            servlet.doPost(req, resp);
    
            verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertEquals(true, responseWriter.toString().contains("Failed to update profile."));
        }
}
