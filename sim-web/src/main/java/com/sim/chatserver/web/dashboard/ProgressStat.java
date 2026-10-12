package com.sim.chatserver.web.dashboard;

final class ProgressStat {

    private static final String DIRECTION_UP = "up";
    private static final String DIRECTION_DOWN = com.sim.chatserver.util.CommonLiteralConstants.VALUE_DOWN_LOWER;
    private static final String DIRECTION_FLAT = "flat";

    private final int today;
    private final int yesterday;
    private final int delta;
    private final double pctDelta;
    private final String direction;

    ProgressStat(int today, int yesterday) {
        this.today = today;
        this.yesterday = yesterday;
        this.delta = today - yesterday;
        this.pctDelta = yesterday == 0
                ? (today > 0 ? 100.0 : 0.0)
                : ((today - yesterday) * 100.0) / yesterday;

        if (delta > 0) {
            this.direction = DIRECTION_UP;
        } else if (delta < 0) {
            this.direction = DIRECTION_DOWN;
        } else {
            this.direction = DIRECTION_FLAT;
        }
    }

    int getToday() {
        return today;
    }

    int getYesterday() {
        return yesterday;
    }

    int getDelta() {
        return delta;
    }

    double getPctDelta() {
        return pctDelta;
    }

    String getDirection() {
        return direction;
    }
}
