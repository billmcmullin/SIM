package com.sim.chatserver.service;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.service.widget.WidgetHealthConfigStore;
import com.sim.chatserver.service.widget.WidgetHealthConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.enterprise.inject.spi.CDI;

/**
 * Resolves outbound API key/header values using the same source priorities as widget healthcheck.
 */
public final class ApiAuthResolver {

    private static final Logger log = Logger.getLogger(ApiAuthResolver.class.getCanonicalName());
    private static final String HEADER_AUTHORIZATION = CommonLiteralConstants.HEADER_AUTHORIZATION;
    private static final String SOURCE_REQUEST = "REQUEST";
    private static final String SOURCE_SERVER_CONFIG = "SERVER_CONFIG";
    private static final String SOURCE_REQUEST_WITH_HEALTH_HEADER = "REQUEST_WITH_HEALTH_HEADER";
    private static final String SOURCE_WIDGET_HEALTH = "WIDGET_HEALTH";
    private static final String SOURCE_SERVER_CONFIG_WITH_HEALTH_HEADER = "SERVER_CONFIG_WITH_HEALTH_HEADER";
    private static final String PREFIX_AUTHORIZATION = "Authorization:";
    private static final String PREFIX_BEARER = CommonLiteralConstants.AUTH_BEARER_PREFIX;
    private static final String MSG_RESOLVE_WIDGET_HEALTH_OVERRIDE_FAILED = "Unable to resolve widget health API key override";
    private static final String MSG_WIDGET_HEALTH_OVERRIDE_UNAVAILABLE = "Widget health API key override unavailable";
    private static final String MSG_LOAD_GLOBAL_API_KEY_FAILED = "Unable to load global API key";
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
            return new ResolvedApiAuth(requestedRaw, requestedToken, HEADER_AUTHORIZATION, SOURCE_REQUEST);
        }

        String globalRaw = loadGlobalServerApiKey();
        String globalToken = normalizeApiKeyToken(globalRaw);
        if (globalToken != null) {
            return new ResolvedApiAuth(globalRaw, globalToken, HEADER_AUTHORIZATION, SOURCE_SERVER_CONFIG);
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
                        defaultHeader(health.headerName), SOURCE_REQUEST_WITH_HEALTH_HEADER);
            }
            return new ResolvedApiAuth(requestedRaw, requestedToken, HEADER_AUTHORIZATION, SOURCE_REQUEST);
        }

        if (health.token != null) {
            return new ResolvedApiAuth(health.rawValue, health.token,
                    defaultHeader(health.headerName), SOURCE_WIDGET_HEALTH);
        }

        if (globalToken != null) {
            if (health.headerName != null) {
                return new ResolvedApiAuth(globalRaw, globalToken,
                        defaultHeader(health.headerName), SOURCE_SERVER_CONFIG_WITH_HEALTH_HEADER);
            }
            return new ResolvedApiAuth(globalRaw, globalToken, HEADER_AUTHORIZATION, SOURCE_SERVER_CONFIG);
        }

        return ResolvedApiAuth.empty();
    }

    public static String normalizeApiKeyToken(String rawKey) {
        String token = trimToNull(rawKey);
        if (token == null) {
            return null;
        }

        if (token.regionMatches(true, 0, PREFIX_AUTHORIZATION, 0, 14)) {
            token = token.substring(14).trim();
        }
        if (token.regionMatches(true, 0, PREFIX_BEARER, 0, 7)) {
            token = token.substring(7).trim();
        }
        return token.isBlank() ? null : token;
    }

    public static String stripAuthorizationPrefix(String rawValue) {
        String value = trimToNull(rawValue);
        if (value == null) {
            return null;
        }
        if (value.regionMatches(true, 0, PREFIX_AUTHORIZATION, 0, 14)) {
            value = value.substring(14).trim();
        }
        return value.isBlank() ? null : value;
    }

    static ResolvedApiAuth emptyResolvedApiAuth() {
        return ResolvedApiAuth.empty();
    }

    private static String defaultHeader(String headerName) {
        String normalized = trimToNull(headerName);
        return normalized == null ? HEADER_AUTHORIZATION : normalized;
    }

    private static WidgetHealthAuth loadWidgetHealthAuth() {
        try {
            AppDataSourceHolder holder = CDI.current().select(AppDataSourceHolder.class).get();
            DataSource ds = holder == null ? null : holder.getDataSource();
            if (ds == null) {
                return WidgetHealthAuth.empty();
            }

            WidgetHealthConfigStore store = new WidgetHealthConfigStore(ds);
            WidgetHealthConfig cfg = store.load();
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
            log.log(Level.FINE, MSG_RESOLVE_WIDGET_HEALTH_OVERRIDE_FAILED, e);
            return WidgetHealthAuth.empty();
        } catch (SQLException e) {
            log.log(Level.FINE, MSG_WIDGET_HEALTH_OVERRIDE_UNAVAILABLE, e);
            return WidgetHealthAuth.empty();
        }
    }

    private static String loadGlobalServerApiKey() {
        try {
            ServerConfig config = EncryptedDbConfigStore.load();
            return trimToNull(config == null ? null : config.getApiKey());
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, MSG_LOAD_GLOBAL_API_KEY_FAILED, e);
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
}

final class WidgetHealthAuth {
    private static final WidgetHealthAuth EMPTY_AUTH = new WidgetHealthAuth(null, null, null);

    final String headerName;
    final String rawValue;
    final String token;

    WidgetHealthAuth(String headerName, String rawValue, String token) {
        this.headerName = headerName;
        this.rawValue = rawValue;
        this.token = token;
    }

    static WidgetHealthAuth empty() {
        return EMPTY_AUTH;
    }
}


