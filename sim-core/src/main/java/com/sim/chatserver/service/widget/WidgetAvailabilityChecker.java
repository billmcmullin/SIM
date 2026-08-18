package com.sim.chatserver.service.widget;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.service.widget.WidgetHealthConfigStore.WidgetHealthConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.ServerDiagnosticsLog;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

/**
 * Performs a synthetic availability check against AnythingLLM/widget backend.
 *
 * Priority: 1) DB config (widget_health_config, id=1) 2) Env vars 3) Hard
 * defaults
 */
@ApplicationScoped
public class WidgetAvailabilityChecker {

    private static final Logger log = Logger.getLogger(WidgetAvailabilityChecker.class.getName());

    private static final String DEFAULT_URL = "http://anythingllm:3001/api/v1/system";
    private static final String DEFAULT_METHOD = "GET";
    private static final int DEFAULT_TIMEOUT_MS = 8000;
    private static final int DEFAULT_CHECK_INTERVAL_SECONDS = 300;
    private static final boolean DEFAULT_HEALTHCHECK_ENABLED = true;
    private static final String DEBUG_FAILURES_ENV = "WIDGET_HEALTHCHECK_DEBUG_FAILURES";
    private static final String DEBUG_FAILURES_PROP = "sim.widget.healthcheck.debug.failures";
    private static final String REQUIRE_HTTPS_WITH_AUTH_ENV = "WIDGET_HEALTHCHECK_REQUIRE_HTTPS_WITH_AUTH";
    private static final String REQUIRE_HTTPS_WITH_AUTH_PROP = "sim.widget.healthcheck.require.https.with.auth";

    private static final Pattern EMBED_STREAM_PATTERN = Pattern.compile("/api/embed/([^/]+)/stream-chat");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");
    private static final Pattern TOKEN_WHITESPACE = Pattern.compile("\\s+");

    @Inject
    AppDataSourceHolder dsHolder;

    private final Object cacheLock = new Object();
    private volatile CachedHealthResult cachedHealthResult;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public WidgetAvailabilityResult checkNow() {
        return checkNow(false, false);
    }

    public WidgetAvailabilityResult checkNow(boolean forceRefresh) {
        return checkNow(forceRefresh, false);
    }

    public WidgetAvailabilityResult checkNow(boolean forceRefresh, boolean runWhenDisabled) {
        EffectiveConfig cfg = resolveEffectiveConfig();
        Instant now = Instant.now();

        if (!cfg.enabled && !runWhenDisabled) {
            WidgetAvailabilityResult disabled = disabled(now, cfg);
            cacheResult(cfg, disabled, now);
            return disabled;
        }

        if (!cfg.enabled && runWhenDisabled) {
            log.info(() -> "Widget healthcheck service is disabled, running one-off manual availability test.");
        }

        if (!forceRefresh && !runWhenDisabled) {
            WidgetAvailabilityResult cached = getCachedResultIfFresh(cfg);
            if (cached != null) {
                return cached;
            }
        }

        synchronized (cacheLock) {
            if (!forceRefresh && !runWhenDisabled) {
                WidgetAvailabilityResult cached = getCachedResultIfFresh(cfg);
                if (cached != null) {
                    return cached;
                }
            }

            Instant checkedAt = Instant.now();
            WidgetAvailabilityResult live = runLiveCheck(cfg, checkedAt);

            if (!cfg.enabled && runWhenDisabled) {
                live = new WidgetAvailabilityResult(
                        live.available(),
                        live.status(),
                        live.checkedAtIso(),
                        live.latencyMs(),
                        safeMsg(live.details()) + " [manual-check-while-disabled]");
            }

            if (!runWhenDisabled) {
                cacheResult(cfg, live, checkedAt);
            }
            return live;
        }
    }

    private WidgetAvailabilityResult getCachedResultIfFresh(EffectiveConfig cfg) {
        CachedHealthResult cached = cachedHealthResult;
        if (cached == null || cfg == null) {
            return null;
        }

        String fingerprint = configFingerprint(cfg);
        if (!fingerprint.equals(cached.configFingerprint)) {
            return null;
        }

        long ageSeconds = Duration.between(cached.checkedAt, Instant.now()).toSeconds();
        if (ageSeconds < 0 || ageSeconds >= Math.max(1, cfg.checkIntervalSeconds)) {
            return null;
        }

        log.fine(() -> "Returning cached widget availability result ageSeconds=" + ageSeconds
                + " intervalSeconds=" + cfg.checkIntervalSeconds + sourceSuffix(cfg));
        return cached.result;
    }

    private void cacheResult(EffectiveConfig cfg, WidgetAvailabilityResult result, Instant checkedAt) {
        if (cfg == null || result == null || checkedAt == null) {
            return;
        }
        cachedHealthResult = new CachedHealthResult(configFingerprint(cfg), checkedAt, result);
    }

