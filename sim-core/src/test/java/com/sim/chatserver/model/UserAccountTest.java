package com.sim.chatserver.model;

import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for UserAccount
 *
 * @see com.sim.chatserver.model.UserAccount
 * @author bmcmullin
 */
public class UserAccountTest
{

    /**
     * Parasoft Jtest UTA: Test for createdAtValue()
     *
     * @see com.sim.chatserver.model.UserAccount#createdAtValue()
     * @author bmcmullin
     */
    @Test
    public void testCreatedAtValue() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        Instant result = underTest.createdAtValue();

        // Then - assertions for result of method createdAtValue()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for emailValue()
     *
     * @see com.sim.chatserver.model.UserAccount#emailValue()
     * @author bmcmullin
     */
    @Test
    public void testEmailValue() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.emailValue();

        // Then - assertions for result of method emailValue()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fullNameValue()
     *
     * @see com.sim.chatserver.model.UserAccount#fullNameValue()
     * @author bmcmullin
     */
    @Test
    public void testFullNameValue() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();
        String fullName = "fullName"; // UTA: default value
        underTest.assignFullName(fullName);

        // When
        String result = underTest.fullNameValue();

        // Then - assertions for result of method fullNameValue()
        assertEquals("fullName", result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fullNameValue()
     *
     * @see com.sim.chatserver.model.UserAccount#fullNameValue()
     * @author bmcmullin
     */
    @Test
    public void testFullNameValueFallback() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.fullNameValue();

        // Then - assertions for result of method fullNameValue()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getId()
     *
     * @see com.sim.chatserver.model.UserAccount#getId()
     * @author bmcmullin
     */
    @Test
    public void testGetId() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        Long result = underTest.getId();

        // Then - assertions for result of method getId()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPassword()
     *
     * @see com.sim.chatserver.model.UserAccount#getPassword()
     * @author bmcmullin
     */
    @Test
    public void testGetPassword() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.getPassword();

        // Then - assertions for result of method getPassword()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPasswordHash()
     *
     * @see com.sim.chatserver.model.UserAccount#getPasswordHash()
     * @author bmcmullin
     */
    @Test
    public void testGetPasswordHash() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.getPasswordHash();

        // Then - assertions for result of method getPasswordHash()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRole()
     *
     * @see com.sim.chatserver.model.UserAccount#getRole()
     * @author bmcmullin
     */
    @Test
    public void testGetRole() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();
        String role = "role"; // UTA: default value
        underTest.setRole(role);

        // When
        String result = underTest.getRole();

        // Then - assertions for result of method getRole()
        assertEquals("role", result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRole()
     *
     * @see com.sim.chatserver.model.UserAccount#getRole()
     * @author bmcmullin
     */
    @Test
    public void testGetRole2() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.getRole();

        // Then - assertions for result of method getRole()
        assertEquals("user", result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUsername()
     *
     * @see com.sim.chatserver.model.UserAccount#getUsername()
     * @author bmcmullin
     */
    @Test
    public void testGetUsername() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.getUsername();

        // Then - assertions for result of method getUsername()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setCreatedAt(Instant)
     *
     * @see com.sim.chatserver.model.UserAccount#setCreatedAt(Instant)
     * @author bmcmullin
     */
    @Test
    public void testSetCreatedAt() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        Instant createdAt = mock(Instant.class);
        underTest.setCreatedAt(createdAt);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNotNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for assignEmail(String)
     *
     * @see com.sim.chatserver.model.UserAccount#assignEmail(String)
     * @author bmcmullin
     */
    @Test
    public void testassignEmail() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String email = "email"; // UTA: default value
        underTest.assignEmail(email);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertEquals("email", underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for assignFullName(String)
     *
     * @see com.sim.chatserver.model.UserAccount#assignFullName(String)
     * @author bmcmullin
     */
    @Test
    public void testassignFullName() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String fullName = "fullName"; // UTA: default value
        underTest.assignFullName(fullName);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for assignId(Long)
     *
     * @see com.sim.chatserver.model.UserAccount#assignId(Long)
     * @author bmcmullin
     */
    @Test
    public void testAssignId() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        Long id = 1L; // UTA: default value
        underTest.assignId(id);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertEquals(1L, underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setPassword(String)
     *
     * @see com.sim.chatserver.model.UserAccount#setPassword(String)
     * @author bmcmullin
     */
    @Test
    public void testSetPassword() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String password = "password"; // UTA: default value
        underTest.setPassword(password);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertEquals("password", underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for assignPasswordHash(String)
     *
     * @see com.sim.chatserver.model.UserAccount#assignPasswordHash(String)
     * @author bmcmullin
     */
    @Test
    public void testAssignPasswordHash() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String hash = "hash"; // UTA: default value
        underTest.assignPasswordHash(hash);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertEquals("hash", underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setRole(String)
     *
     * @see com.sim.chatserver.model.UserAccount#setRole(String)
     * @author bmcmullin
     */
    @Test
    public void testSetRole() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String role = "role"; // UTA: default value
        underTest.setRole(role);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setUsername(String)
     *
     * @see com.sim.chatserver.model.UserAccount#setUsername(String)
     * @author bmcmullin
     */
    @Test
    public void testSetUsername() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String username = "username"; // UTA: default value
        underTest.setUsername(username);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertEquals("username", underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.emailValue());
        }, () -> {
            assertNull(underTest.createdAtValue());
        });

    }

    @Test
    public void testSerializationGuards_throwNotSerializableException() throws Throwable
    {
        UserAccount underTest = new UserAccount();

        Method readObject = UserAccount.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
        readObject.setAccessible(true);
        InvocationTargetException readEx = assertThrows(InvocationTargetException.class,
                () -> readObject.invoke(underTest, new Object[]{null}));
        assertEquals(NotSerializableException.class, readEx.getCause().getClass());

        Method writeObject = UserAccount.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
        writeObject.setAccessible(true);
        InvocationTargetException writeEx = assertThrows(InvocationTargetException.class,
                () -> writeObject.invoke(underTest, new Object[]{null}));
        assertEquals(NotSerializableException.class, writeEx.getCause().getClass());
    }
}



