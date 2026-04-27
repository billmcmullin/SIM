package com.sim.chatserver.salesforce;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
/**
 * Parasoft Jtest UTA: Test class for SalesforceCustomerMatch
 *
 * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch
 * @author bmcmullin
 */
public class SalesforceCustomerMatchTest
{

    /**
     * Parasoft Jtest UTA: Test for getAccountId()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getAccountId()
     * @author bmcmullin
     */
    @Test
    public void testGetAccountId() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getAccountId();

        // Then - assertions for result of method getAccountId()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getName());
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
     * Parasoft Jtest UTA: Test for getContactId()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getContactId()
     * @author bmcmullin
     */
    @Test
    public void testGetContactId() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getContactId();

        // Then - assertions for result of method getContactId()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
     * Parasoft Jtest UTA: Test for getDepartment()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getDepartment()
     * @author bmcmullin
     */
    @Test
    public void testGetDepartment() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getDepartment();

        // Then - assertions for result of method getDepartment()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getRawJson());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getEmail()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getEmail()
     * @author bmcmullin
     */
    @Test
    public void testGetEmail() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getEmail();

        // Then - assertions for result of method getEmail()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
     * Parasoft Jtest UTA: Test for getName()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getName()
     * @author bmcmullin
     */
    @Test
    public void testGetName() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getName();

        // Then - assertions for result of method getName()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
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
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getPhone()
     * @author bmcmullin
     */
    @Test
    public void testGetPhone() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getPhone();

        // Then - assertions for result of method getPhone()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getRawJson()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getRawJson()
     * @author bmcmullin
     */
    @Test
    public void testGetRawJson() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getRawJson();

        // Then - assertions for result of method getRawJson()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getTitle());
        }, () -> {
            assertNull(underTest.getDepartment());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTitle()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#getTitle()
     * @author bmcmullin
     */
    @Test
    public void testGetTitle() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String result = underTest.getTitle();

        // Then - assertions for result of method getTitle()
        assertNull(result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
        }, () -> {
            assertNull(underTest.getEmail());
        }, () -> {
            assertNull(underTest.getPhone());
        }, () -> {
            assertNull(underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setAccountId(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setAccountId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetAccountId() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String accountId = "accountId"; // UTA: default value
        underTest.setAccountId(accountId);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertEquals("accountId", underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
     * Parasoft Jtest UTA: Test for setContactId(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setContactId(String)
     * @author bmcmullin
     */
    @Test
    public void testSetContactId() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String contactId = "contactId"; // UTA: default value
        underTest.setContactId(contactId);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertEquals("contactId", underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
     * Parasoft Jtest UTA: Test for setDepartment(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setDepartment(String)
     * @author bmcmullin
     */
    @Test
    public void testSetDepartment() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String department = "department"; // UTA: default value
        underTest.setDepartment(department);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setEmail(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setEmail(String)
     * @author bmcmullin
     */
    @Test
    public void testSetEmail() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String email = "email"; // UTA: default value
        underTest.setEmail(email);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setName(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setName(String)
     * @author bmcmullin
     */
    @Test
    public void testSetName() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String name = "name"; // UTA: default value
        underTest.setName(name);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertEquals("name", underTest.getName());
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
     * Parasoft Jtest UTA: Test for setPhone(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setPhone(String)
     * @author bmcmullin
     */
    @Test
    public void testSetPhone() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String phone = "phone"; // UTA: default value
        underTest.setPhone(phone);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setRawJson(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setRawJson(String)
     * @author bmcmullin
     */
    @Test
    public void testSetRawJson() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String rawJson = "rawJson"; // UTA: default value
        underTest.setRawJson(rawJson);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
        });

    }

    /**
     * Parasoft Jtest UTA: Test for setTitle(String)
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#setTitle(String)
     * @author bmcmullin
     */
    @Test
    public void testSetTitle() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();

        // When
        String title = "title"; // UTA: default value
        underTest.setTitle(title);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertNull(underTest.getContactId());
        }, () -> {
            assertNull(underTest.getAccountId());
        }, () -> {
            assertNull(underTest.getName());
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
        });

    }

    /**
     * Parasoft Jtest UTA: Test for toString()
     *
     * @see com.sim.chatserver.salesforce.SalesforceCustomerMatch#toString()
     * @author bmcmullin
     */
    @Test
    public void testToString() throws Throwable
    {
        // Given
        SalesforceCustomerMatch underTest = new SalesforceCustomerMatch();
        String accountId = "accountId"; // UTA: default value
        underTest.setAccountId(accountId);
        String contactId = "contactId"; // UTA: default value
        underTest.setContactId(contactId);
        String department = "department"; // UTA: default value
        underTest.setDepartment(department);
        String email = "email"; // UTA: default value
        underTest.setEmail(email);
        String name = "name"; // UTA: default value
        underTest.setName(name);
        String phone = "phone"; // UTA: default value
        underTest.setPhone(phone);
        String title = "title"; // UTA: default value
        underTest.setTitle(title);

        // When
        String result = underTest.toString();

        // Then - assertions for result of method toString()
        assertEquals("SalesforceCustomerMatch{contactId='contactId', accountId='accountId', name='name', email='email', phone='phone', title='title', department='department'}", result);

        // Then - assertions for this instance of SalesforceCustomerMatch
        assertAll(() -> {
            assertEquals("contactId", underTest.getContactId());
        }, () -> {
            assertEquals("accountId", underTest.getAccountId());
        }, () -> {
            assertEquals("name", underTest.getName());
        }, () -> {
            assertEquals("email", underTest.getEmail());
        }, () -> {
            assertEquals("phone", underTest.getPhone());
        }, () -> {
            assertEquals("title", underTest.getTitle());
        }, () -> {
            assertEquals("department", underTest.getDepartment());
        }, () -> {
            assertNull(underTest.getRawJson());
        });

    }
}
