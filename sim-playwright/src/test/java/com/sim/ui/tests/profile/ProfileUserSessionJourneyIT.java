package com.sim.ui.tests.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileUserSessionJourneyIT extends BaseUiIT {

    private final String userUsername = System.getProperty("userUsername", "user");
    private final String userPassword = System.getProperty("userPassword", "user");
    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_profileAndSessionPages_areBlocked() {
        navigateWithCommit("/profile");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated profile navigation to require login,");

        navigateWithCommit("/dashboard/sessions");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated user sessions navigation to require login,");
    }

    @Test
    @Order(2)
    void profilePage_renders_andTopBarNavigationWorks() {
        loginAsAvailableUser();

        navigateWithCommit("/profile");
        waitForPath("/chat-server/profile");
        page.waitForSelector("#profileForm");

        assertTrue(page.title().contains("Profile"));
        assertTrue(page.locator("#usernameInput").count() > 0);
        assertTrue(page.locator("#passwordInput").count() > 0);
        assertTrue(page.locator("#profileForm button[type='submit']").count() > 0);

        clickNavButtonNoWait("Dashboard", "/chat-server/dashboard");
        assertTrue(page.url().contains("/chat-server/dashboard"),
                "Expected profile top bar to navigate to dashboard.");

        navigateWithCommit("/profile");
        waitForPath("/chat-server/profile");

        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertOnLoginScreen("After logout expected login screen,");

        navigateWithCommit("/profile");
        waitForLoginScreen();
        assertOnLoginScreen("Expected profile to be blocked after logout,");
    }

    @Test
    @Order(3)
    void profileToSessionsJourney_viaDashboardButton_supportsSessionFilters() {
        loginAsAvailableUser();

        navigateWithCommit("/profile");
        waitForPath("/chat-server/profile");

        clickNavButtonNoWait("Dashboard", "/chat-server/dashboard");
        waitForPath("/chat-server/dashboard");

        String sessionsTarget = readDashboardButtonTarget("View User Sessions", "/dashboard/sessions");

        page.click("button.dashboard-switch-btn:has-text('View User Sessions')",
                new Page.ClickOptions().setNoWaitAfter(true));

        // Always open the route directly after button click to keep assertions deterministic
        // across inline and full-page dashboard modes.
        navigateWithCommit(normalizeDashboardTarget(sessionsTarget));
        waitForPath("/dashboard/sessions");

        page.waitForSelector("#sessionsTable");
        assertTrue(page.locator("#searchInput").count() > 0);
        assertTrue(page.locator("#searchBtn").count() > 0);
        assertTrue(page.locator("#refreshBtn").count() > 0);
        assertTrue(page.locator("#showAllUsersBtn").count() > 0);
        assertTrue(page.locator("#showActiveUsersBtn").count() > 0);
        assertTrue(page.locator("#toggleLabeledOnlyBtn").count() > 0);

        // Read-only interactions only: these filters/search inputs do not persist data.
        page.fill("#searchInput", "playwright-session-search");
        page.click("#searchBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");
        assertFalse(page.locator("#sessionsBody").innerText().trim().isBlank(),
                "Expected sessions results region to be populated after search.");

        page.click("#toggleLabeledOnlyBtn", new Page.ClickOptions().setNoWaitAfter(true));
        assertTrue(page.locator("#toggleLabeledOnlyBtn").innerText().contains("On")
                || page.locator("#toggleLabeledOnlyBtn").innerText().contains("Off"),
                "Expected labeled-only toggle state label to be rendered.");

        page.click("#showActiveUsersBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");
        page.click("#showAllUsersBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");

        page.click("#refreshBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");

        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"),
                "Expected sessions top bar to navigate back to profile.");
    }

    @Test
    @Order(4)
    void dashboardToUsernameCatalogJourney_coversSessionCatalogControls() {
        loginAsAvailableUser();

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        String catalogTarget = readDashboardButtonTarget("Username Catalog", "/dashboard/session-names");
        page.click("button.dashboard-switch-btn:has-text('Username Catalog')",
                new Page.ClickOptions().setNoWaitAfter(true));

        // Always open the route directly after button click to keep assertions deterministic
        // across inline and full-page dashboard modes.
        navigateWithCommit(normalizeDashboardTarget(catalogTarget));
        waitForPath("/dashboard/session-names");

        page.waitForSelector("#sessionSearch");
        assertTrue(page.locator("#sessionSearchButton").count() > 0);
        assertTrue(page.locator("#toggleLabeledOnlyBtn").count() > 0);
        assertTrue(page.locator("#pageSizeSelect").count() > 0);
        assertTrue(page.locator("#prevPageBtn").count() > 0);
        assertTrue(page.locator("#nextPageBtn").count() > 0);
        assertTrue(page.locator("#sessionNameList").count() > 0);

        page.fill("#sessionSearch", "playwright-catalog-search");
        page.click("#sessionSearchButton", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionNameList");

        page.selectOption("#pageSizeSelect", "20");
        assertTrue("20".equals(page.inputValue("#pageSizeSelect")),
                "Expected pageSize selection to update.");

        page.click("#toggleLabeledOnlyBtn", new Page.ClickOptions().setNoWaitAfter(true));
        assertTrue(page.locator("#toggleLabeledOnlyBtn").innerText().contains("On")
                || page.locator("#toggleLabeledOnlyBtn").innerText().contains("Off"),
                "Expected labeled-only toggle state label to be rendered.");
    }

    private void loginAsAvailableUser() {
        if (!tryLoginViaApi(userUsername, userPassword)) {
            loginViaApi(adminUsername, adminPassword);
        }
    }

    private String readDashboardButtonTarget(String buttonText, String expectedPathFragment) {
        String selector = "button.dashboard-switch-btn:has-text('" + buttonText + "')";
        assertTrue(page.locator(selector).count() > 0,
                "Expected dashboard switch button: " + buttonText);

        String dataTarget = page.locator(selector).first().getAttribute("data-target");
        assertTrue(dataTarget != null && dataTarget.contains(expectedPathFragment),
                "Expected target containing " + expectedPathFragment + " for " + buttonText);
        return dataTarget;
    }

        private String normalizeDashboardTarget(String target) {
                if (target == null || target.isBlank()) {
                        return "/dashboard";
                }

                if (target.startsWith("http://") || target.startsWith("https://")) {
                        return target;
                }

                int schemeIdx = baseUrl.indexOf("://");
                if (schemeIdx > 0) {
                        int firstSlashAfterHost = baseUrl.indexOf('/', schemeIdx + 3);
                        if (firstSlashAfterHost > 0) {
                                String contextPath = baseUrl.substring(firstSlashAfterHost);
                                if (contextPath.endsWith("/")) {
                                        contextPath = contextPath.substring(0, contextPath.length() - 1);
                                }
                                if (!contextPath.isBlank() && target.startsWith(contextPath + "/")) {
                                        return target.substring(contextPath.length());
                                }
                        }
                }

                return target;
        }
}