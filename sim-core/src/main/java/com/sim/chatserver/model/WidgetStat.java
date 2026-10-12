package com.sim.chatserver.model;

public final class WidgetStat {

    private final String widgetId;
    private final String label;
    private final int count;
    private final int todayCount;
    private final int yesterdayCount;
    private final int delta;
    private final String direction;

    public WidgetStat(String widgetId, String label, int count) {
        this(widgetId, label, count, 0, 0);
    }

    public WidgetStat(String widgetId, String label, int count, int todayCount, int yesterdayCount) {
        this.widgetId = widgetId;
        this.label = label;
        this.count = count;
        this.todayCount = todayCount;
        this.yesterdayCount = yesterdayCount;
        this.delta = todayCount - yesterdayCount;
        this.direction = DashboardDirectionUtil.directionForDelta(delta);
    }

    public String getWidgetId() {
        return widgetId;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }

    public int getTodayCount() {
        return todayCount;
    }

    public int getYesterdayCount() {
        return yesterdayCount;
    }

    public int getDelta() {
        return delta;
    }

    public String getDirection() {
        return direction;
    }

    ProgressStat getProgression() {
        return new ProgressStat(todayCount, yesterdayCount);
    }
}