    private String configFingerprint(EffectiveConfig cfg) {
        StringBuilder sb = new StringBuilder();
        sb.append(cfg.enabled).append('|')
                .append(cfg.checkIntervalSeconds).append('|')
                .append(safeMsg(cfg.url)).append('|')
                .append(safeMsg(cfg.method)).append('|')
                .append(cfg.timeoutMs).append('|')
                .append(safeMsg(cfg.expectField)).append('|')
                .append(safeMsg(cfg.expectValue)).append('|')
                .append(safeMsg(cfg.widgetId)).append('|')
                .append(safeMsg(cfg.requestOrigin)).append('|')
                .append(safeMsg(cfg.requestReferer)).append('|')
                .append(safeMsg(cfg.requestUserAgent)).append('|')
                .append(safeMsg(cfg.requestCookie)).append('|')
                .append(safeMsg(cfg.apiKeyHeaderName)).append('|')
                .append(safeMsg(cfg.apiKeyValue));
        return sb.toString();
    }

    private WidgetAvailabilityResult runLiveCheck(EffectiveConfig cfg, Instant start) {
        Instant checkStart = start == null ? Instant.now() : start;
        String checkedAt = DateTimeFormatter.ISO_INSTANT.format(checkStart);
        String requestId = UUID.randomUUID().toString();
        log.info(() -> "Widget availability check starting: method=" + safeMsg(cfg.method)
                + " url=" + safeUrl(cfg.url)
                + " timeoutMs=" + cfg.timeoutMs
                + " expectFieldSet=" + (cfg.expectField != null)
                + " expectValueSet=" + (cfg.expectValue != null)
                + " checkIntervalSeconds=" + cfg.checkIntervalSeconds
                + sourceSuffix(cfg));

        ServerDiagnosticsLog.write(
            "widget-availability-checker",
            requestId,
            "healthcheck-request",
            "method=" + safeMsg(cfg.method)
                + "\nurl=" + safeUrl(cfg.url)
                + "\ntimeoutMs=" + cfg.timeoutMs
                + "\nsource=" + safeMsg(cfg.source)
        );

        boolean sensitiveAuthConfigured = cfg.apiKeyValue != null || cfg.requestCookie != null;
        boolean httpsUrl = isHttpsUrl(cfg.url);
        boolean requireHttpsWithAuth = isHttpsRequiredWithAuth();

        if (sensitiveAuthConfigured && !httpsUrl && requireHttpsWithAuth) {
            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
            log.warning(() -> "Widget availability check blocked: sensitive auth material requires HTTPS url="
                + safeUrl(cfg.url) + sourceSuffix(cfg));
            ServerDiagnosticsLog.write(
                    "widget-availability-checker",
                    requestId,
                    "healthcheck-blocked",
                    "reason=sensitive-auth-without-https\nurl=" + safeUrl(cfg.url)
                            + "\nlatencyMs=" + latencyMs
            );
            return down(checkedAt, latencyMs, "Sensitive auth material configured but healthcheck URL is not HTTPS"
                + sourceSuffix(cfg));
        }

        if (sensitiveAuthConfigured && !httpsUrl && !requireHttpsWithAuth) {
            log.info(() -> "Widget availability check allowing HTTP URL with sensitive auth material because "
                    + REQUIRE_HTTPS_WITH_AUTH_ENV + " is disabled. url=" + safeUrl(cfg.url)
                    + sourceSuffix(cfg));
        }

        try {
            String effectiveWidgetId = effectiveWidgetId(cfg);
            if ("POST".equals(cfg.method) && effectiveWidgetId != null) {
                cfg.widgetId = effectiveWidgetId;
                return runSyntheticSseProbe(cfg, checkStart, checkedAt, requestId);
            }

            HttpResponse<String> response = sendHttpHealthRequest(cfg, null);

            if (shouldRetryWithXApiKey(cfg, response.statusCode())) {
                final int initialStatus = response.statusCode();
                log.info(() -> "Widget availability retrying with X-API-Key after status=" + initialStatus
                        + " url=" + safeUrl(cfg.url) + sourceSuffix(cfg));
                ServerDiagnosticsLog.write(
                        "widget-availability-checker",
                        requestId,
                        "healthcheck-retry",
                        "reason=auth-header-fallback"
                        + "\ninitialStatus=" + initialStatus
                                + "\nfromHeader=Authorization"
                                + "\ntoHeader=X-API-Key"
                                + "\nurl=" + safeUrl(cfg.url)
                );
                response = sendHttpHealthRequest(cfg, "X-API-Key");
            }

            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
            int status = response.statusCode();
            String body = response.body() == null ? "" : response.body();
            String contentType = headerValue(response, "content-type");
            String wwwAuthenticate = headerValue(response, "www-authenticate");

                ServerDiagnosticsLog.write(
                    "widget-availability-checker",
                    requestId,
                    "healthcheck-response",
                    "status=" + status
                        + "\nlatencyMs=" + latencyMs
                        + "\ncontentType=" + safeMsg(contentType)
                        + "\nurl=" + safeUrl(cfg.url)
                        + "\nbodySnippet=" + bodySnippet(body)
                );

            if (status < 200 || status >= 300) {
                log.warning(() -> "Widget availability check returned non-success status=" + status
                        + " contentType=" + safeMsg(contentType)
                        + authHeaderSuffix(wwwAuthenticate)
                        + " latencyMs=" + latencyMs
                        + " url=" + safeUrl(cfg.url)
                        + bodySnippetSuffix(body)
                        + sourceSuffix(cfg));
                return down(checkedAt, latencyMs, "Non-success HTTP status: " + status
                        + authHeaderSuffix(wwwAuthenticate)
                        + bodySnippetSuffix(body) + sourceSuffix(cfg));
            }

            if (cfg.expectField != null && cfg.expectValue != null) {
                boolean jsonOk = matchesExpectedJson(body, cfg.expectField, cfg.expectValue);
                if (!jsonOk) {
                    log.warning(() -> "Widget availability JSON expectation failed field=" + cfg.expectField
                            + " expectedValue=" + cfg.expectValue
                            + " latencyMs=" + latencyMs
                            + " url=" + safeUrl(cfg.url)
                            + bodySnippetSuffix(body)
                            + sourceSuffix(cfg));
                        return down(checkedAt, latencyMs, "JSON expectation failed: "
                            + cfg.expectField + '=' + cfg.expectValue + sourceSuffix(cfg));
                }
            }

            log.info(() -> "Widget availability check succeeded status=" + status
                    + " contentType=" + safeMsg(contentType)
                    + " latencyMs=" + latencyMs
                    + " url=" + safeUrl(cfg.url)
                    + sourceSuffix(cfg));
            return up(checkedAt, latencyMs, "Healthcheck succeeded" + sourceSuffix(cfg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
            String errorRef = UUID.randomUUID().toString();
                ServerDiagnosticsLog.write(
                    "widget-availability-checker",
                    requestId,
                    "healthcheck-error",
                    "errorRef=" + errorRef + "\nlatencyMs=" + latencyMs + "\nurl=" + safeUrl(cfg.url),
                    e
                );
            if (isFailureDebugEnabled()) {
                log.log(Level.WARNING, "Widget availability check interrupted for url=" + safeUrl(cfg.url)
                        + " errorRef=" + errorRef + sourceSuffix(cfg), e);
                return down(checkedAt, latencyMs, "Interrupted during healthcheck" + sourceSuffix(cfg));
            }

            log.warning(() -> "Widget availability check interrupted. errorRef=" + errorRef
                    + " Set " + DEBUG_FAILURES_ENV + "=true for full debug details.");
            return down(checkedAt, latencyMs, genericFailureDetails(errorRef));
        } catch (IOException | IllegalArgumentException e) {
            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
            String errorRef = UUID.randomUUID().toString();
            ServerDiagnosticsLog.write(
                "widget-availability-checker",
                requestId,
                "healthcheck-error",
                "errorRef=" + errorRef + "\nlatencyMs=" + latencyMs + "\nurl=" + safeUrl(cfg.url)
                    + "\nmessage=" + safeMsg(e.getMessage()),
                e
            );
            if (isFailureDebugEnabled()) {
                log.log(Level.WARNING, "Widget availability check error for url=" + safeUrl(cfg.url)
                        + " latencyMs=" + latencyMs + " errorRef=" + errorRef + sourceSuffix(cfg), e);
                return down(checkedAt, latencyMs, "Exception: " + e.getClass().getSimpleName()
                        + " - " + safeMsg(e.getMessage()) + sourceSuffix(cfg));
            }

            log.warning(() -> "Widget availability check failed. errorRef=" + errorRef
                    + " Set " + DEBUG_FAILURES_ENV + "=true for full debug details.");
            return down(checkedAt, latencyMs, genericFailureDetails(errorRef));
        }
    }

    private boolean isFailureDebugEnabled() {
        String prop = readLegacyPropertyEnv(DEBUG_FAILURES_PROP, 16);
        if (prop != null) {
            return isTruthy(prop);
        }
        String envValue = readEnvCanonical(DEBUG_FAILURES_ENV, 16);
        return envValue != null && isTruthy(envValue);
    }

    private boolean isHttpsRequiredWithAuth() {
        String prop = readLegacyPropertyEnv(REQUIRE_HTTPS_WITH_AUTH_PROP, 16);
        if (prop != null) {
            if (isTruthy(prop)) {
                return true;
            }
            if (isFalsy(prop)) {
                return false;
            }
        }

        String envValue = readEnvCanonical(REQUIRE_HTTPS_WITH_AUTH_ENV, 16);
        if (envValue == null) {
            return false;
        }
        if (isTruthy(envValue)) {
            return true;
        }
        if (isFalsy(envValue)) {
            return false;
        }
        return false;
    }

    private boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "y".equals(normalized)
                || "on".equals(normalized);
    }

    private boolean isFalsy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "false".equals(normalized)
                || "0".equals(normalized)
                || "no".equals(normalized)
                || "n".equals(normalized)
                || "off".equals(normalized);
    }

