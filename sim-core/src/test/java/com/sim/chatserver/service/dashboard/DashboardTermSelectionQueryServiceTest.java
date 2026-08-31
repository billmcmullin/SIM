package com.sim.chatserver.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

class DashboardTermSelectionQueryServiceTest {

    @Test
    void loadSnapshotsForRange_returnsEmptyWhenNoWidgets() {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DashboardTermSelectionQueryService service = new DashboardTermSelectionQueryService(
                holder,
                mock(TermsStore.class),
                Logger.getLogger("test"));

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of());

            Map<String, List<TermChatSnapshot>> result = service.loadSnapshotsForRange(
                    LocalDate.now().minusDays(1),
                    LocalDate.now());

            assertTrue(result.isEmpty());
            verifyNoInteractions(holder);
        }
    }

    @Test
    void loadSnapshotsForRange_returnsEmptyWhenWidgetStoreFails() {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DashboardTermSelectionQueryService service = new DashboardTermSelectionQueryService(
                holder,
                mock(TermsStore.class),
                Logger.getLogger("test"));

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {
            widgetStore.when(() -> WidgetStore.list(null)).thenThrow(new SQLException("down"));

            Map<String, List<TermChatSnapshot>> result = service.loadSnapshotsForRange(
                    LocalDate.now().minusDays(1),
                    LocalDate.now());

            assertTrue(result.isEmpty());
            verifyNoInteractions(holder);
        }
    }

    @Test
    void loadSnapshotsForRange_returnsEmptyWhenSummaryIsNull() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        DashboardTermSelectionQueryService service = new DashboardTermSelectionQueryService(
                holder,
                mock(TermsStore.class),
                Logger.getLogger("test"));

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
             MockedConstruction<DashboardTermService> termServices = Mockito.mockConstruction(
                     DashboardTermService.class,
                     (mock, context) -> {
                         when(mock.loadAllTerms()).thenReturn(List.of());
                         when(mock.buildTermSummary(any(Connection.class), anyList(), anyList(), any(LocalDate.class), any(LocalDate.class)))
                                 .thenReturn(null);
                     })) {

            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget("wid-1")));

            Map<String, List<TermChatSnapshot>> result = service.loadSnapshotsForRange(
                    LocalDate.now().minusDays(1),
                    LocalDate.now());

            assertTrue(result.isEmpty());
            assertEquals(1, termServices.constructed().size());
        }
    }

    @Test
    void loadSnapshotsForRange_returnsCopiedSnapshotsWhenSummaryExists() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        TermSummary summary = new TermSummary();
        TermChatSnapshot snapshot = new TermChatSnapshot(
                "alpha",
                "wid-1",
                "chat-1",
                "prompt",
                "response",
                Timestamp.from(Instant.now()),
                "s1");
        summary.recordMatch("alpha", snapshot);

        DashboardTermSelectionQueryService service = new DashboardTermSelectionQueryService(
                holder,
                mock(TermsStore.class),
                Logger.getLogger("test"));

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class);
             MockedConstruction<DashboardTermService> termServices = Mockito.mockConstruction(
                     DashboardTermService.class,
                     (mock, context) -> {
                         when(mock.loadAllTerms()).thenReturn(List.of());
                         when(mock.buildTermSummary(any(Connection.class), anyList(), anyList(), any(LocalDate.class), any(LocalDate.class)))
                                 .thenReturn(summary);
                     })) {

            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget("wid-1")));

            Map<String, List<TermChatSnapshot>> result = service.loadSnapshotsForRange(
                    LocalDate.now().minusDays(2),
                    LocalDate.now());

            assertEquals(1, result.size());
            assertEquals(1, result.get("alpha").size());
            assertEquals("chat-1", result.get("alpha").get(0).getChatId());
            assertNotSame(summary.getTermSnapshots().get("alpha"), result.get("alpha"));
            assertEquals(1, termServices.constructed().size());
        }
    }

    @Test
    void loadSnapshotsForRange_returnsEmptyWhenConnectionFails() throws Exception {
        AppDataSourceHolder holder = mock(AppDataSourceHolder.class);
        DataSource dataSource = mock(DataSource.class);
        when(holder.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenThrow(new SQLException("db down"));

        DashboardTermSelectionQueryService service = new DashboardTermSelectionQueryService(
                holder,
                mock(TermsStore.class),
                Logger.getLogger("test"));

        try (MockedStatic<WidgetStore> widgetStore = Mockito.mockStatic(WidgetStore.class)) {
            widgetStore.when(() -> WidgetStore.list(null)).thenReturn(List.of(widget("wid-1")));

            Map<String, List<TermChatSnapshot>> result = service.loadSnapshotsForRange(
                    LocalDate.now().minusDays(1),
                    LocalDate.now());

            assertTrue(result.isEmpty());
        }
    }

    private static WidgetEntry widget(String widgetId) {
        return DashboardWidgetEntryTestFactory.newWidgetEntry(1, widgetId, "Widget", Instant.now());
    }
}