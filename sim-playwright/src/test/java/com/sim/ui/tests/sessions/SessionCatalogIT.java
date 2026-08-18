package com.sim.ui.tests.sessions;

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
public class SessionCatalogIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void pageRenders_coreUi_afterLogin() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/session-names");
        waitForPath("/chat-server/dashboard/session-names");
        page.waitForSelector("h1:has-text('Username Catalog')");

        assertTrue(page.title().contains("Session Catalog"));
        assertTrue(page.locator("h1:has-text('Username Catalog')").count() > 0);

        assertTrue(page.locator("#sessionSearch").count() > 0);
        assertTrue(page.locator("#sessionSearchButton").count() > 0);
        assertTrue(page.locator("#toggleLabeledOnlyBtn").count() > 0);
        assertTrue(page.locator("#pageSizeSelect").count() > 0);
        assertTrue(page.locator("#prevPageBtn").count() > 0);
        assertTrue(page.locator("#nextPageBtn").count() > 0);
        assertTrue(page.locator("#sessionNameList").count() > 0);
        assertTrue(page.locator("#sessionEditTemplate").count() > 0);
    }

    @Test
    @Order(2)
    void sessionsData_requiresAuth() {
        APIRequestContext req = playwright.request().newContext(
                new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true)
        );

        APIResponse response = req.get(baseUrl + "/dashboard/sessions/data");
        assertEquals(401, response.status(), "Expected 401 when not authenticated");
        assertTrue(response.text().contains("\"status\":\"error\""));
    }

    @Test
    @Order(3)
    void chatsEndpoint_requiresSessionId() {
        login(adminUsername, adminPassword);

        APIResponse response = page.request().get(baseUrl + "/dashboard/sessions/chats");
        assertEquals(400, response.status(), "Expected 400 when sessionId missing");
        assertTrue(response.text().contains("sessionId required"));
    }

    @Test
    @Order(4)
    void selectEndpoint_rejectsInvalidPayload() {
        login(adminUsername, adminPassword);

        APIResponse response = page.request().post(
                baseUrl + "/dashboard/sessions/select",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData("{\"bad\":\"payload\"}")
        );

        assertEquals(400, response.status(), "Expected 400 when selectedChatIds missing");
        assertTrue(response.text().contains("selectedChatIds required"));
    }

    @Test
    @Order(5)
    void unauthenticated_sessionsPage_redirectsToLogin() {
        navigateWithCommit("/dashboard/sessions");
        waitForLoginScreen();
        assertOnLoginScreen("Expected sessions page navigation to require login,");
    }

    @Test
    @Order(6)
    void sessionsPage_renders_coreUi_afterLogin() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard/sessions");
        waitForPath("/chat-server/dashboard/sessions");
        page.waitForSelector("h1:has-text('Review Sessions')");

        assertTrue(page.title().contains("Review Sessions"));
        assertTrue(page.locator("#searchInput").count() > 0);
        assertTrue(page.locator("#searchBtn").count() > 0);
        assertTrue(page.locator("#reviewSelectedBtn").count() > 0);
        assertTrue(page.locator("#sessionsTable").count() > 0);
        assertTrue(page.locator("#sessionsBody").count() > 0);
        assertTrue(page.locator("#sessionsPagination").count() > 0);
    }

    @Test
    @Order(7)
    void chatsEndpoint_unknownSession_returnsOkWithRowsArray() {
        login(adminUsername, adminPassword);

        APIResponse response = page.request().get(
                baseUrl + "/dashboard/sessions/chats?sessionId=does-not-exist"
        );

        assertEquals(200, response.status(), "Expected 200 for known endpoint with unknown sessionId");
        String body = response.text();
        assertTrue(body.contains("\"status\":\"ok\""));
        assertTrue(body.contains("\"rows\":["));
    }

    @Test
    @Order(8)
    void selectEndpoint_rejectsNonStringSelectedIds() {
        login(adminUsername, adminPassword);

        APIResponse response = page.request().post(
                baseUrl + "/dashboard/sessions/select",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData("{\"selectedChatIds\":[1,true,null,{\"x\":1}]}")
        );

        assertEquals(400, response.status(), "Expected 400 when selectedChatIds contains no valid string IDs");
        assertTrue(response.text().contains("No valid chat IDs provided."));
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}
