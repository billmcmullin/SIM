package com.sim.chatserver.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;

public final class ServletTemplateLoaderUtil {

    private static final String EMPTY = "";
    private static final String LOG_TEMPLATE_NOT_FOUND = com.sim.chatserver.util.CommonLiteralConstants.MSG_TEMPLATE_NOT_FOUND_FORMAT;
    private static final String LOG_UNABLE_LOAD_TEMPLATE_PREFIX = "Unable to load template ";

    private ServletTemplateLoaderUtil() {
    }

    public static String loadTemplate(ServletContext context, String path, Logger log) {
        if (context == null || path == null || path.isBlank()) {
            return EMPTY;
        }

        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                if (log != null) {
                    log.log(Level.WARNING, LOG_TEMPLATE_NOT_FOUND, path);
                }
                return EMPTY;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
                return builder.toString();
            }
        } catch (IOException e) {
            if (log != null) {
                log.log(Level.WARNING, LOG_UNABLE_LOAD_TEMPLATE_PREFIX + path, e);
            }
            return EMPTY;
        }
    }
}