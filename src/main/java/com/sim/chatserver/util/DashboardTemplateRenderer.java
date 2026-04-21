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

    public static void clearTemplateCache() {
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
            out = out.replace("${" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String escapeForJs(String value) {
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
