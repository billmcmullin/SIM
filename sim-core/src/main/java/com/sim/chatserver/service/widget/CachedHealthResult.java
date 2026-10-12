package com.sim.chatserver.service.widget;

import java.time.Instant;

final class CachedHealthResult {

    final String configFingerprint;
    final Instant checkedAt;
    final WidgetAvailabilityResult result;

    CachedHealthResult(String configFingerprint, Instant checkedAt, WidgetAvailabilityResult result) {
        this.configFingerprint = configFingerprint;
        this.checkedAt = checkedAt;
        this.result = result;
    }
}
