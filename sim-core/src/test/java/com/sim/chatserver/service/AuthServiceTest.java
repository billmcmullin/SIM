package com.sim.chatserver.service;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.UserAccount;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for AuthService
 *
 * @see com.sim.chatserver.service.AuthService
 * @author bmcmullin
 */
public class AuthServiceTest
{

    private static UserAccount invokeAuthenticate(AuthService underTest, String username, String password) throws Throwable {
        Method method = AuthService.class.getDeclaredMethod("authenticate", String.class, String.class);
        method.setAccessible(true);
        try {
            return (UserAccount) method.invoke(underTest, username, password);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            }
            throw ex;
        }
    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();

        // When
        String username = null; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate2() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();

        // When
        String username = ""; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate3() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = null; // UTA: configured value
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate4() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = null; // UTA: configured value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate5() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getPasswordHashResult = null; // UTA: configured value
        when(findByUsernameResult.getPasswordHash()).thenReturn(getPasswordHashResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate6() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getPasswordHashResult = "getPasswordHashResult"; // UTA: default value
        when(findByUsernameResult.getPasswordHash()).thenReturn(getPasswordHashResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate7() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = "getRoleResult"; // UTA: default value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = null; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = null; // UTA: configured value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate8() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = null; // UTA: configured value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = null; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = null; // UTA: configured value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate9() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = "getRoleResult"; // UTA: default value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = ""; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = null; // UTA: configured value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate10() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = null; // UTA: configured value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = ""; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = null; // UTA: configured value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate11() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getPasswordHashResult = null; // UTA: configured value
        when(findByUsernameResult.getPasswordHash()).thenReturn(getPasswordHashResult);

        String getRoleResult = "getRoleResult"; // UTA: default value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = null; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate12() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getPasswordHashResult = null; // UTA: configured value
        when(findByUsernameResult.getPasswordHash()).thenReturn(getPasswordHashResult);

        String getRoleResult = null; // UTA: configured value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = null; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate13() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = null; // UTA: configured value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = "*"; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = null; // UTA: configured value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate14() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getPasswordHashResult = null; // UTA: configured value
        when(findByUsernameResult.getPasswordHash()).thenReturn(getPasswordHashResult);

        String getRoleResult = null; // UTA: configured value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = ""; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate15() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getPasswordHashResult = "getPasswordHashResult"; // UTA: default value
        when(findByUsernameResult.getPasswordHash()).thenReturn(getPasswordHashResult);

        String getRoleResult = "getRoleResult"; // UTA: default value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = null; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate16() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getPasswordHashResult = "getPasswordHashResult"; // UTA: default value
        when(findByUsernameResult.getPasswordHash()).thenReturn(getPasswordHashResult);

        String getRoleResult = null; // UTA: configured value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = null; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate17() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getUsernameResult = "*"; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate18() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = "getRoleResult"; // UTA: default value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = ""; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.AuthService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate19() throws Throwable
    {
        // Given
        AuthService underTest = new AuthService();
        UserService userServiceValue = mock(UserService.class);
        UserAccount findByUsernameResult = mock(UserAccount.class);
        String getRoleResult = null; // UTA: configured value
        when(findByUsernameResult.getRole()).thenReturn(getRoleResult);

        String getUsernameResult = ""; // UTA: configured value
        when(findByUsernameResult.getUsername()).thenReturn(getUsernameResult);
        when(userServiceValue.authenticateAndGetUser(nullable(String.class), nullable(String.class))).thenReturn(findByUsernameResult);
        underTest.userService = userServiceValue;

        // When
        String username = "*"; // UTA: configured value
        String password = "password"; // UTA: default value
        UserAccount result = invokeAuthenticate(underTest, username, password);

    }
}
