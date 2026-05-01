package com.sim.chatserver.model;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
     * Parasoft Jtest UTA: Test for getCreatedAt()
     *
     * @see com.sim.chatserver.model.UserAccount#getCreatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetCreatedAt() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        Instant result = underTest.getCreatedAt();

        // Then - assertions for result of method getCreatedAt()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.getEmail());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getEmail()
     *
     * @see com.sim.chatserver.model.UserAccount#getEmail()
     * @author bmcmullin
     */
    @Test
    public void testGetEmail() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.getEmail();

        // Then - assertions for result of method getEmail()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFullName()
     *
     * @see com.sim.chatserver.model.UserAccount#getFullName()
     * @author bmcmullin
     */
    @Test
    public void testGetFullName() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();
        String fullName = "fullName"; // UTA: default value
        underTest.setFullName(fullName);

        // When
        String result = underTest.getFullName();

        // Then - assertions for result of method getFullName()
        assertEquals("fullName", result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFullName()
     *
     * @see com.sim.chatserver.model.UserAccount#getFullName()
     * @author bmcmullin
     */
    @Test
    public void testGetFullName2() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String result = underTest.getFullName();

        // Then - assertions for result of method getFullName()
        assertNull(result);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setEmail(String)
     *
     * @see com.sim.chatserver.model.UserAccount#setEmail(String)
     * @author bmcmullin
     */
    @Test
    public void testSetEmail() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String email = "email"; // UTA: default value
        underTest.setEmail(email);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertEquals("email", underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setFullName(String)
     *
     * @see com.sim.chatserver.model.UserAccount#setFullName(String)
     * @author bmcmullin
     */
    @Test
    public void testSetFullName() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String fullName = "fullName"; // UTA: default value
        underTest.setFullName(fullName);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setId(Long)
     *
     * @see com.sim.chatserver.model.UserAccount#setId(Long)
     * @author bmcmullin
     */
    @Test
    public void testSetId() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        Long id = 1L; // UTA: default value
        underTest.setId(id);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertEquals(1L, underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertNull(underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setPasswordHash(String)
     *
     * @see com.sim.chatserver.model.UserAccount#setPasswordHash(String)
     * @author bmcmullin
     */
    @Test
    public void testSetPasswordHash() throws Throwable
    {
        // Given
        UserAccount underTest = new UserAccount();

        // When
        String hash = "hash"; // UTA: default value
        underTest.setPasswordHash(hash);

        // Then - assertions for this instance of UserAccount
        assertAll(() -> {
            assertNull(underTest.getId());
        }, () -> {
            assertNull(underTest.getUsername());
        }, () -> {
            assertEquals("hash", underTest.getPasswordHash());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
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
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        });

    }
}
