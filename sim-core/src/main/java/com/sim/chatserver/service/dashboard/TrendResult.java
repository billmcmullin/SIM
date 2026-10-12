package com.sim.chatserver.service.dashboard;

import java.time.LocalDate;
import java.util.Map;

public final class TrendResult {
    public final Map<LocalDate, Integer> totalDaily;
    public final Map<String, Map<LocalDate, Integer>> widgetDaily;
    public final Map<String, String> widgetNameToId;

    public TrendResult(
            Map<LocalDate, Integer> totalDaily,
            Map<String, Map<LocalDate, Integer>> widgetDaily,
            Map<String, String> widgetNameToId
    ) {
        this.totalDaily = totalDaily;
        this.widgetDaily = widgetDaily;
        this.widgetNameToId = widgetNameToId;
    }
}
