package com.sim.chatserver.util;

/**
 * Utility helpers for bounded text construction and truncation.
 */
public final class TextBudgetUtil {

    private TextBudgetUtil() {
        // util
    }

    /**
     * Appends text if there is space left in maxChars. If text exceeds budget,
     * appends a truncated piece and returns false.
     *
     * @return true if full text was appended, false if truncated/no room.
     */
    public static boolean appendWithinLimit(StringBuilder sb, String text, int maxChars) {
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
    public static String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return "";
        }
        return value.length() <= maxChars ? value : value.substring(0, maxChars);
    }

    /**
     * Compresses whitespace and truncates with ellipsis if needed.
     */
    public static String compressText(String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        if (maxChars <= 0) {
            return "";
        }

        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }

        if (maxChars == 1) {
            return "â€¦";
        }
        return normalized.substring(0, maxChars - 1) + "â€¦";
    }

    /**
     * Builds a safe concatenation of base + suffix under a hard cap. Priority
     * is given to base text; suffix is appended only if room exists.
     */
    static String concatWithBudget(String base, String suffix, int maxChars) {
        String b = defaultString(base).trim();
        String s = defaultString(suffix);

        if (maxChars <= 0) {
            return "";
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
    public static int estimateTokens(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Math.max(1, (value.length() + 3) / 4);
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
        return s == null ? "" : s;
    }
}
