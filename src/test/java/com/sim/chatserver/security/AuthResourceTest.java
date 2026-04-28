package com.sim.chatserver.security;

import java.util.HashMap;
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
        String getResult = ""; // UTA: configured value
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
    public void testLogin4() throws Throwable
    {
        // Given
        AuthResource underTest = new AuthResource();
        UserService userServiceValue = mock(UserService.class);
        UserAccount authenticateAndGetUserResult = null; // UTA: configured value
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(authenticateAndGetUserResult);
        underTest.userService = userServiceValue;

        // When
        Map<String, String> payload = mock(Map.class);
        String getResult = "*"; // UTA: configured value
        String getResult2 = "getResult2"; // UTA: default value
        when(payload.get(nullable(Object.class))).thenReturn(getResult, getResult2);
        Response result = underTest.login(payload);

    }

}