    private String genericFailureDetails(String errorRef) {
        return "Something went wrong during widget healthcheck. To enable debug details set "
                + DEBUG_FAILURES_ENV + "=true"
                + " (or system property " + DEBUG_FAILURES_PROP + "=true)."
                + " Reference: " + errorRef;
    }

    private WidgetAvailabilityResult runSyntheticSseProbe(EffectiveConfig cfg, Instant start, String checkedAt, String requestId) throws IOException, InterruptedException {
        String probeUrl = buildEmbedStreamUrl(cfg.url, cfg.widgetId);
        String payload = buildSyntheticPayload();
        log.info(() -> "Widget synthetic SSE probe starting: url=" + safeUrl(probeUrl)
            + " timeoutMs=" + cfg.timeoutMs
            + sourceSuffix(cfg));

        ServerDiagnosticsLog.write(
                "widget-availability-checker",
                requestId,
                "sse-request",
                "method=POST\nurl=" + safeUrl(probeUrl)
                        + "\nwidgetId=" + safeMsg(cfg.widgetId)
                        + "\npayload=" + payload
        );

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(probeUrl))
                .timeout(Duration.ofMillis(cfg.timeoutMs))
                .header("Accept", "text/event-stream")
                .header("Content-Type", "text/plain;charset=UTF-8");

