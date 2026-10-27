package com.sim.chatserver.web.dashboard.widgets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import com.sim.chatserver.service.WorkflowRuntime;
import com.sim.chatserver.term.TermChatSnapshot;
import jakarta.servlet.http.HttpSession;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class SelectionPersistenceTest {
    @Test
    void draftSurvivesANewSession_andIsOwnedByTheUser() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        try {
            WorkflowRuntime.initialize(dataSource);
            String response = "response ".repeat(1000);
            TermChatSnapshot snapshot = new TermChatSnapshot("term", "widget", "chat", "prompt", response,
                    Timestamp.valueOf("2026-01-01 12:00:00"), "conversation");
            String selectionId = WidgetReviewStartServlet.createSnapshotSelection(session("alice"), "Draft", List.of(snapshot), "/dashboard");
            Selection reopened = WidgetReviewStartServlet.fetchSelection(session("alice"), selectionId);
            assertNotNull(reopened);
            assertEquals("Draft", reopened.displayName);
            assertEquals(response, reopened.snapshots.get(0).getResponse());
            assertNull(WidgetReviewStartServlet.fetchSelection(session("bob"), selectionId));
        } finally {
            WorkflowRuntime.shutdown();
        }
    }

    private HttpSession session(String username) {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(username);
        return session;
    }
}