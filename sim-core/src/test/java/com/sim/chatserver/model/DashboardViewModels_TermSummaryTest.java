package com.sim.chatserver.model;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.term.TermChatSnapshot;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for TermSummary
 *
 * @see com.sim.chatserver.model.DashboardViewModels.TermSummary
 * @author bmcmullin
 */
public class DashboardViewModels_TermSummaryTest
{

    /**
     * Parasoft Jtest UTA: Test for copyTermSnapshots()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermSummary#copyTermSnapshots()
     * @author bmcmullin
     */
    @Test
    public void testCopyTermSnapshots() throws Throwable
    {
        // Given
        TermSummary underTest = new TermSummary();

        // When
        Map<String, List<TermChatSnapshot>> result = underTest.copyTermSnapshots();

        // Then - assertions for result of method copyTermSnapshots()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of DashboardViewModels.TermSummary
        assertAll(() -> {
            assertNotNull(underTest.getTermCounts());
            assertEquals(0, underTest.getTermCounts().size());
        }, () -> {
            assertNotNull(underTest.getTermSnapshots());
            assertEquals(0, underTest.getTermSnapshots().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ensureTerm(String)
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermSummary#ensureTerm(String)
     * @author bmcmullin
     */
    @Test
    public void testEnsureTerm() throws Throwable
    {
        // Given
        TermSummary underTest = new TermSummary();

        // When
        String termName = "termName"; // UTA: default value
        underTest.ensureTerm(termName);

        // Then - assertions for this instance of DashboardViewModels.TermSummary
        assertAll(() -> {
            assertNotNull(underTest.getTermCounts());
            assertEquals(1, underTest.getTermCounts().size());
        }, () -> {
            assertNotNull(underTest.getTermSnapshots());
            assertEquals(1, underTest.getTermSnapshots().size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTermCounts()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermSummary#getTermCounts()
     * @author bmcmullin
     */
    @Test
    public void testGetTermCounts() throws Throwable
    {
        // Given
        TermSummary underTest = new TermSummary();

        // When
        Map<String, Integer> result = underTest.getTermCounts();

        // Then - assertions for result of method getTermCounts()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of DashboardViewModels.TermSummary
        assertNotNull(underTest.getTermSnapshots());
        assertEquals(0, underTest.getTermSnapshots().size());

    }

    /**
     * Parasoft Jtest UTA: Test for getTermSnapshots()
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermSummary#getTermSnapshots()
     * @author bmcmullin
     */
    @Test
    public void testGetTermSnapshots() throws Throwable
    {
        // Given
        TermSummary underTest = new TermSummary();

        // When
        Map<String, List<TermChatSnapshot>> result = underTest.getTermSnapshots();

        // Then - assertions for result of method getTermSnapshots()
        assertNotNull(result);
        assertEquals(0, result.size());

        // Then - assertions for this instance of DashboardViewModels.TermSummary
        assertNotNull(underTest.getTermCounts());
        assertEquals(0, underTest.getTermCounts().size());

    }

    /**
     * Parasoft Jtest UTA: Test for recordMatch(String, TermChatSnapshot)
     *
     * @see com.sim.chatserver.model.DashboardViewModels.TermSummary#recordMatch(String, TermChatSnapshot)
     * @author bmcmullin
     */
    @Test
    public void testRecordMatch() throws Throwable
    {
        // Given
        TermSummary underTest = new TermSummary();

        // When
        String termName = "termName"; // UTA: default value
        TermChatSnapshot snapshot = mock(TermChatSnapshot.class);
        underTest.recordMatch(termName, snapshot);

        // Then - assertions for this instance of DashboardViewModels.TermSummary
        assertAll(() -> {
            assertNotNull(underTest.getTermCounts());
            assertEquals(1, underTest.getTermCounts().size());
        }, () -> {
            assertNotNull(underTest.getTermSnapshots());
            assertEquals(1, underTest.getTermSnapshots().size());
        });

    }
}
