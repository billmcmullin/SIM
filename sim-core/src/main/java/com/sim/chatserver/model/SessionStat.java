package com.sim.chatserver.model;

public final class SessionStat {

    private final String sessionId;
    private final int count;
    private final String lastEntry;

    public SessionStat(String sessionId, int count, String lastEntry) {
        this.sessionId = sessionId;
        this.count = count;
        this.lastEntry = lastEntry;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getCount() {
        return count;
    }

    public String getLastEntry() {
        return lastEntry;
    }
}
