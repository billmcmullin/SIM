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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet.SearchTerms;
import static org.junit.jupiter.api.Assertions.assertAll;
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
        List<String> chatIds = null; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds3() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
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
    public void testCreateSelectionFromGlobalChatIds4() throws Throwable
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
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSelectionFromGlobalChatIds(HttpSession, List, String, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSelectionFromGlobalChatIds(HttpSession,
     *      List, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSelectionFromGlobalChatIds5() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = "label"; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds6() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = "label"; // UTA: configured value
        String backUrl = null; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds7() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = null; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds8() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = null; // UTA: configured value
        String backUrl = null; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds9() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = "label"; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds10() throws Throwable
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
     * Parasoft Jtest UTA: Test for createSelectionFromGlobalChatIds(HttpSession, List, String, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSelectionFromGlobalChatIds(HttpSession,
     *      List, String, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSelectionFromGlobalChatIds11() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = null; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds12() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        List<String> chatIds = new ArrayList<String>(); // UTA: default value
        String label = null; // UTA: configured value
        String backUrl = null; // UTA: configured value
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
    public void testCreateSelectionFromGlobalChatIds13() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
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
        String label = null; // UTA: configured value
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
    public void testCreateSnapshotSelection3() throws Throwable
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
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSnapshotSelection(HttpSession, String, List, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSnapshotSelection(HttpSession,
     *      String, List, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSnapshotSelection4() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        String label = "label"; // UTA: configured value
        List<TermChatSnapshot> snapshots = null; // UTA: configured value
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
    public void testCreateSnapshotSelection5() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        String label = "label"; // UTA: configured value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
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
    public void testCreateSnapshotSelection6() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        String label = "label"; // UTA: configured value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        String backUrl = "backUrl"; // UTA: default value
        String result = WidgetReviewStartServlet.createSnapshotSelection(session, label, snapshots, backUrl);

        // Then - assertions for result of method createSnapshotSelection(HttpSession, String, List, String)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSnapshotSelection(HttpSession, String, List, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSnapshotSelection(HttpSession,
     *      String, List, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSnapshotSelection7() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        String label = "label"; // UTA: configured value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        String backUrl = null; // UTA: configured value
        String result = WidgetReviewStartServlet.createSnapshotSelection(session, label, snapshots, backUrl);

        // Then - assertions for result of method createSnapshotSelection(HttpSession, String, List, String)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSnapshotSelection(HttpSession, String, List, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSnapshotSelection(HttpSession,
     *      String, List, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSnapshotSelection8() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        String label = "label"; // UTA: configured value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        String backUrl = "backUrl"; // UTA: default value
        String result = WidgetReviewStartServlet.createSnapshotSelection(session, label, snapshots, backUrl);

        // Then - assertions for result of method createSnapshotSelection(HttpSession, String, List, String)
        assertNotNull(result);

    }

    /**
     * Parasoft Jtest UTA: Test for createSnapshotSelection(HttpSession, String, List, String)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#createSnapshotSelection(HttpSession,
     *      String, List, String)
     * @author bmcmullin
     */
    @Test
    public void testCreateSnapshotSelection9() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        String label = "label"; // UTA: configured value
        List<TermChatSnapshot> snapshots = new ArrayList<TermChatSnapshot>(); // UTA: default value
        TermChatSnapshot item = mock(TermChatSnapshot.class);
        snapshots.add(item);
        String backUrl = null; // UTA: configured value
        String result = WidgetReviewStartServlet.createSnapshotSelection(session, label, snapshots, backUrl);

        // Then - assertions for result of method createSnapshotSelection(HttpSession, String, List, String)
        assertNotNull(result);

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
     * Parasoft Jtest UTA: Test for doPost(HttpServletRequest, HttpServletResponse)
     *
     * @see com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @author bmcmullin
     */
    @Test
    public void testDoPost2() throws Throwable
    {
        // Given
        WidgetReviewStartServlet underTest = new WidgetReviewStartServlet();

        // When
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
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
        String selectionId = null; // UTA: configured value
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
    public void testFetchSelection3() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        String selectionId = "selectionId"; // UTA: configured value
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
    public void testFetchSelection4() throws Throwable
    {
        // When
        HttpSession session = mock(HttpSession.class);
        Object getAttributeResult = null; // UTA: configured value
        when(session.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        String selectionId = "selectionId"; // UTA: configured value
        Selection result = WidgetReviewStartServlet.fetchSelection(session, selectionId);

        // Then - assertions for result of method fetchSelection(HttpSession, String)
        assertNull(result);

    }



    // Merged from WidgetReviewStartServlet_SelectionTest
    
    
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
