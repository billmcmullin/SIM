(() => {
    'use strict';

    function boot() {
        const dashboardConfig = window.dashboardConfig || {};
        const contextPath = dashboardConfig.contextPath || '';

        if (!window.DashboardCore || !window.DashboardMetrics || !window.DashboardCharts || !window.DashboardSummary || !window.DashboardSessions) {
            console.error('Dashboard modules missing. Check script load order and file contents.');
            return;
        }

        window.DashboardCharts.bootPieAndTermCharts(contextPath);
        window.DashboardCharts.renderLastFiveDaysTrendChart(contextPath);

        window.DashboardSummary.wireSummaryCopyButton();
        window.DashboardSummary.loadDailySummary(contextPath);

        window.DashboardSessions.loadTopSessions(contextPath);

        window.DashboardMetrics.wireDynamicLinks();
        window.DashboardMetrics.applyProgressionDirectionStyling();
        window.DashboardMetrics.hydrateDailyProgressSection(contextPath);
        window.DashboardMetrics.applyDeltaClasses(document);
    }

    window.DashboardInit = { boot };
})();
