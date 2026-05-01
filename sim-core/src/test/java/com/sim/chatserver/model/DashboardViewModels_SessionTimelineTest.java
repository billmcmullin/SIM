package com.sim.chatserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 * Parasoft Jtest UTA: Test class for SessionTimeline
 *
 * @see com.sim.chatserver.model.DashboardViewModels.SessionTimeline
 * @author bmcmullin
 */
public class DashboardViewModels_SessionTimelineTest
{

    /**
     * Parasoft Jtest UTA: Test for getCountsBySession()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionTimeline#getCountsBySession()
     * @author bmcmullin
     */
    @Test
    public void testGetCountsBySession() throws Throwable
    {
        // Given
        List<String> labels = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        labels.add(item);
        Map<String, List<Integer>> countsBySession = new HashMap<String, List<Integer>>(); // UTA: default value
        String key = "key"; // UTA: default value
        List<Integer> value = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        value.add(item2);
        countsBySession.put(key, value);
        SessionTimeline underTest = new SessionTimeline(labels, countsBySession);

        // When
        Map<String, List<Integer>> result = underTest.getCountsBySession();

        // Then - assertions for result of method getCountsBySession()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of DashboardViewModels.SessionTimeline
        assertNotNull(underTest.getLabels());
        assertEquals(1, underTest.getLabels().size());

    }

    /**
     * Parasoft Jtest UTA: Test for getLabels()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.SessionTimeline#getLabels()
     * @author bmcmullin
     */
    @Test
    public void testGetLabels() throws Throwable
    {
        // Given
        List<String> labels = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        labels.add(item);
        Map<String, List<Integer>> countsBySession = new HashMap<String, List<Integer>>(); // UTA: default value
        String key = "key"; // UTA: default value
        List<Integer> value = new ArrayList<Integer>(); // UTA: default value
        Integer item2 = 1; // UTA: default value
        value.add(item2);
        countsBySession.put(key, value);
        SessionTimeline underTest = new SessionTimeline(labels, countsBySession);

        // When
        List<String> result = underTest.getLabels();

        // Then - assertions for result of method getLabels()
        assertNotNull(result);
        assertEquals(1, result.size());

        // Then - assertions for this instance of DashboardViewModels.SessionTimeline
        assertNotNull(underTest.getCountsBySession());
        assertEquals(1, underTest.getCountsBySession().size());

    }
}
