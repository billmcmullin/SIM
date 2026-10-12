package com.sim.chatserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.ServletContext;

/**
 * Handles dashboard HTML template loading/caching and basic escaping.
 */
public final class DashboardTemplateRenderer {

    private static final Object TEMPLATE_LOCK = new Object();
    private static final String EMPTY = "";
    private static final String TEMPLATE_TOKEN_PREFIX = "${";
    private static final String HTML_ESCAPE_AMP = CommonLiteralConstants.HTML_ESCAPE_AMP;
    private static final String HTML_ESCAPE_LT = CommonLiteralConstants.HTML_ESCAPE_LT;
    private static final String HTML_ESCAPE_GT = CommonLiteralConstants.HTML_ESCAPE_GT;
    private static final String HTML_ESCAPE_QUOT = CommonLiteralConstants.HTML_ESCAPE_QUOT;
    private static final String HTML_ESCAPE_APOS = CommonLiteralConstants.HTML_ESCAPE_APOS;
    private static final String BACKSLASH = "\\";
    private static final String DOUBLE_BACKSLASH = com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO;
    private static final String SINGLE_QUOTE = "'";
    private static final String ESCAPED_SINGLE_QUOTE = com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_SINGLE_QUOTE;
    private static final String ESCAPED_NEWLINE = com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO;
    private static final String ESCAPED_CARRIAGE_RETURN = CommonLiteralConstants.ESCAPED_CARRIAGE_RETURN;
    private static final String MSG_TEMPLATE_NOT_FOUND_PREFIX = "Template not found: ";
    private static volatile String cachedDashboardTemplate;

    private DashboardTemplateRenderer() {
    }

    public static String loadTemplateCached(ServletContext context, String path) throws IOException {
        String local = cachedDashboardTemplate;
        if (local != null) {
            return local;
        }
        synchronized (TEMPLATE_LOCK) {
            if (cachedDashboardTemplate == null) {
                cachedDashboardTemplate = loadTemplate(context, path);
            }
            return cachedDashboardTemplate;
        }
    }

    static void clearTemplateCache() {
        synchronized (TEMPLATE_LOCK) {
            cachedDashboardTemplate = null;
        }
    }

    public static String renderTemplate(String template, Map<String, String> values) {
        String out = template == null ? EMPTY : template;
        if (values == null || values.isEmpty()) {
            return out;
        }
        for (Map.Entry<String, String> e : values.entrySet()) {
            String key = e.getKey() == null ? EMPTY : e.getKey();
            String token = new StringBuilder(key.length() + 3).append(TEMPLATE_TOKEN_PREFIX).append(key).append('}').toString();
            out = out.replace(token, e.getValue() == null ? EMPTY : e.getValue());
        }
        return out;
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return EMPTY;
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '&') {
                out.append(HTML_ESCAPE_AMP);
            } else if (ch == '<') {
                out.append(HTML_ESCAPE_LT);
            } else if (ch == '>') {
                out.append(HTML_ESCAPE_GT);
            } else if (ch == '"') {
                out.append(HTML_ESCAPE_QUOT);
            } else if (ch == '\'') {
                out.append(HTML_ESCAPE_APOS);
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    static String escapeForJs(String value) {
        if (value == null) {
            return EMPTY;
        }
        return value.replace(BACKSLASH, DOUBLE_BACKSLASH)
                .replace(SINGLE_QUOTE, ESCAPED_SINGLE_QUOTE)
                .replace(System.lineSeparator(), ESCAPED_NEWLINE)
                .replace(System.lineSeparator(), ESCAPED_CARRIAGE_RETURN);
    }

    private static String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException(MSG_TEMPLATE_NOT_FOUND_PREFIX + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
                return builder.toString();
            }
        }
    }
}
