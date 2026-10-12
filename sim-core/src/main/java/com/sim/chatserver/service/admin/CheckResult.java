package com.sim.chatserver.service.admin;

public final class CheckResult {
    public final boolean exists;
    public final Long count;
    public final String message;
    public final boolean created;

    CheckResult(boolean exists, Long count, String message) {
        this(exists, count, message, false);
    }

    CheckResult(boolean exists, Long count, String message, boolean created) {
        this.exists = exists;
        this.count = count;
        this.message = message;
        this.created = created;
    }
}
