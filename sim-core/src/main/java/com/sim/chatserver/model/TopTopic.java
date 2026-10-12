package com.sim.chatserver.model;

public final class TopTopic {

    private final String label;
    private final int today;
    private final int yesterday;
    private final int total;

    public TopTopic(String label, int today, int yesterday) {
        this.label = label;
        this.today = today;
        this.yesterday = yesterday;
        this.total = today + yesterday;
    }

    public String getLabel() {
        return label;
    }

    public int getToday() {
        return today;
    }

    public int getYesterday() {
        return yesterday;
    }

    public int getTotal() {
        return total;
    }
}
