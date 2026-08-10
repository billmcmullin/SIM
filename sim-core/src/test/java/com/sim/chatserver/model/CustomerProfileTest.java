package com.sim.chatserver.model;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * Parasoft Jtest UTA: Test class for CustomerProfile
 *
 * @see com.sim.chatserver.model.CustomerProfile
 * @author bmcmullin
 */
public class CustomerProfileTest
{

    /**
     * Parasoft Jtest UTA: Test for getDepartment()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getDepartment()
     * @author bmcmullin
     */
    @Test
    public void testGetDepartment() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getDepartment();

        // Then - assertions for result of method getDepartment()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getEmail()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getEmail()
     * @author bmcmullin
     */
    @Test
    public void testGetEmail() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getEmail();

        // Then - assertions for result of method getEmail()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getFriendlyName()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getFriendlyName()
     * @author bmcmullin
     */
    @Test
    public void testGetFriendlyName() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getFriendlyName();

        // Then - assertions for result of method getFriendlyName()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getLastSyncedAt()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getLastSyncedAt()
     * @author bmcmullin
     */
    @Test
    public void testGetLastSyncedAt() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        OffsetDateTime result = underTest.getLastSyncedAt();

        // Then - assertions for result of method getLastSyncedAt()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getPhone()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getPhone()
     * @author bmcmullin
     */
    @Test
    public void testGetPhone() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getPhone();

        // Then - assertions for result of method getPhone()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRawJson()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getRawJson()
     * @author bmcmullin
     */
    @Test
    public void testGetRawJson() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getRawJson();

        // Then - assertions for result of method getRawJson()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceAccountId()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getSalesforceAccountId()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceAccountId() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getSalesforceAccountId();

        // Then - assertions for result of method getSalesforceAccountId()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSalesforceContactId()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getSalesforceContactId()
     * @author bmcmullin
     */
    @Test
    public void testGetSalesforceContactId() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getSalesforceContactId();

        // Then - assertions for result of method getSalesforceContactId()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSessionId()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getSessionId()
     * @author bmcmullin
     */
    @Test
    public void testGetSessionId() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getSessionId();

        // Then - assertions for result of method getSessionId()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTitle()
     *
     * @see com.sim.chatserver.model.CustomerProfile#getTitle()
     * @author bmcmullin
     */
    @Test
    public void testGetTitle() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String result = underTest.getTitle();

        // Then - assertions for result of method getTitle()
        assertNull(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String sessionId = "sessionId"; // UTA: default value
        underTest.setSessionId(sessionId);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertEquals("sessionId", underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty2() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String friendlyName = "friendlyName"; // UTA: default value
        underTest.setFriendlyName(friendlyName);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertEquals("friendlyName", underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty3() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String salesforceContactId = "salesforceContactId"; // UTA: default value
        underTest.setSalesforceContactId(salesforceContactId);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty4() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String salesforceAccountId = "salesforceAccountId"; // UTA: default value
        underTest.setSalesforceAccountId(salesforceAccountId);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty5() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String email = "email"; // UTA: default value
        underTest.setEmail(email);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty6() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String phone = "phone"; // UTA: default value
        underTest.setPhone(phone);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty7() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String title = "title"; // UTA: default value
        underTest.setTitle(title);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty8() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String department = "department"; // UTA: default value
        underTest.setDepartment(department);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty9() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        String rawJson = "rawJson"; // UTA: default value
        underTest.setRawJson(rawJson);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty10() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();
        OffsetDateTime lastSyncedAt = mock(OffsetDateTime.class);
        underTest.setLastSyncedAt(lastSyncedAt);

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertFalse(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNotNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isEmpty()
     *
     * @see com.sim.chatserver.model.CustomerProfile#isEmpty()
     * @author bmcmullin
     */
    @Test
    public void testIsEmpty11() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        boolean result = underTest.isEmpty();

        // Then - assertions for result of method isEmpty()
        assertTrue(result);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setDepartment(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setDepartment(String)
     * @author bmcmullin
     */
    @Test
    public void testSetDepartment() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String department = "department"; // UTA: default value
        underTest.setDepartment(department);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setEmail(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setEmail(String)
     * @author bmcmullin
     */
    @Test
    public void testSetEmail() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String email = "email"; // UTA: default value
        underTest.setEmail(email);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setFriendlyName(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setFriendlyName(String)
     * @author bmcmullin
     */
    @Test
    public void testSetFriendlyName() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String friendlyName = "friendlyName"; // UTA: default value
        underTest.setFriendlyName(friendlyName);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertEquals("friendlyName", underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setLastSyncedAt(OffsetDateTime)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setLastSyncedAt(OffsetDateTime)
     * @author bmcmullin
     */
    @Test
    public void testSetLastSyncedAt() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        OffsetDateTime lastSyncedAt = mock(OffsetDateTime.class);
        underTest.setLastSyncedAt(lastSyncedAt);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNotNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setPhone(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setPhone(String)
     * @author bmcmullin
     */
    @Test
    public void testSetPhone() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String phone = "phone"; // UTA: default value
        underTest.setPhone(phone);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setRawJson(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setRawJson(String)
     * @author bmcmullin
     */
    @Test
    public void testSetRawJson() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String rawJson = "rawJson"; // UTA: default value
        underTest.setRawJson(rawJson);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceAccountId(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setSalesforceAccountId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceAccountId() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String salesforceAccountId = "salesforceAccountId"; // UTA: default value
        underTest.setSalesforceAccountId(salesforceAccountId);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSalesforceContactId(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setSalesforceContactId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSalesforceContactId() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String salesforceContactId = "salesforceContactId"; // UTA: default value
        underTest.setSalesforceContactId(salesforceContactId);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setSessionId(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setSessionId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetSessionId() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String sessionId = "sessionId"; // UTA: default value
        underTest.setSessionId(sessionId);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertEquals("sessionId", underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setTitle(String)
     *
     * @see com.sim.chatserver.model.CustomerProfile#setTitle(String)
     * @author bmcmullin
     */
    @Test
    public void testSetTitle() throws Throwable
    {
        // Given
        CustomerProfile underTest = new CustomerProfile();

        // When
        String title = "title"; // UTA: default value
        underTest.setTitle(title);

        // Then - assertions for this instance of CustomerProfile
        assertAll(() -> {
            assertNull(underTest.getSessionId());
        }, () -> {
            assertNull(underTest.getFriendlyName());
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
            assertNull(underTest.getLastSyncedAt());
        });

    }


    // Merged from CustomerProfileSerializationGuardTest
    
    
        @Test
        void readObject_throwsNotSerializableException() throws Exception {
            CustomerProfile profile = new CustomerProfile();
            Method readObject = CustomerProfile.class.getDeclaredMethod("readObject", java.io.ObjectInputStream.class);
            readObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> readObject.invoke(profile, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(CustomerProfile.class.getName(), cause.getMessage());
        }
    
        @Test
        void writeObject_throwsNotSerializableException() throws Exception {
            CustomerProfile profile = new CustomerProfile();
            Method writeObject = CustomerProfile.class.getDeclaredMethod("writeObject", java.io.ObjectOutputStream.class);
            writeObject.setAccessible(true);
    
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> writeObject.invoke(profile, new Object[] { null }));
            NotSerializableException cause = assertInstanceOf(NotSerializableException.class, ex.getCause());
            assertEquals(CustomerProfile.class.getName(), cause.getMessage());
        }
}
