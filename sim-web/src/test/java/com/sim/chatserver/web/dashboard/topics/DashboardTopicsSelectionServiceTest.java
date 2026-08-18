package com.sim.chatserver.web.dashboard.topics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.widget.WidgetEntry;

class DashboardTopicsSelectionServiceTest {

    @Test
    void resolveSelectedChats_returnsSnapshotsAndFoundIds() throws Exception {
        AppDataSourceHolder holder = new AppDataSourceHolder();
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet tableRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rows = mock(ResultSet.class);

        holder.setDataSource(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), eq("widget_1"), any(String[].class))).thenReturn(tableRs);
        when(tableRs.next()).thenReturn(true);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true, false);
        when(rows.getCharacterStream("widget_chat_id")).thenReturn(new StringReader("chat-1"));
        when(rows.getCharacterStream("prompt")).thenReturn(new StringReader("Prompt text"));
        when(rows.getCharacterStream("response_text")).thenReturn(new StringReader("Response text"));
        when(rows.getCharacterStream("session_id")).thenReturn(new StringReader("session-1"));
        when(rows.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")));

        DashboardTopicsSelectionService service = new DashboardTopicsSelectionService(holder, Logger.getLogger("test"));

        Set<String> requested = new LinkedHashSet<>();
        requested.add("chat-1");
        requested.add("chat-2");

        Map<String, WidgetEntry> widgetById = new LinkedHashMap<>();
        widgetById.put("widget-1", new WidgetEntry(1, "widget-1", "Widget One", Instant.now()));

        DashboardTopicsSelectionService.SelectionResolution result = service.resolveSelectedChats(requested, widgetById);

        assertEquals(1, result.snapshots().size());
        TermChatSnapshot snapshot = result.snapshots().getFirst();
        assertEquals("Popular Topics", snapshot.getTermName());
        assertEquals("widget-1", snapshot.getWidgetId());
        assertEquals("chat-1", snapshot.getChatId());
        assertEquals("Prompt text", snapshot.getPrompt());
        assertEquals("Response text", snapshot.getResponse());
        assertEquals("session-1", snapshot.getSessionId());

        assertEquals(1, result.foundIds().size());
        assertTrue(result.foundIds().contains("chat-1"));
    }

    @Test
    void resolveSelectedChats_skipsUnknownTables() throws Exception {
        AppDataSourceHolder holder = new AppDataSourceHolder();
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        DatabaseMetaData meta = mock(DatabaseMetaData.class);
        ResultSet rsFalse = mock(ResultSet.class);

        holder.setDataSource(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getTables(eq(null), eq(null), anyString(), any(String[].class))).thenReturn(rsFalse);
        when(rsFalse.next()).thenReturn(false, false, false);

        DashboardTopicsSelectionService service = new DashboardTopicsSelectionService(holder, Logger.getLogger("test"));

        Set<String> requested = Set.of("chat-1");
        Map<String, WidgetEntry> widgetById = Map.of(
                "widget-1", new WidgetEntry(1, "widget-1", "Widget One", Instant.now())
        );

        DashboardTopicsSelectionService.SelectionResolution result = service.resolveSelectedChats(requested, widgetById);

        assertTrue(result.snapshots().isEmpty());
        assertTrue(result.foundIds().isEmpty());
    }
}
