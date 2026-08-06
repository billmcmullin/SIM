package com.sim.chatserver.web.profile;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class ProfileServletCoverageTest {

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
