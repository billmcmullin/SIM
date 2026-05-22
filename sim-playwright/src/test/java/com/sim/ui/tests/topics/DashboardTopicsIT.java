package com.sim.ui.tests.topics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.Page;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardTopicsIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticatedUser_redirectedToLogin_fromTopicsPage() {
        page.navigate(baseUrl + "/dashboard/topics");
        page.waitForURL(url -> url.contains("/chat-server/login"));

        assertTrue(page.url().contains("/chat-server/login"),
                "Expected redirect to /chat-server/login, got: " + page.url());
    }

    @Test
    @Order(2)
    void topicsPage_rendersCoreSections_whenAuthenticated() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/topics");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/topics"));

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

        page.navigate(baseUrl + "/dashboard/topics?day=2026-05-21");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/topics"));

        assertTrue(page.locator("h1:has-text('Popular Topics')").count() > 0);

        page.navigate(baseUrl + "/dashboard/topics?start=2026-05-01&end=2026-05-21&includeOther=true");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/topics"));

        assertTrue(page.locator("h2:has-text('Topics Across All Widgets')").count() > 0);
    }

    @Test
    @Order(4)
    void topicsPage_topNavButtons_work() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/topics");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/topics"));

        // Dashboard button
        page.click("button:has-text('Dashboard')");
        page.waitForURL(url -> url.contains("/chat-server/dashboard"));
        assertTrue(page.url().contains("/chat-server/dashboard"));

        // Go back to topics
        page.navigate(baseUrl + "/dashboard/topics");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/topics"));

        // Profile button
        page.click("button:has-text('Profile')");
        page.waitForURL(url -> url.contains("/chat-server/profile"));
        assertTrue(page.url().contains("/chat-server/profile"));

        // Return
        page.navigate(baseUrl + "/dashboard/topics");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/topics"));

        // Logout button
        page.click("button:has-text('Logout')");
        page.waitForURL(url -> url.contains("/chat-server/login"));
        assertTrue(page.url().contains("/chat-server/login"));

        // Access blocked again after logout
        page.navigate(baseUrl + "/dashboard/topics");
        page.waitForURL(url -> url.contains("/chat-server/login"));
        assertFalse(page.url().contains("/chat-server/dashboard/topics"));
    }

    private void login(String username, String password) {
        page.navigate(baseUrl + "/login");
        page.waitForURL(url -> url.contains("/chat-server/login"));

        page.fill("#username", username);
        page.fill("#password", password);
        page.click("button[type='submit']");

        page.waitForURL(
                url -> url.contains("/chat-server/dashboard") || url.contains("/chat-server/admin"),
                new Page.WaitForURLOptions().setTimeout(15000)
        );

        assertTrue(
                page.url().contains("/chat-server/dashboard") || page.url().contains("/chat-server/admin"),
                "Login expected /dashboard or /admin, got: " + page.url()
        );
    }
}
