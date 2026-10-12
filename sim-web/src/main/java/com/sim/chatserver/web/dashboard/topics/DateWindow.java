package com.sim.chatserver.web.dashboard.topics;

import java.time.LocalDate;

final class DateWindow {

    final LocalDate startInclusive;
    final LocalDate endExclusive;
    final String dayToken;

    DateWindow(LocalDate startInclusive, LocalDate endExclusive, String dayToken) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
        this.dayToken = dayToken;
    }
}
