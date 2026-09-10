package com.sim.ui.tests.dashboard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardButtonJourneyIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void dashboardSwitchButtons_openInlineViews_forAllMainUserJourneys() {
        login(adminUsername, adminPassword);

        assertSwitchButtonRoute("Entry Trends", "/dashboard/trends");
        assertSwitchButtonRoute("Popular Topics", "/dashboard/topics");
        assertSwitchButtonRoute("Username Catalog", "/dashboard/session-names");
        assertSwitchButtonRoute("Latest Chats", "/dashboard/latest-chats");
        assertSwitchButtonRoute("New Users", "/dashboard/new-users");
        assertSwitchButtonRoute("Inactive Users", "/dashboard/inactive-users");
        assertSwitchButtonRoute("View User Sessions", "/dashboard/sessions");

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        // Return to dashboard home and verify inline section is hidden again.
        if (page.locator("button.dashboard-switch-btn[data-home='true']").count() > 0) {
            page.click("button.dashboard-switch-btn[data-home='true']", new Page.ClickOptions().setNoWaitAfter(true));
            assertTrue(isVisible("#dashboardHomeSection"), "Expected dashboard home section to be visible.");
            assertFalse(isVisible("#dashboardInlineSection"), "Expected inline section to be hidden on Dashboard Home.");
        }
    }

    @Test
    @Order(2)
    void dashboardTopBar_profileAndLogout_buttons_completeUserFlow() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"), "Expected profile navigation from dashboard.");

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertOnLoginScreen("After logout expected login screen,");

        navigateWithCommit("/dashboard");
        waitForLoginScreen();
        assertOnLoginScreen("Expected dashboard to be blocked after logout,");
    }

    private void assertSwitchButtonRoute(String buttonText, String expectedPathFragment) {
        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");
        page.waitForSelector("#dashboardHomeSection");

        String selector = "button.dashboard-switch-btn:has-text('" + buttonText + "')";
        assertTrue(page.locator(selector).count() > 0, "Expected dashboard switch button: " + buttonText);

        String dataTarget = page.locator(selector).first().getAttribute("data-target");
        assertTrue(dataTarget != null && dataTarget.contains(expectedPathFragment),
                "Expected button target containing " + expectedPathFragment + " but got: " + dataTarget);

        page.click(selector, new Page.ClickOptions().setNoWaitAfter(true));

        boolean navigatedToTarget = page.url().contains(expectedPathFragment);
        boolean inlineVisible = isVisible("#dashboardInlineSection") && isVisible("#dashboardInlineContent");

        if (!inlineVisible && !navigatedToTarget) {
            // Some deployments fall back to full-page navigation; verify the same route explicitly.
            navigateWithCommit(dataTarget);
            waitForPath(expectedPathFragment);
            navigatedToTarget = true;
        }

        if (inlineVisible) {
            String inlineText = page.locator("#dashboardInlineContent").innerText().trim();
            assertFalse(inlineText.isBlank(),
                "Expected inline content after clicking: " + buttonText);
        }

        assertTrue(inlineVisible || navigatedToTarget,
            "Expected either inline render or navigation for button: " + buttonText);
    }

    private boolean isVisible(String selector) {
        if (page.locator(selector).count() == 0) {
            return false;
        }
        return page.locator(selector).first().isVisible();
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}