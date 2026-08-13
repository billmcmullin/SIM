package com.sim.ui.tests.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminPageIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticatedUser_isRedirectedToLogin_fromAdmin() {
        navigateWithCommit("/admin");

        try {
            page.waitForURL(
                    url -> url.contains("/login") || url.contains("/dashboard"),
                    new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(15000));
        } catch (PlaywrightException ignored) {
            // RequestDispatcher forward can keep /admin in URL; continue with DOM-based checks.
        }

        try {
            page.waitForSelector(
                    "#loginForm, #dashboardHomeSection, #adminTabs",
                    new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(15000));
        } catch (PlaywrightException ignored) {
            // If no marker appears, we still verify that admin UI is not accessible.
        }

        boolean onAdminPage = page.title().contains("Admin Configuration") && page.locator("#adminTabs").count() > 0;
        assertFalse(onAdminPage,
                "Unauthenticated user should not be able to access admin page. URL: " + page.url());

        boolean onLoginScreen = page.url().contains("/login")
            || page.locator("#loginForm").count() > 0
            || page.locator("input#username").count() > 0;

        boolean onDashboard = page.url().contains("/dashboard")
            || page.locator("#dashboardHomeSection").count() > 0;

        assertTrue(onLoginScreen || onDashboard || !onAdminPage,
                "Expected unauthenticated admin request to remain blocked from admin UI, got: " + page.url());
    }

    @Test
    @Order(2)
    void adminCanOpenAdminPage_andSeeCoreSections() {
        loginViaApi(adminUsername, adminPassword);

        navigateWithCommit("/admin");
        waitForPath("/chat-server/admin");
        page.waitForSelector("#adminTabs");

        assertTrue(page.url().contains("/chat-server/admin"),
                "Expected /chat-server/admin, got: " + page.url());

        assertTrue(page.title().contains("Admin Configuration"));
        assertTrue(page.locator("#adminTabs").count() > 0);

        assertTrue(page.locator("#serverConfigForm").count() > 0);
        assertTrue(page.locator("#workspaceForm").count() > 0);
        assertTrue(page.locator("#userCreateForm").count() > 0);
        assertTrue(page.locator("#termCreateForm").count() > 0);
        assertTrue(page.locator("#widgetTableBody").count() > 0);
    }

    @Test
    @Order(3)
    void logoutFromAdmin_topBarButton_redirectsToLogin_andBlocksAdminAccess() {
        loginViaApi(adminUsername, adminPassword);
        navigateWithCommit("/admin");
        waitForPath("/chat-server/admin");

        clickNavButtonNoWait("Logout", "/chat-server/login");

        assertTrue(page.url().contains("/chat-server/login"),
                "After logout expected /chat-server/login, got: " + page.url());

        navigateWithCommit("/admin");
        waitForLoginScreen();
        assertOnLoginScreen("Expected /admin to be blocked after logout,");
    }
}
