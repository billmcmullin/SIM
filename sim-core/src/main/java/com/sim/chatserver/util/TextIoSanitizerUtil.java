package com.sim.chatserver.util;

import java.io.IOException;
import java.io.Reader;

public final class TextIoSanitizerUtil {

    private TextIoSanitizerUtil() {
    }

    public static String readAtMostChars(Reader reader, int maxChars) throws IOException {
        if (reader == null || maxChars <= 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(Math.min(maxChars, 512));
        char[] buffer = new char[Math.min(1024, Math.max(64, maxChars))];

        while (builder.length() < maxChars) {
            int requested = Math.min(buffer.length, maxChars - builder.length());
            int read = reader.read(buffer, 0, requested);
            if (read < 0) {
                break;
            }
            if (read > 0) {
                builder.append(buffer, 0, read);
            }
        }

        return builder.toString();
    }

    public static String stripControlCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        return input.codePoints()
                .filter(codePoint -> codePoint == '\n'
                        || codePoint == '\r'
                        || codePoint == '\t'
                        || !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String safeFileToken(String value, String fallback) {
        String effectiveFallback = fallback == null || fallback.isBlank() ? "server" : fallback;
        String input = value == null || value.isBlank() ? effectiveFallback : value.trim();
        String sanitized = input.replaceAll("[^A-Za-z0-9_-]", "_");
        return sanitized.isBlank() ? effectiveFallback : sanitized;
    }
}