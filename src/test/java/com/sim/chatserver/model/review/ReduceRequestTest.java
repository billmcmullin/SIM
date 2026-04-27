package com.sim.chatserver.model.review;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.ReduceRequest.Builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Parasoft Jtest UTA: Test class for ReduceRequest
 *
 * @see com.sim.chatserver.model.review.ReduceRequest
 * @author bmcmullin
 */
public class ReduceRequestTest
{

    /**
     * Parasoft Jtest UTA: Test for builder()
     *
     * @see com.sim.chatserver.model.review.ReduceRequest#builder()
     * @author bmcmullin
     */
    @Test
    public void testBuilder() throws Throwable
    {
        // When
        Builder result = ReduceRequest.builder();

        // Then - assertions for result of method builder()
        assertNotNull(result);

    }
}
