package com.sim.chatserver.model;

public final class TermDayCount {

    private int today = 0;
    private int yesterday = 0;

    public int getToday() {
        return today;
    }

    public void incToday() {
        this.today++;
    }

    public int getYesterday() {
        return yesterday;
    }

    public void incYesterday() {
        this.yesterday++;
    }
}
