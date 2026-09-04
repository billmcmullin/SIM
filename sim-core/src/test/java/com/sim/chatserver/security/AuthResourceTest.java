package com.sim.chatserver.security;

import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    @Test
    public void testLoginApi_MissingPassword_BuildsBadRequestResponse() {
        AuthResource underTest = new AuthResource();
        underTest.userService = mock(UserService.class);
        underTest.servletRequest = mock(HttpServletRequest.class);

        Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
        Response expected = mock(Response.class);

        try (MockedStatic<Response> responseStatic = mockStatic(Response.class)) {
            responseStatic.when(() -> Response.status(Response.Status.BAD_REQUEST)).thenReturn(builder);
            when(builder.entity(any())).thenReturn(builder);
            when(builder.build()).thenReturn(expected);

            Response result = underTest.loginApi(Map.of("username", "alice"));

            assertSame(expected, result);
            responseStatic.verify(() -> Response.status(Response.Status.BAD_REQUEST));
            verify(builder).entity(any());
            verify(builder).build();
            verify(underTest.userService, never()).authenticateAndGetUser(anyString(), anyString());
        }
    }

    @Test
    public void testLoginApi_InvalidCredentials_BuildsUnauthorizedResponse() {
        AuthResource underTest = new AuthResource();

        UserService userService = mock(UserService.class);
        underTest.userService = userService;
        underTest.servletRequest = mock(HttpServletRequest.class);
        when(userService.authenticateAndGetUser("alice", "wrong")).thenReturn(null);

        Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
        Response expected = mock(Response.class);

        try (MockedStatic<Response> responseStatic = mockStatic(Response.class)) {
            responseStatic.when(() -> Response.status(Response.Status.UNAUTHORIZED)).thenReturn(builder);
            when(builder.entity(any())).thenReturn(builder);
            when(builder.build()).thenReturn(expected);

            Response result = underTest.loginApi(Map.of("username", "alice", "password", "wrong"));

            assertSame(expected, result);
            responseStatic.verify(() -> Response.status(Response.Status.UNAUTHORIZED));
            verify(builder).entity(any());
            verify(builder).build();
        }
    }

    @Test
    public void testLoginApi_Success_BuildsOkResponse() {
        AuthResource underTest = new AuthResource();

        UserService userService = mock(UserService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession newSession = mock(HttpSession.class);
        UserAccount user = mock(UserAccount.class);

        underTest.userService = userService;
        underTest.servletRequest = request;

        when(userService.authenticateAndGetUser("alice", "password")).thenReturn(user);
        when(user.getUsername()).thenReturn("alice");
        when(user.getRole()).thenReturn("admin");
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(newSession);

        Response.ResponseBuilder builder = mock(Response.ResponseBuilder.class);
        Response expected = mock(Response.class);

        try (MockedStatic<Response> responseStatic = mockStatic(Response.class)) {
            responseStatic.when(() -> Response.ok(Map.of("authenticated", Boolean.TRUE, "username", "alice")))
                    .thenReturn(builder);
            when(builder.build()).thenReturn(expected);

            Response result = underTest.loginApi(Map.of("username", "alice", "password", "password"));

            assertSame(expected, result);
            responseStatic.verify(() -> Response.ok(Map.of("authenticated", Boolean.TRUE, "username", "alice")));
            verify(builder).build();
        }
    }

    @Test
    public void testLogin_SuccessWithoutExistingSession_SetsNullRole() {
        AuthResource underTest = new AuthResource();

        UserService userService = mock(UserService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession newSession = mock(HttpSession.class);
        UserAccount user = mock(UserAccount.class);

        underTest.userService = userService;
        underTest.servletRequest = request;

        when(userService.authenticateAndGetUser("alice", "password")).thenReturn(user);
        when(user.getUsername()).thenReturn("alice");
        when(user.getRole()).thenReturn(null);
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession(true)).thenReturn(newSession);

        Response response = loginAllowingMissingRuntimeDelegate(
                underTest,
                Map.of("username", "alice", "password", "password")
        );
        if (response != null) {
            assertEquals(200, response.getStatus());
        }

        verify(request).getSession(false);
        verify(request).getSession(true);
        verify(newSession).setAttribute("user", "alice");
        verify(newSession).setAttribute("role", null);
    }

    @Test
    public void testLogin_MissingPassword_BadRequestPath() {
        AuthResource underTest = new AuthResource();

        UserService userService = mock(UserService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        underTest.userService = userService;
        underTest.servletRequest = request;

        Response response = loginAllowingMissingRuntimeDelegate(
                underTest,
                Map.of("username", "alice")
        );

        if (response != null) {
            assertEquals(400, response.getStatus());
        }
        verify(userService, never()).authenticateAndGetUser(anyString(), anyString());
        verify(request, never()).getSession(eq(true));
    }

    @Test
    public void testExtractCredentials_NullPayload_ReturnsNull() throws Exception {
        Method extract = AuthResource.class.getDeclaredMethod("extractCredentials", Map.class);
        extract.setAccessible(true);

        Object credentials = extract.invoke(null, new Object[] { null });

        assertNull(credentials);
    }

    @Test
    public void testExtractCredentials_BlankUsername_ReturnsNull() throws Exception {
        Method extract = AuthResource.class.getDeclaredMethod("extractCredentials", Map.class);
        extract.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> payload = mock(Map.class);
        when(payload.get("username")).thenReturn("   ");
        when(payload.get("password")).thenReturn("pw");

        Object credentials = extract.invoke(null, payload);

        assertNull(credentials);
    }

    @Test
    public void testExtractCredentials_TrimsUsername() throws Exception {
        Method extract = AuthResource.class.getDeclaredMethod("extractCredentials", Map.class);
        extract.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> payload = mock(Map.class);
        when(payload.get("username")).thenReturn("  alice  ");
        when(payload.get("password")).thenReturn("pw");

        Object credentials = extract.invoke(null, payload);

        assertNotNull(credentials);
        Method usernameAccessor = credentials.getClass().getDeclaredMethod("username");
        usernameAccessor.setAccessible(true);
        assertEquals("alice", usernameAccessor.invoke(credentials));
    }

    @Test
    public void testNormalizeRole_NullAndValue() throws Exception {
        Method normalizeRole = AuthResource.class.getDeclaredMethod("normalizeRole", String.class);
        normalizeRole.setAccessible(true);

        assertNull(normalizeRole.invoke(null, new Object[] { null }));
        assertEquals("ADMIN", normalizeRole.invoke(null, "admin"));
    }

    @Test
    public void testSerializationGuards_ThrowNotSerializableException() throws Exception {
        AuthResource underTest = new AuthResource();
        Method readObject = AuthResource.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
        Method writeObject = AuthResource.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
        readObject.setAccessible(true);
        writeObject.setAccessible(true);

        assertInvocationCauseIsNotSerializable(readObject, underTest);
        assertInvocationCauseIsNotSerializable(writeObject, underTest);
    }

    private void assertInvocationCauseIsNotSerializable(Method method, Object target) {
        try {
            method.invoke(target, new Object[] { null });
            fail("Expected NotSerializableException from " + method.getName());
        } catch (InvocationTargetException ex) {
            assertNotNull(ex.getCause());
            assertEquals(java.io.NotSerializableException.class, ex.getCause().getClass());
        } catch (IllegalAccessException ex) {
            fail("Unexpected reflection access issue", ex);
        }
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