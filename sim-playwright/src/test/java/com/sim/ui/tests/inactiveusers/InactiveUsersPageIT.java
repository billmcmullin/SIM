package com.sim.ui.tests.inactiveusers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.Page;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InactiveUsersPageIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_redirectedToLogin() {
        page.navigate(baseUrl + "/dashboard/inactive-users");

        waitForLoginScreen();
        assertOnLoginScreen("Expected redirect/forward to login,");
    }

    @Test
    @Order(2)
    void inactiveUsersPage_rendersCoreUi_whenAuthenticated() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/inactive-users");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/inactive-users"));

        assertTrue(page.title().contains("Inactive Users"));
        assertTrue(page.locator("h1:has-text('Inactive Users')").count() > 0);

        // Toolbar
        assertTrue(page.locator("#daysSelect").count() > 0);
        assertTrue(page.locator("#applyDaysBtn").count() > 0);

        // Main sections/tables
        assertTrue(page.locator("h2:has-text('All Widgets')").count() > 0);
        assertTrue(page.locator("#allWidgetsBody").count() > 0);

        assertTrue(page.locator("h2:has-text('Per Widget')").count() > 0);
        assertTrue(page.locator("#widgetTablesContainer").count() > 0);

        // Injected page config should exist
        assertTrue(page.evaluate("() => !!window.inactiveUsersConfig").equals(Boolean.TRUE));
        assertTrue(page.evaluate("() => typeof window.inactiveUsersConfig.data === 'string'").equals(Boolean.TRUE));
    }

    @Test
    @Order(3)
    void applyDays_updatesUrlAndStaysOnPage() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/inactive-users");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/inactive-users"));

        page.selectOption("#daysSelect", "14");
        page.click("#applyDaysBtn");

        // typical client behavior is reload with ?days=...
        page.waitForURL(url -> url.contains("/chat-server/dashboard/inactive-users"));
        assertTrue(page.url().contains("/chat-server/dashboard/inactive-users"),
                "Expected to remain on inactive-users page, got: " + page.url());
    }

    @Test
    @Order(4)
    void topNav_buttons_dashboardProfileLogout_work() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/inactive-users");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/inactive-users"));

        page.click("button:has-text('Dashboard')");
        page.waitForURL(url -> url.contains("/chat-server/dashboard"));
        assertTrue(page.url().contains("/chat-server/dashboard"));

        page.navigate(baseUrl + "/dashboard/inactive-users");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/inactive-users"));

        page.click("button:has-text('Profile')");
        page.waitForURL(url -> url.contains("/chat-server/profile"));
        assertTrue(page.url().contains("/chat-server/profile"));

        page.navigate(baseUrl + "/dashboard/inactive-users");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/inactive-users"));

        page.click("button:has-text('Logout')");
        page.waitForURL(url -> url.contains("/chat-server/login"));
        assertTrue(page.url().contains("/chat-server/login"));

        // verify blocked after logout
        page.navigate(baseUrl + "/dashboard/inactive-users");
        waitForLoginScreen();
        assertOnLoginScreen("Expected inactive-users to be blocked after logout,");
    }

    private void login(String username, String password) {
        page.navigate(baseUrl + "/login");
        waitForLoginScreen();
        assertOnLoginScreen("Expected login page before submitting credentials,");

        page.fill("#username", username);
        page.fill("#password", password);
        page.click("button[type='submit']");

        page.waitForURL(
                url -> url.contains("/chat-server/dashboard") || url.contains("/chat-server/admin"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        assertTrue(
                page.url().contains("/chat-server/dashboard") || page.url().contains("/chat-server/admin"),
                "Login expected /dashboard or /admin, got: " + page.url()
        );
    }
}
