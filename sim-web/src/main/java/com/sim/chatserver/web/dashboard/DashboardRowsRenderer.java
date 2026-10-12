package com.sim.chatserver.web.dashboard;

import com.sim.chatserver.util.SessionLabel;
import java.util.List;
import java.util.Map;

import com.sim.chatserver.model.OtherParasoftEntry;
import com.sim.chatserver.model.SessionStat;
import com.sim.chatserver.model.TopTopic;
import com.sim.chatserver.model.WidgetStat;
import com.sim.chatserver.util.SessionLabelStore;

final class DashboardRowsRenderer {

    private DashboardRowsRenderer() {
    }

    static String renderWidgetStatsRows(List<WidgetStat> stats, String contextPath) {
        return com.sim.chatserver.render.DashboardRowsRenderer.renderWidgetStatsRows(stats, contextPath);
    }

    static String renderDailyTopTermsRows(List<TopTopic> terms, String contextPath) {
        return com.sim.chatserver.render.DashboardRowsRenderer.renderDailyTopTermsRows(terms, contextPath);
    }

    static String renderOtherParasoftLatestRows(List<OtherParasoftEntry> rows, String contextPath) {
        return com.sim.chatserver.render.DashboardRowsRenderer.renderOtherParasoftLatestRows(rows, contextPath);
    }

    static String renderSessionRows(
            List<SessionStat> sessions,
            Map<String, SessionLabel> labels,
            String contextPath
    ) {
        return com.sim.chatserver.render.DashboardRowsRenderer.renderSessionRows(sessions, labels, contextPath);
    }
}

