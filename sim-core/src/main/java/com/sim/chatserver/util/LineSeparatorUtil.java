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

    public static String stripCrLf(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != CARRIAGE_RETURN && c != LINE_FEED) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String normalizeToLf(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == CARRIAGE_RETURN) {
                if (i + 1 < value.length() && value.charAt(i + 1) == LINE_FEED) {
                    i++;
                }
                sb.append(LINE_FEED);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String replaceCrLf(String value, char replacement) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == CARRIAGE_RETURN || c == LINE_FEED) {
                sb.append(replacement);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
