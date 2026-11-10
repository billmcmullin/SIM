package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import com.sim.chatserver.widget.WidgetStore;

class FullAnalysisSourceStoreTest {
    @Test
    void snapshotReadsFullTextBeyondQuickSummaryCapsInAReadOnlySnapshot() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement exists = mock(PreparedStatement.class);
        PreparedStatement query = mock(PreparedStatement.class);
        PreparedStatement fullText = mock(PreparedStatement.class);
        ResultSet table = mock(ResultSet.class);
        ResultSet rows = mock(ResultSet.class);
        ResultSet full = mock(ResultSet.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(startsWith("SELECT to_regclass"))).thenReturn(exists);
        when(connection.prepareStatement(startsWith("SELECT widget_chat_id"))).thenReturn(query);
        when(connection.prepareStatement(startsWith("SELECT prompt,response_text"))).thenReturn(fullText);
        when(exists.executeQuery()).thenReturn(table);
        when(table.next()).thenReturn(true);
        when(table.getString(1)).thenReturn("widget_a");
        when(query.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true, false);
        when(fullText.executeQuery()).thenReturn(full);
        when(full.next()).thenReturn(true, false);
        String question = "Full question ".repeat(1000);
        String answer = "Full detailed answer ".repeat(3000);
        when(rows.getString("widget_chat_id")).thenReturn("chat-1");
        when(full.getString("prompt")).thenReturn(question);
        when(full.getString("response_text")).thenReturn(answer);
        when(rows.getLong("source_bytes")).thenReturn((long) question.length() + answer.length());
        var widget = com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(1, "widget-a", "Widget", Instant.now());
        try (var registry = mockStatic(WidgetStore.class)) {
            registry.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget));
            var chats = new FullAnalysisSourceStore(source).snapshot(LocalDate.of(2026, 11, 10), Instant.parse("2026-11-11T00:00:00Z"));
            assertEquals(1, chats.size());
            assertEquals(question, chats.get(0).question());
            assertEquals(answer, chats.get(0).answer());
            verify(connection).setReadOnly(true);
            verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            verify(connection).rollback();
            verify(query).setFetchSize(100);
        }
    }

    @Test
    void oversizedSourceRowIsRejectedBeforeLoadingItsFullText() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement exists = mock(PreparedStatement.class);
        PreparedStatement query = mock(PreparedStatement.class);
        ResultSet table = mock(ResultSet.class);
        ResultSet rows = mock(ResultSet.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(startsWith("SELECT to_regclass"))).thenReturn(exists);
        when(connection.prepareStatement(startsWith("SELECT widget_chat_id"))).thenReturn(query);
        when(exists.executeQuery()).thenReturn(table);
        when(table.next()).thenReturn(true);
        when(table.getString(1)).thenReturn("widget_a");
        when(query.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true);
        when(rows.getLong("source_bytes")).thenReturn(9L * 1024 * 1024);
        var widget = com.sim.chatserver.web.TestWidgetEntryFactory.newWidgetEntry(1, "widget-a", "Widget", Instant.now());
        try (var registry = mockStatic(WidgetStore.class)) {
            registry.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget));
            assertThrows(IllegalStateException.class, () -> new FullAnalysisSourceStore(source)
                    .snapshot(LocalDate.now(), Instant.now()));
            verify(connection, never()).prepareStatement(startsWith("SELECT prompt,response_text"));
        }
    }

    @Test
    void databaseFailureIsNotAnEmptySnapshot() throws Exception {
        DataSource source = mock(DataSource.class);
        when(source.getConnection()).thenThrow(new SQLException("unavailable"));
        try (var registry = mockStatic(WidgetStore.class)) {
            registry.when(() -> WidgetStore.list(null)).thenReturn(List.of());
            assertThrows(IllegalStateException.class, () -> new FullAnalysisSourceStore(source)
                    .snapshot(LocalDate.now(), Instant.now()));
        }
    }
}