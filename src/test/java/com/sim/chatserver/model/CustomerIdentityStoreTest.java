package com.sim.chatserver.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Parasoft Jtest UTA: Test class for CustomerIdentityStore
 *
 * @see com.sim.chatserver.model.CustomerIdentityStore
 * @author bmcmullin
 */
public class CustomerIdentityStoreTest
{

    /**
     * Parasoft Jtest UTA: Test for findByCanonicalEmail(String)
     *
     * @see com.sim.chatserver.model.CustomerIdentityStore#findByCanonicalEmail(String)
     * @author bmcmullin
     */
    @Test
    public void testFindByCanonicalEmail() throws Throwable
    {
        // When
        String email = null; // UTA: configured value
        CustomerIdentity result = CustomerIdentityStore.findByCanonicalEmail(email);

    }

    /**
     * Parasoft Jtest UTA: Test for insertIdentity(String, String, String)
     *
     * @see com.sim.chatserver.model.CustomerIdentityStore#insertIdentity(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testInsertIdentity() throws Throwable
    {
        // When
        String canonicalEmail = null; // UTA: configured value
        String canonicalName = null; // UTA: configured value
        String confidence = "confidence"; // UTA: configured value
        long result = CustomerIdentityStore.insertIdentity(canonicalEmail, canonicalName, confidence);

    }

}
