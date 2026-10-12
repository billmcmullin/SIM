package com.sim.chatserver.model;

import java.sql.Timestamp;

public final class SessionAccumulator {

    private int count;
    private Timestamp lastEntry;

    public int getCount() {
        return count;
    }

    public void addCount(int delta) {
        this.count += delta;
    }

    public Timestamp getLastEntry() {
        return lastEntry;
    }

    public void setLastEntry(Timestamp lastEntry) {
        this.lastEntry = lastEntry;
    }
}
