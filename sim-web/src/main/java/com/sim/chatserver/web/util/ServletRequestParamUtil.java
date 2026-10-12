package com.sim.chatserver.web.util;

import java.io.IOException;
import java.io.Reader;
import java.text.Normalizer;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Shared request parameter helpers for servlet endpoints.
 */
public final class ServletRequestParamUtil {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String NULL_CHAR = com.sim.chatserver.util.CommonLiteralConstants.NULL_CHAR_STRING;
    private static final String EMPTY = "";
    private static final String MSG_PAYLOAD_EXCEEDS_SIZE = "Payload exceeds allowed size.";
    private static final String MSG_UNABLE_READ_REQUEST_BODY = "Unable to read request body.";
    private static final String OWASP_LOGGER = com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP;
    private static final String HANDLED_EXCEPTION = com.sim.chatserver.util.CommonLiteralConstants.LOG_HANDLED_EXCEPTION;

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

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
            .replace(NULL_CHAR, EMPTY)
                .replace(System.lineSeparator(), EMPTY)
                .trim();

        if (nullIfEmpty && normalized.isEmpty()) {
            return null;
        }

        if (maxLen > 0 && normalized.length() > maxLen) {
            return new String(normalized.toCharArray(), 0, maxLen);
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
            int maxLen) {
        return readNormalizedBodyText(reader, maxLen, 2048);
    }

    public static String readNormalizedBodyText(Reader reader,
            int maxLen,
            int bufferSize) {
        if (reader == null) {
            return EMPTY;
        }
        char[] buffer = new char[Math.max(256, bufferSize)];
        StringBuilder body = new StringBuilder(Math.min(Math.max(maxLen, 1), 4096));

        int total = 0;
        int read;
        try {
            while ((read = reader.read(buffer)) != -1) {
                total += read;
                if (maxLen > 0 && total > maxLen) {
                    throw new IllegalStateException(MSG_PAYLOAD_EXCEEDS_SIZE);
                }
                body.append(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new IllegalStateException(MSG_UNABLE_READ_REQUEST_BODY, e);
        }

        return normalizeBodyText(body.toString(), 0, false);
    }

    public static String readNormalizedBodyTextOrEmptyOnLimit(Reader reader,
            int maxLen) {
        return readNormalizedBodyTextOrEmptyOnLimit(reader, maxLen, 2048);
    }

    private static String readNormalizedBodyTextOrEmptyOnLimit(Reader reader,
            int maxLen,
            int bufferSize) {
        if (reader == null) {
            return EMPTY;
        }
        char[] buffer = new char[Math.max(256, bufferSize)];
        StringBuilder body = new StringBuilder(Math.min(Math.max(maxLen, 1), 4096));

        int total = 0;
        int read;
        try {
            while ((read = reader.read(buffer)) != -1) {
                total += read;
                if (maxLen > 0 && total > maxLen) {
                    return EMPTY;
                }
                body.append(buffer, 0, read);
            }
        } catch (IOException e) {
            java.util.logging.Logger.getLogger(OWASP_LOGGER).log(java.util.logging.Level.FINE, HANDLED_EXCEPTION, e);
            return EMPTY;
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

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        if (stripNullByte) {
            normalized = normalized.replace(NULL_CHAR, EMPTY);
        }
        normalized = normalized.replace(System.lineSeparator(), EMPTY).replace(System.lineSeparator(), EMPTY).trim();

        if (nullIfEmpty && normalized.isEmpty()) {
            return null;
        }

        if (maxLen > 0 && normalized.length() > maxLen) {
            return new String(normalized.toCharArray(), 0, maxLen);
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
                    : value.replace('\u0000', ' ').replace(System.lineSeparator().charAt(0), ' ').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), ' ');
        }
        return sanitized;
    }
}
