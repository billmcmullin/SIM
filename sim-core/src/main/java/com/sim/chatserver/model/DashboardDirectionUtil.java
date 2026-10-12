package com.sim.chatserver.model;

final class DashboardDirectionUtil {

    private static final String DIRECTION_UP = "up";
    private static final String DIRECTION_DOWN = com.sim.chatserver.util.CommonLiteralConstants.VALUE_DOWN_LOWER;
    private static final String DIRECTION_FLAT = "flat";

    private DashboardDirectionUtil() {
    }

    static String directionForDelta(int delta) {
        if (delta > 0) {
            return DIRECTION_UP;
        }
        if (delta < 0) {
            return DIRECTION_DOWN;
        }
        return DIRECTION_FLAT;
    }
}
