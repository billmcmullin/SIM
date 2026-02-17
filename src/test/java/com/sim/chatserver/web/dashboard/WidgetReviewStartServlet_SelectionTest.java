package com.sim.chatserver.web.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.WidgetReviewStartServlet.SearchTerms;
import com.sim.chatserver.web.dashboard.WidgetReviewStartServlet.Selection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for Selection
 *
 * @see com.sim.chatserver.web.dashboard.WidgetReviewStartServlet.Selection
 * @author bmcmullin
 */
public class WidgetReviewStartServlet_SelectionTest
{

    /**
     * Parasoft Jtest UTA: Test for fromTermSnapshots(String, String, List)
     *
     * @see com.sim.chatserver.web.dashboard.WidgetReviewStartServlet.Selection#fromTermSnapshots(String, String, List)
     * @author bmcmullin
     */
    @Test
    public void testFromTermSnapshots() throws Throwable
    {
        // When
        String displayName = "displayName"; // UTA: default value
        String backUrl = "backUrl"; // UTA: default value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        Selection result = Selection.fromTermSnapshots(displayName, backUrl, snapshots);

        // Then - assertions for result of method fromTermSnapshots(String, String, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("displayName", result.widgetId);
        }, () -> {
            assertEquals("displayName", result.displayName);
        }, () -> {
            assertEquals("backUrl", result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(1, result.chatIds.size());
        }, () -> {
            assertNotNull(result.searchTerms);
        }, () -> {
            assertEquals("", result.searchTerms.global);
        }, () -> {
            assertEquals("", result.searchTerms.prompt);
        }, () -> {
            assertEquals("", result.searchTerms.response);
        }, () -> {
            assertNotNull(result.snapshots);
            assertEquals(1, result.snapshots.size());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms)
     *
     * @see com.sim.chatserver.web.dashboard.WidgetReviewStartServlet.Selection#fromWidget(String, List, SearchTerms)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: default value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = mock(SearchTerms.class);
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("widgetId", result.widgetId);
        }, () -> {
            assertEquals("widgetId", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(1, result.chatIds.size());
        }, () -> {
            assertNotNull(result.searchTerms);
        }, () -> {
            assertNull(result.snapshots);
        });

    }
}
