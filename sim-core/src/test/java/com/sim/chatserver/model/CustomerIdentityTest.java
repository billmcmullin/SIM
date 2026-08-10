package com.sim.chatserver.model;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * Parasoft Jtest UTA: Test class for CustomerIdentity
 *
 * @see com.sim.chatserver.model.CustomerIdentity
 * @author bmcmullin
 */
public class CustomerIdentityTest
{

    /**
     * Parasoft Jtest UTA: Test for getCanonicalEmail()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getCanonicalEmail()
     * @author bmcmullin
     */
    @Test
    public void testGetCanonicalEmail() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getCanonicalEmail();

        // Then - assertions for result of method getCanonicalEmail()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getCanonicalName()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getCanonicalName()
     * @author bmcmullin
     */
    @Test
    public void testGetCanonicalName() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getCanonicalName();

        // Then - assertions for result of method getCanonicalName()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getConfidence()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getConfidence()
     * @author bmcmullin
     */
    @Test
    public void testGetConfidence() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getConfidence();

        // Then - assertions for result of method getConfidence()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getCreatedAt()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getCreatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetCreatedAt() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        OffsetDateTime result = underTest.getCreatedAt();

        // Then - assertions for result of method getCreatedAt()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getDepartment()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getDepartment()
     * @author bmcmullin
     */
    @Test
    public void testGetDepartment() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getDepartment();

        // Then - assertions for result of method getDepartment()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getEmail()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getEmail()
     * @author bmcmullin
     */
    @Test
    public void testGetEmail() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getEmail();

        // Then - assertions for result of method getEmail()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getIdentityId()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getIdentityId()
     * @author bmcmullin
     */
    @Test
    public void testGetIdentityId() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        Long result = underTest.getIdentityId();

        // Then - assertions for result of method getIdentityId()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLastSyncedAt()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getLastSyncedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetLastSyncedAt() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        OffsetDateTime result = underTest.getLastSyncedAt();

        // Then - assertions for result of method getLastSyncedAt()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPhone()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getPhone()
     * @author bmcmullin
     */
    @Test
    public void testGetPhone() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getPhone();

        // Then - assertions for result of method getPhone()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRawJson()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getRawJson()
     * @author bmcmullin
     */
    @Test
    public void testGetRawJson() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getRawJson();

        // Then - assertions for result of method getRawJson()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceAccountId()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getSalesforceAccountId()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceAccountId() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getSalesforceAccountId();

        // Then - assertions for result of method getSalesforceAccountId()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceContactId()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getSalesforceContactId()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceContactId() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getSalesforceContactId();

        // Then - assertions for result of method getSalesforceContactId()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTitle()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getTitle()
     * @author bmcmullin
     */
    @Test
    public void testGetTitle() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String result = underTest.getTitle();

        // Then - assertions for result of method getTitle()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getUpdatedAt()
     *
     * @see com.sim.chatserver.model.CustomerIdentity#getUpdatedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetUpdatedAt() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        OffsetDateTime result = underTest.getUpdatedAt();

