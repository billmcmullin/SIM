package com.sim.chatserver.api;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;

class AuthResourceTest {

    private AuthResource resource;
    private UserService userService;
    private HttpServletRequest servletRequest;
    private HttpSession session;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() throws Exception {
        resource = new AuthResource();

        userService = mock(UserService.class);
        servletRequest = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);

        userAccount = new UserAccount();
        userAccount.setUsername("alice");
        userAccount.setRole("admin");

        Field userServiceField = AuthResource.class.getDeclaredField("userService");
        userServiceField.setAccessible(true);
        userServiceField.set(resource, userService);

        Field servletRequestField = AuthResource.class.getDeclaredField("servletRequest");
        servletRequestField.setAccessible(true);
        servletRequestField.set(resource, servletRequest);
    }

    @Test
    void login_returnsBadRequest_whenPayloadMissingEntries() {
        Response response = resource.login(null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "username and password required"), response.getEntity());
    }

    @Test
    void login_returnsBadRequest_whenUsernameMissing() {
        Response response = resource.login(Map.of("password", "secret"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "username and password required"), response.getEntity());
    }

    @Test
    void login_returnsBadRequest_whenPasswordMissing() {
        Response response = resource.login(Map.of("username", "alice"));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "username and password required"), response.getEntity());
    }

    @Test
    void login_returnsUnauthorized_whenAuthenticationFails() {
        doReturn(userAccount).when(userService).findByUsername("alice");
        doReturn(false).when(userService).authenticate("alice", "wrong");

        Response response = resource.login(Map.of("username", "alice", "password", "wrong"));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("authenticated", false), response.getEntity());
        verify(servletRequest, never()).getSession(true);
    }

    @Test
    void login_setsSessionAttribute_andReturnsOk_whenAuthenticated() {
        doReturn(userAccount).when(userService).findByUsername("alice");
        doReturn(true).when(userService).authenticate("alice", "secret");
        doReturn(session).when(servletRequest).getSession(true);

        Response response = resource.login(Map.of("username", "alice", "password", "secret"));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Map.of("authenticated", true, "username", "alice"), response.getEntity());
        verify(servletRequest).getSession(true);
        verify(session).setAttribute("user", "alice");
        verify(session).setAttribute("role", "ADMIN");
    }
}
