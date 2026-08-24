package com.sim.chatserver.service.dashboard;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.DashboardViewModels.SessionOverview;
import com.sim.chatserver.service.dashboard.DashboardSessionService;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardDbUtil;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.widget.WidgetEntry;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

public final class DashboardServletQueryService {

    private final Logger log;

    public DashboardServletQueryService(Logger log) {
        this.log = log;
    }

    public com.sim.chatserver.model.DashboardViewModels.TermSummary loadTermSummary(
            DashboardTermService termService,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd
    ) {
        try (Connection conn = openConnectionSafe()) {
            var terms = termService.loadAllTerms();
            return termService.buildTermSummary(conn, widgets, terms, rangeStart, rangeEnd);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to compute term summary", ex);
            return null;
        }
    }

    public SessionOverview loadSessionOverview(
            DashboardSessionService sessionService,
            List<WidgetEntry> widgets,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int activeDays
    ) {
        try (Connection conn = openConnectionSafe()) {
            return sessionService.buildSessionOverview(conn, widgets, rangeStart, rangeEnd, activeDays);
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to compute session overview", ex);
            return null;
        }
    }

    public String buildLastFiveDaysTrendJson(List<WidgetEntry> widgets) {
        LocalDate end = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = end.minusDays(4);

        Map<LocalDate, Integer> totalDaily = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            totalDaily.put(start.plusDays(i), Integer.valueOf(0));
        }

        try (Connection conn = openConnectionSafe()) {
            List<WidgetEntry> sourceWidgets = widgets == null ? List.of() : widgets;

            Map<String, Boolean> tableExistsCache = DashboardDbUtil.newRequestTableCache();

            for (WidgetEntry widget : sourceWidgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }

                String tableName = DashboardDbUtil.sanitizeWidgetTableName(widget.getWidgetId());
                if (!DashboardDbUtil.tableExistsCached(conn, tableName, tableExistsCache)) {
                    continue;
                }

                String sql = "SELECT created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?";

                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));

                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Timestamp ts = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            if (ts == null) {
                                continue;
                            }

                            LocalDate entryDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (!totalDaily.containsKey(entryDate)) {
                                continue;
                            }

                            Integer existing = totalDaily.get(entryDate);
                            int currentCount = 0;
                            if (existing != null) {
                                currentCount = existing.intValue();
                            }
                            totalDaily.put(entryDate, Integer.valueOf(currentCount + 1));
                        }
                    }
                }
            }
        } catch (SQLException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to load 5-day trend data", ex);
        }

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (Map.Entry<LocalDate, Integer> entry : totalDaily.entrySet()) {
            labels.add(entry.getKey().toString());
            Integer dayCount = entry.getValue();
            int safeCount = 0;
            if (dayCount != null) {
                safeCount = dayCount.intValue();
            }
            values.add(safeCount);
        }

        return Json.createObjectBuilder()
                .add("labels", labels)
                .add("values", values)
                .add("days", 5)
                .build()
                .toString();
    }

    private Connection openConnectionSafe() {
        try {
            return dataSourceHolder().getDataSource().getConnection();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to open dashboard data connection", ex);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        if (!identifier.matches("^[A-Za-z_][A-Za-z0-9_]{0,62}$")) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}