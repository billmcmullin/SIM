package com.sim.ui.tests.admin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminPageIT {

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;

    private static String baseUrl;
    private static boolean headless;
    private static boolean ignoreHttpsErrors;

    private static String adminUsername;
    private static String adminPassword;

    @BeforeAll
    static void beforeAll() {
        baseUrl = System.getProperty("baseUrl", "http://localhost:8080/chat-server");
        headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        ignoreHttpsErrors = Boolean.parseBoolean(System.getProperty("ignoreHttpsErrors", "true"));

        adminUsername = System.getProperty("adminUsername", "admin");
        adminPassword = System.getProperty("adminPassword", "admin");

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    static void afterAll() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setIgnoreHTTPSErrors(ignoreHttpsErrors)
                        .setViewportSize(1440, 900)
        );
        page = context.newPage();
        page.setDefaultTimeout(10000);
        page.setDefaultNavigationTimeout(15000);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @Order(1)
    void unauthenticatedUser_isRedirectedToLogin_fromAdmin() {
        page.navigate(baseUrl + "/admin");
        page.waitForURL(url -> url.contains("/chat-server/login"));

        assertTrue(page.url().contains("/chat-server/login"),
                "Expected redirect to /chat-server/login, got: " + page.url());
    }

    @Test
    @Order(2)
    void adminCanOpenAdminPage_andSeeCoreSections() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/admin");
        page.waitForURL(url -> url.contains("/chat-server/admin"));

        assertTrue(page.url().contains("/chat-server/admin"),
                "Expected /chat-server/admin, got: " + page.url());

        assertTrue(page.title().contains("Admin Configuration"));
        assertTrue(page.locator("h1:has-text('Admin Configuration')").count() > 0);

        assertTrue(page.locator("#serverConfigForm").count() > 0);
        assertTrue(page.locator("#workspaceForm").count() > 0);
        assertTrue(page.locator("#userCreateForm").count() > 0);
        assertTrue(page.locator("#termCreateForm").count() > 0);
        assertTrue(page.locator("#widgetTableBody").count() > 0);
    }

    @Test
    @Order(3)
    void logoutFromAdmin_topBarButton_redirectsToLogin_andBlocksAdminAccess() {
        login(adminUsername, adminPassword);
        page.navigate(baseUrl + "/admin");
        page.waitForURL(url -> url.contains("/chat-server/admin"));

        page.click("button:has-text('Logout')");
        page.waitForURL(url -> url.contains("/chat-server/login"));

        assertTrue(page.url().contains("/chat-server/login"),
                "After logout expected /chat-server/login, got: " + page.url());

        page.navigate(baseUrl + "/admin");
        page.waitForURL(url -> url.contains("/chat-server/login"));
        assertTrue(page.url().contains("/chat-server/login"),
                "Expected /admin to redirect to /login after logout, got: " + page.url());
    }

    private void login(String username, String password) {
        page.navigate(baseUrl + "/login");
        page.waitForURL(url -> url.contains("/chat-server/login"));

        page.fill("#username", username);
        page.fill("#password", password);

        // Click submit (works even if visible text differs slightly)
        page.click("button[type='submit']");

        // Wait for app-auth redirect target
        page.waitForURL(
                url -> url.contains("/chat-server/dashboard") || url.contains("/chat-server/admin"),
                new Page.WaitForURLOptions().setTimeout(15000)
        );

        assertTrue(page.url().contains("/chat-server/dashboard") || page.url().contains("/chat-server/admin"),
                "Login expected /dashboard or /admin, got: " + page.url());
    }
}
