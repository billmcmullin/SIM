package com.sim.ui.tests.drilldown;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.RequestOptions;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WidgetReviewIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_redirectsToLogin() {
        page.navigate(baseUrl + "/dashboard/widgets/drilldown/review?selectionId=test");
        page.waitForURL(url -> url.contains("/chat-server/login"));

        assertTrue(page.url().contains("/chat-server/login"),
                "Expected redirect to /chat-server/login, got: " + page.url());
    }

    @Test
    @Order(2)
    void missingSelectionId_returns400() {
        login(adminUsername, adminPassword);

        APIResponse res = page.request().get(baseUrl + "/dashboard/widgets/drilldown/review");
        assertEquals(400, res.status(), "Expected 400 when selectionId is missing");
    }

    @Test
    @Order(3)
    void unknownSelectionId_returns404() {
        login(adminUsername, adminPassword);

        APIResponse res = page.request().get(
                baseUrl + "/dashboard/widgets/drilldown/review?selectionId=does-not-exist"
        );
        assertEquals(404, res.status(), "Expected 404 when selection not found");
    }

    @Test
    @Order(4)
    void reviewPage_renders_whenSelectionAutoBuiltFromLiveSessionData() {
        login(adminUsername, adminPassword);

        String anySessionId = findAnySessionId();
        assumeTrue(anySessionId != null && !anySessionId.isBlank(),
                "Skipping: no available session IDs found in /dashboard/sessions/data.");

        List<String> chatIds = findChatIdsForSession(anySessionId, 2);
        assumeTrue(!chatIds.isEmpty(),
                "Skipping: no chats found for sessionId=" + anySessionId);

        String selectionId = createSelectionFromChatIds(chatIds);
        assumeTrue(selectionId != null && !selectionId.isBlank(),
                "Skipping: unable to create selection from discovered chat IDs.");

        page.navigate(baseUrl + "/dashboard/widgets/drilldown/review?selectionId=" + urlEncode(selectionId));
        page.waitForURL(url -> url.contains("/chat-server/dashboard/widgets/drilldown/review"));

        assertTrue(page.title().contains("Review Selected Chats"));
        assertTrue(page.locator("h1:has-text('Review Selected Chats')").count() > 0);

        // Core controls
        assertTrue(page.locator("#reviewSearchInput").count() > 0);
        assertTrue(page.locator("#reviewPageSize").count() > 0);
        assertTrue(page.locator("#selectAllEntriesBtn").count() > 0);
        assertTrue(page.locator("#deselectAllBtn").count() > 0);
        assertTrue(page.locator("#manualMessageToggleBtn").count() > 0);
        assertTrue(page.locator("#exportSelectedBtn").count() > 0);
        assertTrue(page.locator("#exportFormatSelect").count() > 0);

        // Table + detail panel anchors
        assertTrue(page.locator("#widgetReviewBody").count() > 0);
        assertTrue(page.locator("#detailCard").count() > 0);

        // Config injected by template
        assertTrue(page.evaluate("() => !!window.widgetReviewConfig").equals(Boolean.TRUE));
        assertTrue(page.evaluate("() => !!window.widgetReviewConfig.selectionId").equals(Boolean.TRUE));
    }

    private String findAnySessionId() {
        APIResponse response = page.request().get(baseUrl + "/dashboard/sessions/data?all=true");
        if (response.status() != 200) {
            return null;
        }
        String body = response.text();

        // looks for first: "sessionId":"..."
        return extractFirstJsonStringValue(body, "sessionId");
    }

    private List<String> findChatIdsForSession(String sessionId, int max) {
        List<String> out = new ArrayList<>();
        APIResponse response = page.request().get(
                baseUrl + "/dashboard/sessions/chats?sessionId=" + urlEncode(sessionId)
        );
        if (response.status() != 200) {
            return out;
        }
        String body = response.text();

        // gather repeated "chatId":"..."
        String needle = "\"chatId\":\"";
        int from = 0;
        while (out.size() < max) {
            int idx = body.indexOf(needle, from);
            if (idx < 0) {
                break;
            }
            int start = idx + needle.length();
            int end = body.indexOf('"', start);
            if (end <= start) {
                break;
            }
            String chatId = body.substring(start, end);
            if (!chatId.isBlank()) {
                out.add(chatId);
            }
            from = end + 1;
        }
        return out;
    }

    private String createSelectionFromChatIds(List<String> chatIds) {
        if (chatIds == null || chatIds.isEmpty()) {
            return null;
        }

        StringBuilder b = new StringBuilder();
        b.append("{\"selectedChatIds\":[");
        for (int i = 0; i < chatIds.size(); i++) {
            if (i > 0) {
                b.append(",");
            }
            b.append("\"").append(chatIds.get(i).replace("\"", "\\\"")).append("\"");
        }
        b.append("]}");

        APIResponse response = page.request().post(
                baseUrl + "/dashboard/sessions/select",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(b.toString())
        );

        if (response.status() != 200) {
            return null;
        }

        return extractFirstJsonStringValue(response.text(), "selectionId");
    }

    private String extractFirstJsonStringValue(String json, String key) {
        if (json == null || key == null || key.isBlank()) {
            return null;
        }
        String needle = "\"" + key + "\":\"";
        int idx = json.indexOf(needle);
        if (idx < 0) {
            return null;
        }
        int start = idx + needle.length();
        int end = json.indexOf('"', start);
        if (end <= start) {
            return null;
        }
        return json.substring(start, end);
    }

    private String urlEncode(String v) {
        return java.net.URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private void login(String username, String password) {
        page.navigate(baseUrl + "/login");
        page.waitForURL(url -> url.contains("/chat-server/login"));

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
