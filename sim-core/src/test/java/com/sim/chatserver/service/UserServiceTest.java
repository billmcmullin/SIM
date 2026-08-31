package com.sim.chatserver.service;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for UserService
 *
 * @see com.sim.chatserver.service.UserService
 * @author bmcmullin
 */
public class UserServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.UserService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        Object getSingleResultResult = null; // UTA: configured value
        when(setParameterResult.getSingleResult()).thenReturn(getSingleResultResult);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        String password = "password"; // UTA: default value
        boolean result = underTest.authenticate(username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticate(String, String)
     *
     * @see com.sim.chatserver.service.UserService#authenticate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticate2() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getEmf()).thenThrow(IllegalStateException.class);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        String password = "password"; // UTA: default value
        assertThrows(IllegalStateException.class, () -> {
            underTest.authenticate(username, password);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for authenticateAndGetUser(String, String)
     *
     * @see com.sim.chatserver.service.UserService#authenticateAndGetUser(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticateAndGetUser() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        Object getSingleResultResult = null; // UTA: configured value
        when(setParameterResult.getSingleResult()).thenReturn(getSingleResultResult);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        String password = "password"; // UTA: default value
        UserAccount result = underTest.authenticateAndGetUser(username, password);

    }

    /**
     * Parasoft Jtest UTA: Test for authenticateAndGetUser(String, String)
     *
     * @see com.sim.chatserver.service.UserService#authenticateAndGetUser(String, String)
     * @author bmcmullin
     */
    @Test
    public void testAuthenticateAndGetUser2() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getEmf()).thenThrow(IllegalStateException.class);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        String password = "password"; // UTA: default value
        assertThrows(IllegalStateException.class, () -> {
            underTest.authenticateAndGetUser(username, password);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for createUser(String, String, String)
     *
     * @see com.sim.chatserver.service.UserService#createUser(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateUser() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        Query createNativeQueryResult = mock(Query.class);
        doReturn(createNativeQueryResult).when(createEntityManagerResult).createNativeQuery(nullable(String.class));

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        String password = "password"; // UTA: default value
        String role = "role"; // UTA: default value
        UserAccount result = underTest.createUser(username, password, role);

    }

    /**
     * Parasoft Jtest UTA: Test for createUser(String, String, String)
     *
     * @see com.sim.chatserver.service.UserService#createUser(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateUser2() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        when(createEntityManagerResult.createNativeQuery(nullable(String.class))).thenThrow(RuntimeException.class);

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        String password = "password"; // UTA: default value
        String role = "role"; // UTA: default value
        assertThrows(RuntimeException.class, () -> {
            underTest.createUser(username, password, role);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for deleteUser(String)
     *
     * @see com.sim.chatserver.service.UserService#deleteUser(String)
     * @author bmcmullin
     */
    @Test
    public void testDeleteUser() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        Object findResult = null; // UTA: configured value
        when(createEntityManagerResult.find((Class) any(), nullable(Object.class))).thenReturn(findResult);

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String userId = "userId"; // UTA: default value
        boolean result = underTest.deleteUser(userId);

    }

    /**
     * Parasoft Jtest UTA: Test for deleteUser(String)
     *
     * @see com.sim.chatserver.service.UserService#deleteUser(String)
     * @author bmcmullin
     */
    @Test
    public void testDeleteUser2() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        Object findResult = new Object(); // UTA: default value
        when(createEntityManagerResult.find((Class) any(), nullable(Object.class))).thenReturn(findResult);

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String userId = "userId"; // UTA: default value
        boolean result = underTest.deleteUser(userId);

    }

    /**
     * Parasoft Jtest UTA: Test for deleteUser(String)
     *
     * @see com.sim.chatserver.service.UserService#deleteUser(String)
     * @author bmcmullin
     */
    @Test
    public void testDeleteUser3() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        EntityTransaction getTransactionResult2 = mock(EntityTransaction.class);
        boolean isActiveResult = false; // UTA: configured value
        when(getTransactionResult2.isActive()).thenReturn(isActiveResult);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult, getTransactionResult2);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String userId = "userId"; // UTA: default value
        boolean result = underTest.deleteUser(userId);

    }

    /**
     * Parasoft Jtest UTA: Test for deleteUser(String)
     *
     * @see com.sim.chatserver.service.UserService#deleteUser(String)
     * @author bmcmullin
     */
    @Test
    public void testDeleteUser4() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        EntityTransaction getTransactionResult2 = mock(EntityTransaction.class);
        boolean isActiveResult = true; // UTA: configured value
        when(getTransactionResult2.isActive()).thenReturn(isActiveResult);
        EntityTransaction getTransactionResult3 = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult, getTransactionResult2, getTransactionResult3);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String userId = "userId"; // UTA: default value
        boolean result = underTest.deleteUser(userId);

    }

    /**
     * Parasoft Jtest UTA: Test for ensureAdminExists()
     *
     * @see com.sim.chatserver.service.UserService#ensureAdminExists()
     * @author bmcmullin
     */
    @Test
    public void testEnsureAdminExists() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        UserAccount getSingleResultResult = new UserAccount(); // user exists, no createUser() fallback
        when(setParameterResult.getSingleResult()).thenReturn(getSingleResultResult);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        underTest.ensureAdminExists();

    }

    /**
     * Parasoft Jtest UTA: Test for ensureAdminExists()
     *
     * @see com.sim.chatserver.service.UserService#ensureAdminExists()
     * @author bmcmullin
     */
    @Test
    public void testEnsureAdminExists2() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        Query createNativeQueryResult = mock(Query.class);
        doReturn(createNativeQueryResult).when(createEntityManagerResult).createNativeQuery(nullable(String.class));

        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        Object getSingleResultResult = null; // UTA: configured value
        when(setParameterResult.getSingleResult()).thenReturn(getSingleResultResult);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        underTest.ensureAdminExists();

    }

    /**
     * Parasoft Jtest UTA: Test for ensureAdminExists()
     *
     * @see com.sim.chatserver.service.UserService#ensureAdminExists()
     * @author bmcmullin
     */
    @Test
    public void testEnsureAdminExists3() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getEmf()).thenThrow(IllegalStateException.class);
        underTest.dsHolder = dsHolderValue;

        // When
        underTest.ensureAdminExists();

    }

    /**
     * Parasoft Jtest UTA: Test for ensureAdminExists()
     *
     * @see com.sim.chatserver.service.UserService#ensureAdminExists()
     * @author bmcmullin
     */
    @Test
    public void testEnsureAdminExists4() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManagerFactory getEmfResult2 = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        Query createNativeQueryResult = mock(Query.class);
        doReturn(createNativeQueryResult).when(createEntityManagerResult).createNativeQuery(nullable(String.class));

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        EntityTransaction getTransactionResult2 = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult, getTransactionResult2);
        when(getEmfResult2.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult, getEmfResult2);
        underTest.dsHolder = dsHolderValue;

        // When
        underTest.ensureAdminExists();

    }

    /**
     * Parasoft Jtest UTA: Test for ensureAdminExists()
     *
     * @see com.sim.chatserver.service.UserService#ensureAdminExists()
     * @author bmcmullin
     */
    @Test
    public void testEnsureAdminExists5() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManagerFactory getEmfResult2 = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        when(createEntityManagerResult.createNativeQuery(nullable(String.class))).thenThrow(RuntimeException.class);

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        EntityTransaction getTransactionResult2 = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult, getTransactionResult2);
        when(getEmfResult2.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult, getEmfResult2);
        underTest.dsHolder = dsHolderValue;

        // When
        underTest.ensureAdminExists();

    }

    /**
     * Parasoft Jtest UTA: Test for ensureAdminExists()
     *
     * @see com.sim.chatserver.service.UserService#ensureAdminExists()
     * @author bmcmullin
     */
    @Test
    public void testEnsureAdminExists6() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        when(createEntityManagerResult.createNativeQuery(nullable(String.class))).thenThrow(RuntimeException.class);

        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        Object getSingleResultResult = null; // UTA: configured value
        when(setParameterResult.getSingleResult()).thenReturn(getSingleResultResult);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        assertThrows(RuntimeException.class, () -> {
            underTest.ensureAdminExists();
        });

    }

    /**
     * Parasoft Jtest UTA: Test for findByUsername(String)
     *
     * @see com.sim.chatserver.service.UserService#findByUsername(String)
     * @author bmcmullin
     */
    @Test
    public void testFindByUsername() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        UserAccount result = invokeFindByUsername(underTest, username);

    }

    /**
     * Parasoft Jtest UTA: Test for listAllUsers()
     *
     * @see com.sim.chatserver.service.UserService#listAllUsers()
     * @author bmcmullin
     */
    @Test
    public void testListAllUsers() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        List<UserAccount> result = underTest.listAllUsers();

    }

    /**
     * Parasoft Jtest UTA: Test for updateCredentials(String, String, String)
     *
     * @see com.sim.chatserver.service.UserService#updateCredentials(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateCredentials() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        Object getSingleResultResult = new Object(); // UTA: default value
        when(setParameterResult.getSingleResult()).thenReturn(getSingleResultResult);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        boolean isActiveResult = false; // UTA: configured value
        when(getTransactionResult.isActive()).thenReturn(isActiveResult);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String currentUsername = "currentUsername"; // UTA: default value
        String newUsername = "newUsername"; // UTA: default value
        String newPassword = "newPassword"; // UTA: default value
        assertThrows(Exception.class, () -> {
            underTest.updateCredentials(currentUsername, newUsername, newPassword);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for updateCredentials(String, String, String)
     *
     * @see com.sim.chatserver.service.UserService#updateCredentials(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testUpdateCredentials2() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        Object getSingleResultResult = new Object(); // UTA: default value
        when(setParameterResult.getSingleResult()).thenReturn(getSingleResultResult);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());

        EntityTransaction getTransactionResult = mock(EntityTransaction.class);
        boolean isActiveResult = true; // UTA: configured value
        when(getTransactionResult.isActive()).thenReturn(isActiveResult);
        when(createEntityManagerResult.getTransaction()).thenReturn(getTransactionResult);
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String currentUsername = "currentUsername"; // UTA: default value
        String newUsername = "newUsername"; // UTA: default value
        String newPassword = "newPassword"; // UTA: default value
        assertThrows(Exception.class, () -> {
            underTest.updateCredentials(currentUsername, newUsername, newPassword);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for userExists(String)
     *
     * @see com.sim.chatserver.service.UserService#userExists(String)
     * @author bmcmullin
     */
    @Test
    public void testUserExists() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        EntityManagerFactory getEmfResult = mock(EntityManagerFactory.class);
        EntityManager createEntityManagerResult = mock(EntityManager.class);
        TypedQuery createQueryResult = mock(TypedQuery.class);
        TypedQuery setParameterResult = mock(TypedQuery.class);
        doReturn(setParameterResult).when(createQueryResult).setParameter(nullable(String.class), nullable(Object.class));
        doReturn(createQueryResult).when(createEntityManagerResult).createQuery(nullable(String.class), (Class) any());
        when(getEmfResult.createEntityManager()).thenReturn(createEntityManagerResult);
        when(dsHolderValue.getEmf()).thenReturn(getEmfResult);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        boolean result = invokeUserExists(underTest, username);

    }

    /**
     * Parasoft Jtest UTA: Test for userExists(String)
     *
     * @see com.sim.chatserver.service.UserService#userExists(String)
     * @author bmcmullin
     */
    @Test
    public void testUserExists2() throws Throwable
    {
        // Given
        UserService underTest = new UserService();
        AppDataSourceHolder dsHolderValue = mock(AppDataSourceHolder.class);
        when(dsHolderValue.getEmf()).thenThrow(IllegalStateException.class);
        underTest.dsHolder = dsHolderValue;

        // When
        String username = "username"; // UTA: default value
        assertThrows(IllegalStateException.class, () -> {
            invokeUserExists(underTest, username);
        });

    }

    private static boolean invokeUserExists(UserService underTest, String username) throws Exception {
        Method method = UserService.class.getDeclaredMethod("userExists", String.class);
        method.setAccessible(true);
        return ((Boolean) method.invoke(underTest, username)).booleanValue();
    }

    private static UserAccount invokeFindByUsername(UserService underTest, String username) throws Exception {
        Method method = UserService.class.getDeclaredMethod("findByUsername", String.class);
        method.setAccessible(true);
        return (UserAccount) method.invoke(underTest, username);
    }
}
