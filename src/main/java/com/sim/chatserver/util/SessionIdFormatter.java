package com.sim.chatserver.util;

/**
 * Small helper that produces a deterministic, human-friendly short form for
 * session IDs. Default strategy: if value looks like a UUID or is long, show
 * first 8 chars + "…" + last 4. Otherwise return a truncated form.
 *
 * Kept tiny so formatting strategy can be changed centrally.
 */
public final class SessionIdFormatter {

    private SessionIdFormatter() {
        // utility
    }

    /**
     * Produce a short, human-friendly representation of the session id. Returns
     * empty string for null/empty input.
     */
    public static String formatForDisplay(String sessionId) {
        if (sessionId == null) {
            return "";
        }
        String s = sessionId.trim();
        if (s.isEmpty()) {
            return "";
        }

        // If already short enough, return as-is
        if (s.length() <= 16) {
            return s;
        }

        // If looks like a UUID (contains dashes or is long), collapse to first 8 + last 4
        if (s.contains("-") || s.length() > 24) {
            String first = s.substring(0, Math.min(8, s.length()));
            String last = s.substring(Math.max(0, s.length() - 4));
            return first + "…" + last;
        }

        // Otherwise default shorten: first 10 + last 3
        String first = s.substring(0, Math.min(10, s.length()));
        String last = s.substring(Math.max(0, s.length() - 3));
        return first + "…" + last;
    }
}
