package com.sim.ui.tests.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileCustomerDeepLinkJourneyIT extends BaseUiIT {

    private final String userUsername = System.getProperty("userUsername", "user");
    private final String userPassword = System.getProperty("userPassword", "user");
    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void sessionsPage_customerProfileDeepLink_opensProfile_withReadOnlyJourney() {
        loginAsAvailableUser();

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        String sessionsTarget = readDashboardButtonTarget("View User Sessions", "/dashboard/sessions");
        page.click("button.dashboard-switch-btn:has-text('View User Sessions')",
                new Page.ClickOptions().setNoWaitAfter(true));

        // Open the route directly after click to keep behavior deterministic across
        // inline and full-page dashboard modes.
        navigateWithCommit(normalizeDashboardTarget(sessionsTarget));
        waitForPath("/dashboard/sessions");

        page.waitForSelector("#sessionsBody");
        assertTrue(page.locator("a.customer-profile-link").count() > 0,
                "Expected at least one customer profile deep-link in sessions view.");

        page.click("a.customer-profile-link", new Page.ClickOptions().setNoWaitAfter(true));
        waitForPath("/customer-profile");

        page.waitForSelector("#cpSessionId");
        assertTrue(page.title().contains("Customer Profile"));
        assertFalse(page.locator("#cpSessionId").innerText().trim().isBlank(),
                "Expected customer profile session identity to be rendered.");

        // Read-only interactions only: search/clear do not mutate persisted data.
        assertTrue(page.locator("#cpChatSearchInput").count() > 0);
        assertTrue(page.locator("#cpChatSearchBtn").count() > 0);
        assertTrue(page.locator("#cpChatClearSearchBtn").count() > 0);

        page.fill("#cpChatSearchInput", "playwright-readonly-profile-search");
        page.click("#cpChatSearchBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#cpSubmittedChatsBody");

        page.click("#cpChatClearSearchBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#cpSubmittedChatsBody");

        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"),
                "Expected customer profile top bar to navigate to profile page.");
    }

    @Test
    @Order(2)
    void usernameCatalog_customerProfileDeepLink_opensProfile_withReadOnlyJourney() {
        loginAsAvailableUser();

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        String catalogTarget = readDashboardButtonTarget("Username Catalog", "/dashboard/session-names");
        page.click("button.dashboard-switch-btn:has-text('Username Catalog')",
                new Page.ClickOptions().setNoWaitAfter(true));

        // Open the route directly after click to keep behavior deterministic across
        // inline and full-page dashboard modes.
        navigateWithCommit(normalizeDashboardTarget(catalogTarget));
        waitForPath("/dashboard/session-names");

        page.waitForSelector("#sessionNameList");
        assertTrue(page.locator("#sessionSearch").count() > 0);
        assertTrue(page.locator("#sessionSearchButton").count() > 0);
        assertTrue(page.locator("#toggleLabeledOnlyBtn").count() > 0);

        // Keep query broad/empty so the deep-link assertion remains stable.
        page.fill("#sessionSearch", "");
        page.click("#sessionSearchButton", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionNameList");

        // Exercise labeled-only toggle in both directions to avoid hiding all rows
        // in environments where no labeled entries are present.
        page.click("#toggleLabeledOnlyBtn", new Page.ClickOptions().setNoWaitAfter(true));
        assertTrue(page.locator("#toggleLabeledOnlyBtn").innerText().contains("On")
                        || page.locator("#toggleLabeledOnlyBtn").innerText().contains("Off"),
                "Expected labeled-only toggle state label to be rendered.");
        page.click("#toggleLabeledOnlyBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionNameList tr");

        int catalogLinkCount = page.locator("#sessionNameList a.customer-profile-link").count();
        Assumptions.assumeTrue(catalogLinkCount > 0,
            "Username catalog currently has no customer-profile deep-links for this dataset.");

        page.click("#sessionNameList a.customer-profile-link", new Page.ClickOptions().setNoWaitAfter(true));
        waitForPath("/customer-profile");

        page.waitForSelector("#cpSessionId");
        assertTrue(page.title().contains("Customer Profile"));
        assertTrue(page.locator("#cpFriendlyName").count() > 0,
                "Expected customer profile identity panel to be rendered.");

        clickNavButtonNoWait("Dashboard", "/chat-server/dashboard");
        assertTrue(page.url().contains("/chat-server/dashboard"),
                "Expected customer profile top bar to navigate to dashboard page.");
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