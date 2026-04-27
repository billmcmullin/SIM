package com.sim.chatserver.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.CustomerIdentity;
import com.sim.chatserver.model.CustomerIdentitySessionLink;

import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Parasoft Jtest UTA: Test class for CustomerIdentityService
 *
 * @see com.sim.chatserver.service.CustomerIdentityService
 * @author bmcmullin
 */
public class CustomerIdentityServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for resolveOrCreateBySessionId(String)
     *
     * @see com.sim.chatserver.service.CustomerIdentityService#resolveOrCreateBySessionId(String)
     * @author bmcmullin
     */
    @Test
    public void testResolveOrCreateBySessionId() throws Throwable
    {
        // Given
        CustomerIdentityService underTest = new CustomerIdentityService();

        // When
        String sessionId = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            underTest.resolveOrCreateBySessionId(sessionId);
        });

    }

}
