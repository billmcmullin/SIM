package com.sim.chatserver.web.util;

import java.io.IOException;
import java.io.Reader;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Shared request parameter helpers for servlet endpoints.
 */
public final class ServletRequestParamUtil {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private ServletRequestParamUtil() {
        // utility
    }

    public static String firstParam(HttpServletRequest request,
            String name,
            int maxLen,
            boolean stripNullByte,
            boolean nullIfEmpty) {
        if (request == null || name == null || name.isBlank()) {
            return null;
        }

        return normalizeParam(readParam(request, name), maxLen, stripNullByte, nullIfEmpty);
    }

    public static String firstParamFromValues(HttpServletRequest request,
            String name,
            int maxLen,
            boolean stripNullByte,
            boolean nullIfEmpty) {
        if (request == null || name == null || name.isBlank()) {
            return null;
        }

        String[] values = readParameterValues(request, name);

        for (String value : values) {
            String normalized = normalizeParam(value, maxLen, stripNullByte, nullIfEmpty);
            if (normalized != null) {
                return normalized;
            }
        }

        return normalizeParam(readParam(request, name), maxLen, stripNullByte, nullIfEmpty);
    }

    public static String normalizeValue(String value,
            int maxLen,
            boolean stripNullByte,
            boolean nullIfEmpty) {
        return normalizeParam(value, maxLen, stripNullByte, nullIfEmpty);
    }

    public static String normalizeBodyText(String value,
            int maxLen,
            boolean nullIfEmpty) {
        if (value == null) {
            return null;
        }

        String normalized = value.replace("\u0000", "")
                .replace("\r", "")
                .trim();

        if (nullIfEmpty && normalized.isEmpty()) {
            return null;
        }

        if (maxLen > 0 && normalized.length() > maxLen) {
            return normalized.substring(0, maxLen);
        }

        return normalized;
    }

    public static boolean hasValidContentLength(HttpServletRequest request,
            long maxLen) {
        if (request == null) {
            return false;
        }
        long len = request.getContentLengthLong();
        return len >= 0 && len <= maxLen;
    }

    public static String readNormalizedBodyText(Reader reader,
            int maxLen) throws IOException {
        return readNormalizedBodyText(reader, maxLen, 2048);
    }

    public static String readNormalizedBodyText(Reader reader,
            int maxLen,
            int bufferSize) throws IOException {
        if (reader == null) {
            return "";
        }
        char[] buffer = new char[Math.max(256, bufferSize)];
        StringBuilder body = new StringBuilder(Math.min(Math.max(maxLen, 1), 4096));

        int total = 0;
        int read;
        while ((read = reader.read(buffer)) != -1) {
            total += read;
            if (maxLen > 0 && total > maxLen) {
                throw new IOException("Payload exceeds allowed size.");
            }
            body.append(buffer, 0, read);
        }

        return normalizeBodyText(body.toString(), 0, false);
    }

    public static String readNormalizedBodyTextOrEmptyOnLimit(Reader reader,
            int maxLen) throws IOException {
        return readNormalizedBodyTextOrEmptyOnLimit(reader, maxLen, 2048);
    }

    private static String readNormalizedBodyTextOrEmptyOnLimit(Reader reader,
            int maxLen,
            int bufferSize) throws IOException {
        if (reader == null) {
            return "";
        }
        char[] buffer = new char[Math.max(256, bufferSize)];
        StringBuilder body = new StringBuilder(Math.min(Math.max(maxLen, 1), 4096));

        int total = 0;
        int read;
        while ((read = reader.read(buffer)) != -1) {
            total += read;
            if (maxLen > 0 && total > maxLen) {
                return "";
            }
            body.append(buffer, 0, read);
        }

        return normalizeBodyText(body.toString(), 0, false);
    }

    private static String normalizeParam(String value,
            int maxLen,
            boolean stripNullByte,
            boolean nullIfEmpty) {
        if (value == null) {
            return null;
        }

        String normalized = value;
        if (stripNullByte) {
            normalized = normalized.replace("\u0000", "");
        }
        normalized = normalized.replace("\r", "").replace("\n", "").trim();

        if (nullIfEmpty && normalized.isEmpty()) {
            return null;
        }

        if (maxLen > 0 && normalized.length() > maxLen) {
            return normalized.substring(0, maxLen);
        }

        return normalized;
    }

    private static String readParam(HttpServletRequest request, String name) {
        String[] values = readParameterValues(request, name);
        if (values.length == 0) {
            return null;
        }
        return values[0];
    }

    private static String[] readParameterValues(HttpServletRequest request, String name) {
        if (request == null || name == null || name.isBlank()) {
            return EMPTY_STRING_ARRAY;
        }
        String[] values = request.getParameterValues(name);
        if (values == null) {
            return EMPTY_STRING_ARRAY;
        }
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            sanitized[i] = value == null
                    ? null
                    : value.replace('\u0000', ' ').replace('\r', ' ').replace('\n', ' ');
        }
        return sanitized;
    }
}
