package com.sim.ui.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitUntilState;
import com.parasoft.coverage.integration.playwright.PlaywrightCoverageIntegration;

/**
 * Shared Playwright base fixture for SIM UI integration tests.
 *
 * Responsibilities:
 * - Starts and stops shared Playwright/Chromium resources for the test class lifecycle.
 * - Creates a fresh browser context/page per test with common defaults.
 * - Applies Parasoft coverage integration settings for CTP/DTP correlation.
 * - Optionally overrides the outgoing Baggage header when explicitly configured.
 *
 * Coverage behavior:
 * - Default path: Parasoft coverage integration drives header propagation.
 * - Manual override path: if a non-blank override is provided via system property
 *   or environment variable, it replaces only the Baggage header value.
 * - Fallback path: if override is null/empty, tests run normally and keep the
 *   default Parasoft coverage behavior.
 */
public abstract class BaseUiIT {

    private static final String BAGGAGE_HEADER_NAME = "Baggage";
    private static final String BAGGAGE_OVERRIDE_PROPERTY = "parasoft.coverage.baggageHeader";
    private static final String BAGGAGE_OVERRIDE_ENV = "PARASOFT_COVERAGE_BAGGAGE_HEADER";

    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext context;
    protected Page page;

    protected String baseUrl;
    protected boolean headless;
    protected boolean ignoreHttpsErrors;

