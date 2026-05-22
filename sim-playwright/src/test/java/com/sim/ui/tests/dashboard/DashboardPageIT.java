package com.sim.ui.tests.dashboard;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardPageIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    private final String userUsername = System.getProperty("userUsername", "user");
    private final String userPassword = System.getProperty("userPassword", "user");

    @Test
    @Order(1)
    void unauthenticated_dashboardRedirectsToLogin() {
        page.navigate(
                baseUrl + "/dashboard",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(30000)
        );
        page.waitForURL(
                url -> url.contains("/chat-server/login"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        assertTrue(page.url().contains("/chat-server/login"),
                "Expected redirect to /chat-server/login, got: " + page.url());
    }

    @Test
    @Order(2)
    void admin_seesDashboardCoreSections_andAdminLink() {
        login(adminUsername, adminPassword);

        page.navigate(
                baseUrl + "/dashboard",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(30000)
        );
        page.waitForURL(
                url -> url.contains("/chat-server/dashboard"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        // Wait for core content to be visible before assertions
        page.waitForSelector("h1:has-text('Welcome')");
        page.waitForSelector("h2:has-text('Daily Progress')");

        assertTrue(page.locator("h1").first().innerText().contains("Welcome"),
                "Expected Welcome heading");
        assertTrue(page.locator("h2:has-text('Daily Progress')").count() > 0);
        assertTrue(page.locator("h2:has-text('Widget Chat Overview')").count() > 0);
        assertTrue(page.locator("h2:has-text('Term Distribution based on Prompts')").count() > 0);
        assertTrue(page.locator("h2:has-text('Top 10 Sessions')").count() > 0);

        assertTrue(page.locator("a[href$='/admin']:has-text('Go to Admin Configuration')").count() > 0,
                "Admin should see Admin Configuration link");

        assertTrue(page.locator("#dpTodayChats").count() > 0);
        assertTrue(page.locator("#dpTopTermsBody").count() > 0);
        assertTrue(page.locator("#otherParasoftLatestBody").count() > 0);
        assertTrue(page.locator("#widgetStatsBody").count() > 0);
        assertTrue(page.locator("#topSessionList").count() > 0);
    }

    @Test
    @Order(3)
    void user_doesNotSeeAdminLink() {
        login(userUsername, userPassword);

        page.navigate(
                baseUrl + "/dashboard",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(30000)
        );
        page.waitForURL(
                url -> url.contains("/chat-server/dashboard"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        page.waitForSelector("h1:has-text('Welcome')");

        assertFalse(page.locator("a[href$='/admin']:has-text('Go to Admin Configuration')").count() > 0,
                "Non-admin user should not see Admin Configuration link");
    }

    @Test
    @Order(4)
    void msg_noIncreaseForTerm_showsBanner() {
        login(adminUsername, adminPassword);

        page.navigate(
                baseUrl + "/dashboard?msg=noIncreaseForTerm",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(30000)
        );
        page.waitForURL(
                url -> url.contains("/chat-server/dashboard"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        page.waitForSelector(".dashboard-info-banner");

        assertTrue(page.locator(".dashboard-info-banner:has-text('No increased chats found for that term today.')").count() > 0);
    }

    @Test
    @Order(5)
    void msg_noYesterdayForTerm_showsBanner() {
        login(adminUsername, adminPassword);

        page.navigate(
                baseUrl + "/dashboard?msg=noYesterdayForTerm",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(30000)
        );
        page.waitForURL(
                url -> url.contains("/chat-server/dashboard"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        page.waitForSelector(".dashboard-info-banner");

        assertTrue(page.locator(".dashboard-info-banner:has-text('No chats found for that term yesterday.')").count() > 0);
    }

    private void login(String username, String password) {
        page.navigate(
                baseUrl + "/login",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(30000)
        );
        page.waitForURL(
                url -> url.contains("/chat-server/login"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        page.fill("#username", username);
        page.fill("#password", password);
        page.click("button[type='submit']");

        page.waitForURL(
                url -> url.contains("/chat-server/dashboard") || url.contains("/chat-server/admin"),
                new Page.WaitForURLOptions().setTimeout(30000)
        );

        assertTrue(
                page.url().contains("/chat-server/dashboard") || page.url().contains("/chat-server/admin"),
                "Login expected /dashboard or /admin, got: " + page.url()
        );
    }
}
