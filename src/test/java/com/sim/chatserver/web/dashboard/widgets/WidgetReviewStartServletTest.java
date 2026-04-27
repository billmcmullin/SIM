package com.sim.chatserver.web.dashboard.widgets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.Selection;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for WidgetReviewStartServlet
 *
 * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet
 * @author bmcmullin
 */
public class WidgetReviewStartServletTest
{

    /**
     * Parasoft Jtest UTA: Test for createSelectionFromGlobalChatIds(HttpSession, List, String, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSelectionFromGlobalChatIds(HttpSession,
     *      List, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSelectionFromGlobalChatIds() throws Throwable
    {
        // When
        HttpSession session = null; // UTA: configured value
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        String label = "label"; // UTA: default value
        String backUrl = "backUrl"; // UTA: default value
        String result = WidgetReviewStartServlet.createSelectionFromGlobalChatIds(session, chatIds, label, backUrl);

        // Then - assertions for result of method createSelectionFromGlobalChatIds(HttpSession, List, String, String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSelectionFromGlobalChatIds(HttpSession, List, String, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSelectionFromGlobalChatIds(HttpSession,
     *      List, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSelectionFromGlobalChatIds2() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String item = "item"; // UTA: default value
        chatIds.add(item);
        String label = "label"; // UTA: default value
        String backUrl = "backUrl"; // UTA: default value
        String result = WidgetReviewStartServlet.createSelectionFromGlobalChatIds(session, chatIds, label, backUrl);

        // Then - assertions for result of method createSelectionFromGlobalChatIds(HttpSession, List, String, String)
        assertEquals("85ee0afc-379e-4a82-b9d8-a7394ce18b4a", result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSelectionFromGlobalChatIds(HttpSession, List, String, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSelectionFromGlobalChatIds(HttpSession,
     *      List, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSelectionFromGlobalChatIds3() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = "label"; // UTA: configured value
        String backUrl = null; // UTA: configured value
        String result = WidgetReviewStartServlet.createSelectionFromGlobalChatIds(session, chatIds, label, backUrl);

        // Then - assertions for result of method createSelectionFromGlobalChatIds(HttpSession, List, String, String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSnapshotSelection(HttpSession, String, List, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSnapshotSelection(HttpSession,
     *      String, List, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSnapshotSelection() throws Throwable
    {
        // When
        HttpSession session = null; // UTA: configured value
        String label = "label"; // UTA: default value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        String backUrl = "backUrl"; // UTA: default value
        String result = WidgetReviewStartServlet.createSnapshotSelection(session, label, snapshots, backUrl);

        // Then - assertions for result of method createSnapshotSelection(HttpSession, String, List, String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSnapshotSelection(HttpSession, String, List, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSnapshotSelection(HttpSession,
     *      String, List, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSnapshotSelection2() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        String label = "label"; // UTA: configured value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        String backUrl = "backUrl"; // UTA: default value
        String result = WidgetReviewStartServlet.createSnapshotSelection(session, label, snapshots, backUrl);

        // Then - assertions for result of method createSnapshotSelection(HttpSession, String, List, String)
        assertEquals("a016b31d-46b5-4107-92b5-d8ac32a18a5c", result);

    }

    /**
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost() throws Throwable
    {
        // Given
        WidgetReviewStartServlet underTest = new WidgetReviewStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(req.getSession(anyBoolean())).thenReturn(getSessionResult);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter getWriterResult = mock(PrintWriter.class);
        when(resp.getWriter()).thenReturn(getWriterResult);
        underTest.doPost(req, resp);

    }

    /**
     * Parasoft Jtest UTA: Test for fetchSelection(HttpSession, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#fetchSelection(HttpSession, String)
     * @author bmcmullin
     */
    @Test
    public void testFetchSelection() throws Throwable
    {
        // When
        HttpSession session = null; // UTA: configured value
        String selectionId = "selectionId"; // UTA: default value
        Selection result = WidgetReviewStartServlet.fetchSelection(session, selectionId);

        // Then - assertions for result of method fetchSelection(HttpSession, String)
        assertNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for fetchSelection(HttpSession, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#fetchSelection(HttpSession, String)
     * @author bmcmullin
     */
    @Test
    public void testFetchSelection2() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        String selectionId = "selectionId"; // UTA: configured value
        Selection result = WidgetReviewStartServlet.fetchSelection(session, selectionId);

        // Then - assertions for result of method fetchSelection(HttpSession, String)
        assertNull(result);

    }

}
