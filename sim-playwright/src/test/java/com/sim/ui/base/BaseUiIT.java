package com.sim.ui.base;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

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

        page = context.newPage();

        // Optional: default timeout for actions/assertions
        page.setDefaultTimeout(10000);
        page.setDefaultNavigationTimeout(15000);
    }

    @AfterEach
    void testTeardown() {
        if (context != null) {
            context.close();
        }
    }
}
