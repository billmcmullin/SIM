package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

public final class DashboardTermSelectionQueryService {

    private final AppDataSourceHolder dataSourceHolder;
    private final TermsStore termsStore;
    private final Logger log;

    public DashboardTermSelectionQueryService(AppDataSourceHolder dataSourceHolder, TermsStore termsStore, Logger log) {
        this.dataSourceHolder = dataSourceHolder;
        this.termsStore = termsStore;
        this.log = log;
    }

    public Map<String, List<TermChatSnapshot>> loadSnapshotsForRange(LocalDate rangeStart, LocalDate rangeEnd) {
        List<WidgetEntry> widgets = listWidgets();
        if (widgets.isEmpty()) {
            return Map.of();
        }

        DashboardTermService termService = new DashboardTermService(termsStore);
        try (Connection conn = dataSourceHolder.getDataSource().getConnection()) {
            var summary = termService.buildTermSummary(
                    conn,
                    widgets,
                    termService.loadAllTerms(),
                    rangeStart,
                    rangeEnd
            );
            if (summary == null) {
                return Map.of();
            }
            return summary.copyTermSnapshots();
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to rebuild term snapshots on demand", ex);
            return Map.of();
        }
    }

    private List<WidgetEntry> listWidgets() {
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            return widgets == null ? List.of() : widgets;
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to list widgets for term selection", ex);
            return List.of();
        }
    }
}