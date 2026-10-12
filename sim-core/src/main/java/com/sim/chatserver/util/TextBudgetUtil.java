package com.sim.chatserver.util;

/**
 * Utility helpers for bounded text construction and truncation.
 */
public final class TextBudgetUtil {

    private static final String EMPTY = "";
    private static final String VALUE_EMPTY_TEXT = com.sim.chatserver.util.CommonLiteralConstants.MARKER_EMPTY_PARENS;
    private static final String SINGLE_SPACE = " ";
    private static final String REGEX_WHITESPACE = com.sim.chatserver.util.CommonLiteralConstants.REGEX_WHITESPACE;
    private static final String ELLIPSIS = com.sim.chatserver.util.CommonLiteralConstants.ELLIPSIS;

    private TextBudgetUtil() {
        // util
    }

    /**
     * Appends text if there is space left in maxChars. If text exceeds budget,
     * appends a truncated piece and returns false.
     *
     * @return true if full text was appended, false if truncated/no room.
     */
    static boolean appendWithinLimit(StringBuilder sb, String text, int maxChars) {
        if (sb == null || text == null || text.isEmpty() || maxChars <= 0) {
            return false;
        }
        if (sb.length() >= maxChars) {
            return false;
        }

        int room = maxChars - sb.length();
        if (text.length() <= room) {
            sb.append(text);
            return true;
        }

        sb.append(text, 0, room);
        return false;
    }

    /**
     * Returns text trimmed to max chars.
     */
    private static String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return EMPTY;
        }
        return value.length() <= maxChars ? value : new String(value.toCharArray(), 0, maxChars);
    }

    /**
     * Compresses whitespace and truncates with ellipsis if needed.
     */
    static String compressText(String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return VALUE_EMPTY_TEXT;
        }
        if (maxChars <= 0) {
            return EMPTY;
        }

        String normalized = value.replaceAll(REGEX_WHITESPACE, SINGLE_SPACE).trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }

        if (maxChars <= ELLIPSIS.length()) {
            return new String(ELLIPSIS.toCharArray(), 0, maxChars);
        }
        return new String(normalized.toCharArray(), 0, maxChars - ELLIPSIS.length()) + ELLIPSIS;
    }

    /**
     * Builds a safe concatenation of base + suffix under a hard cap. Priority
     * is given to base text; suffix is appended only if room exists.
     */
    static String concatWithBudget(String base, String suffix, int maxChars) {
        String b = defaultString(base).trim();
        String s = defaultString(suffix);

        if (maxChars <= 0) {
            return EMPTY;
        }
        if (b.length() >= maxChars) {
            return trimTo(b, maxChars);
        }

        int room = maxChars - b.length();
        if (s.isEmpty() || room <= 0) {
            return b;
        }

        return b + trimTo(s, room);
    }

    /**
     * Estimates tokens very roughly from chars. Rule-of-thumb: ~4 chars/token
     * for English-ish text.
     */
    static int estimateTokens(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        long len = value.length();
        long estimated = (len + 3L) / 4L;
        if (estimated <= 1L) {
            return 1;
        }
        return Math.toIntExact(Math.min(estimated, (long) Integer.MAX_VALUE));
    }

    /**
     * Converts token budget to approximate char budget.
     */
    static int approxCharsForTokens(int tokens) {
        if (tokens <= 0) {
            return 0;
        }
        return tokens * 4;
    }

    private static String defaultString(String s) {
        return s == null ? EMPTY : s;
    }
}