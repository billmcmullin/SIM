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
import com.microsoft.playwright.options.RequestOptions;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardNewUsersIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_pageRedirectsToLogin() {
        navigateWithCommit("/dashboard/new-users");

        waitForLoginScreen();
        assertOnLoginScreen("Expected redirect/forward to login,");
    }

    @Test
    @Order(2)
    void page_rendersCoreSections_whenAuthenticated() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/new-users");
        waitForPath("/chat-server/dashboard/new-users");
        page.waitForSelector("h1:has-text('New Session ID / User Metrics')");

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

    @Test
    @Order(6)
    void dayDataAlias_requiresAuth() {
        APIRequestContext req = playwright.request().newContext(
                new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true)
        );
        try {
            APIResponse response = req.get(baseUrl + "/dashboard/new-users/day-data?day=2026-05-21");
            assertEquals(401, response.status(), "Expected 401 for unauthenticated /day-data");
            assertTrue(response.text().contains("\"status\":\"error\""));
        } finally {
            req.dispose();
        }
    }

    @Test
    @Order(7)
    void dayDataAlias_validAndInvalidInputs() {
        login(adminUsername, adminPassword);

        APIResponse bad = page.request().get(baseUrl + "/dashboard/new-users/day-data?day=bad-date");
        assertEquals(400, bad.status(), "Expected 400 when /day-data day format is invalid");
        assertTrue(bad.text().contains("Missing or invalid day"));

        APIResponse valid = page.request().get(baseUrl + "/dashboard/new-users/day-data?day=2026-05-21");
        assertEquals(200, valid.status(), "Expected 200 for valid /day-data request");
        String body = valid.text();
        assertTrue(body.contains("\"status\":\"ok\""));
        assertTrue(body.contains("\"rows\""));
    }

    @Test
    @Order(8)
    void pageQuery_days_normalizesUnsupportedToDefault() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/new-users?days=30");
        waitForPath("/chat-server/dashboard/new-users");
        assertTrue("30".equals(page.inputValue("#daysRangeSelect")),
                "Expected selected period 30 for supported value.");

        navigateWithCommit("/dashboard/new-users?days=999");
        waitForPath("/chat-server/dashboard/new-users");
        assertTrue("7".equals(page.inputValue("#daysRangeSelect")),
                "Expected unsupported days to normalize to default 7.");
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}