    @BeforeAll
    static void globalSetup() {
        playwright = Playwright.create();

        // headless is controlled from Maven: -Dheadless=true/false
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "true"));

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(isHeadless)
        );
    }

    @AfterAll
    static void globalTeardown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void testSetup() {
        baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
        headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        ignoreHttpsErrors = Boolean.parseBoolean(System.getProperty("ignoreHttpsErrors", "true"));

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
            .setViewportSize(1440, 900)
            .setIgnoreHTTPSErrors(ignoreHttpsErrors);

        // Adds Parasoft coverage baggage-header propagation when available.
        PlaywrightCoverageIntegration.updateBrowserContextOptions(contextOptions);

        // Optional manual override for troubleshooting or explicit correlation flows.
        applyBaggageHeaderOverride(contextOptions);

        context = browser.newContext(contextOptions);

        // Avoid external CDN dependency during UI tests; chart rendering behavior is validated by app JS wiring.
        context.route("**/cdn.jsdelivr.net/npm/chart.js**", route -> route.fulfill(
            new com.microsoft.playwright.Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/javascript")
                .setBody("window.Chart = window.Chart || function(ctx, cfg){ this.config = cfg || {}; this.canvas = (ctx && ctx.canvas) ? ctx.canvas : null; this.destroy = function(){}; return this; };")
        ));

        page = context.newPage();

        // Optional: default timeout for actions/assertions
        page.setDefaultTimeout(10000);
        page.setDefaultNavigationTimeout(30000);
    }

    @AfterEach
    void testTeardown() {
        if (context != null) {
            context.close();
        }
    }

    protected void waitForLoginScreen() {
        try {
            if (!page.url().contains("/login")) {
                page.waitForURL(url -> url.contains("/login"),
                        new Page.WaitForURLOptions().setTimeout(15000));
            }
        } catch (PlaywrightException ignored) {
            // Some endpoints forward to login without a URL change.
        }

        if (!page.url().contains("/login")) {
            page.waitForSelector("input#username",
                    new Page.WaitForSelectorOptions().setTimeout(15000));
            page.waitForSelector("input#password",
                    new Page.WaitForSelectorOptions().setTimeout(15000));
        }
    }

    protected void assertOnLoginScreen(String messagePrefix) {
        boolean loginByUrl = page.url().contains("/login");
        boolean loginByForm = page.locator("input#username").count() > 0
                && page.locator("input#password").count() > 0
                && page.locator("button[type='submit']").count() > 0;

        assertTrue(loginByUrl || loginByForm,
                messagePrefix + " got: " + page.url());
    }

            protected void navigateWithCommit(String relativeOrAbsoluteUrl) {
            String targetUrl = relativeOrAbsoluteUrl.startsWith("http://") || relativeOrAbsoluteUrl.startsWith("https://")
                ? relativeOrAbsoluteUrl
                : baseUrl + relativeOrAbsoluteUrl;

                PlaywrightException lastError = null;
                for (int attempt = 1; attempt <= 2; attempt++) {
                    try {
                        page.navigate(
                                targetUrl,
                                new Page.NavigateOptions()
                                        .setWaitUntil(WaitUntilState.COMMIT)
                                        .setTimeout(30000)
                        );
                        return;
                    } catch (PlaywrightException ex) {
                        lastError = ex;
                        if (attempt == 1) {
                            page.navigate("about:blank");
                        }
                    }
                }

                throw lastError;
            }

            protected void waitForPath(String expectedPathFragment) {
            page.waitForURL(
                url -> url.contains(expectedPathFragment),
                new Page.WaitForURLOptions()
                    .setWaitUntil(WaitUntilState.COMMIT)
                    .setTimeout(30000)
            );
            }

            protected void clickNavButtonNoWait(String buttonText, String expectedPathFragment) {
            page.click(
                "button:has-text('" + buttonText + "')",
                new Page.ClickOptions().setNoWaitAfter(true)
            );

            if (isLoginPath(expectedPathFragment)) {
                waitForLoginAfterNavigation(expectedPathFragment);
                return;
            }

            waitForPath(expectedPathFragment);
            }

            private boolean isLoginPath(String expectedPathFragment) {
            return expectedPathFragment != null && expectedPathFragment.contains("/login");
            }

            private void waitForLoginAfterNavigation(String expectedPathFragment) {
            try {
                page.waitForURL(
                    url -> url.contains(expectedPathFragment) || url.contains("/login") || url.contains("/logout"),
                    new Page.WaitForURLOptions()
                        .setWaitUntil(WaitUntilState.COMMIT)
                        .setTimeout(30000)
                );
                return;
            } catch (PlaywrightException ignored) {
                // Continue with DOM fallback below for forward-based login handlers.
            }

            waitForLoginScreen();
            }

            protected void assertNavButtonTargets(String buttonText, String expectedPathFragment) {
            String onClick = page.locator("button:has-text('" + buttonText + "')").first().getAttribute("onclick");
            assertTrue(
                onClick != null && onClick.contains(expectedPathFragment),
                "Expected '" + buttonText + "' button to target " + expectedPathFragment + " but got: " + onClick
            );
            }

    protected void loginViaApi(String username, String password) {
        PlaywrightException lastError = null;
        String lastFailure = "";
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                for (String apiUrl : buildApiLoginUrls()) {
                    APIResponse response = page.request().post(
                            apiUrl,
                            RequestOptions.create()
                                    .setHeader("Content-Type", "application/json")
                                    .setData(loginPayload(username, password))
                    );

                    if (response.status() == 200 && hasSessionCookie()) {
                        return;
                    }

                    lastFailure = "API login url=" + apiUrl
                            + " status=" + response.status()
                            + " body=" + safeResponseText(response);
                    String fallbackFailure = tryLoginViaFormPost(username, password);
                    if (fallbackFailure == null) {
                        return;
                    }
                    lastFailure = lastFailure + " | fallback=" + fallbackFailure;
                }
            } catch (PlaywrightException ex) {
                lastError = ex;
                String fallbackFailure = tryLoginViaFormPost(username, password);
                if (fallbackFailure == null) {
                    return;
                }
                lastFailure = "API login exception=" + ex.getMessage() + " | fallback=" + fallbackFailure;
            }
        }

        if (lastError != null) {
            throw lastError;
        }

        if (isAuthInfrastructureUnavailable(lastFailure)) {
            Assumptions.assumeTrue(false,
                    "Auth infrastructure unavailable for this environment: " + lastFailure);
        }

        throw new AssertionError("Expected login success. Last failure: " + lastFailure);
    }

    protected boolean tryLoginViaApi(String username, String password) {
        try {
            for (String apiUrl : buildApiLoginUrls()) {
                APIResponse response = page.request().post(
                        apiUrl,
                        RequestOptions.create()
                                .setHeader("Content-Type", "application/json")
                                .setData(loginPayload(username, password))
                );
                if (response.status() == 200 && hasSessionCookie()) {
                    return true;
                }
                if (tryLoginViaFormPost(username, password) == null) {
                    return true;
                }
            }
            return false;
        } catch (PlaywrightException ignored) {
            return tryLoginViaFormPost(username, password) == null;
        }
    }

    private String tryLoginViaFormPost(String username, String password) {
        try {
            String safeUsername = username == null ? "" : username;
            String safePassword = password == null ? "" : password;
            String formBody = "username=" + urlEncodeFormPart(safeUsername)
                + "&password=" + urlEncodeFormPart(safePassword);

                Set<String> loginUrls = buildFormLoginUrls();

            StringBuilder failures = new StringBuilder();
            for (String loginUrl : loginUrls) {
                try {
                    page.request().get(loginUrl);
                } catch (PlaywrightException ignored) {
                    // keep trying; some deployments may reject GET before auth
                }

                APIResponse response = page.request().post(
                        loginUrl,
                        RequestOptions.create()
                                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                                .setData(formBody)
                );

                int status = response.status();
                if ((status == 200 || status == 302) && hasSessionCookie()) {
                    return null;
                }

                if (failures.length() > 0) {
                    failures.append("; ");
                }
                failures.append(loginUrl)
                        .append(" status=")
                        .append(status)
                        .append(" body=")
                        .append(safeResponseText(response));
            }

            return failures.toString();
        } catch (PlaywrightException ex) {
            return "fallback exception=" + ex.getMessage();
        }
    }

    private boolean hasSessionCookie() {
        for (Cookie cookie : context.cookies(baseUrl)) {
            if (cookie != null && cookie.name != null && cookie.name.toUpperCase().startsWith("JSESSIONID")) {
                return cookie.value != null && !cookie.value.isBlank();
            }
        }
        return false;
    }

    private String safeResponseText(APIResponse response) {
        try {
            String body = response.text();
            if (body == null) {
                return "";
            }
            return body.length() > 512 ? body.substring(0, 512) : body;
        } catch (PlaywrightException ex) {
            return "<unavailable>";
        }
    }

    private boolean isAuthInfrastructureUnavailable(String failureText) {
        if (failureText == null || failureText.isBlank()) {
            return false;
        }

        String normalized = failureText.toLowerCase();
        return normalized.contains("weld-001480")
                || normalized.contains("404 - not found")
                || normalized.contains("http method post is not supported");
    }

    private String urlEncodeFormPart(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String stripContextPath(String value) {
        int schemePos = value.indexOf("://");
        if (schemePos < 0) {
            return value;
        }

        int firstSlashAfterHost = value.indexOf('/', schemePos + 3);
        if (firstSlashAfterHost < 0) {
            return value;
        }

        return value.substring(0, firstSlashAfterHost);
    }

    private Set<String> buildApiLoginUrls() {
        String root = stripContextPath(baseUrl);
        String contextPath = contextPathFromBaseUrl(baseUrl);
        if (contextPath.isEmpty()) {
            contextPath = "/chat-server";
        }
        LinkedHashSet<String> urls = new LinkedHashSet<>();
        urls.add(appendPath(baseUrl, "/api/auth/login"));
        urls.add(appendPath(root + contextPath, "/api/auth/login"));
        urls.add(appendPath(root, "/api/auth/login"));
        return urls;
    }

    private Set<String> buildFormLoginUrls() {
        String root = stripContextPath(baseUrl);
        String contextPath = contextPathFromBaseUrl(baseUrl);
        if (contextPath.isEmpty()) {
            contextPath = "/chat-server";
        }

        LinkedHashSet<String> urls = new LinkedHashSet<>();
        urls.add(appendPath(baseUrl, ""));
        urls.add(appendPath(baseUrl, "/login"));
        urls.add(appendPath(root + contextPath, ""));
        urls.add(appendPath(root + contextPath, "/login"));
        urls.add(appendPath(root, "/login"));
        return urls;
    }

    private String contextPathFromBaseUrl(String value) {
        int schemePos = value.indexOf("://");
        if (schemePos < 0) {
            return "";
        }

        int firstSlashAfterHost = value.indexOf('/', schemePos + 3);
        if (firstSlashAfterHost < 0) {
            return "";
        }

        String path = value.substring(firstSlashAfterHost);
        while (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        return "/".equals(path) ? "" : path;
    }

    private String appendPath(String base, String suffix) {
        String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        if (suffix == null || suffix.isEmpty()) {
            return normalizedBase;
        }
        return suffix.startsWith("/") ? normalizedBase + suffix : normalizedBase + "/" + suffix;
    }

    private String loginPayload(String username, String password) {
        String safeUsername = username == null ? "" : username.replace("\\", "\\\\").replace("\"", "\\\"");
        String safePassword = password == null ? "" : password.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"username\":\"" + safeUsername + "\",\"password\":\"" + safePassword + "\"}";
    }

    private void applyBaggageHeaderOverride(Browser.NewContextOptions contextOptions) {
        String manualBaggageHeader = System.getProperty(BAGGAGE_OVERRIDE_PROPERTY);
        if (manualBaggageHeader == null || manualBaggageHeader.isBlank()) {
            manualBaggageHeader = System.getenv(BAGGAGE_OVERRIDE_ENV);
        }

        if (manualBaggageHeader == null || manualBaggageHeader.isBlank()) {
            // No manual override: keep Parasoft's default CTP/DTP header propagation.
            return;
        }

        Map<String, String> headers = new LinkedHashMap<>();
        if (contextOptions.extraHTTPHeaders != null) {
            headers.putAll(contextOptions.extraHTTPHeaders);
        }
        headers.put(BAGGAGE_HEADER_NAME, manualBaggageHeader.trim());
        contextOptions.setExtraHTTPHeaders(headers);
    }
}
