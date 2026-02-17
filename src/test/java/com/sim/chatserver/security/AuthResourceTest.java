package com.sim.chatserver.security;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for AuthResource
 *
 * @see com.sim.chatserver.security.AuthResource
 * @author bmcmullin
 */
public class AuthResourceTest
{

    /**
     * Parasoft Jtest UTA: Test for login(Map)
     *
     * @see com.sim.chatserver.security.AuthResource#login(Map)
     * @author bmcmullin
     */
    @Test
    public void testLogin() throws Throwable
    {
        // Given
        AuthResource underTest = new AuthResource();

        // When
        Map<String, String> payload = null; // UTA: configured value
        Response result = underTest.login(payload);

    }

    /**
     * Parasoft Jtest UTA: Test for login(Map)
     *
     * @see com.sim.chatserver.security.AuthResource#login(Map)
     * @author bmcmullin
     */
    @Test
    public void testLogin2() throws Throwable
    {
        // Given
        AuthResource underTest = new AuthResource();

        // When
        Map<String, String> payload = mock(Map.class);
        String getResult = null; // UTA: configured value
        when(payload.get(nullable(Object.class))).thenReturn(getResult);
        Response result = underTest.login(payload);

    }

    /**
     * Parasoft Jtest UTA: Test for login(Map)
     *
     * @see com.sim.chatserver.security.AuthResource#login(Map)
     * @author bmcmullin
     */
    @Test
    public void testLogin3() throws Throwable
    {
        // Given
        AuthResource underTest = new AuthResource();

        // When
        Map<String, String> payload = mock(Map.class);
        String getResult = "getResult"; // UTA: default value
        String getResult2 = null; // UTA: configured value
        when(payload.get(nullable(Object.class))).thenReturn(getResult, getResult2);
        Response result = underTest.login(payload);

    }

    /**
     * Parasoft Jtest UTA: Test for login(Map)
     *
     * @see com.sim.chatserver.security.AuthResource#login(Map)
     * @author bmcmullin
     */
    @Test
    public void testLogin4() throws Throwable
    {
        // Given
        AuthResource underTest = new AuthResource();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = null; // UTA: configured value
        when(userServiceValue.findByUsername(nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        Map<String, String> payload = mock(Map.class);
        String getResult = "getResult"; // UTA: default value
        String getResult2 = "getResult2"; // UTA: default value
        when(payload.get(nullable(Object.class))).thenReturn(getResult, getResult2);
        Response result = underTest.login(payload);

    }

    /**
     * Parasoft Jtest UTA: Test for login(Map)
     *
     * @see com.sim.chatserver.security.AuthResource#login(Map)
     * @author bmcmullin
     */
    @Test
    public void testLogin5() throws Throwable
    {
        // Given
        AuthResource underTest = new AuthResource();
        UserService userServiceValue = mock(UserService.class);
        boolean authenticateResult = false; // UTA: configured value
        when(userServiceValue.authenticate(nullable(String.class), nullable(String.class))).thenReturn(authenticateResult);

        UserAccount findByUsernameResult = mock(UserAccount.class);
        when(userServiceValue.findByUsername(nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        Map<String, String> payload = mock(Map.class);
        String getResult = "getResult"; // UTA: default value
        String getResult2 = "getResult2"; // UTA: default value
        when(payload.get(nullable(Object.class))).thenReturn(getResult, getResult2);
        Response result = underTest.login(payload);

    }

    /**
     * Parasoft Jtest UTA: Test for login(Map)
     *
     * @see com.sim.chatserver.security.AuthResource#login(Map)
     * @author bmcmullin
     */
    @Test
    public void testLogin6() throws Throwable
    {
        // Given
        AuthResource underTest = new AuthResource();
        HttpServletRequest servletRequestValue = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        when(servletRequestValue.getSession(anyBoolean())).thenReturn(getSessionResult);
        underTest.servletRequest = servletRequestValue;
        UserService userServiceValue = mock(UserService.class);
        boolean authenticateResult = true; // UTA: configured value
        when(userServiceValue.authenticate(nullable(String.class), nullable(String.class))).thenReturn(authenticateResult);

        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = "getRoleResult"; // UTA: default value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = "getUsernameResult"; // UTA: default value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.findByUsername(nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        Map<String, String> payload = mock(Map.class);
        String getResult = "getResult"; // UTA: default value
        String getResult2 = "getResult2"; // UTA: default value
        when(payload.get(nullable(Object.class))).thenReturn(getResult, getResult2);
        Response result = underTest.login(payload);

    }
}
