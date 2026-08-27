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
        String out = template == null ? "" : template;
        if (values == null || values.isEmpty()) {
            return out;
        }
        for (Map.Entry<String, String> e : values.entrySet()) {
            String key = e.getKey() == null ? "" : e.getKey();
            String token = new StringBuilder(key.length() + 3).append("${").append(key).append('}').toString();
            out = out.replace(token, e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '&') {
                out.append("&amp;");
            } else if (ch == '<') {
                out.append("&lt;");
            } else if (ch == '>') {
                out.append("&gt;");
            } else if (ch == '"') {
                out.append("&quot;");
            } else if (ch == '\'') {
                out.append("&#39;");
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    static String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        }
    }
}
