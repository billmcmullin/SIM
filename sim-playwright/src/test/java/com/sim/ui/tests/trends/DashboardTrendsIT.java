package com.sim.ui.tests.trends;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardTrendsIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticatedUser_redirectedToLogin_fromTrends() {
        navigateWithCommit("/dashboard/trends");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated trends page request to land on login,");
    }

    @Test
    @Order(2)
    void trendsPage_rendersCoreUi_whenAuthenticated() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/trends");
        waitForPath("/chat-server/dashboard/trends");
        page.waitForSelector("h1:has-text('Entry Trends')");

        assertTrue(page.title().contains("Entry Trends"));
        assertTrue(page.locator("h1:has-text('Entry Trends')").count() > 0);
        assertTrue(page.locator("h2:has-text('Total Entries Trend')").count() > 0);

        assertTrue(page.locator("#trendDaysSelect").count() > 0);
        assertTrue(page.locator("#trendChart").count() > 0);
        assertTrue(page.locator("#widgetTrendCharts").count() > 0);

        // Script-injected globals from template
        assertTrue(page.evaluate("() => typeof window.trendData === 'string' && window.trendData.length > 0").equals(Boolean.TRUE));
        assertTrue(page.evaluate("() => ['10','30','90','120','180'].includes(String(window.trendDaysSelected))").equals(Boolean.TRUE));
    }

    @Test
    @Order(3)
    void trends_daysQueryParam_accepts10_30_90_andFallsBackTo30() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/trends?days=10");
        waitForPath("/chat-server/dashboard/trends");
        page.waitForSelector("#trendDaysSelect");
        assertTrue("10".equals(page.inputValue("#trendDaysSelect")),
                "Expected selected period 10");

        navigateWithCommit("/dashboard/trends?days=90");
        waitForPath("/chat-server/dashboard/trends");
        assertTrue("90".equals(page.inputValue("#trendDaysSelect")),
                "Expected selected period 90");

        // Updated UI supports 120 and 180 as valid options.
        navigateWithCommit("/dashboard/trends?days=120");
        waitForPath("/chat-server/dashboard/trends");
        assertTrue("120".equals(page.inputValue("#trendDaysSelect")),
            "Expected selected period 120");

        navigateWithCommit("/dashboard/trends?days=180");
        waitForPath("/chat-server/dashboard/trends");
        assertTrue("180".equals(page.inputValue("#trendDaysSelect")),
            "Expected selected period 180");

        // Invalid value should still fall back to default period.
        navigateWithCommit("/dashboard/trends?days=999");
        waitForPath("/chat-server/dashboard/trends");
        assertTrue("30".equals(page.inputValue("#trendDaysSelect")),
            "Expected fallback period 30 for invalid days");
    }

    @Test
    @Order(4)
    void trends_topButtons_dashboardProfileLogout_work() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/trends");
        waitForPath("/chat-server/dashboard/trends");

        // Validate dashboard target intent without triggering /dashboard server-side load.
        assertNavButtonTargets("Dashboard", "/chat-server/dashboard");

        // Back to trends
        navigateWithCommit("/dashboard/trends");
        waitForPath("/chat-server/dashboard/trends");

        // Profile button
        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"));

        // Back to trends
        navigateWithCommit("/dashboard/trends");
        waitForPath("/chat-server/dashboard/trends");

        // Logout button
        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertTrue(page.url().contains("/chat-server/login"));

        // After logout trends should be blocked
        navigateWithCommit("/dashboard/trends");
        waitForLoginScreen();
        assertOnLoginScreen("Expected trends page to be blocked after logout,");
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}
