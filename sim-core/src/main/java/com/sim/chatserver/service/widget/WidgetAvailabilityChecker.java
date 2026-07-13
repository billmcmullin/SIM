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

import com.sim.chatserver.service.widget.WidgetHealthConfigStore.WidgetHealthConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
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

    private static final String DEFAULT_URL = "http://anythingllm:3001/api/health";
    private static final String DEFAULT_METHOD = "GET";
    private static final int DEFAULT_TIMEOUT_MS = 8000;

    private static final Pattern EMBED_STREAM_PATTERN = Pattern.compile("/api/embed/([^/]+)/stream-chat");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\u0000-\\u001F\\u007F]");

    @Inject
    AppDataSourceHolder dsHolder;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public WidgetAvailabilityResult checkNow() {
        Instant start = Instant.now();
        String checkedAt = DateTimeFormatter.ISO_INSTANT.format(start);

        EffectiveConfig cfg = resolveEffectiveConfig();

        try {
            String effectiveWidgetId = effectiveWidgetId(cfg);
            if ("POST".equals(cfg.method) && effectiveWidgetId != null) {
                cfg.widgetId = effectiveWidgetId;
                return runSyntheticSseProbe(cfg, start, checkedAt);
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(cfg.url))
                    .timeout(Duration.ofMillis(cfg.timeoutMs));

            switch (cfg.method) {
                case "HEAD" -> builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                case "POST" -> {
                    builder.POST(HttpRequest.BodyPublishers.noBody());
                    builder.header("Content-Type", "application/json");
                }
                default -> builder.GET();
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            int status = response.statusCode();
            String body = response.body() == null ? "" : response.body();

            if (status < 200 || status >= 300) {
                return down(checkedAt, latencyMs, "Non-success HTTP status: " + status
                        + bodySnippetSuffix(body) + sourceSuffix(cfg));
            }

            if (cfg.expectField != null && cfg.expectValue != null) {
                boolean jsonOk = matchesExpectedJson(body, cfg.expectField, cfg.expectValue);
                if (!jsonOk) {
                    return down(checkedAt, latencyMs, "JSON expectation failed: "
                            + cfg.expectField + "=" + cfg.expectValue + sourceSuffix(cfg));
                }
            }

            return up(checkedAt, latencyMs, "Healthcheck succeeded" + sourceSuffix(cfg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            log.log(Level.FINE, "Widget availability check interrupted", e);
            return down(checkedAt, latencyMs, "Interrupted during healthcheck" + sourceSuffix(cfg));
        } catch (IOException | IllegalArgumentException e) {
            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            log.log(Level.FINE, "Widget availability check error", e);
            return down(checkedAt, latencyMs, "Exception: " + e.getClass().getSimpleName()
                    + " - " + safeMsg(e.getMessage()) + sourceSuffix(cfg));
        }
    }

    private WidgetAvailabilityResult runSyntheticSseProbe(EffectiveConfig cfg, Instant start, String checkedAt) throws IOException, InterruptedException {
        String probeUrl = buildEmbedStreamUrl(cfg.url, cfg.widgetId);
        String payload = buildSyntheticPayload();

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(probeUrl))
                .timeout(Duration.ofMillis(cfg.timeoutMs))
                .header("Accept", "text/event-stream")
                .header("Content-Type", "text/plain;charset=UTF-8");

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

        if (status < 200 || status >= 300) {
            return down(checkedAt, latencyMs, "Synthetic SSE probe failed: HTTP " + status
                    + bodySnippetSuffix(body) + sourceSuffix(cfg));
        }

        String contentType = headerValue(response, "content-type");
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("text/event-stream")) {
            return down(checkedAt, latencyMs, "Synthetic SSE probe failed: content-type is not text/event-stream"
                    + " (actual=" + safeMsg(contentType) + ")" + sourceSuffix(cfg));
        }

        SseValidationResult sse = validateSseBody(body);
        if (!sse.ok) {
            return down(checkedAt, latencyMs, "Synthetic SSE probe failed: " + sse.reason + sourceSuffix(cfg));
        }

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
                } catch (RuntimeException ex) {
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

                    return cfg;
                }
            }
        } catch (java.sql.SQLException | RuntimeException e) {
            log.log(Level.FINE, "DB config unavailable, falling back to env/defaults", e);
        }

        EffectiveConfig envCfg = new EffectiveConfig();
        envCfg.source = "ENV/DEFAULT";
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

    private boolean matchesExpectedJson(String body, String expectedField, String expectedValue) {
        String canonicalBody = canonicalizeInput(body == null ? "" : body, 100_000);
        try (JsonReader reader = Json.createReader(new StringReader(canonicalBody))) {
            JsonObject obj = reader.readObject();
            if (!obj.containsKey(expectedField) || obj.isNull(expectedField)) {
                return false;
            }
            String actual = obj.get(expectedField).toString();
            if (actual.startsWith("\"") && actual.endsWith("\"") && actual.length() >= 2) {
                actual = actual.substring(1, actual.length() - 1);
            }
            return expectedValue.equalsIgnoreCase(actual.trim());
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
            log.log(Level.FINE, "stringVal fallback for key=" + key, e);
            String v = obj.get(key).toString();
            if (v != null && v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
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
        } catch (RuntimeException e) {
            log.log(Level.FINE, "boolVal parse fallback for key=" + key, e);
            try {
                return Boolean.parseBoolean(obj.get(key).toString().replace("\"", "").trim());
            } catch (RuntimeException ex) {
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

    private String safeMsg(String msg) {
        return msg == null ? "" : msg;
    }

    private String sourceSuffix(EffectiveConfig cfg) {
        StringBuilder sb = new StringBuilder(" [source=").append(cfg.source).append("]");
        if (cfg.widgetId != null) {
            sb.append(" [widgetId=").append(cfg.widgetId).append("]");
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
        return " [body=\"" + s + "\"]";
    }

    private WidgetAvailabilityResult up(String checkedAt, long latencyMs, String details) {
        return new WidgetAvailabilityResult(true, "UP", checkedAt, latencyMs, details);
    }

    private WidgetAvailabilityResult down(String checkedAt, long latencyMs, String details) {
        return new WidgetAvailabilityResult(false, "DOWN", checkedAt, latencyMs, details);
    }

    private static final class EffectiveConfig {

        String source;
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
    }

    private static final class SseValidationResult {

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
