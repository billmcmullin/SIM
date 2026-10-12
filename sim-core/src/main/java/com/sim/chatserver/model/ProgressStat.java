package com.sim.chatserver.model;

public final class ProgressStat {

    private final int today;
    private final int yesterday;
    private final int delta;
    private final double pctDelta;
    private final String direction;

    public ProgressStat(int today, int yesterday) {
        this.today = today;
        this.yesterday = yesterday;
        this.delta = today - yesterday;
        this.pctDelta = yesterday == 0
                ? (today > 0 ? 100.0 : 0.0)
                : ((today - yesterday) * 100.0) / yesterday;
        this.direction = DashboardDirectionUtil.directionForDelta(delta);
    }

    private int getToday() {
        return today;
    }

    private int getYesterday() {
        return yesterday;
    }

    public int todayValue() {
        return getToday();
    }

    public int yesterdayValue() {
        return getYesterday();
    }

    public int getDelta() {
        return delta;
    }

    public double getPctDelta() {
        return pctDelta;
    }

    public String getDirection() {
        return direction;
    }
}
