package com.sim.chatserver.service.widget;

final class SseValidationResult {

    private static final SseValidationResult OK_RESULT = new SseValidationResult(true, null);

    final boolean ok;
    final String reason;

    private SseValidationResult(boolean ok, String reason) {
        this.ok = ok;
        this.reason = reason;
    }

    static SseValidationResult ok() {
        return OK_RESULT;
    }

    static SseValidationResult fail(String reason) {
        return new SseValidationResult(false, reason);
    }
}