        // Then - assertions for result of method getUpdatedAt()
        assertNull(result);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setCanonicalEmail(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setCanonicalEmail(String)
     * @author bmcmullin
     */
    @Test
    public void testSetCanonicalEmail() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String canonicalEmail = "canonicalEmail"; // UTA: default value
        underTest.setCanonicalEmail(canonicalEmail);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertEquals("canonicalEmail", underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setCanonicalName(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setCanonicalName(String)
     * @author bmcmullin
     */
    @Test
    public void testSetCanonicalName() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String canonicalName = "canonicalName"; // UTA: default value
        underTest.setCanonicalName(canonicalName);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertEquals("canonicalName", underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setConfidence(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setConfidence(String)
     * @author bmcmullin
     */
    @Test
    public void testSetConfidence() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String confidence = "confidence"; // UTA: default value
        underTest.setConfidence(confidence);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertEquals("confidence", underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setCreatedAt(OffsetDateTime)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setCreatedAt(OffsetDateTime)
     * @author bmcmullin
     */
    @Test
    public void testSetCreatedAt() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        OffsetDateTime createdAt = mock(OffsetDateTime.class);
        underTest.setCreatedAt(createdAt);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNotNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setDepartment(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setDepartment(String)
     * @author bmcmullin
     */
    @Test
    public void testSetDepartment() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String department = "department"; // UTA: default value
        underTest.setDepartment(department);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertEquals("department", underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setEmail(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setEmail(String)
     * @author bmcmullin
     */
    @Test
    public void testSetEmail() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String email = "email"; // UTA: default value
        underTest.setEmail(email);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertEquals("email", underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setIdentityId(Long)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setIdentityId(Long)
     * @author bmcmullin
     */
    @Test
    public void testSetIdentityId() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        Long identityId = 1L; // UTA: default value
        underTest.setIdentityId(identityId);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertEquals(1L, underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setLastSyncedAt(OffsetDateTime)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setLastSyncedAt(OffsetDateTime)
     * @author bmcmullin
     */
    @Test
    public void testSetLastSyncedAt() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        OffsetDateTime lastSyncedAt = mock(OffsetDateTime.class);
        underTest.setLastSyncedAt(lastSyncedAt);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNotNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setPhone(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setPhone(String)
     * @author bmcmullin
     */
    @Test
    public void testSetPhone() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String phone = "phone"; // UTA: default value
        underTest.setPhone(phone);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertEquals("phone", underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setRawJson(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setRawJson(String)
     * @author bmcmullin
     */
    @Test
    public void testSetRawJson() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String rawJson = "rawJson"; // UTA: default value
        underTest.setRawJson(rawJson);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertEquals("rawJson", underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceAccountId(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setSalesforceAccountId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceAccountId() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String salesforceAccountId = "salesforceAccountId"; // UTA: default value
        underTest.setSalesforceAccountId(salesforceAccountId);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertEquals("salesforceAccountId", underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceContactId(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setSalesforceContactId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceContactId() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String salesforceContactId = "salesforceContactId"; // UTA: default value
        underTest.setSalesforceContactId(salesforceContactId);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertEquals("salesforceContactId", underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setTitle(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setTitle(String)
     * @author bmcmullin
     */
    @Test
    public void testSetTitle() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        String title = "title"; // UTA: default value
        underTest.setTitle(title);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertEquals("title", underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setUpdatedAt(OffsetDateTime)
     *
     * @see com.sim.chatserver.model.CustomerIdentity#setUpdatedAt(OffsetDateTime)
     * @author bmcmullin
     */
    @Test
    public void testSetUpdatedAt() throws Throwable
    {
        // Given
        CustomerIdentity underTest = new CustomerIdentity();

        // When
        OffsetDateTime updatedAt = mock(OffsetDateTime.class);
        underTest.setUpdatedAt(updatedAt);

        // Then - assertions for this instance of CustomerIdentity
        assertAll(() -> {
            assertNull(underTest.getIdentityId());
        }, () -> {
            assertNull(underTest.getCanonicalEmail());
        }, () -> {
            assertNull(underTest.getCanonicalName());
        }, () -> {
            assertNull(underTest.getSalesforceContactId());
        }, () -> {
            assertNull(underTest.getSalesforceAccountId());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        }, () -> {
            assertNull(underTest.getConfidence());
        }, () -> {
            assertNull(underTest.getCreatedAt());
        }, () -> {
            assertNotNull(underTest.getUpdatedAt());
        }, () -> {
            assertNull(underTest.getLastSyncedAt());
        });

    }


    // Merged from CustomerIdentitySerializationGuardTest
    
    
        @Test
        void readObject_throwsNotSerializableException() throws Exception {
            CustomerIdentity value = new CustomerIdentity();
            Method readObject = CustomerIdentity.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
            readObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> readObject.invoke(value, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(CustomerIdentity.class.getName(), cause.getMessage());
        }
    
        @Test
        void writeObject_throwsNotSerializableException() throws Exception {
            CustomerIdentity value = new CustomerIdentity();
            Method writeObject = CustomerIdentity.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
            writeObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> writeObject.invoke(value, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(CustomerIdentity.class.getName(), cause.getMessage());
        }
}