        applyApiKeyHeader(req, cfg);

        if (cfg.requestOrigin != null) {
            req.header("Origin", cfg.requestOrigin);
        }

        if (cfg.requestReferer != null) {
            req.header("Referer", cfg.requestReferer);
        }

        if (cfg.requestUserAgent != null) {
            req.header("User-Agent", cfg.requestUserAgent);
        }

        if (cfg.requestCookie != null) {
            req.header("Cookie", cfg.requestCookie);
        }

        HttpRequest request = req
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        long latencyMs = Duration.between(start, Instant.now()).toMillis();
        int status = response.statusCode();
        String body = response.body() == null ? "" : response.body();
        String contentType = headerValue(response, "content-type");
        String wwwAuthenticate = headerValue(response, "www-authenticate");

        ServerDiagnosticsLog.write(
            "widget-availability-checker",
            requestId,
            "sse-response",
            "status=" + status
                + "\nlatencyMs=" + latencyMs
                + "\ncontentType=" + safeMsg(contentType)
                + "\nurl=" + safeUrl(probeUrl)
                + "\nbodySnippet=" + bodySnippet(body)
        );

        if (status < 200 || status >= 300) {
            log.warning(() -> "Widget synthetic SSE probe returned non-success status=" + status
                + " contentType=" + safeMsg(contentType)
                + authHeaderSuffix(wwwAuthenticate)
                + " latencyMs=" + latencyMs
                + " url=" + safeUrl(probeUrl)
                + bodySnippetSuffix(body)
                + sourceSuffix(cfg));
            return down(checkedAt, latencyMs, "Synthetic SSE probe failed: HTTP " + status
                + authHeaderSuffix(wwwAuthenticate)
                    + bodySnippetSuffix(body) + sourceSuffix(cfg));
        }

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("text/event-stream")) {
            log.warning(() -> "Widget synthetic SSE probe returned unexpected content-type=" + safeMsg(contentType)
                + " latencyMs=" + latencyMs
                + " url=" + safeUrl(probeUrl)
                + bodySnippetSuffix(body)
                + sourceSuffix(cfg));
            return down(checkedAt, latencyMs, "Synthetic SSE probe failed: content-type is not text/event-stream"
                    + " (actual=" + safeMsg(contentType) + ')' + sourceSuffix(cfg));
        }

        String canonicalBody = canonicalizeBodyForValidation(body, 100_000);
        SseValidationResult sse = validateSseBody(canonicalBody);
        if (!sse.ok) {
            log.warning(() -> "Widget synthetic SSE probe validation failed reason=" + safeMsg(sse.reason)
                + " latencyMs=" + latencyMs
                + " url=" + safeUrl(probeUrl)
                + bodySnippetSuffix(canonicalBody)
                + sourceSuffix(cfg));
            return down(checkedAt, latencyMs, "Synthetic SSE probe failed: " + sse.reason + sourceSuffix(cfg));
        }

        log.info(() -> "Widget synthetic SSE probe succeeded status=" + status
            + " contentType=" + safeMsg(contentType)
            + " latencyMs=" + latencyMs
            + " url=" + safeUrl(probeUrl)
            + sourceSuffix(cfg));
        return up(checkedAt, latencyMs, "Synthetic SSE probe succeeded" + sourceSuffix(cfg));
    }

    private String effectiveWidgetId(EffectiveConfig cfg) {
        String explicit = trimToNull(cfg.widgetId);
        if (explicit != null) {
            return explicit;
        }

        String u = trimToNull(cfg.url);
        if (u == null) {
            return null;
        }

        Matcher m = EMBED_STREAM_PATTERN.matcher(u);
        if (m.find()) {
            return trimToNull(m.group(1));
        }

        return null;
    }

    private String buildEmbedStreamUrl(String configuredUrl, String widgetId) {
        String base = configuredUrl == null ? "" : configuredUrl.trim();
        String wid = widgetId == null ? "" : widgetId.trim();

        if (base.isEmpty()) {
            throw new IllegalArgumentException("Healthcheck URL is empty");
        }
        if (wid.isEmpty()) {
            throw new IllegalArgumentException("Widget ID is required for POST synthetic check");
        }

        String normalizedBase = stripQueryAndFragment(base);

        if (normalizedBase.contains("/api/embed/") && normalizedBase.endsWith("/stream-chat")) {
            return normalizedBase;
        }

        URI u = URI.create(normalizedBase);
        String origin = u.getScheme() + "://" + u.getAuthority();

        return origin + "/api/embed/" + URLEncoder.encode(wid, StandardCharsets.UTF_8) + "/stream-chat";
    }

    private String stripQueryAndFragment(String url) {
        URI u = URI.create(url);
        URI clean = URI.create(u.getScheme() + "://" + u.getAuthority()
                + (u.getPath() == null ? "" : u.getPath()));
        String s = clean.toString();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private String buildSyntheticPayload() {
        JsonObject payload = Json.createObjectBuilder()
                .add("message", "test")
                .add("sessionId", UUID.randomUUID().toString())
                .add("username", "Customer Embedded Widget")
                .addNull("prompt")
                .addNull("model")
                .addNull("temperature")
                .build();
        return payload.toString();
    }

    private SseValidationResult validateSseBody(String body) {
        if (body == null || body.isBlank()) {
            return SseValidationResult.fail("empty response body");
        }

        String[] events = body.split("\\R\\R+");
        boolean sawDataEvent = false;
        boolean sawGoodChunk = false;
        boolean sawClose = false;
        boolean sawErrorTrue = false;

        for (String event : events) {
            if (event == null || event.isBlank()) {
                continue;
            }

            String[] lines = event.split("\\R");
            for (String line : lines) {
                String trimmed = line == null ? "" : line.trim();
                if (!trimmed.startsWith("data:")) {
                    continue;
                }

                String jsonPart = trimmed.substring("data:".length()).trim();
                if (jsonPart.isEmpty()) {
                    continue;
                }

                sawDataEvent = true;
                try (JsonReader reader = Json.createReader(new StringReader(jsonPart))) {
                    JsonObject obj = reader.readObject();

                    String type = stringVal(obj, "type");
                    boolean error = boolVal(obj, "error", false);
                    boolean close = boolVal(obj, "close", false);
                    String text = stringVal(obj, "textResponse");

                    if (error) {
                        sawErrorTrue = true;
                    }
                    if (close) {
                        sawClose = true;
                    }

                    if ("textResponseChunk".equals(type) && !error && text != null && !text.trim().isEmpty()) {
                        sawGoodChunk = true;
                    }
                } catch (JsonException | ClassCastException | IllegalStateException ex) {
                    log.log(Level.FINE, "Ignoring malformed SSE data chunk", ex);
                }
            }
        }

        if (!sawDataEvent) {
            return SseValidationResult.fail("no SSE data events found");
        }
        if (sawErrorTrue) {
            return SseValidationResult.fail("SSE returned error=true");
        }
        if (!sawGoodChunk) {
            return SseValidationResult.fail("no valid non-empty textResponseChunk found");
        }
        if (!sawClose) {
            return SseValidationResult.fail("stream did not include close=true event");
        }

        return SseValidationResult.ok();
    }

    private String headerValue(HttpResponse<?> response, String header) {
        return response.headers().firstValue(header).orElse(null);
    }

    private EffectiveConfig resolveEffectiveConfig() {
        try {
            DataSource ds = dsHolder != null ? dsHolder.getDataSource() : null;
            if (ds != null) {
                WidgetHealthConfigStore store = new WidgetHealthConfigStore(ds);
                store.ensureTable();
                store.ensureDefaultRow();

                WidgetHealthConfig db = store.load();
                if (db != null && trimToNull(db.getHealthcheckUrl()) != null) {
                    EffectiveConfig cfg = new EffectiveConfig();
                    cfg.source = "DB";
                    cfg.enabled = db.isHealthcheckEnabled();
                    cfg.checkIntervalSeconds = normalizeCheckIntervalSeconds(db.getCheckIntervalSeconds());
                    cfg.url = trimToNull(db.getHealthcheckUrl());
                    cfg.method = normalizeMethod(db.getMethod());
                    cfg.timeoutMs = normalizeTimeout(db.getTimeoutMs());
                    cfg.expectField = trimToNull(db.getExpectJsonField());
                    cfg.expectValue = trimToNull(db.getExpectJsonValue());
                    cfg.widgetId = trimToNull(db.getWidgetId());

                    // New configurable request-shaping headers
                    cfg.requestOrigin = trimToNull(db.getRequestOrigin());
                    cfg.requestReferer = trimToNull(db.getRequestReferer());
                    cfg.requestUserAgent = trimToNull(db.getRequestUserAgent());
                    cfg.requestCookie = trimToNull(db.getRequestCookie());
                    cfg.apiKeyHeaderName = trimToNull(db.getApiKeyHeaderName());
                    cfg.apiKeyValue = trimToNull(db.getApiKeyValue());

                    if (cfg.apiKeyValue == null) {
                        cfg.apiKeyValue = resolveGlobalServerApiKey();
                    }
                    if (cfg.apiKeyValue != null && cfg.apiKeyHeaderName == null) {
                        cfg.apiKeyHeaderName = "Authorization";
                    }

                    log.info(() -> "Widget availability config resolved from DB: method=" + safeMsg(cfg.method)
                            + " url=" + safeUrl(cfg.url)
                            + " timeoutMs=" + cfg.timeoutMs
                            + " enabled=" + cfg.enabled
                            + " checkIntervalSeconds=" + cfg.checkIntervalSeconds
                            + sourceSuffix(cfg));
                    return cfg;
                }
            }
        } catch (java.sql.SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, "DB config unavailable, falling back to env/defaults", e);
        }

        EffectiveConfig envCfg = new EffectiveConfig();
        envCfg.source = "ENV/DEFAULT";
        envCfg.enabled = parseBooleanEnv("WIDGET_HEALTHCHECK_ENABLED", DEFAULT_HEALTHCHECK_ENABLED);
        envCfg.checkIntervalSeconds = normalizeCheckIntervalSeconds(
            parseIntEnv("WIDGET_HEALTHCHECK_INTERVAL_SECONDS", DEFAULT_CHECK_INTERVAL_SECONDS));
        envCfg.url = env("WIDGET_HEALTHCHECK_URL", DEFAULT_URL);
        envCfg.method = normalizeMethod(env("WIDGET_HEALTHCHECK_METHOD", DEFAULT_METHOD));
        envCfg.timeoutMs = normalizeTimeout(parseIntEnv("WIDGET_HEALTHCHECK_TIMEOUT_MS", DEFAULT_TIMEOUT_MS));
        envCfg.expectField = trimToNull(env("WIDGET_HEALTHCHECK_EXPECT_JSON_FIELD", ""));
        envCfg.expectValue = trimToNull(env("WIDGET_HEALTHCHECK_EXPECT_JSON_VALUE", ""));
        envCfg.widgetId = trimToNull(env("WIDGET_HEALTHCHECK_WIDGET_ID", ""));

        // Optional env fallbacks for synthetic probe headers
        envCfg.requestOrigin = trimToNull(env("WIDGET_HEALTHCHECK_REQUEST_ORIGIN", ""));
        envCfg.requestReferer = trimToNull(env("WIDGET_HEALTHCHECK_REQUEST_REFERER", ""));
        envCfg.requestUserAgent = trimToNull(env("WIDGET_HEALTHCHECK_REQUEST_USER_AGENT", ""));
        envCfg.requestCookie = trimToNull(env("WIDGET_HEALTHCHECK_REQUEST_COOKIE", ""));
        envCfg.apiKeyHeaderName = trimToNull(env("WIDGET_HEALTHCHECK_API_KEY_HEADER", "Authorization"));
        envCfg.apiKeyValue = trimToNull(env("WIDGET_HEALTHCHECK_API_KEY", ""));
        if (envCfg.apiKeyValue == null) {
            envCfg.apiKeyValue = resolveGlobalServerApiKey();
        }
        if (envCfg.apiKeyValue != null && envCfg.apiKeyHeaderName == null) {
            envCfg.apiKeyHeaderName = "Authorization";
        }

        log.info(() -> "Widget availability config resolved from ENV/DEFAULT: method=" + safeMsg(envCfg.method)
            + " url=" + safeUrl(envCfg.url)
            + " timeoutMs=" + envCfg.timeoutMs
            + " enabled=" + envCfg.enabled
            + " checkIntervalSeconds=" + envCfg.checkIntervalSeconds
            + sourceSuffix(envCfg));
        return envCfg;
    }

    private String normalizeMethod(String method) {
        String m = method == null ? DEFAULT_METHOD : method.trim().toUpperCase(Locale.ROOT);
        if (!"GET".equals(m) && !"HEAD".equals(m) && !"POST".equals(m)) {
            return DEFAULT_METHOD;
        }
        return m;
    }

    private int normalizeTimeout(int timeoutMs) {
        if (timeoutMs <= 0) {
            return DEFAULT_TIMEOUT_MS;
        }
        return Math.min(timeoutMs, 120_000);
    }

    private int normalizeCheckIntervalSeconds(int checkIntervalSeconds) {
        if (checkIntervalSeconds <= 0) {
            return DEFAULT_CHECK_INTERVAL_SECONDS;
        }
        if (checkIntervalSeconds < 30) {
            return 30;
        }
        return Math.min(checkIntervalSeconds, 86_400);
    }

    private boolean matchesExpectedJson(String body, String expectedField, String expectedValue) {
        String canonicalBody = canonicalizeInput(body == null ? "" : body, 100_000);
        try (JsonReader reader = Json.createReader(new StringReader(canonicalBody))) {
            JsonObject obj = reader.readObject();
            if (!obj.containsKey(expectedField) || obj.isNull(expectedField)) {
                return false;
            }
            String actual = obj.get(expectedField).toString();
            if (actual.length() >= 2 && actual.charAt(0) == '"' && actual.charAt(actual.length() - 1) == '"') {
                actual = actual.substring(1, actual.length() - 1);
            }
            return expectedValue.equalsIgnoreCase(actual.trim());
        } catch (JsonException | ClassCastException | IllegalStateException e) {
            log.log(Level.FINE, "Failed to parse expected JSON health payload", e);
            return false;
        }
    }

    private String stringVal(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.containsKey(key) || obj.isNull(key)) {
            return null;
        }
        try {
            return obj.getString(key, null);
        } catch (ClassCastException | IllegalStateException e) {
            log.log(Level.FINE, "stringVal fallback for key=" + key, e);
            String v = obj.get(key).toString();
            if (v != null && v.length() >= 2 && v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
                v = v.substring(1, v.length() - 1);
            }
            return v;
        }
    }

    private boolean boolVal(JsonObject obj, String key, boolean fallback) {
        if (obj == null || key == null || !obj.containsKey(key) || obj.isNull(key)) {
            return fallback;
        }
        try {
            return obj.getBoolean(key);
        } catch (ClassCastException | IllegalStateException e) {
            log.log(Level.FINE, "boolVal parse fallback for key=" + key, e);
            try {
                return Boolean.parseBoolean(obj.get(key).toString().replace("\"", "").trim());
            } catch (Throwable ex) {
                log.log(Level.FINE, "boolVal parse failed for key=" + key, ex);
                return fallback;
            }
        }
    }

    private String env(String key, String defaultValue) {
        String v = readEnvCanonical(key, 4096);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    private int parseIntEnv(String key, int defaultValue) {
        try {
            return Integer.parseInt(env(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid integer environment value for " + key, e);
            return defaultValue;
        }
    }

    private boolean parseBooleanEnv(String key, boolean defaultValue) {
        String raw = readEnvCanonical(key, 32);
        if (raw == null) {
            return defaultValue;
        }
        if (isTruthy(raw)) {
            return true;
        }
        if (isFalsy(raw)) {
            return false;
        }
        return defaultValue;
    }

    private String canonicalizeInput(String value, int maxChars) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC).trim();
        if (normalized.isBlank()) {
            return "";
        }
        if (normalized.length() > maxChars || CONTROL_CHARS.matcher(normalized).find()) {
            return "";
        }
        return normalized;
    }

    private String canonicalizeBodyForValidation(String body, int maxChars) {
        String normalized = Normalizer.normalize(body == null ? "" : body, Normalizer.Form.NFKC);
        String withoutNul = normalized.replace('\u0000', ' ');
        if (withoutNul.length() > maxChars) {
            return withoutNul.substring(0, maxChars);
        }
        return withoutNul;
    }

    private String readEnvCanonical(String key, int maxChars) {
        String raw = System.getenv(key);
        if (raw == null) {
            return null;
        }
        String normalized = canonicalizeInput(raw, maxChars);
        return normalized.isBlank() ? null : normalized;
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String readLegacyPropertyEnv(String propertyName, int maxLen) {
        if (propertyName == null || propertyName.isBlank()) {
            return null;
        }
        String envName = propertyName.toUpperCase(Locale.ROOT).replace('.', '_');
        return readEnvCanonical(envName, maxLen);
    }

    private boolean isHttpsUrl(String value) {
        String t = trimToNull(value);
        if (t == null) {
            return false;
        }
        try {
            URI uri = URI.create(t);
            return "https".equalsIgnoreCase(uri.getScheme());
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to parse healthcheck URL for HTTPS check", e);
            return false;
        }
    }

    private HttpResponse<String> sendHttpHealthRequest(EffectiveConfig cfg, String apiKeyHeaderOverride)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(cfg.url))
                .timeout(Duration.ofMillis(cfg.timeoutMs));

        if (builder == null) {
            throw new IllegalStateException("Unable to initialize HTTP request builder");
        }

        applyApiKeyHeader(builder, cfg, apiKeyHeaderOverride);

        if ("HEAD".equals(cfg.method)) {
            builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        } else if ("POST".equals(cfg.method)) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
            builder.header("Content-Type", "application/json");
        } else {
            builder.GET();
        }

        HttpRequest request = builder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean shouldRetryWithXApiKey(EffectiveConfig cfg, int statusCode) {
        if (cfg == null || cfg.apiKeyValue == null) {
            return false;
        }
        String configuredHeader = trimToNull(cfg.apiKeyHeaderName);
        if (configuredHeader != null && !"authorization".equalsIgnoreCase(configuredHeader)) {
            return false;
        }
        return statusCode == 400 || statusCode == 401 || statusCode == 403;
    }

    private String safeMsg(String msg) {
        return msg == null ? "" : msg;
    }

    private void applyApiKeyHeader(HttpRequest.Builder requestBuilder, EffectiveConfig cfg) {
        applyApiKeyHeader(requestBuilder, cfg, null);
    }

    private void applyApiKeyHeader(HttpRequest.Builder requestBuilder, EffectiveConfig cfg, String headerOverride) {
        if (requestBuilder == null || cfg == null || cfg.apiKeyValue == null) {
            return;
        }
        String headerName = headerOverride != null
                ? headerOverride
                : (cfg.apiKeyHeaderName == null ? "Authorization" : cfg.apiKeyHeaderName);
        String token = normalizeApiTokenForHeader(cfg.apiKeyValue);
        if (token.isEmpty()) {
            return;
        }
        String headerValue;
        if ("authorization".equalsIgnoreCase(headerName)) {
            headerValue = "Bearer " + token;
        } else {
            headerValue = token;
        }
        requestBuilder.header(headerName, headerValue);
    }

    private String stripAuthorizationPrefix(String token) {
        String t = trimToNull(token);
        if (t == null) {
            return "";
        }
        if (t.regionMatches(true, 0, "Authorization:", 0, 14)) {
            return t.substring(14).trim();
        }
        return t;
    }

    private String normalizeApiTokenForHeader(String rawToken) {
        String token = stripAuthorizationPrefix(rawToken);
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        token = CONTROL_CHARS.matcher(token).replaceAll("");
        token = token.trim();

        if (token.length() >= 2) {
            char first = token.charAt(0);
            char last = token.charAt(token.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                token = token.substring(1, token.length() - 1).trim();
            }
        }

        return TOKEN_WHITESPACE.matcher(token).replaceAll("");
    }

    private String resolveGlobalServerApiKey() {
        try {
            ServerConfig config = EncryptedDbConfigStore.load();
            if (config == null) {
                return null;
            }
            return trimToNull(config.getApiKey());
        } catch (java.sql.SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to resolve global server API key fallback", e);
            return null;
        }
    }

    private String authHeaderSuffix(String wwwAuthenticate) {
        String t = trimToNull(wwwAuthenticate);
        return t == null ? "" : " [www-authenticate=" + t + ']';
    }

    private String safeUrl(String url) {
        if (url == null) {
            return "";
        }
        try {
            URI uri = URI.create(url);
            StringBuilder sb = new StringBuilder();
            if (uri.getScheme() != null) {
                sb.append(uri.getScheme()).append("://");
            }
            if (uri.getHost() != null) {
                sb.append(uri.getHost());
            } else if (uri.getAuthority() != null) {
                sb.append(uri.getAuthority());
            }
            if (uri.getPort() != -1) {
                sb.append(':').append(uri.getPort());
            }
            if (uri.getPath() != null) {
                sb.append(uri.getPath());
            }
            return sb.toString();
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Unable to parse URL for safe log rendering", e);
            return safeMsg(url);
        }
    }

    private String sourceSuffix(EffectiveConfig cfg) {
        StringBuilder sb = new StringBuilder(" [source=").append(cfg.source).append(']')
                .append(" [enabled=").append(cfg.enabled).append(']')
                .append(" [checkIntervalSeconds=").append(cfg.checkIntervalSeconds).append(']');
        if (cfg.widgetId != null) {
            sb.append(" [widgetId=").append(cfg.widgetId).append(']');
        }
        if (cfg.requestOrigin != null) {
            sb.append(" [origin-set]");
        }
        if (cfg.requestReferer != null) {
            sb.append(" [referer-set]");
        }
        if (cfg.requestUserAgent != null) {
            sb.append(" [ua-set]");
        }
        if (cfg.requestCookie != null) {
            sb.append(" [cookie-set]");
        }
        if (cfg.apiKeyValue != null) {
            sb.append(" [api-key-set]");
            sb.append(" [api-key-header=").append(cfg.apiKeyHeaderName == null ? "Authorization" : cfg.apiKeyHeaderName).append(']');
        }
        return sb.toString();
    }

    private String bodySnippetSuffix(String body) {
        if (body == null) {
            return "";
        }
        String s = body.replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) {
            return "";
        }
        if (s.length() > 300) {
            s = s.substring(0, 300) + "...";
        }
        return new StringBuilder(" [body=\"")
                .append(s)
                .append('"')
                .append(']')
                .toString();
    }

    private String bodySnippet(String body) {
        if (body == null) {
            return "";
        }
        String s = body.replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) {
            return "";
        }
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    private WidgetAvailabilityResult up(String checkedAt, long latencyMs, String details) {
        return new WidgetAvailabilityResult(true, "UP", checkedAt, latencyMs, details);
    }

    private WidgetAvailabilityResult down(String checkedAt, long latencyMs, String details) {
        return new WidgetAvailabilityResult(false, "DOWN", checkedAt, latencyMs, details);
    }

    private WidgetAvailabilityResult disabled(Instant checkedAt, EffectiveConfig cfg) {
        String checkedAtIso = checkedAt == null
                ? DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                : DateTimeFormatter.ISO_INSTANT.format(checkedAt);
        return new WidgetAvailabilityResult(
            true,
                "DISABLED",
                checkedAtIso,
                0L,
                "Widget healthcheck service is disabled by configuration" + sourceSuffix(cfg));
    }

    static final class EffectiveConfig {

        String source;
        boolean enabled;
        int checkIntervalSeconds;
        String url;
        String method;
        int timeoutMs;
        String expectField;
        String expectValue;
        String widgetId;

        // Optional synthetic probe request headers
        String requestOrigin;
        String requestReferer;
        String requestUserAgent;
        String requestCookie;
        String apiKeyHeaderName;
        String apiKeyValue;
    }

    static final class CachedHealthResult {

        final String configFingerprint;
        final Instant checkedAt;
        final WidgetAvailabilityResult result;

        private CachedHealthResult(String configFingerprint, Instant checkedAt, WidgetAvailabilityResult result) {
            this.configFingerprint = configFingerprint;
            this.checkedAt = checkedAt;
            this.result = result;
        }
    }

    static final class SseValidationResult {

        final boolean ok;
        final String reason;

        private SseValidationResult(boolean ok, String reason) {
            this.ok = ok;
            this.reason = reason;
        }

        private static SseValidationResult ok() {
            return new SseValidationResult(true, null);
        }

        private static SseValidationResult fail(String reason) {
            return new SseValidationResult(false, reason);
        }
    }

    public static final class WidgetAvailabilityResult {

        private final boolean available;
        private final String status;
        private final String checkedAtIso;
        private final long latencyMs;
        private final String details;

        public WidgetAvailabilityResult(boolean available, String status, String checkedAtIso, long latencyMs, String details) {
            this.available = available;
            this.status = status;
            this.checkedAtIso = checkedAtIso;
            this.latencyMs = latencyMs;
            this.details = details;
        }

        public boolean available() {
            return available;
        }

        public String status() {
            return status;
        }

        public String checkedAtIso() {
            return checkedAtIso;
        }

        public long latencyMs() {
            return latencyMs;
        }

        public String details() {
            return details;
        }
    }
}
