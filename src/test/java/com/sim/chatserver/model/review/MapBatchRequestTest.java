package com.sim.chatserver.model.review;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.review.MapBatchRequest.Builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Parasoft Jtest UTA: Test class for MapBatchRequest
 *
 * @see com.sim.chatserver.model.review.MapBatchRequest
 * @author bmcmullin
 */
public class MapBatchRequestTest
{

    /**
     * Parasoft Jtest UTA: Test for builder()
     *
     * @see com.sim.chatserver.model.review.MapBatchRequest#builder()
     * @author bmcmullin
     */
    @Test
    public void testBuilder() throws Throwable
    {
        // When
        Builder result = MapBatchRequest.builder();

        // Then - assertions for result of method builder()
        assertNotNull(result);

    }
}
