package com.sim.ui.base;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.WaitUntilState;

public abstract class BaseUiIT {

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

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900)
                .setIgnoreHTTPSErrors(ignoreHttpsErrors));

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
            waitForPath(expectedPathFragment);
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
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                APIResponse response = page.request().post(
                        baseUrl + "/api/auth/login",
                        RequestOptions.create()
                                .setHeader("Content-Type", "application/json")
                                .setData(loginPayload(username, password))
                );

                assertTrue(response.status() == 200,
                        "Expected API login success (200), got " + response.status() + " body=" + response.text());
                return;
            } catch (PlaywrightException ex) {
                lastError = ex;
            }
        }

        throw lastError;
    }

    protected boolean tryLoginViaApi(String username, String password) {
        try {
            APIResponse response = page.request().post(
                    baseUrl + "/api/auth/login",
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(loginPayload(username, password))
            );
            return response.status() == 200;
        } catch (PlaywrightException ignored) {
            return false;
        }
    }

    private String loginPayload(String username, String password) {
        String safeUsername = username == null ? "" : username.replace("\\", "\\\\").replace("\"", "\\\"");
        String safePassword = password == null ? "" : password.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"username\":\"" + safeUsername + "\",\"password\":\"" + safePassword + "\"}";
    }
}
