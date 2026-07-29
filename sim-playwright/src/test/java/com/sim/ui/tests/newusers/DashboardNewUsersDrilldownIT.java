package com.sim.ui.tests.newusers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardNewUsersDrilldownIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_redirectsToLogin() {
        navigateWithCommit("/dashboard/new-users/drilldown");

        waitForLoginScreen();
        assertOnLoginScreen("Expected redirect/forward to login,");
    }

    @Test
    @Order(2)
    void pageRenders_coreElements_whenAuthenticated() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/new-users/drilldown");
        waitForPath("/chat-server/dashboard/new-users/drilldown");
        page.waitForSelector("h1:has-text('All Session IDs / Users (Newest First)')");

        assertTrue(page.title().contains("Newest Users Drilldown"));
        assertTrue(page.locator("h1:has-text('All Session IDs / Users (Newest First)')").count() > 0);

        assertTrue(page.locator("#pageSizeSelect").count() > 0);
        assertTrue(page.locator("#prevPageBtn").count() > 0);
        assertTrue(page.locator("#nextPageBtn").count() > 0);

        assertTrue(page.locator("table.session-table").count() > 0);
        assertTrue(page.locator("table.session-table thead th:has-text('Rank')").count() > 0);
        assertTrue(page.locator("table.session-table thead th:has-text('Session ID / User')").count() > 0);
        assertTrue(page.locator("table.session-table thead th:has-text('First Seen')").count() > 0);
        assertTrue(page.locator("table.session-table thead th:has-text('Total Chats')").count() > 0);
    }

    @Test
    @Order(3)
    void pageSize_queryParam_behavesAsExpected() {
        login(adminUsername, adminPassword);

        // Allowed page sizes
        navigateWithCommit("/dashboard/new-users/drilldown?page=1&pageSize=25");
        waitForPath("/chat-server/dashboard/new-users/drilldown");
        assertTrue("25".equals(page.inputValue("#pageSizeSelect")),
                "Expected pageSizeSelect=25");

        navigateWithCommit("/dashboard/new-users/drilldown?page=1&pageSize=50");
        waitForPath("/chat-server/dashboard/new-users/drilldown");
        assertTrue("50".equals(page.inputValue("#pageSizeSelect")),
                "Expected pageSizeSelect=50");

        // Invalid page size should fall back to 10
        navigateWithCommit("/dashboard/new-users/drilldown?page=1&pageSize=999");
        waitForPath("/chat-server/dashboard/new-users/drilldown");
        assertTrue("10".equals(page.inputValue("#pageSizeSelect")),
                "Expected invalid pageSize to fall back to 10");
    }

    @Test
    @Order(4)
    void dayFilter_and_navButtons_work() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/new-users/drilldown?day=2026-05-21&page=1&pageSize=10");
        waitForPath("/chat-server/dashboard/new-users/drilldown");

        assertTrue(page.locator("h1:has-text('All Session IDs / Users (Newest First)')").count() > 0);

        // Validate dashboard target intent without triggering /dashboard server-side load.
        assertNavButtonTargets("Dashboard", "/chat-server/dashboard");

        navigateWithCommit("/dashboard/new-users/drilldown");
        waitForPath("/chat-server/dashboard/new-users/drilldown");

        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"));

        navigateWithCommit("/dashboard/new-users/drilldown");
        waitForPath("/chat-server/dashboard/new-users/drilldown");

        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertTrue(page.url().contains("/chat-server/login"));
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}
