package com.sim.chatserver.web.dashboard.widgets;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.SearchTerms;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
/**
 * Parasoft Jtest UTA: Test class for Selection
 *
 * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection
 * @author bmcmullin
 */
public class WidgetReviewStartServlet_SelectionTest
{

    /**
     * Parasoft Jtest UTA: Test for fromTermSnapshots(String, String, List)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromTermSnapshots(String,
     *      String, List)
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
            assertEquals(0, result.chatIds.size());
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
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromTermSnapshots(String, String, List)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromTermSnapshots(String,
     *      String, List)
     * @author bmcmullin
     */
    @Test
    public void testFromTermSnapshots2() throws Throwable
    {
        // When
        String displayName = "displayName"; // UTA: default value
        String backUrl = null; // UTA: configured value
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
            assertEquals("", result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(0, result.chatIds.size());
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
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromTermSnapshots(String, String, List)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromTermSnapshots(String,
     *      String, List)
     * @author bmcmullin
     */
    @Test
    public void testFromTermSnapshots3() throws Throwable
    {
        // When
        String displayName = null; // UTA: configured value
        String backUrl = "backUrl"; // UTA: default value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        Selection result = Selection.fromTermSnapshots(displayName, backUrl, snapshots);

        // Then - assertions for result of method fromTermSnapshots(String, String, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertEquals("backUrl", result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(0, result.chatIds.size());
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
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromTermSnapshots(String, String, List)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromTermSnapshots(String,
     *      String, List)
     * @author bmcmullin
     */
    @Test
    public void testFromTermSnapshots4() throws Throwable
    {
        // When
        String displayName = null; // UTA: configured value
        String backUrl = null; // UTA: configured value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        Selection result = Selection.fromTermSnapshots(displayName, backUrl, snapshots);

        // Then - assertions for result of method fromTermSnapshots(String, String, List)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertEquals("", result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(0, result.chatIds.size());
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
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
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
        String date = null; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
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
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget2() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = mock(SearchTerms.class);
        String date = null; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(1, result.chatIds.size());
        }, () -> {
            assertNotNull(result.searchTerms);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget3() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: default value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
        String date = null; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
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
            assertEquals("", result.searchTerms.global);
        }, () -> {
            assertEquals("", result.searchTerms.prompt);
        }, () -> {
            assertEquals("", result.searchTerms.response);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget4() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
        String date = null; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
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
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget5() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: default value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = mock(SearchTerms.class);
        String date = "*"; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
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
        }, () -> {
            assertEquals("*", result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget6() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: default value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = mock(SearchTerms.class);
        String date = ""; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
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
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget7() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = mock(SearchTerms.class);
        String date = "*"; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(1, result.chatIds.size());
        }, () -> {
            assertNotNull(result.searchTerms);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertEquals("*", result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget8() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = mock(SearchTerms.class);
        String date = ""; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(1, result.chatIds.size());
        }, () -> {
            assertNotNull(result.searchTerms);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget9() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: default value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
        String date = "*"; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
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
            assertEquals("", result.searchTerms.global);
        }, () -> {
            assertEquals("", result.searchTerms.prompt);
        }, () -> {
            assertEquals("", result.searchTerms.response);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertEquals("*", result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget10() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: default value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
        String date = ""; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
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
            assertEquals("", result.searchTerms.global);
        }, () -> {
            assertEquals("", result.searchTerms.prompt);
        }, () -> {
            assertEquals("", result.searchTerms.response);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget11() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
        String date = "*"; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
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
            assertNull(result.snapshots);
        }, () -> {
            assertEquals("*", result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms, String)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget12() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
        String date = ""; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms, date);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
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
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget13() throws Throwable
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
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget14() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = mock(SearchTerms.class);
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
        }, () -> {
            assertNotNull(result.chatIds);
            assertEquals(1, result.chatIds.size());
        }, () -> {
            assertNotNull(result.searchTerms);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget15() throws Throwable
    {
        // When
        String widgetId = "widgetId"; // UTA: default value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
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
            assertEquals("", result.searchTerms.global);
        }, () -> {
            assertEquals("", result.searchTerms.prompt);
        }, () -> {
            assertEquals("", result.searchTerms.response);
        }, () -> {
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fromWidget(String, List, SearchTerms)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection#fromWidget(String, List,
     *      SearchTerms)
     * @author bmcmullin
     */
    @Test
    public void testFromWidget16() throws Throwable
    {
        // When
        String widgetId = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        SearchTerms searchTerms = null; // UTA: configured value
        Selection result = Selection.fromWidget(widgetId, chatIds, searchTerms);

        // Then - assertions for result of method fromWidget(String, List, WidgetReviewStartServlet.SearchTerms)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertEquals("", result.widgetId);
        }, () -> {
            assertEquals("", result.displayName);
        }, () -> {
            assertNull(result.backUrl);
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
            assertNull(result.snapshots);
        }, () -> {
            assertNull(result.date);
        });

    }
}
