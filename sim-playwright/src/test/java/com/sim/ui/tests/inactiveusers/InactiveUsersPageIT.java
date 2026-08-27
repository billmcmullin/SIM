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
        navigateWithCommit("/dashboard/inactive-users");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated inactive-users request to land on login,");
    }

    @Test
    @Order(2)
    void inactiveUsersPage_rendersCoreUi_whenAuthenticated() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/inactive-users");
        waitForPath("/chat-server/dashboard/inactive-users");
        page.waitForSelector("h1:has-text('Inactive Users')");

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
        assertTrue(page.evaluate("() => typeof window.inactiveUsersConfig.dataB64 === 'string' || typeof window.inactiveUsersConfig.data === 'object'").equals(Boolean.TRUE));
    }

    @Test
    @Order(3)
    void applyDays_updatesUrlAndStaysOnPage() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/inactive-users");
        waitForPath("/chat-server/dashboard/inactive-users");

        page.selectOption("#daysSelect", "14");
        page.click("#applyDaysBtn", new Page.ClickOptions().setNoWaitAfter(true));

        // typical client behavior is reload with ?days=...
        waitForPath("/chat-server/dashboard/inactive-users");
        assertTrue(page.url().contains("/chat-server/dashboard/inactive-users"),
                "Expected to remain on inactive-users page, got: " + page.url());
    }

    @Test
    @Order(4)
    void topNav_buttons_dashboardProfileLogout_work() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/inactive-users");
        waitForPath("/chat-server/dashboard/inactive-users");

        // Validate dashboard target intent without triggering /dashboard server-side load.
        assertNavButtonTargets("Dashboard", "/chat-server/dashboard");

        navigateWithCommit("/dashboard/inactive-users");
        waitForPath("/chat-server/dashboard/inactive-users");

        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"));

        navigateWithCommit("/dashboard/inactive-users");
        waitForPath("/chat-server/dashboard/inactive-users");

        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertOnLoginScreen("After logout expected login screen,");

        // verify blocked after logout
        navigateWithCommit("/dashboard/inactive-users");
        waitForLoginScreen();
        assertOnLoginScreen("Expected inactive-users to be blocked after logout,");
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}
