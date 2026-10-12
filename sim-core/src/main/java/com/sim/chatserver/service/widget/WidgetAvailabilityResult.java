package com.sim.chatserver.service.widget;

public final class WidgetAvailabilityResult {

    private final boolean available;
    private final String status;
    private final String checkedAtIso;
    private final long latencyMs;
    private final String details;

    public WidgetAvailabilityResult(boolean available, String status, String checkedAtIso, long latencyMs, String details) {
        this.available = available;
        this.status = status;
        this.checkedAtIso = checkedAtIso;
        this.latencyMs = latencyMs;
        this.details = details;
    }

    public boolean available() {
        return available;
    }

    public String status() {
        return status;
    }

    public String checkedAtIso() {
        return checkedAtIso;
    }

    public long latencyMs() {
        return latencyMs;
    }

    public String details() {
        return details;
    }
}
