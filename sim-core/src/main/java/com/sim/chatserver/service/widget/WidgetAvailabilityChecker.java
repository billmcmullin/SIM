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
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.service.widget.WidgetHealthConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.util.TextIoSanitizerUtil;

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

    private static final Logger log = Logger.getLogger(WidgetAvailabilityChecker.class.getCanonicalName());

    private static final String DEFAULT_URL = "http://anythingllm:3001/api/v1/system";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_POST = CommonLiteralConstants.HTTP_METHOD_POST;
    private static final String DEFAULT_METHOD = METHOD_GET;
    private static final int DEFAULT_TIMEOUT_MS = 8000;
    private static final int DEFAULT_CHECK_INTERVAL_SECONDS = 300;
    private static final boolean DEFAULT_HEALTHCHECK_ENABLED = true;
    private static final String DIAG_COMPONENT = "widget-availability-checker";
    private static final String HEADER_AUTHORIZATION = CommonLiteralConstants.HEADER_AUTHORIZATION;
    private static final String HEADER_CONTENT_TYPE = CommonLiteralConstants.HEADER_CONTENT_TYPE;
    private static final String HEADER_CONTENT_TYPE_LOWER = "content-type";
    private static final String HEADER_AUTHORIZATION_LOWER = CommonLiteralConstants.HEADER_AUTHORIZATION_LOWER;
    private static final String HEADER_WWW_AUTHENTICATE_LOWER = "www-authenticate";
    private static final String HEADER_ACCEPT = CommonLiteralConstants.HEADER_ACCEPT;
    private static final String HEADER_X_API_KEY = CommonLiteralConstants.HEADER_X_API_KEY;
    private static final String HEADER_ORIGIN = "Origin";
    private static final String HEADER_REFERER = "Referer";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_COOKIE = "Cookie";
    private static final String CONTENT_TYPE_EVENT_STREAM = "text/event-stream";
    private static final String CONTENT_TYPE_JSON = com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String CONTENT_TYPE_TEXT_PLAIN_UTF8 = "text/plain;charset=UTF-8";
    private static final String AUTHORIZATION_PREFIX = "Authorization:";
    private static final String AUTH_SCHEME_BEARER = com.sim.chatserver.util.CommonLiteralConstants.AUTH_BEARER_PREFIX;
    private static final String SSE_DATA_PREFIX = "data:";
    private static final String EMBED_API_PATH = "/api/embed/";
    private static final String STREAM_CHAT_PATH = "/stream-chat";
    private static final String URL_SCHEME_SEPARATOR = CommonLiteralConstants.SCHEME_SEPARATOR;
    private static final String LOG_KV_METHOD = "method=";
    private static final String LOG_KV_STATUS = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_STATUS;
    private static final String LOG_KV_CONTENT_TYPE_DIAG = "contentType=";
    private static final String LOG_KV_BODY_SNIPPET = "bodySnippet=";
    private static final String LOG_KV_ERROR_REF = "errorRef=";
    private static final String LOG_KV_URL_SPACED = " url=";
    private static final String LOG_KV_ENABLED = " enabled=";
    private static final String EVENT_HEALTHCHECK_ERROR = "healthcheck-error";
    private static final String MSG_DEBUG_SET_PREFIX = " Set ";
    private static final String MSG_DEBUG_DETAIL_SUFFIX = "=true for full debug details.";
    private static final String LOG_KV_URL = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_URL;
    private static final String LOG_KV_LATENCY_MS = "latencyMs=";
    private static final String LOG_KV_TIMEOUT_MS = " timeoutMs=";
    private static final String LOG_KV_CHECK_INTERVAL_SECONDS = " checkIntervalSeconds=";
    private static final String LOG_KV_CONTENT_TYPE = " contentType=";
    private static final String SOURCE_DB = "DB";
    private static final String SOURCE_ENV_DEFAULT = "ENV/DEFAULT";
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = CommonLiteralConstants.STATUS_DOWN;
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String MSG_DISABLED_BY_CONFIGURATION = "Widget healthcheck service is disabled by configuration";
    private static final String MSG_MANUAL_CHECK_WHEN_DISABLED = "Widget healthcheck service is disabled, running one-off manual availability test.";
    private static final String DETAIL_MANUAL_CHECK_WHILE_DISABLED = " [manual-check-while-disabled]";
    private static final String MSG_GENERIC_FAILURE_PREFIX = "Something went wrong during widget healthcheck. To enable debug details set ";
    private static final String MSG_OR_SYSTEM_PROPERTY_PREFIX = " (or system property ";
    private static final String MSG_REFERENCE_PREFIX = " Reference: ";
    private static final String VALUE_TRUE = CommonLiteralConstants.VALUE_TRUE;
    private static final String VALUE_FALSE = CommonLiteralConstants.VALUE_FALSE;
    private static final String VALUE_ONE = "1";
    private static final String VALUE_ZERO = "0";
    private static final String VALUE_YES = CommonLiteralConstants.VALUE_YES;
    private static final String VALUE_NO = "no";
    private static final String VALUE_Y = "y";
    private static final String VALUE_N = "n";
    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    private static final String DEBUG_FAILURES_ENV = "WIDGET_HEALTHCHECK_DEBUG_FAILURES";
    private static final String DEBUG_FAILURES_PROP = "sim.widget.healthcheck.debug.failures";
    private static final String REQUIRE_HTTPS_WITH_AUTH_ENV = "WIDGET_HEALTHCHECK_REQUIRE_HTTPS_WITH_AUTH";
    private static final String REQUIRE_HTTPS_WITH_AUTH_PROP = "sim.widget.healthcheck.require.https.with.auth";
    private static final String ENV_WIDGET_HEALTHCHECK_ENABLED = "WIDGET_HEALTHCHECK_ENABLED";
    private static final String ENV_WIDGET_HEALTHCHECK_INTERVAL_SECONDS = "WIDGET_HEALTHCHECK_INTERVAL_SECONDS";
    private static final String ENV_WIDGET_HEALTHCHECK_URL = "WIDGET_HEALTHCHECK_URL";
    private static final String ENV_WIDGET_HEALTHCHECK_METHOD = "WIDGET_HEALTHCHECK_METHOD";
    private static final String ENV_WIDGET_HEALTHCHECK_TIMEOUT_MS = "WIDGET_HEALTHCHECK_TIMEOUT_MS";
    private static final String ENV_WIDGET_HEALTHCHECK_EXPECT_JSON_FIELD = "WIDGET_HEALTHCHECK_EXPECT_JSON_FIELD";
    private static final String ENV_WIDGET_HEALTHCHECK_EXPECT_JSON_VALUE = "WIDGET_HEALTHCHECK_EXPECT_JSON_VALUE";
    private static final String ENV_WIDGET_HEALTHCHECK_WIDGET_ID = "WIDGET_HEALTHCHECK_WIDGET_ID";
    private static final String ENV_WIDGET_HEALTHCHECK_REQUEST_ORIGIN = "WIDGET_HEALTHCHECK_REQUEST_ORIGIN";
    private static final String ENV_WIDGET_HEALTHCHECK_REQUEST_REFERER = "WIDGET_HEALTHCHECK_REQUEST_REFERER";
    private static final String ENV_WIDGET_HEALTHCHECK_REQUEST_USER_AGENT = "WIDGET_HEALTHCHECK_REQUEST_USER_AGENT";
    private static final String ENV_WIDGET_HEALTHCHECK_REQUEST_COOKIE = "WIDGET_HEALTHCHECK_REQUEST_COOKIE";
    private static final String ENV_WIDGET_HEALTHCHECK_API_KEY_HEADER = "WIDGET_HEALTHCHECK_API_KEY_HEADER";
    private static final String ENV_WIDGET_HEALTHCHECK_API_KEY = "WIDGET_HEALTHCHECK_API_KEY";
    private static final String JSON_KEY_MESSAGE = CommonLiteralConstants.FIELD_MESSAGE;
    private static final String JSON_KEY_SESSION_ID = com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String JSON_KEY_USERNAME = CommonLiteralConstants.KEY_USERNAME;
    private static final String JSON_KEY_PROMPT = CommonLiteralConstants.FIELD_PROMPT;
    private static final String JSON_KEY_MODEL = "model";
    private static final String JSON_KEY_TEMPERATURE = "temperature";
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_ERROR = CommonLiteralConstants.KEY_ERROR;
    private static final String JSON_KEY_CLOSE = "close";
    private static final String JSON_KEY_TEXT_RESPONSE = com.sim.chatserver.util.CommonLiteralConstants.FIELD_TEXT_RESPONSE;
    private static final String JSON_VALUE_TEST = "test";
    private static final String JSON_VALUE_CUSTOMER_EMBEDDED_WIDGET = "Customer Embedded Widget";
    private static final String JSON_VALUE_TEXT_RESPONSE_CHUNK = "textResponseChunk";
    private static final String REGEX_DOUBLE_LINEBREAK = "\\R\\R+";
    private static final String REGEX_LINEBREAK = "\\R";
    private static final String MSG_EMPTY_RESPONSE_BODY = "empty response body";
    private static final String MSG_IGNORE_MALFORMED_SSE_CHUNK = "Ignoring malformed SSE data chunk";
    private static final String MSG_NO_SSE_DATA_EVENTS = "no SSE data events found";
    private static final String MSG_SSE_ERROR_TRUE = "SSE returned error=true";
    private static final String MSG_NO_VALID_TEXT_RESPONSE_CHUNK = "no valid non-empty textResponseChunk found";
    private static final String MSG_NO_CLOSE_TRUE_EVENT = "stream did not include close=true event";
    private static final String SCHEME_HTTPS = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS;
    private static final String MSG_PARSE_HTTPS_URL_FAILURE = "Unable to parse healthcheck URL for HTTPS check";
    private static final String MSG_INIT_HTTP_REQUEST_BUILDER_FAILURE = "Unable to initialize HTTP request builder";
    private static final String PATH_SLASH = "/";
    private static final String VALUE_TRUE_SUFFIX = "=true";
    private static final String VALUE_TRUE_WITH_CLOSE = "=true).";
    private static final String LOG_MSG_CACHE_HIT_PREFIX = "Returning cached widget availability result ageSeconds=";
    private static final String LOG_MSG_CACHE_HIT_INTERVAL = " intervalSeconds=";
    private static final String LOG_MSG_CHECK_START_PREFIX = "Widget availability check starting: method=";
    private static final String LOG_KV_EXPECT_FIELD_SET = " expectFieldSet=";
    private static final String LOG_KV_EXPECT_VALUE_SET = " expectValueSet=";
    private static final String LOG_KV_TIMEOUT_MS_PLAIN = "timeoutMs=";
    private static final String LOG_KV_SOURCE = "source=";
    private static final String EVENT_HEALTHCHECK_REQUEST = "healthcheck-request";
    private static final String LOG_MSG_BLOCKED_HTTPS_REQUIRED = "Widget availability check blocked: sensitive auth material requires HTTPS url=";
    private static final String EVENT_HEALTHCHECK_BLOCKED = "healthcheck-blocked";
    private static final String REASON_SENSITIVE_AUTH_WITHOUT_HTTPS = "reason=sensitive-auth-without-https";
    private static final String DETAIL_SENSITIVE_AUTH_WITHOUT_HTTPS = "Sensitive auth material configured but healthcheck URL is not HTTPS";
    private static final String LOG_MSG_HTTP_ALLOWED_PREFIX = "Widget availability check allowing HTTP URL with sensitive auth material because ";
    private static final String LOG_MSG_HTTP_ALLOWED_URL = " is disabled. url=";
    private static final String LOG_MSG_RETRY_X_API_KEY_PREFIX = "Widget availability retrying with X-API-Key after status=";
    private static final String EVENT_HEALTHCHECK_RETRY = "healthcheck-retry";
    private static final String REASON_AUTH_HEADER_FALLBACK = "reason=auth-header-fallback";
    private static final String LOG_KV_INITIAL_STATUS = "initialStatus=";
    private static final String LOG_KV_FROM_HEADER_AUTHORIZATION = "fromHeader=Authorization";
    private static final String LOG_KV_TO_HEADER = "toHeader=";
    private static final String EVENT_HEALTHCHECK_RESPONSE = "healthcheck-response";
    private static final String LOG_MSG_NON_SUCCESS_STATUS_PREFIX = "Widget availability check returned non-success status=";
    private static final String DETAIL_NON_SUCCESS_HTTP_STATUS = "Non-success HTTP status: ";
    private static final String LOG_MSG_JSON_EXPECTATION_FAILED_FIELD = "Widget availability JSON expectation failed field=";
    private static final String LOG_MSG_JSON_EXPECTED_VALUE = " expectedValue=";
    private static final String DETAIL_JSON_EXPECTATION_FAILED = "JSON expectation failed: ";
    private static final String DETAIL_HEALTHCHECK_SUCCEEDED = "Healthcheck succeeded";
    private static final String LOG_MSG_CHECK_SUCCEEDED_STATUS_PREFIX = "Widget availability check succeeded status=";
    private static final String LOG_MSG_INTERRUPTED_FOR_URL = "Widget availability check interrupted for url=";
    private static final String DETAIL_INTERRUPTED_DURING_HEALTHCHECK = "Interrupted during healthcheck";
    private static final String LOG_MSG_INTERRUPTED_ERROR_REF = "Widget availability check interrupted. errorRef=";
    private static final String LOG_KV_MESSAGE = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_MESSAGE;
    private static final String LOG_MSG_CHECK_ERROR_FOR_URL = "Widget availability check error for url=";
    private static final String DETAIL_EXCEPTION_PREFIX = "Exception: ";
    private static final String DETAIL_EXCEPTION_SEPARATOR = " - ";
    private static final String LOG_MSG_CHECK_FAILED_ERROR_REF = "Widget availability check failed. errorRef=";
    private static final String EVENT_SSE_REQUEST = "sse-request";
    private static final String EVENT_SSE_RESPONSE = "sse-response";
    private static final String LOG_KV_WIDGET_ID = "widgetId=";
    private static final String LOG_KV_PAYLOAD = "payload=";
    private static final String LOG_MSG_SSE_STARTING_PREFIX = "Widget synthetic SSE probe starting: url=";
    private static final String LOG_MSG_SSE_NON_SUCCESS_STATUS_PREFIX = "Widget synthetic SSE probe returned non-success status=";
    private static final String DETAIL_SSE_FAILED_HTTP_PREFIX = "Synthetic SSE probe failed: HTTP ";
    private static final String LOG_MSG_SSE_UNEXPECTED_CONTENT_TYPE = "Widget synthetic SSE probe returned unexpected content-type=";
    private static final String DETAIL_SSE_CONTENT_TYPE_PREFIX = "Synthetic SSE probe failed: content-type is not ";
    private static final String DETAIL_SSE_ACTUAL_PREFIX = " (actual=";
    private static final String LOG_MSG_SSE_VALIDATION_FAILED = "Widget synthetic SSE probe validation failed reason=";
    private static final String DETAIL_SSE_FAILED_PREFIX = "Synthetic SSE probe failed: ";
    private static final String LOG_MSG_SSE_SUCCEEDED_STATUS_PREFIX = "Widget synthetic SSE probe succeeded status=";
    private static final String DETAIL_SSE_SUCCEEDED = "Synthetic SSE probe succeeded";
    private static final String ERR_HEALTHCHECK_URL_EMPTY = "Healthcheck URL is empty";
    private static final String ERR_WIDGET_ID_REQUIRED_POST = "Widget ID is required for POST synthetic check";
    private static final String LOG_MSG_CONFIG_DB_RESOLVED = "Widget availability config resolved from DB: method=";
    private static final String LOG_MSG_DB_CONFIG_UNAVAILABLE = "DB config unavailable, falling back to env/defaults";
    private static final String LOG_MSG_CONFIG_ENV_RESOLVED = "Widget availability config resolved from ENV/DEFAULT: method=";
    private static final String MSG_PARSE_EXPECTED_JSON_FAILED = "Failed to parse expected JSON health payload";
    private static final String MSG_STRINGVAL_FALLBACK_PREFIX = "stringVal fallback for key=";
    private static final String MSG_BOOLVAL_FALLBACK_PREFIX = "boolVal parse fallback for key=";
    private static final String MSG_INVALID_INTEGER_ENV_PREFIX = "Invalid integer environment value for ";
    private static final String MSG_UNABLE_RESOLVE_GLOBAL_API_KEY = "Unable to resolve global server API key fallback";
    private static final String MSG_UNABLE_PARSE_URL_SAFE_LOG = "Unable to parse URL for safe log rendering";
    private static final String SUFFIX_WWW_AUTHENTICATE_PREFIX = " [www-authenticate=";
    private static final String SUFFIX_SOURCE_PREFIX = " [source=";
    private static final String SUFFIX_ENABLED_PREFIX = " [enabled=";
    private static final String SUFFIX_INTERVAL_PREFIX = " [checkIntervalSeconds=";
    private static final String SUFFIX_WIDGET_ID_PREFIX = " [widgetId=";
    private static final String SUFFIX_ORIGIN_SET = " [origin-set]";
    private static final String SUFFIX_REFERER_SET = " [referer-set]";
    private static final String SUFFIX_UA_SET = " [ua-set]";
    private static final String SUFFIX_COOKIE_SET = " [cookie-set]";
    private static final String SUFFIX_API_KEY_SET = " [api-key-set]";
    private static final String SUFFIX_API_KEY_HEADER_PREFIX = " [api-key-header=";
    private static final String SUFFIX_BODY_PREFIX = " [body=\"";
    private static final String ELLIPSIS = CommonLiteralConstants.ELLIPSIS;
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final Map<String, String> ENV = new ProcessBuilder().environment();

    private static final Pattern EMBED_STREAM_PATTERN = Pattern.compile("/api/embed/([^/]+)/stream-chat");
    private static final Pattern CONTROL_CHARS = Pattern.compile(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CONTROL_CHARS);
    private static final Pattern TOKEN_WHITESPACE = Pattern.compile(CommonLiteralConstants.REGEX_WHITESPACE);

    @Inject
    AppDataSourceHolder dsHolder;

    private final Object cacheLock = new Object();
    private volatile CachedHealthResult cachedHealthResult;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
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
            log.info(() -> MSG_MANUAL_CHECK_WHEN_DISABLED);
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
                    safeMsg(live.details()) + DETAIL_MANUAL_CHECK_WHILE_DISABLED);
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

        log.fine(() -> LOG_MSG_CACHE_HIT_PREFIX + ageSeconds
            + LOG_MSG_CACHE_HIT_INTERVAL + cfg.checkIntervalSeconds + sourceSuffix(cfg));
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
        log.info(() -> LOG_MSG_CHECK_START_PREFIX + safeMsg(cfg.method)
            + SPACE + LOG_KV_URL + safeUrl(cfg.url)
            + LOG_KV_TIMEOUT_MS + cfg.timeoutMs
                + LOG_KV_EXPECT_FIELD_SET + (cfg.expectField != null)
                + LOG_KV_EXPECT_VALUE_SET + (cfg.expectValue != null)
            + LOG_KV_CHECK_INTERVAL_SECONDS + cfg.checkIntervalSeconds
                + sourceSuffix(cfg));

        ServerDiagnosticsLog.write(
            DIAG_COMPONENT,
            requestId,
            EVENT_HEALTHCHECK_REQUEST,
            LOG_KV_METHOD + safeMsg(cfg.method)
                + LINE_SEPARATOR + LOG_KV_URL + safeUrl(cfg.url)
                + LINE_SEPARATOR + LOG_KV_TIMEOUT_MS_PLAIN + cfg.timeoutMs
                + LINE_SEPARATOR + LOG_KV_SOURCE + safeMsg(cfg.source)
        );

        boolean sensitiveAuthConfigured = cfg.apiKeyValue != null || cfg.requestCookie != null;
        boolean httpsUrl = isHttpsUrl(cfg.url);
        boolean requireHttpsWithAuth = isHttpsRequiredWithAuth();

        if (sensitiveAuthConfigured && !httpsUrl && requireHttpsWithAuth) {
            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
                log.warning(() -> LOG_MSG_BLOCKED_HTTPS_REQUIRED
                + safeUrl(cfg.url) + sourceSuffix(cfg));
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_HEALTHCHECK_BLOCKED,
                    REASON_SENSITIVE_AUTH_WITHOUT_HTTPS + LINE_SEPARATOR + LOG_KV_URL + safeUrl(cfg.url)
                        + LINE_SEPARATOR + LOG_KV_LATENCY_MS + latencyMs
            );
                return down(checkedAt, latencyMs, DETAIL_SENSITIVE_AUTH_WITHOUT_HTTPS
                + sourceSuffix(cfg));
        }

        if (sensitiveAuthConfigured && !httpsUrl && !requireHttpsWithAuth) {
                log.info(() -> LOG_MSG_HTTP_ALLOWED_PREFIX
                    + REQUIRE_HTTPS_WITH_AUTH_ENV + LOG_MSG_HTTP_ALLOWED_URL + safeUrl(cfg.url)
                    + sourceSuffix(cfg));
        }

        try {
            String effectiveWidgetId = effectiveWidgetId(cfg);
            if (METHOD_POST.equals(cfg.method) && effectiveWidgetId != null) {
                cfg.widgetId = effectiveWidgetId;
                return runSyntheticSseProbe(cfg, checkStart, checkedAt, requestId);
            }

            HttpResponse<String> response = sendHttpHealthRequest(cfg, null);

            if (shouldRetryWithXApiKey(cfg, response.statusCode())) {
                final int initialStatus = response.statusCode();
                log.info(() -> LOG_MSG_RETRY_X_API_KEY_PREFIX + initialStatus
                        + SPACE + LOG_KV_URL + safeUrl(cfg.url) + sourceSuffix(cfg));
                ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                        requestId,
                        EVENT_HEALTHCHECK_RETRY,
                        REASON_AUTH_HEADER_FALLBACK
                    + LINE_SEPARATOR + LOG_KV_INITIAL_STATUS + initialStatus
                        + LINE_SEPARATOR + LOG_KV_FROM_HEADER_AUTHORIZATION
                        + LINE_SEPARATOR + LOG_KV_TO_HEADER + HEADER_X_API_KEY
                        + LINE_SEPARATOR + LOG_KV_URL + safeUrl(cfg.url)
                );
                response = sendHttpHealthRequest(cfg, HEADER_X_API_KEY);
            }

            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
            int status = response.statusCode();
            String body = response.body() == null ? EMPTY : response.body();
            String contentType = headerValue(response, HEADER_CONTENT_TYPE_LOWER);
            String wwwAuthenticate = headerValue(response, HEADER_WWW_AUTHENTICATE_LOWER);

                ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_HEALTHCHECK_RESPONSE,
                    LOG_KV_STATUS + status
                        + LINE_SEPARATOR + LOG_KV_LATENCY_MS + latencyMs
                        + LINE_SEPARATOR + LOG_KV_CONTENT_TYPE_DIAG + safeMsg(contentType)
                        + LINE_SEPARATOR + LOG_KV_URL + safeUrl(cfg.url)
                        + LINE_SEPARATOR + LOG_KV_BODY_SNIPPET + bodySnippet(body)
                );

            if (status < 200 || status >= 300) {
                log.warning(() -> LOG_MSG_NON_SUCCESS_STATUS_PREFIX + status
                    + LOG_KV_CONTENT_TYPE + safeMsg(contentType)
                        + authHeaderSuffix(wwwAuthenticate)
                    + SPACE + LOG_KV_LATENCY_MS + latencyMs
                    + SPACE + LOG_KV_URL + safeUrl(cfg.url)
                        + bodySnippetSuffix(body)
                        + sourceSuffix(cfg));
                return down(checkedAt, latencyMs, DETAIL_NON_SUCCESS_HTTP_STATUS + status
                        + authHeaderSuffix(wwwAuthenticate)
                        + bodySnippetSuffix(body) + sourceSuffix(cfg));
            }

            if (cfg.expectField != null && cfg.expectValue != null) {
                boolean jsonOk = matchesExpectedJson(body, cfg.expectField, cfg.expectValue);
                if (!jsonOk) {
                        log.warning(() -> LOG_MSG_JSON_EXPECTATION_FAILED_FIELD + cfg.expectField
                            + LOG_MSG_JSON_EXPECTED_VALUE + cfg.expectValue
                            + SPACE + LOG_KV_LATENCY_MS + latencyMs
                            + SPACE + LOG_KV_URL + safeUrl(cfg.url)
                            + bodySnippetSuffix(body)
                            + sourceSuffix(cfg));
                        return down(checkedAt, latencyMs, DETAIL_JSON_EXPECTATION_FAILED
                            + cfg.expectField + '=' + cfg.expectValue + sourceSuffix(cfg));
                }
            }

                    log.info(() -> LOG_MSG_CHECK_SUCCEEDED_STATUS_PREFIX + status
                    + LOG_KV_CONTENT_TYPE + safeMsg(contentType)
                    + SPACE + LOG_KV_LATENCY_MS + latencyMs
                    + SPACE + LOG_KV_URL + safeUrl(cfg.url)
                    + sourceSuffix(cfg));
                    return up(checkedAt, latencyMs, DETAIL_HEALTHCHECK_SUCCEEDED + sourceSuffix(cfg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
            String errorRef = UUID.randomUUID().toString();
                ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_HEALTHCHECK_ERROR,
                    LOG_KV_ERROR_REF + errorRef + LINE_SEPARATOR + LOG_KV_LATENCY_MS + latencyMs + LINE_SEPARATOR + LOG_KV_URL + safeUrl(cfg.url),
                    e
                );
            if (isFailureDebugEnabled()) {
                log.log(Level.WARNING, LOG_MSG_INTERRUPTED_FOR_URL + safeUrl(cfg.url)
                        + SPACE + LOG_KV_ERROR_REF + errorRef + sourceSuffix(cfg), e);
                return down(checkedAt, latencyMs, DETAIL_INTERRUPTED_DURING_HEALTHCHECK + sourceSuffix(cfg));
            }

            log.warning(() -> LOG_MSG_INTERRUPTED_ERROR_REF + errorRef
                    + MSG_DEBUG_SET_PREFIX + DEBUG_FAILURES_ENV + MSG_DEBUG_DETAIL_SUFFIX);
            return down(checkedAt, latencyMs, genericFailureDetails(errorRef));
        } catch (IOException | IllegalArgumentException e) {
            long latencyMs = Duration.between(checkStart, Instant.now()).toMillis();
            String errorRef = UUID.randomUUID().toString();
            ServerDiagnosticsLog.write(
                DIAG_COMPONENT,
                requestId,
                EVENT_HEALTHCHECK_ERROR,
                LOG_KV_ERROR_REF + errorRef + LINE_SEPARATOR + LOG_KV_LATENCY_MS + latencyMs + LINE_SEPARATOR + LOG_KV_URL + safeUrl(cfg.url)
                    + LINE_SEPARATOR + LOG_KV_MESSAGE + safeMsg(e.getMessage()),
                e
            );
            if (isFailureDebugEnabled()) {
                log.log(Level.WARNING, LOG_MSG_CHECK_ERROR_FOR_URL + safeUrl(cfg.url)
                    + SPACE + LOG_KV_LATENCY_MS + latencyMs + SPACE + LOG_KV_ERROR_REF + errorRef + sourceSuffix(cfg), e);
                return down(checkedAt, latencyMs, DETAIL_EXCEPTION_PREFIX + e.getClass().getSimpleName()
                        + DETAIL_EXCEPTION_SEPARATOR + safeMsg(e.getMessage()) + sourceSuffix(cfg));
            }

            log.warning(() -> LOG_MSG_CHECK_FAILED_ERROR_REF + errorRef
                    + MSG_DEBUG_SET_PREFIX + DEBUG_FAILURES_ENV + MSG_DEBUG_DETAIL_SUFFIX);
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
        return VALUE_TRUE.equals(normalized)
            || VALUE_ONE.equals(normalized)
            || VALUE_YES.equals(normalized)
            || VALUE_Y.equals(normalized)
            || VALUE_ON.equals(normalized);
    }

    private boolean isFalsy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return VALUE_FALSE.equals(normalized)
            || VALUE_ZERO.equals(normalized)
            || VALUE_NO.equals(normalized)
            || VALUE_N.equals(normalized)
            || VALUE_OFF.equals(normalized);
    }

    private String genericFailureDetails(String errorRef) {
        return MSG_GENERIC_FAILURE_PREFIX
                + DEBUG_FAILURES_ENV + VALUE_TRUE_SUFFIX
            + MSG_OR_SYSTEM_PROPERTY_PREFIX + DEBUG_FAILURES_PROP + VALUE_TRUE_WITH_CLOSE
            + MSG_REFERENCE_PREFIX + errorRef;
    }

    private WidgetAvailabilityResult runSyntheticSseProbe(EffectiveConfig cfg, Instant start, String checkedAt, String requestId) throws IOException, InterruptedException {
        String probeUrl = buildEmbedStreamUrl(cfg.url, cfg.widgetId);
        String payload = buildSyntheticPayload();
        log.info(() -> LOG_MSG_SSE_STARTING_PREFIX + safeUrl(probeUrl)
            + LOG_KV_TIMEOUT_MS + cfg.timeoutMs
            + sourceSuffix(cfg));

        ServerDiagnosticsLog.write(
            DIAG_COMPONENT,
                requestId,
                EVENT_SSE_REQUEST,
            LOG_KV_METHOD + METHOD_POST + LINE_SEPARATOR + LOG_KV_URL + safeUrl(probeUrl)
                + LINE_SEPARATOR + LOG_KV_WIDGET_ID + safeMsg(cfg.widgetId)
                + LINE_SEPARATOR + LOG_KV_PAYLOAD + payload
        );

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(probeUrl))
                .timeout(Duration.ofMillis(cfg.timeoutMs))
            .header(HEADER_ACCEPT, CONTENT_TYPE_EVENT_STREAM)
            .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_TEXT_PLAIN_UTF8);

        applyApiKeyHeader(req, cfg);

        if (cfg.requestOrigin != null) {
            req.header(HEADER_ORIGIN, cfg.requestOrigin);
        }

        if (cfg.requestReferer != null) {
            req.header(HEADER_REFERER, cfg.requestReferer);
        }

        if (cfg.requestUserAgent != null) {
            req.header(HEADER_USER_AGENT, cfg.requestUserAgent);
        }

        if (cfg.requestCookie != null) {
            req.header(HEADER_COOKIE, cfg.requestCookie);
        }

        HttpRequest request = req
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        long latencyMs = Duration.between(start, Instant.now()).toMillis();
        int status = response.statusCode();
        String body = response.body() == null ? EMPTY : response.body();
        String contentType = headerValue(response, HEADER_CONTENT_TYPE_LOWER);
        String wwwAuthenticate = headerValue(response, HEADER_WWW_AUTHENTICATE_LOWER);

        ServerDiagnosticsLog.write(
            DIAG_COMPONENT,
            requestId,
            EVENT_SSE_RESPONSE,
            LOG_KV_STATUS + status
                + LINE_SEPARATOR + LOG_KV_LATENCY_MS + latencyMs
                + LINE_SEPARATOR + LOG_KV_CONTENT_TYPE_DIAG + safeMsg(contentType)
                + LINE_SEPARATOR + LOG_KV_URL + safeUrl(probeUrl)
                + LINE_SEPARATOR + LOG_KV_BODY_SNIPPET + bodySnippet(body)
        );

        if (status < 200 || status >= 300) {
            log.warning(() -> LOG_MSG_SSE_NON_SUCCESS_STATUS_PREFIX + status
                + LOG_KV_CONTENT_TYPE + safeMsg(contentType)
                + authHeaderSuffix(wwwAuthenticate)
                + SPACE + LOG_KV_LATENCY_MS + latencyMs
                + SPACE + LOG_KV_URL + safeUrl(probeUrl)
                + bodySnippetSuffix(body)
                + sourceSuffix(cfg));
            return down(checkedAt, latencyMs, DETAIL_SSE_FAILED_HTTP_PREFIX + status
                + authHeaderSuffix(wwwAuthenticate)
                    + bodySnippetSuffix(body) + sourceSuffix(cfg));
        }

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains(CONTENT_TYPE_EVENT_STREAM)) {
            log.warning(() -> LOG_MSG_SSE_UNEXPECTED_CONTENT_TYPE + safeMsg(contentType)
                + SPACE + LOG_KV_LATENCY_MS + latencyMs
                + SPACE + LOG_KV_URL + safeUrl(probeUrl)
                + bodySnippetSuffix(body)
                + sourceSuffix(cfg));
            return down(checkedAt, latencyMs, DETAIL_SSE_CONTENT_TYPE_PREFIX + CONTENT_TYPE_EVENT_STREAM
                    + DETAIL_SSE_ACTUAL_PREFIX + safeMsg(contentType) + ')' + sourceSuffix(cfg));
        }

        String canonicalBody = canonicalizeBodyForValidation(body, 100_000);
        SseValidationResult sse = validateSseBody(canonicalBody);
        if (!sse.ok) {
            log.warning(() -> LOG_MSG_SSE_VALIDATION_FAILED + safeMsg(sse.reason)
                + SPACE + LOG_KV_LATENCY_MS + latencyMs
                + SPACE + LOG_KV_URL + safeUrl(probeUrl)
                + bodySnippetSuffix(canonicalBody)
                + sourceSuffix(cfg));
            return down(checkedAt, latencyMs, DETAIL_SSE_FAILED_PREFIX + sse.reason + sourceSuffix(cfg));
        }

        log.info(() -> LOG_MSG_SSE_SUCCEEDED_STATUS_PREFIX + status
            + LOG_KV_CONTENT_TYPE + safeMsg(contentType)
            + SPACE + LOG_KV_LATENCY_MS + latencyMs
            + SPACE + LOG_KV_URL + safeUrl(probeUrl)
            + sourceSuffix(cfg));
        return up(checkedAt, latencyMs, DETAIL_SSE_SUCCEEDED + sourceSuffix(cfg));
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
        String base = configuredUrl == null ? EMPTY : configuredUrl.trim();
        String wid = widgetId == null ? EMPTY : widgetId.trim();

        if (base.isEmpty()) {
            throw new IllegalArgumentException(ERR_HEALTHCHECK_URL_EMPTY);
        }
        if (wid.isEmpty()) {
            throw new IllegalArgumentException(ERR_WIDGET_ID_REQUIRED_POST);
        }

        String normalizedBase = stripQueryAndFragment(base);

        if (normalizedBase.contains(EMBED_API_PATH) && normalizedBase.endsWith(STREAM_CHAT_PATH)) {
            return normalizedBase;
        }

        URI u = URI.create(normalizedBase);
        String origin = u.getScheme() + URL_SCHEME_SEPARATOR + u.getAuthority();

        return origin + EMBED_API_PATH + URLEncoder.encode(wid, StandardCharsets.UTF_8) + STREAM_CHAT_PATH;
    }

    private String stripQueryAndFragment(String url) {
        URI u = URI.create(url);
        URI clean = URI.create(u.getScheme() + URL_SCHEME_SEPARATOR + u.getAuthority()
                + (u.getPath() == null ? EMPTY : u.getPath()));
        String s = clean.toString();
        while (s.endsWith(PATH_SLASH)) {
            s = TextIoSanitizerUtil.safeSlice(s, 0, s.length() - 1);
        }
        return s;
    }

    private String buildSyntheticPayload() {
        JsonObject payload = Json.createObjectBuilder()
                .add(JSON_KEY_MESSAGE, JSON_VALUE_TEST)
                .add(JSON_KEY_SESSION_ID, UUID.randomUUID().toString())
                .add(JSON_KEY_USERNAME, JSON_VALUE_CUSTOMER_EMBEDDED_WIDGET)
                .addNull(JSON_KEY_PROMPT)
                .addNull(JSON_KEY_MODEL)
                .addNull(JSON_KEY_TEMPERATURE)
                .build();
        return payload.toString();
    }

    private SseValidationResult validateSseBody(String body) {
        if (body == null || body.isBlank()) {
            return SseValidationResult.fail(MSG_EMPTY_RESPONSE_BODY);
        }

        String[] events = body.split(REGEX_DOUBLE_LINEBREAK);
        boolean sawDataEvent = false;
        boolean sawGoodChunk = false;
        boolean sawClose = false;
        boolean sawErrorTrue = false;

        for (String event : events) {
            if (event == null || event.isBlank()) {
                continue;
            }

            String[] lines = event.split(REGEX_LINEBREAK);
            for (String line : lines) {
                String trimmed = line == null ? EMPTY : line.trim();
                if (!trimmed.startsWith(SSE_DATA_PREFIX)) {
                    continue;
                }

                String jsonPart = TextIoSanitizerUtil.safeSlice(trimmed, SSE_DATA_PREFIX.length(), trimmed.length()).trim();
                if (jsonPart.isEmpty()) {
                    continue;
                }

                sawDataEvent = true;
                try (JsonReader reader = Json.createReader(new StringReader(jsonPart))) {
                    JsonObject obj = reader.readObject();

                    String type = stringVal(obj, JSON_KEY_TYPE);
                    boolean error = boolVal(obj, JSON_KEY_ERROR, false);
                    boolean close = boolVal(obj, JSON_KEY_CLOSE, false);
                    String text = stringVal(obj, JSON_KEY_TEXT_RESPONSE);

                    if (error) {
                        sawErrorTrue = true;
                    }
                    if (close) {
                        sawClose = true;
                    }

                    if (JSON_VALUE_TEXT_RESPONSE_CHUNK.equals(type) && !error && text != null && !text.trim().isEmpty()) {
                        sawGoodChunk = true;
                    }
                } catch (JsonException | ClassCastException | IllegalStateException ex) {
                    log.log(Level.FINE, MSG_IGNORE_MALFORMED_SSE_CHUNK, ex);
                }
            }
        }

        if (!sawDataEvent) {
            return SseValidationResult.fail(MSG_NO_SSE_DATA_EVENTS);
        }
        if (sawErrorTrue) {
            return SseValidationResult.fail(MSG_SSE_ERROR_TRUE);
        }
        if (!sawGoodChunk) {
            return SseValidationResult.fail(MSG_NO_VALID_TEXT_RESPONSE_CHUNK);
        }
        if (!sawClose) {
            return SseValidationResult.fail(MSG_NO_CLOSE_TRUE_EVENT);
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
                    cfg.source = SOURCE_DB;
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
                        cfg.apiKeyHeaderName = HEADER_AUTHORIZATION;
                    }

                        log.info(() -> LOG_MSG_CONFIG_DB_RESOLVED + safeMsg(cfg.method)
                            + LOG_KV_URL_SPACED + safeUrl(cfg.url)
                            + LOG_KV_TIMEOUT_MS + cfg.timeoutMs
                            + LOG_KV_ENABLED + cfg.enabled
                            + LOG_KV_CHECK_INTERVAL_SECONDS + cfg.checkIntervalSeconds
                            + sourceSuffix(cfg));
                    return cfg;
                }
            }
        } catch (java.sql.SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, LOG_MSG_DB_CONFIG_UNAVAILABLE, e);
        }

        EffectiveConfig envCfg = new EffectiveConfig();
        envCfg.source = SOURCE_ENV_DEFAULT;
        envCfg.enabled = parseBooleanEnv(ENV_WIDGET_HEALTHCHECK_ENABLED, DEFAULT_HEALTHCHECK_ENABLED);
        envCfg.checkIntervalSeconds = normalizeCheckIntervalSeconds(
            parseIntEnv(ENV_WIDGET_HEALTHCHECK_INTERVAL_SECONDS, DEFAULT_CHECK_INTERVAL_SECONDS));
        envCfg.url = env(ENV_WIDGET_HEALTHCHECK_URL, DEFAULT_URL);
        envCfg.method = normalizeMethod(env(ENV_WIDGET_HEALTHCHECK_METHOD, DEFAULT_METHOD));
        envCfg.timeoutMs = normalizeTimeout(parseIntEnv(ENV_WIDGET_HEALTHCHECK_TIMEOUT_MS, DEFAULT_TIMEOUT_MS));
        envCfg.expectField = trimToNull(env(ENV_WIDGET_HEALTHCHECK_EXPECT_JSON_FIELD, EMPTY));
        envCfg.expectValue = trimToNull(env(ENV_WIDGET_HEALTHCHECK_EXPECT_JSON_VALUE, EMPTY));
        envCfg.widgetId = trimToNull(env(ENV_WIDGET_HEALTHCHECK_WIDGET_ID, EMPTY));

        // Optional env fallbacks for synthetic probe headers
        envCfg.requestOrigin = trimToNull(env(ENV_WIDGET_HEALTHCHECK_REQUEST_ORIGIN, EMPTY));
        envCfg.requestReferer = trimToNull(env(ENV_WIDGET_HEALTHCHECK_REQUEST_REFERER, EMPTY));
        envCfg.requestUserAgent = trimToNull(env(ENV_WIDGET_HEALTHCHECK_REQUEST_USER_AGENT, EMPTY));
        envCfg.requestCookie = trimToNull(env(ENV_WIDGET_HEALTHCHECK_REQUEST_COOKIE, EMPTY));
        envCfg.apiKeyHeaderName = trimToNull(env(ENV_WIDGET_HEALTHCHECK_API_KEY_HEADER, HEADER_AUTHORIZATION));
        envCfg.apiKeyValue = trimToNull(env(ENV_WIDGET_HEALTHCHECK_API_KEY, EMPTY));
        if (envCfg.apiKeyValue == null) {
            envCfg.apiKeyValue = resolveGlobalServerApiKey();
        }
        if (envCfg.apiKeyValue != null && envCfg.apiKeyHeaderName == null) {
            envCfg.apiKeyHeaderName = HEADER_AUTHORIZATION;
        }

        log.info(() -> LOG_MSG_CONFIG_ENV_RESOLVED + safeMsg(envCfg.method)
            + LOG_KV_URL_SPACED + safeUrl(envCfg.url)
            + LOG_KV_TIMEOUT_MS + envCfg.timeoutMs
            + LOG_KV_ENABLED + envCfg.enabled
            + LOG_KV_CHECK_INTERVAL_SECONDS + envCfg.checkIntervalSeconds
            + sourceSuffix(envCfg));
        return envCfg;
    }

    private String normalizeMethod(String method) {
        String m = method == null ? DEFAULT_METHOD : method.trim().toUpperCase(Locale.ROOT);
        if (!METHOD_GET.equals(m) && !METHOD_HEAD.equals(m) && !METHOD_POST.equals(m)) {
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
        String canonicalBody = canonicalizeInput(body == null ? EMPTY : body, 100_000);
        try (JsonReader reader = Json.createReader(new StringReader(canonicalBody))) {
            JsonObject obj = reader.readObject();
            if (!obj.containsKey(expectedField) || obj.isNull(expectedField)) {
                return false;
            }
            String actual = obj.get(expectedField).toString();
            if (actual.length() >= 2 && actual.charAt(0) == '"' && actual.charAt(actual.length() - 1) == '"') {
                actual = TextIoSanitizerUtil.safeSlice(actual, 1, actual.length() - 1);
            }
            return expectedValue.equalsIgnoreCase(actual.trim());
        } catch (JsonException | ClassCastException | IllegalStateException e) {
            log.log(Level.FINE, MSG_PARSE_EXPECTED_JSON_FAILED, e);
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
            log.log(Level.FINE, MSG_STRINGVAL_FALLBACK_PREFIX + key, e);
            String v = obj.get(key).toString();
            if (v != null && v.length() >= 2 && v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
                v = TextIoSanitizerUtil.safeSlice(v, 1, v.length() - 1);
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
            log.log(Level.FINE, MSG_BOOLVAL_FALLBACK_PREFIX + key, e);
            return Boolean.parseBoolean(obj.get(key).toString().replace(String.valueOf('"'), EMPTY).trim());
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
            log.log(Level.FINE, MSG_INVALID_INTEGER_ENV_PREFIX + key, e);
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
        String normalized = Normalizer.normalize(value == null ? EMPTY : value, Normalizer.Form.NFKC).trim();
        if (normalized.isBlank()) {
            return EMPTY;
        }
        if (normalized.length() > maxChars || CONTROL_CHARS.matcher(normalized).find()) {
            return EMPTY;
        }
        return normalized;
    }

    private String canonicalizeBodyForValidation(String body, int maxChars) {
        String normalized = Normalizer.normalize(body == null ? EMPTY : body, Normalizer.Form.NFKC);
        String withoutNul = normalized.replace('\u0000', ' ');
        if (withoutNul.length() > maxChars) {
            return TextIoSanitizerUtil.safeSlice(withoutNul, 0, maxChars);
        }
        return withoutNul;
    }

    private String readEnvCanonical(String key, int maxChars) {
        String raw = ENV.get(key);
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
            return SCHEME_HTTPS.equalsIgnoreCase(uri.getScheme());
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, MSG_PARSE_HTTPS_URL_FAILURE, e);
            return false;
        }
    }

    private HttpResponse<String> sendHttpHealthRequest(EffectiveConfig cfg, String apiKeyHeaderOverride)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(cfg.url))
                .timeout(Duration.ofMillis(cfg.timeoutMs));

        if (builder == null) {
            throw new IllegalStateException(MSG_INIT_HTTP_REQUEST_BUILDER_FAILURE);
        }

        applyApiKeyHeader(builder, cfg, apiKeyHeaderOverride);

        if (METHOD_HEAD.equals(cfg.method)) {
            builder.method(METHOD_HEAD, HttpRequest.BodyPublishers.noBody());
        } else if (METHOD_POST.equals(cfg.method)) {
            builder.POST(HttpRequest.BodyPublishers.noBody());
            builder.header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
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
        if (configuredHeader != null && !HEADER_AUTHORIZATION_LOWER.equalsIgnoreCase(configuredHeader)) {
            return false;
        }
        return statusCode == 400 || statusCode == 401 || statusCode == 403;
    }

    private String safeMsg(String msg) {
        return msg == null ? EMPTY : msg;
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
                : (cfg.apiKeyHeaderName == null ? HEADER_AUTHORIZATION : cfg.apiKeyHeaderName);
        String token = normalizeApiTokenForHeader(cfg.apiKeyValue);
        if (token.isEmpty()) {
            return;
        }
        String headerValue;
        if (HEADER_AUTHORIZATION_LOWER.equalsIgnoreCase(headerName)) {
            headerValue = AUTH_SCHEME_BEARER + token;
        } else {
            headerValue = token;
        }
        requestBuilder.header(headerName, headerValue);
    }

    private String stripAuthorizationPrefix(String token) {
        String t = trimToNull(token);
        if (t == null) {
            return EMPTY;
        }
        if (t.regionMatches(true, 0, AUTHORIZATION_PREFIX, 0, AUTHORIZATION_PREFIX.length())) {
            return TextIoSanitizerUtil.safeSlice(t, AUTHORIZATION_PREFIX.length(), t.length()).trim();
        }
        return t;
    }

    private String normalizeApiTokenForHeader(String rawToken) {
        String token = stripAuthorizationPrefix(rawToken);
        if (token.regionMatches(true, 0, AUTH_SCHEME_BEARER, 0, AUTH_SCHEME_BEARER.length())) {
            token = TextIoSanitizerUtil.safeSlice(token, AUTH_SCHEME_BEARER.length(), token.length()).trim();
        }

        token = CONTROL_CHARS.matcher(token).replaceAll(EMPTY);
        token = token.trim();

        if (token.length() >= 2) {
            char first = token.charAt(0);
            char last = token.charAt(token.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                token = TextIoSanitizerUtil.safeSlice(token, 1, token.length() - 1).trim();
            }
        }

        return TOKEN_WHITESPACE.matcher(token).replaceAll(EMPTY);
    }

    private String resolveGlobalServerApiKey() {
        try {
            ServerConfig config = EncryptedDbConfigStore.load();
            if (config == null) {
                return null;
            }
            return trimToNull(config.getApiKey());
        } catch (java.sql.SQLException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.FINE, MSG_UNABLE_RESOLVE_GLOBAL_API_KEY, e);
            return null;
        }
    }

    private String authHeaderSuffix(String wwwAuthenticate) {
        String t = trimToNull(wwwAuthenticate);
        return t == null ? EMPTY : SUFFIX_WWW_AUTHENTICATE_PREFIX + t + ']';
    }

    private String safeUrl(String url) {
        if (url == null) {
            return EMPTY;
        }
        try {
            URI uri = URI.create(url);
            StringBuilder sb = new StringBuilder();
            if (uri.getScheme() != null) {
                sb.append(uri.getScheme()).append(URL_SCHEME_SEPARATOR);
            }
            if (uri.getHost() != null) {
                sb.append(uri.getHost());
            } else if (uri.getAuthority() != null) {
                sb.append(uri.getAuthority());
            }
            if (uri.getPort() != -1) {
                sb.append((char) 58).append(uri.getPort());
            }
            if (uri.getPath() != null) {
                sb.append(uri.getPath());
            }
            return sb.toString();
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, MSG_UNABLE_PARSE_URL_SAFE_LOG, e);
            return safeMsg(url);
        }
    }

    private String sourceSuffix(EffectiveConfig cfg) {
        StringBuilder sb = new StringBuilder(SUFFIX_SOURCE_PREFIX).append(cfg.source).append(']')
                .append(SUFFIX_ENABLED_PREFIX).append(cfg.enabled).append(']')
                .append(SUFFIX_INTERVAL_PREFIX).append(cfg.checkIntervalSeconds).append(']');
        if (cfg.widgetId != null) {
            sb.append(SUFFIX_WIDGET_ID_PREFIX).append(cfg.widgetId).append(']');
        }
        if (cfg.requestOrigin != null) {
            sb.append(SUFFIX_ORIGIN_SET);
        }
        if (cfg.requestReferer != null) {
            sb.append(SUFFIX_REFERER_SET);
        }
        if (cfg.requestUserAgent != null) {
            sb.append(SUFFIX_UA_SET);
        }
        if (cfg.requestCookie != null) {
            sb.append(SUFFIX_COOKIE_SET);
        }
        if (cfg.apiKeyValue != null) {
            sb.append(SUFFIX_API_KEY_SET);
            sb.append(SUFFIX_API_KEY_HEADER_PREFIX).append(cfg.apiKeyHeaderName == null ? HEADER_AUTHORIZATION : cfg.apiKeyHeaderName).append(']');
        }
        return sb.toString();
    }

    private String bodySnippetSuffix(String body) {
        if (body == null) {
            return EMPTY;
        }
        String s = TOKEN_WHITESPACE.matcher(body).replaceAll(SPACE).trim();
        if (s.isEmpty()) {
            return EMPTY;
        }
        if (s.length() > 300) {
            s = TextIoSanitizerUtil.safeSlice(s, 0, 300) + ELLIPSIS;
        }
        return new StringBuilder(SUFFIX_BODY_PREFIX)
                .append(s)
                .append('"')
                .append(']')
                .toString();
    }

    private String bodySnippet(String body) {
        if (body == null) {
            return EMPTY;
        }
        String s = TOKEN_WHITESPACE.matcher(body).replaceAll(SPACE).trim();
        if (s.isEmpty()) {
            return EMPTY;
        }
        return s.length() > 300 ? TextIoSanitizerUtil.safeSlice(s, 0, 300) + ELLIPSIS : s;
    }

    private WidgetAvailabilityResult up(String checkedAt, long latencyMs, String details) {
        return new WidgetAvailabilityResult(true, STATUS_UP, checkedAt, latencyMs, details);
    }

    private WidgetAvailabilityResult down(String checkedAt, long latencyMs, String details) {
        return new WidgetAvailabilityResult(false, STATUS_DOWN, checkedAt, latencyMs, details);
    }

    private WidgetAvailabilityResult disabled(Instant checkedAt, EffectiveConfig cfg) {
        String checkedAtIso = checkedAt == null
                ? DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                : DateTimeFormatter.ISO_INSTANT.format(checkedAt);
        return new WidgetAvailabilityResult(
            true,
                STATUS_DISABLED,
                checkedAtIso,
                0L,
                MSG_DISABLED_BY_CONFIGURATION + sourceSuffix(cfg));
    }
}



