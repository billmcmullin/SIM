package com.sim.ui.tests.newusers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.RequestOptions;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardNewUsersIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_pageRedirectsToLogin() {
        page.navigate(baseUrl + "/dashboard/new-users");

        waitForLoginScreen();
        assertOnLoginScreen("Expected redirect/forward to login,");
    }

    @Test
    @Order(2)
    void page_rendersCoreSections_whenAuthenticated() {
        login(adminUsername, adminPassword);

        page.navigate(baseUrl + "/dashboard/new-users");
        page.waitForURL(url -> url.contains("/chat-server/dashboard/new-users"));

        assertTrue(page.title().contains("New Session ID / User Metrics"));
        assertTrue(page.locator("h1:has-text('New Session ID / User Metrics')").count() > 0);

        assertTrue(page.locator("#daysRangeSelect").count() > 0);
        assertTrue(page.locator("#newUsersStatus").count() > 0);
        assertTrue(page.locator("#newUsersTrendChart").count() > 0);

        assertTrue(page.locator("#latestNewUsersBody").count() > 0);
        assertTrue(page.locator("#dayResultsTitle").count() > 0);
        assertTrue(page.locator("#dayResultsBody").count() > 0);

        assertTrue(page.locator("a[href*='/dashboard/new-users/drilldown']").count() > 0);
        assertTrue(page.evaluate("() => typeof window.newUsersTrendData === 'string' && window.newUsersTrendData.length > 0").equals(Boolean.TRUE));
    }

    @Test
    @Order(3)
    void dataEndpoint_requiresAuth() {
        APIRequestContext req = playwright.request().newContext(
                new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true)
        );

        APIResponse response = req.get(baseUrl + "/dashboard/new-users/data");
        assertEquals(401, response.status(), "Expected 401 for unauthenticated /data");
        assertTrue(response.text().contains("\"status\":\"error\""));
    }

    @Test
    @Order(4)
    void dataEndpoint_returnsOk_forAllowedAndInvalidDays() {
        login(adminUsername, adminPassword);

        APIResponse ok7 = page.request().get(baseUrl + "/dashboard/new-users/data?days=7");
        assertEquals(200, ok7.status());
        assertTrue(ok7.text().contains("\"status\":\"ok\""));
        assertTrue(ok7.text().contains("\"trend\""));

        APIResponse ok14 = page.request().get(baseUrl + "/dashboard/new-users/data?days=14");
        assertEquals(200, ok14.status());
        assertTrue(ok14.text().contains("\"status\":\"ok\""));

        // Invalid days should gracefully fallback (server default behavior)
        APIResponse invalid = page.request().get(baseUrl + "/dashboard/new-users/data?days=999");
        assertEquals(200, invalid.status());
        assertTrue(invalid.text().contains("\"status\":\"ok\""));
    }

    @Test
    @Order(5)
    void dayEndpoint_validAndInvalidInputs() {
        login(adminUsername, adminPassword);

        APIResponse missing = page.request().get(baseUrl + "/dashboard/new-users/day");
        assertEquals(400, missing.status(), "Expected 400 when day is missing");
        assertTrue(missing.text().contains("Missing or invalid day"));

        APIResponse bad = page.request().get(baseUrl + "/dashboard/new-users/day?day=bad-date");
        assertEquals(400, bad.status(), "Expected 400 when day format invalid");
        assertTrue(bad.text().contains("Missing or invalid day"));

        APIResponse valid = page.request().get(
                baseUrl + "/dashboard/new-users/day?day=2026-05-21",
                RequestOptions.create().setHeader("Accept", "application/json")
        );
        assertEquals(200, valid.status());
        assertTrue(valid.text().contains("\"status\":\"ok\""));
        assertTrue(valid.text().contains("\"rows\""));
    }

    private void login(String username, String password) {
        page.navigate(baseUrl + "/login");
        waitForLoginScreen();
        assertOnLoginScreen("Expected login page before submitting credentials,");

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
