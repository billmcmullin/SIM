package com.sim.chatserver.util;

public final class LineSeparatorUtil {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    public static final char LINE_FEED = 0x0A;
    public static final char CARRIAGE_RETURN = 0x0D;

    private LineSeparatorUtil() {
    }

    public static String lineSeparator() {
        return LINE_SEPARATOR;
    }

    public static char lineFeedChar() {
        return LINE_SEPARATOR.charAt(Math.max(0, LINE_SEPARATOR.length() - 1));
    }

    public static char carriageReturnChar() {
        return LINE_SEPARATOR.charAt(0);
    }
}
