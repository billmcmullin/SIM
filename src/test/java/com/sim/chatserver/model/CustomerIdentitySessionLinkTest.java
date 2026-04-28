package com.sim.chatserver.model;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for CustomerIdentitySessionLink
 *
 * @see com.sim.chatserver.model.CustomerIdentitySessionLink
 * @author bmcmullin
 */
public class CustomerIdentitySessionLinkTest
{

    /**
     * Parasoft Jtest UTA: Test for getContactEmailSnapshot()
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#getContactEmailSnapshot()
     * @author bmcmullin
     */
    @Test
    public void testGetContactEmailSnapshot() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        String result = underTest.getContactEmailSnapshot();

        // Then - assertions for result of method getContactEmailSnapshot()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getDisplayNameSnapshot()
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#getDisplayNameSnapshot()
     * @author bmcmullin
     */
    @Test
    public void testGetDisplayNameSnapshot() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        String result = underTest.getDisplayNameSnapshot();

        // Then - assertions for result of method getDisplayNameSnapshot()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getIdentityId()
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#getIdentityId()
     * @author bmcmullin
     */
    @Test
    public void testGetIdentityId() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        Long result = underTest.getIdentityId();

        // Then - assertions for result of method getIdentityId()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLinkedAt()
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#getLinkedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetLinkedAt() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        OffsetDateTime result = underTest.getLinkedAt();

        // Then - assertions for result of method getLinkedAt()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionId()
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#getSessionId()
     * @author bmcmullin
     */
    @Test
    public void testGetSessionId() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        String result = underTest.getSessionId();

        // Then - assertions for result of method getSessionId()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUpdatedAt()
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#getUpdatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetUpdatedAt() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        OffsetDateTime result = underTest.getUpdatedAt();

        // Then - assertions for result of method getUpdatedAt()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setContactEmailSnapshot(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#setContactEmailSnapshot(String)
     * @author bmcmullin
     */
    @Test
    public void testSetContactEmailSnapshot() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        String contactEmailSnapshot = "contactEmailSnapshot"; // UTA: default value
        underTest.setContactEmailSnapshot(contactEmailSnapshot);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertEquals("contactEmailSnapshot", underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setDisplayNameSnapshot(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#setDisplayNameSnapshot(String)
     * @author bmcmullin
     */
    @Test
    public void testSetDisplayNameSnapshot() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        String displayNameSnapshot = "displayNameSnapshot"; // UTA: default value
        underTest.setDisplayNameSnapshot(displayNameSnapshot);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertEquals("displayNameSnapshot", underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setIdentityId(Long)
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#setIdentityId(Long)
     * @author bmcmullin
     */
    @Test
    public void testSetIdentityId() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        Long identityId = 1L; // UTA: default value
        underTest.setIdentityId(identityId);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertEquals(1L, underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setLinkedAt(OffsetDateTime)
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#setLinkedAt(OffsetDateTime)
     * @author bmcmullin
     */
    @Test
    public void testSetLinkedAt() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        OffsetDateTime linkedAt = mock(OffsetDateTime.class);
        underTest.setLinkedAt(linkedAt);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNotNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSessionId(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#setSessionId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSessionId() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        String sessionId = "sessionId"; // UTA: default value
        underTest.setSessionId(sessionId);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertEquals("sessionId", underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setUpdatedAt(OffsetDateTime)
     *
     * @see com.sim.chatserver.model.CustomerIdentitySessionLink#setUpdatedAt(OffsetDateTime)
     * @author bmcmullin
     */
    @Test
    public void testSetUpdatedAt() throws Throwable
    {
        // Given
        CustomerIdentitySessionLink underTest = new CustomerIdentitySessionLink();

        // When
        OffsetDateTime updatedAt = mock(OffsetDateTime.class);
        underTest.setUpdatedAt(updatedAt);

        // Then - assertions for this instance of CustomerIdentitySessionLink
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getDisplayNameSnapshot());
        }, () -> {
            assertNull(underTest.getContactEmailSnapshot());
        }, () -> {
            assertNull(underTest.getLinkedAt());
        }, () -> {
            assertNotNull(underTest.getUpdatedAt());
        });

    }
}
