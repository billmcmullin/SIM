package com.sim.ui.tests.topics;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardTopicsIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticatedUser_redirectedToLogin_fromTopicsPage() {
        navigateWithCommit("/dashboard/topics");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated topics page request to land on login,");
    }

    @Test
    @Order(2)
    void topicsPage_rendersCoreSections_whenAuthenticated() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/topics");
        waitForPath("/chat-server/dashboard/topics");
        page.waitForSelector("h1:has-text('Popular Topics')");

        assertTrue(page.title().contains("Popular Topics"));
        assertTrue(page.locator("h1:has-text('Popular Topics')").count() > 0);

        // Core filter controls
        assertTrue(page.locator("#topicsFilterBar").count() > 0);
        assertTrue(page.locator("#topicDayInput").count() > 0);
        assertTrue(page.locator("#topicUseRange").count() > 0);
        assertTrue(page.locator("#topicStartDateInput").count() > 0);
        assertTrue(page.locator("#topicEndDateInput").count() > 0);
        assertTrue(page.locator("#topicIncludeOtherBtn").count() > 0);
        assertTrue(page.locator("#topicRefreshBtn").count() > 0);

        // Summary fields
        assertTrue(page.locator("#topicsSummaryDay").count() > 0);
        assertTrue(page.locator("#topicsSummaryRange").count() > 0);
        assertTrue(page.locator("#topicsSummaryMentions").count() > 0);
        assertTrue(page.locator("#topicsSummaryUniqueChats").count() > 0);

        // Global table/chart + per-widget container
        assertTrue(page.locator("#globalTopicsBody").count() > 0);
        assertTrue(page.locator("#globalTopicsPieChart").count() > 0);
        assertTrue(page.locator("#perWidgetTopicsContainer").count() > 0);
    }

    @Test
    @Order(3)
    void topicsPage_queryParams_dayAndRange_loadSuccessfully() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/topics?day=2026-05-21");
        waitForPath("/chat-server/dashboard/topics");
        page.waitForSelector("h1:has-text('Popular Topics')");

        assertTrue(page.locator("h1:has-text('Popular Topics')").count() > 0);

        navigateWithCommit("/dashboard/topics?start=2026-05-01&end=2026-05-21&includeOther=true");
        waitForPath("/chat-server/dashboard/topics");

        assertTrue(page.locator("h2:has-text('Topics Across All Widgets')").count() > 0);
    }

    @Test
    @Order(4)
    void topicsPage_topNavButtons_work() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/topics");
        waitForPath("/chat-server/dashboard/topics");

        // Validate dashboard target intent without triggering /dashboard server-side load.
        assertNavButtonTargets("Dashboard", "/chat-server/dashboard");

        // Go back to topics
        navigateWithCommit("/dashboard/topics");
        waitForPath("/chat-server/dashboard/topics");

        // Profile button
        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"));

        // Return
        navigateWithCommit("/dashboard/topics");
        waitForPath("/chat-server/dashboard/topics");

        // Logout button
        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertTrue(page.url().contains("/chat-server/login"));

        // Access blocked again after logout
        navigateWithCommit("/dashboard/topics");
        waitForLoginScreen();
        assertOnLoginScreen("Expected topics page to be blocked after logout,");
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}
