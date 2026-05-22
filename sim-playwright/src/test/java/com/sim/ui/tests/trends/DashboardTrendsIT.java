package com.sim.ui.tests.trends;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.Page;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardTrendsIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticatedUser_redirectedToLogin_fromTrends() {
        page.navigate(baseUrl + "/dashboard/trends");
        page.waitForURL(url -> url.contains("/chat-server/login"));

        assertTrue(page.url().contains("/chat-server/login"),
                "Expected redirect to /chat-server/login, got: " + page.url());
    }

    @Test
    @Order(2)
    void trendsPage_rendersCoreUi_whenAuthenticated() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/trends");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/trends"));

        assertTrue(page.title().contains("Entry Trends"));
        assertTrue(page.locator("h1:has-text('Entry Trends')").count() > 0);
        assertTrue(page.locator("h2:has-text('Total Entries Trend')").count() > 0);

        assertTrue(page.locator("#trendDaysSelect").count() > 0);
        assertTrue(page.locator("#trendChart").count() > 0);
        assertTrue(page.locator("#widgetTrendCharts").count() > 0);

        // Script-injected globals from template
        assertTrue(page.evaluate("() => typeof window.trendData === 'string' && window.trendData.length > 0").equals(Boolean.TRUE));
        assertTrue(page.evaluate("() => ['7','30','90'].includes(String(window.trendDaysSelected))").equals(Boolean.TRUE));
    }

    @Test
    @Order(3)
    void trends_daysQueryParam_accepts7_30_90_andFallsBackTo30() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/trends?days=7");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/trends"));
        assertTrue("7".equals(page.inputValue("#trendDaysSelect")),
                "Expected selected period 7");

        page.navigate(baseUrl + "/dashboard/trends?days=90");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/trends"));
        assertTrue("90".equals(page.inputValue("#trendDaysSelect")),
                "Expected selected period 90");

        // Invalid value should default to 30
        page.navigate(baseUrl + "/dashboard/trends?days=999");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/trends"));
        assertTrue("30".equals(page.inputValue("#trendDaysSelect")),
                "Expected fallback period 30 for invalid days");
    }

    @Test
    @Order(4)
    void trends_topButtons_dashboardProfileLogout_work() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/trends");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/trends"));

        // Dashboard button
        page.click("button:has-text('Dashboard')");
        page.waitForURL(url -> url.contains("/chat-server/dashboard"));
        assertTrue(page.url().contains("/chat-server/dashboard"));

        // Back to trends
        page.navigate(baseUrl + "/dashboard/trends");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/trends"));

        // Profile button
        page.click("button:has-text('Profile')");
        page.waitForURL(url -> url.contains("/chat-server/profile"));
        assertTrue(page.url().contains("/chat-server/profile"));

        // Back to trends
        page.navigate(baseUrl + "/dashboard/trends");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/trends"));

        // Logout button
        page.click("button:has-text('Logout')");
        page.waitForURL(url -> url.contains("/chat-server/login"));
        assertTrue(page.url().contains("/chat-server/login"));

        // After logout trends should be blocked
        page.navigate(baseUrl + "/dashboard/trends");
        page.waitForURL(url -> url.contains("/chat-server/login"));
        assertFalse(page.url().contains("/chat-server/dashboard/trends"));
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
