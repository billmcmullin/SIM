package com.sim.chatserver.security;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;

public class AuthResourceTest {

    @Test
    public void testLogin_RotatesSessionOnSuccess() {
        AuthResource underTest = new AuthResource();

        UserService userService = mock(UserService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession existingSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        UserAccount user = mock(UserAccount.class);

        underTest.userService = userService;
        underTest.servletRequest = request;

        when(userService.authenticateAndGetUser("alice", "password")).thenReturn(user);
        when(user.getUsername()).thenReturn("alice");
        when(user.getRole()).thenReturn("admin");

        when(request.getSession(false)).thenReturn(existingSession);
        when(request.getSession(true)).thenReturn(newSession);

        Response response = loginAllowingMissingRuntimeDelegate(
            underTest,
            Map.of("username", "alice", "password", "password")
        );
        if (response != null) {
            assertEquals(200, response.getStatus());
        }

        InOrder order = inOrder(request, existingSession, newSession);
        order.verify(request).getSession(false);
        order.verify(existingSession).invalidate();
        order.verify(request).getSession(true);

        verify(newSession).setAttribute("user", "alice");
        verify(newSession).setAttribute("role", "ADMIN");
        verify(newSession).setMaxInactiveInterval(30 * 60);
    }

    @Test
    public void testLogin_InvalidCredentialsDoesNotCreateSession() {
        AuthResource underTest = new AuthResource();

        UserService userService = mock(UserService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        underTest.userService = userService;
        underTest.servletRequest = request;

        when(userService.authenticateAndGetUser(anyString(), anyString())).thenReturn(null);

        Response response = loginAllowingMissingRuntimeDelegate(
                underTest,
                Map.of("username", "alice", "password", "wrong")
        );
        if (response != null) {
            assertEquals(401, response.getStatus());
            Object entity = response.getEntity();
            if (entity instanceof Map<?, ?> mapEntity) {
                assertFalse((Boolean) mapEntity.get("authenticated"));
            }
        }

        verify(request, never()).getSession(eq(true));
        verify(request, never()).getSession(eq(false));
    }

    private Response loginAllowingMissingRuntimeDelegate(AuthResource underTest, Map<String, String> payload) {
        try {
            return underTest.loginApi(payload);
        } catch (RuntimeException ex) {
            assertTrue(
                    causedByMissingRuntimeDelegate(ex),
                    "Unexpected runtime exception from AuthResource.login: " + ex.getMessage()
            );
            return null;
        }
    }

    private boolean causedByMissingRuntimeDelegate(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains("RuntimeDelegate")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}