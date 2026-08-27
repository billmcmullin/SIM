package com.sim.ui.tests.inactiveusers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.APIResponse;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InactiveUsersListPageIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_listEndpoint_requiresLogin() {
        APIResponse response = page.request().get(baseUrl + "/dashboard/inactive-users/list?scope=all");

        assertTrue(response.status() == 200 || response.status() == 401,
                "Expected login-forward or unauthorized status, got: " + response.status());
        String body = response.text();
        assertTrue(
                body.contains("id=\"loginForm\"")
                        || body.contains("input id=\"username\"")
                        || body.contains("name=\"username\""),
                "Expected login form markers in unauthenticated response body.");
    }

    @Test
    @Order(2)
    void listPage_rendersAndNormalizesInvalidPaging_whenAuthenticated() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/inactive-users/list?scope=all&days=-10&page=0&limit=abc&search=demo");
        waitForPath("/chat-server/dashboard/inactive-users/list");
        page.waitForSelector("#inactiveListBody");

        assertTrue(page.title().toLowerCase().contains("inactive users"));
        assertTrue(page.locator("#searchInput").count() > 0);
        assertTrue(page.locator("#searchBtn").count() > 0);
        assertTrue(page.locator("#clearSearchBtn").count() > 0);
        assertTrue(page.locator("#inactiveListBody").count() > 0);

        assertTrue(page.evaluate("() => !!window.inactiveUsersListConfig").equals(Boolean.TRUE));
        assertTrue(page.evaluate("() => String(window.inactiveUsersListConfig.limit)").equals("10"),
                "Expected invalid limit query to normalize to default 10.");
        assertTrue(page.evaluate("() => String(window.inactiveUsersListConfig.page)").equals("1"),
                "Expected invalid page query to normalize to default 1.");
    }

    @Test
    @Order(3)
    void listPage_logout_blocksSubsequentAccess() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/inactive-users/list?scope=all");
        waitForPath("/chat-server/dashboard/inactive-users/list");

        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertOnLoginScreen("After logout expected login screen,");

        APIResponse response = page.request().get(baseUrl + "/dashboard/inactive-users/list?scope=all");
        assertEquals(200, response.status(), "Expected login-forward status after logout.");
        assertTrue(response.text().contains("id=\"loginForm\""),
                "Expected login form in response after logout.");
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}
