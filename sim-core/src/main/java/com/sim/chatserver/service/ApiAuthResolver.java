package com.sim.chatserver.service;

import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.service.widget.WidgetHealthConfigStore;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.inject.spi.CDI;

/**
 * Resolves outbound API key/header values using the same source priorities as widget healthcheck.
 */
public final class ApiAuthResolver {

    private static final Logger log = Logger.getLogger(ApiAuthResolver.class.getName());

    private ApiAuthResolver() {
    }

    /**
     * Resolve outbound auth for Server/Workspace features only.
     *
     * This path intentionally ignores Widget Health overrides so connection
     * tests and summary generation use the exact same credential source.
     */
    public static ResolvedApiAuth resolveForServerConfigOutbound(String requestedApiKey) {
        String requestedRaw = trimToNull(requestedApiKey);
        String requestedToken = normalizeApiKeyToken(requestedRaw);
        if (requestedToken != null) {
            return new ResolvedApiAuth(requestedRaw, requestedToken, "Authorization", "REQUEST");
        }

        String globalRaw = loadGlobalServerApiKey();
        String globalToken = normalizeApiKeyToken(globalRaw);
        if (globalToken != null) {
            return new ResolvedApiAuth(globalRaw, globalToken, "Authorization", "SERVER_CONFIG");
        }

        return ResolvedApiAuth.empty();
    }

    public static ResolvedApiAuth resolveForOutbound(String requestedApiKey) {
        String requestedRaw = trimToNull(requestedApiKey);
        String requestedToken = normalizeApiKeyToken(requestedRaw);

        String globalRaw = loadGlobalServerApiKey();
        String globalToken = normalizeApiKeyToken(globalRaw);

        WidgetHealthAuth health = loadWidgetHealthAuth();

        if (requestedToken != null) {
            // Explicit caller-provided token must win; only inherit preferred header when available.
            if (health.headerName != null) {
                return new ResolvedApiAuth(requestedRaw, requestedToken,
                        defaultHeader(health.headerName), "REQUEST_WITH_HEALTH_HEADER");
            }
            return new ResolvedApiAuth(requestedRaw, requestedToken, "Authorization", "REQUEST");
        }

        if (health.token != null) {
            return new ResolvedApiAuth(health.rawValue, health.token,
                    defaultHeader(health.headerName), "WIDGET_HEALTH");
        }

        if (globalToken != null) {
            if (health.headerName != null) {
                return new ResolvedApiAuth(globalRaw, globalToken,
                        defaultHeader(health.headerName), "SERVER_CONFIG_WITH_HEALTH_HEADER");
            }
            return new ResolvedApiAuth(globalRaw, globalToken, "Authorization", "SERVER_CONFIG");
        }

        return ResolvedApiAuth.empty();
    }

    public static String normalizeApiKeyToken(String rawKey) {
        String token = trimToNull(rawKey);
        if (token == null) {
            return null;
        }

        if (token.regionMatches(true, 0, "Authorization:", 0, 14)) {
            token = token.substring(14).trim();
        }
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        return token.isBlank() ? null : token;
    }

    public static String stripAuthorizationPrefix(String rawValue) {
        String value = trimToNull(rawValue);
        if (value == null) {
            return null;
        }
        if (value.regionMatches(true, 0, "Authorization:", 0, 14)) {
            value = value.substring(14).trim();
        }
        return value.isBlank() ? null : value;
    }

    static ResolvedApiAuth emptyResolvedApiAuth() {
        return ResolvedApiAuth.empty();
    }

    private static String defaultHeader(String headerName) {
        String normalized = trimToNull(headerName);
        return normalized == null ? "Authorization" : normalized;
    }

    private static WidgetHealthAuth loadWidgetHealthAuth() {
        try {
            AppDataSourceHolder holder = CDI.current().select(AppDataSourceHolder.class).get();
            DataSource ds = holder == null ? null : holder.getDataSource();
            if (ds == null) {
                return WidgetHealthAuth.empty();
            }

            WidgetHealthConfigStore store = new WidgetHealthConfigStore(ds);
            WidgetHealthConfigStore.WidgetHealthConfig cfg = store.load();
            if (cfg == null) {
                return WidgetHealthAuth.empty();
            }

            String headerName = trimToNull(cfg.getApiKeyHeaderName());
            String rawValue = trimToNull(cfg.getApiKeyValue());
            String token = normalizeApiKeyToken(rawValue);

            if (token == null && headerName == null) {
                return WidgetHealthAuth.empty();
            }

            return new WidgetHealthAuth(headerName, rawValue, token);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to resolve widget health API key override", e);
            return WidgetHealthAuth.empty();
        } catch (SQLException e) {
            log.log(Level.FINE, "Widget health API key override unavailable", e);
            return WidgetHealthAuth.empty();
        }
    }

    private static String loadGlobalServerApiKey() {
        try {
            ServerConfig config = EncryptedDbConfigStore.load();
            return trimToNull(config == null ? null : config.getApiKey());
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to load global API key", e);
            return null;
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static final class WidgetHealthAuth {
        final String headerName;
        final String rawValue;
        final String token;

        private WidgetHealthAuth(String headerName, String rawValue, String token) {
            this.headerName = headerName;
            this.rawValue = rawValue;
            this.token = token;
        }

        static WidgetHealthAuth empty() {
            return new WidgetHealthAuth(null, null, null);
        }
    }

    public static final class ResolvedApiAuth {
        private final String rawValue;
        private final String token;
        private final String preferredHeaderName;
        private final String source;

        private ResolvedApiAuth(String rawValue, String token, String preferredHeaderName, String source) {
            this.rawValue = rawValue;
            this.token = token;
            this.preferredHeaderName = preferredHeaderName;
            this.source = source == null ? "UNKNOWN" : source.toUpperCase(Locale.ROOT);
        }

        public String rawValue() {
            return rawValue;
        }

        public String token() {
            return token;
        }

        public String preferredHeaderName() {
            return preferredHeaderName;
        }

        public String source() {
            return source;
        }

        public boolean hasToken() {
            return token != null && !token.isBlank();
        }

        private static ResolvedApiAuth empty() {
            return new ResolvedApiAuth(null, null, "Authorization", "NONE");
        }
    }
}