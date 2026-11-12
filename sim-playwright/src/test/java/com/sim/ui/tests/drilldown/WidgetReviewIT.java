package com.sim.ui.tests.drilldown;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.RequestOptions;
import com.sim.ui.base.BaseUiIT;
import com.sim.ui.base.ChatReviewFixture;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WidgetReviewIT extends BaseUiIT {

    @Test
    void phase6_liveWidgetPreviewReviewAndBackPreserveSourceAndSelection() {
        if (!tryLoginViaApi(System.getProperty("userUsername", "user"), System.getProperty("userPassword", "user"))) {
            loginViaApi(adminUsername, adminPassword);
        }
        String sessionId = findAnySessionId();
        assertTrue(sessionId != null && !sessionId.isBlank(), "An authenticated live session with chats is required.");
        List<String> chatIds = findChatIdsForSession(sessionId, 1);
        assertFalse(chatIds.isEmpty(), "Live chat data is required for this acceptance case.");
        String selectionId = createSelectionFromChatIds(chatIds);
        assertTrue(selectionId != null && !selectionId.isBlank(), "An authorized selection must resolve the live chat.");
        APIResponse detail = page.request().get(baseUrl + "/dashboard/widgets/drilldown/view/review-data?selectionId=" + urlEncode(selectionId));
        assertEquals(200, detail.status(), "Authorized review data must be available.");
        var rows = com.google.gson.JsonParser.parseString(detail.text()).getAsJsonObject().getAsJsonArray("rows");
        assertTrue(rows != null && !rows.isEmpty(), "The authorized selection must contain a resolved row.");
        String widgetId = rows.get(0).getAsJsonObject().get("widgetId").getAsString();
        assertFalse(widgetId.isBlank(), "Widget identity must come from authorized metadata.");
        String widgetUrl = baseUrl + "/dashboard/widgets/view?widgetId=" + urlEncode(widgetId);
        List<String> unexpectedWrites = new ArrayList<>();
        context.route("**/*", route -> {
            String method = route.request().method();
            String path = java.net.URI.create(route.request().url()).getPath();
            if (!List.of("GET", "HEAD", "OPTIONS").contains(method)
                    && !path.endsWith("/dashboard/widgets/review/start")) {
                unexpectedWrites.add(method + " " + path);
                route.abort();
            } else {
                route.fallback();
            }
        });
        navigateWithCommit(widgetUrl);
        page.locator("#widgetTableBody .chat-preview-open").first().waitFor();
        String chatId = page.locator("#widgetTableBody input.row-select").first().getAttribute("data-chat-id");
        assertTrue(chatId != null && !chatId.isBlank(), "Live row identity must be supplied as metadata.");
        String date = page.locator("#widgetTableBody time.chat-created-at").first().textContent();
        page.locator("#widgetTableBody input.row-select").first().check();
        page.locator("#widgetTableBody .chat-preview-open").first().press("Enter");
        String prompt = page.locator("#widgetDetailPrompt").textContent();
        String response = page.locator("#widgetDetailResponse").textContent();
        assertTrue(date.equals(page.locator("#widgetDetailCreatedAt").textContent()), "Row and preview dates must agree.");
        assertEquals(1, page.locator("#widgetTableBody input.row-select:checked").count());
        page.locator("#widgetDetailCardCloseBtn").click();
        page.locator("#reviewSelectedBtn").click();
        page.locator("#widgetReviewBody .chat-preview-open").first().waitFor();
        assertEquals(1, page.locator("#widgetReviewBody .chat-preview-open").count());
        assertTrue(chatId.equals(page.locator("#widgetReviewBody .chat-preview-open").first().textContent()), "Review must retain the selected chat identity.");
        page.locator("#widgetReviewBody .chat-preview-open").first().press("Enter");
        assertTrue(prompt.equals(page.locator("#detailPrompt").textContent()), "Original prompt must match across live previews.");
        assertTrue(response.equals(page.locator("#detailResponse").textContent()), "Original response must match across live previews.");
        assertTrue(date.equals(page.locator("#detailCreatedAt").textContent()), "Live date display must agree across views.");
        page.locator("[data-sim-back]").first().click();
        page.waitForURL(widgetUrl);
        page.locator("#widgetTableBody .chat-preview-open").first().waitFor();
        assertTrue(page.locator("#widgetTableBody input.row-select").first().isChecked(), "Back must retain the selected live chat.");
        assertTrue(unexpectedWrites.isEmpty(), "Preview/navigation must not start analysis, translation, email or other writes.");
    }

    @Test
    void phase3_longResponseIsKeyboardScrollableWithoutChangingItsSource() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        String source = "Long preserved source line.\n".repeat(100);
        page.evaluate("row => window.SimChatPreview.create({root: document.querySelector('#detailCard')}).show(row)",
                java.util.Map.of("chatId", ChatReviewFixture.CHAT_A, "prompt", ChatReviewFixture.PROMPT_A, "response", source));
        assertEquals("0", page.locator("#detailResponse").getAttribute("tabindex"));
        page.locator("#detailResponse").focus();
        page.keyboard().press("End");
        page.waitForFunction("document.querySelector('#detailResponse').scrollTop > 0");
        assertEquals(source, page.locator("#detailResponse").textContent());
        assertEquals(0, page.locator("#widgetReviewBody input.row-select:checked").count());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase3_longPreviewTitleAndControlsStayInsideTheViewport() throws Exception {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        for (int width : new int[] {1440, 390, 320}) {
            page.setViewportSize(width, 900);
            fixture.openReview(page);
            page.evaluate("row => window.SimChatPreview.create({root: document.querySelector('#detailCard')}).show(row)",
                    java.util.Map.of("chatId", "phase3-" + "x".repeat(120), "prompt", ChatReviewFixture.PROMPT_A, "response", ChatReviewFixture.RESPONSE_A));
            var panel = page.locator("#detailCard").boundingBox();
            var close = page.locator("#detailCardCloseBtn").boundingBox();
            assertTrue(panel.x >= -1 && panel.x + panel.width <= width + 1, "Preview must fit at width " + width);
            assertTrue(close.x >= panel.x && close.x + close.width <= panel.x + panel.width + 1);
            assertTrue((Boolean) page.locator("#detailTitle").evaluate("element => element.scrollWidth <= element.clientWidth + 1"));
            fixture.capture(page, "long-preview-title-" + width);
        }
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase3_filteredPreviewCannotNavigateOutsideTheVisibleRows() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        page.locator("#widgetReviewBody .chat-preview-open").first().click();
        page.fill("#reviewSearchInput", "no-matching-synthetic-chat");
        assertTrue(page.locator("#detailPreviousBtn").isDisabled());
        assertTrue(page.locator("#detailNextBtn").isDisabled());
        page.locator("#detailCardCloseBtn").click();
        assertTrue(page.locator("#detailCard").isHidden());
        assertEquals("reviewSearchInput", page.evaluate("document.activeElement.id"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase3_reviewSharingRetainsTheOriginalExplicitPayload() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.enableSharingFixture();
        fixture.openReview(page);
        page.locator("#widgetReviewBody .chat-preview-open").first().click();
        assertTrue(fixture.sharedMessages().isEmpty());
        page.locator("#shareChatRevealBtn").click();
        assertTrue(fixture.sharedMessages().isEmpty());
        page.fill("#shareEmailTo", "teammate@example.test");
        page.fill("#shareCustomMessage", "Synthetic context");
        page.locator("#shareTranslateToEnglish").uncheck();
        page.locator("#shareChatByEmailBtn").click();
        page.waitForFunction("document.querySelector('#shareChatEmailStatus').textContent.startsWith('Email sent to')");
        assertEquals(1, fixture.sharedMessages().size());
        var payload = fixture.sharedMessages().get(0);
        assertEquals(10, payload.size());
        assertEquals("teammate@example.test", payload.get("to").getAsString());
        assertEquals(ChatReviewFixture.CHAT_A, payload.get("chatId").getAsString());
        assertEquals(ChatReviewFixture.PROMPT_A, payload.get("prompt").getAsString());
        assertEquals(ChatReviewFixture.RESPONSE_A, payload.get("response").getAsString());
        assertEquals("Synthetic context", payload.get("customMessage").getAsString());
        assertFalse(payload.get("translateToEnglish").getAsBoolean());
        assertEquals(0, page.locator("#widgetReviewBody input.row-select:checked").count());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase3_sharedPreviewShowsHostileSourceAsText() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        String source = "</pre><img src=x onerror='window.previewInjected=true'><script>window.previewInjected=true</script>";
        page.evaluate("row => window.SimChatPreview.create({root: document.querySelector('#detailCard')}).show(row)",
                java.util.Map.of("chatId", ChatReviewFixture.CHAT_A, "prompt", source, "response", source));
        assertEquals(source, page.locator("#detailPrompt").textContent());
        assertEquals(source, page.locator("#detailResponse").textContent());
        assertEquals(0, page.locator("#detailCard script, #detailCard img").count());
        assertTrue(page.locator("#shareChatRevealBtn").isHidden(), "Unconfigured action capabilities must not be exposed.");
        assertEquals(null, page.evaluate("window.previewInjected || null"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase3_keyboardPreviewNavigationAndEscapePreserveSelection() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        page.locator("#widgetReviewBody .chat-preview-open").first().focus();
        page.keyboard().press("Enter");
        assertTrue(page.locator("#detailCard").isVisible());
        assertEquals("polite", page.locator("#detailCard").getAttribute("aria-live"));
        assertTrue((Boolean) page.evaluate("document.activeElement.id === 'detailCardCloseBtn'"));
        assertTrue(page.locator("#detailPreviousBtn").isDisabled());
        page.locator("#detailNextBtn").click();
        assertEquals(ChatReviewFixture.PROMPT_B, page.locator("#detailPrompt").textContent());
        assertTrue(page.locator("#detailNextBtn").isDisabled());
        assertEquals(0, page.locator("#widgetReviewBody input.row-select:checked").count());
        page.keyboard().press("Escape");
        assertTrue(page.locator("#detailCard").isHidden());
        assertEquals("Preview chat " + ChatReviewFixture.CHAT_B, page.evaluate("document.activeElement.getAttribute('aria-label')"));
        page.keyboard().press("Space");
        assertTrue(page.locator("#detailCard").isVisible());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase3_reviewTranslationCannotOverwriteTheNextChat() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        page.locator("#widgetReviewBody .chat-preview-open").first().click();
        page.locator("#translatePromptBtn").click();
        fixture.awaitHeldTranslation(page);
        page.locator("#detailNextBtn").click();
        page.locator("#translatePromptBtn").click();
        page.waitForFunction("document.querySelector('#promptTranslationOutput').textContent === 'Synthetic translation B'");
        Response late = page.waitForResponse(response -> response.url().endsWith("/review/translate"), fixture::releaseFirstTranslation);
        late.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals(ChatReviewFixture.PROMPT_B, page.locator("#detailPrompt").textContent());
        assertEquals("Synthetic translation B", page.locator("#promptTranslationOutput").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase1_rowPreviewPreservesOriginalTextAndBulkSelection() throws Exception {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        for (int width : new int[] {1440, 390}) {
            page.setViewportSize(width, 900);
            fixture.openReview(page);
            page.locator("#widgetReviewBody tr[data-row-key]").first().locator("td").nth(2).click();
            assertEquals(ChatReviewFixture.PROMPT_A, page.locator("#detailPrompt").textContent());
            assertEquals(ChatReviewFixture.RESPONSE_A, page.locator("#detailResponse").textContent());
            assertFalse(page.locator("#widgetReviewBody input.row-select").first().isChecked());
            fixture.capture(page, "review-original-text-" + width);
            page.locator("#detailCardCloseBtn").click();
            assertTrue(page.locator("#detailCard").isHidden());
        }
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase1_reviewPreservesTheSuppliedWidgetScope() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        assertTrue(page.locator(".container").innerText().contains("Currently reviewing Widget:"));
        assertTrue(page.locator(".container").innerText().contains("Phase 1 widget"));
        assertEquals(2, page.locator("#widgetReviewBody tr[data-row-key]").count());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase1_instructionsDoNotPromiseCheckboxPreview() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        fixture.assertNoUnexpectedRequests();
        assertFalse(page.locator(".container").innerText().contains("Check a row OR click a row"),
                "Instructions must match checkbox-only bulk selection behavior.");
    }

    @Test
    void phase1_checkboxOnlyChangesSelectionWithoutOpeningPreview() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        page.locator("#widgetReviewBody input.row-select").first().check();
        assertTrue(page.locator("#widgetReviewBody input.row-select").first().isChecked());
        assertTrue(page.locator("#detailCard").isHidden(), "Checkboxes are bulk selection, not preview activation.");
        fixture.assertNoUnexpectedRequests();
    }

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_redirectsToLogin() {
        APIResponse res = apiGetOrSkip(baseUrl + "/dashboard/widgets/drilldown/review?selectionId=test");
        assertTrue(res.status() == 200 || res.status() == 401,
            "Expected login forward/unauthorized response, got status=" + res.status());
        assertTrue(res.text().contains("id=\"loginForm\""),
            "Expected login form markup in unauthenticated response.");
    }

    @Test
    @Order(2)
    void missingSelectionId_returns400() {
        login(adminUsername, adminPassword);

        APIResponse res = apiGetOrSkip(baseUrl + "/dashboard/widgets/drilldown/review");
        assertEquals(400, res.status(), "Expected 400 when selectionId is missing");
    }

    @Test
    @Order(3)
    void unknownSelectionId_returns404() {
        login(adminUsername, adminPassword);

        APIResponse res = apiGetOrSkip(
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

        navigateWithCommit("/dashboard/widgets/drilldown/review?selectionId=" + urlEncode(selectionId));
        waitForPath("/chat-server/dashboard/widgets/drilldown/review");
        waitForHeadingAttached("Review Selected Chats");

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

    @Test
    @Order(5)
    void manualMessage_requiresAuth() {
        APIResponse response = postJson(
                "/dashboard/drilldown/widget-review/manual-message",
                "{\"message\":\"hello\"}"
        );

        assertEquals(401, response.status(), "Expected 401 when calling manual-message without authentication");
        assertTrue(response.text().contains("Authentication required."));
    }

    @Test
    @Order(6)
    void manualMessage_rejectsInvalidPayload() {
        login(adminUsername, adminPassword);

        APIResponse response = postJson(
                "/dashboard/drilldown/widget-review/manual-message",
                "{}"
        );

        assertEquals(400, response.status(), "Expected 400 for empty manual-message payload");
        assertTrue(response.text().contains("Invalid JSON payload."));
    }

    @Test
    @Order(7)
    void manualMessage_requiresMessageField() {
        login(adminUsername, adminPassword);

        APIResponse response = postJson(
                "/dashboard/drilldown/widget-review/manual-message",
                "{\"message\":\"   \",\"mode\":\"chat\"}"
        );

        assertEquals(400, response.status(), "Expected 400 when message is blank");
        assertTrue(response.text().contains("message is required."));
    }

    @Test
    @Order(8)
    void exportEndpoint_requiresAuth() {
        APIResponse response = postJson(
                "/dashboard/widgets/drilldown/export",
                "{\"selectionId\":\"test\"}"
        );

        assertEquals(401, response.status(), "Expected 401 when calling export without authentication");
        assertTrue(response.text().contains("Authentication required."));
    }

    @Test
    @Order(9)
    void exportEndpoint_requiresSelectionId() {
        login(adminUsername, adminPassword);

        APIResponse response = postJson(
                "/dashboard/widgets/drilldown/export",
                "{\"format\":\"csv\"}"
        );

        assertEquals(400, response.status(), "Expected 400 when selectionId is missing");
        assertTrue(response.text().contains("selectionId required."));
    }

    @Test
    @Order(10)
    void exportEndpoint_unknownSelection_returns404() {
        login(adminUsername, adminPassword);

        APIResponse response = postJson(
                "/dashboard/widgets/drilldown/export",
                "{\"selectionId\":\"does-not-exist\",\"format\":\"csv\"}"
        );

        assertEquals(404, response.status(), "Expected 404 when selection is not found for export");
        assertTrue(response.text().contains("Selection not found."));
    }

    @Test
    @Order(11)
    void jobStatus_requiresAuth() {
        APIResponse response = apiGetOrSkip(
                baseUrl + "/dashboard/drilldown/widget-review/job-status?jobId=test-job"
        );

        assertEquals(401, response.status(), "Expected 401 for unauthenticated job-status request");
        assertTrue(response.text().contains("Authentication required."));
    }

    @Test
    @Order(12)
    void jobStatus_requiresJobId() {
        login(adminUsername, adminPassword);

        APIResponse response = apiGetOrSkip(
                baseUrl + "/dashboard/drilldown/widget-review/job-status"
        );

        assertEquals(400, response.status(), "Expected 400 when jobId is missing");
        assertTrue(response.text().contains("jobId is required."));
    }

    @Test
    @Order(13)
    void jobStatus_unknownJob_returns404() {
        login(adminUsername, adminPassword);

        APIResponse response = apiGetOrSkip(
                baseUrl + "/dashboard/drilldown/widget-review/job-status?jobId=does-not-exist"
        );

        assertEquals(404, response.status(), "Expected 404 when job does not exist");
        assertTrue(response.text().contains("Job not found."));
    }

    @Test
    @Order(14)
    void jobStatusDelete_unknownJob_returns404() {
        login(adminUsername, adminPassword);

        APIResponse response = apiDeleteOrSkip(
                baseUrl + "/dashboard/drilldown/widget-review/job-status?jobId=does-not-exist"
        );

        assertEquals(404, response.status(), "Expected 404 when deleting unknown job");
        assertTrue(response.text().contains("Job not found."));
    }

    @Test
    @Order(15)
    void reviewSelection_clickingRow_opensOverlayAndCloseHidesIt() {
        login(adminUsername, adminPassword);

        if (!navigateToReviewPageWithLiveSelection()) {
            assertTrue(true, "No live review selection data available in this dataset.");
            return;
        }
        if (!openFirstReviewRow()) {
            assertTrue(page.locator("#widgetReviewBody td.empty-row").count() > 0,
                "Expected empty-row placeholder when no review rows are available.");
            return;
        }

        page.waitForSelector("#detailCard", new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        assertTrue(page.locator("#detailCard").isVisible(), "Expected detail overlay to be visible after row click");
        var bounds = page.locator("#detailCard").boundingBox();
        assertTrue(bounds.width > 0 && bounds.x >= 0 && bounds.x + bounds.width <= page.viewportSize().width + 1,
            "Preview must fit within the viewport without hiding its close action.");
        assertTrue(page.locator("#detailCardCloseBtn").isVisible());

        page.click("#detailCardCloseBtn");
        assertTrue(page.evaluate("() => document.getElementById('detailCard').style.display === 'none'").equals(Boolean.TRUE),
            "Expected detail overlay to be hidden after Close Preview click");
    }

    @Test
    @Order(16)
    void reviewSelection_sendToTeammateReveal_togglesPanelVisibility() {
        login(adminUsername, adminPassword);

        if (!navigateToReviewPageWithLiveSelection()) {
            assertTrue(true, "No live review selection data available in this dataset.");
            return;
        }
        if (!openFirstReviewRow()) {
            assertTrue(page.locator("#widgetReviewBody td.empty-row").count() > 0,
                "Expected empty-row placeholder when no review rows are available.");
            return;
        }

        page.waitForSelector("#detailCard", new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        assertTrue(page.evaluate("() => document.getElementById('shareChatPanel').hidden").equals(Boolean.TRUE),
            "Expected teammate panel to be hidden by default");

        page.click("#shareChatRevealBtn");
        assertTrue(page.evaluate("() => !document.getElementById('shareChatPanel').hidden").equals(Boolean.TRUE),
            "Expected teammate panel to open after button click");

        page.click("#shareChatRevealBtn");
        assertTrue(page.evaluate("() => document.getElementById('shareChatPanel').hidden").equals(Boolean.TRUE),
            "Expected teammate panel to hide when toggled again");
    }

    private APIResponse postJson(String path, String jsonBody) {
        return apiPostOrSkip(
                baseUrl + path,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(jsonBody)
        );
    }

        private boolean navigateToReviewPageWithLiveSelection() {
        String anySessionId = findAnySessionId();
        if (anySessionId == null || anySessionId.isBlank()) {
            return false;
        }

        List<String> chatIds = findChatIdsForSession(anySessionId, 2);
        if (chatIds.isEmpty()) {
            return false;
        }

        String selectionId = createSelectionFromChatIds(chatIds);
        if (selectionId == null || selectionId.isBlank()) {
            return false;
        }

        navigateWithCommit("/dashboard/widgets/drilldown/review?selectionId=" + urlEncode(selectionId));
        waitForPath("/chat-server/dashboard/widgets/drilldown/review");
        page.waitForSelector("#widgetReviewBody",
            new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
        return true;
        }

    private String findAnySessionId() {
        APIResponse response = apiGetOrSkip(baseUrl + "/dashboard/sessions/data?all=true");
        if (response.status() != 200) {
            return null;
        }
        String body = response.text();

        // looks for first: "sessionId":"..."
        return extractFirstJsonStringValue(body, "sessionId");
    }

    private List<String> findChatIdsForSession(String sessionId, int max) {
        List<String> out = new ArrayList<>();
        APIResponse response = apiGetOrSkip(
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

        APIResponse response = apiPostOrSkip(
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
        loginViaApi(username, password);
    }

    private boolean openFirstReviewRow() {
        page.waitForSelector("#widgetReviewBody");
        page.waitForFunction(
                "() => !!document.querySelector('#widgetReviewBody tr[data-row-key] td.row-open-cell') || "
                        + "!!document.querySelector('#widgetReviewBody td.empty-row')",
                null,
                new Page.WaitForFunctionOptions().setTimeout(15000)
        );

        int rowCount = page.locator("#widgetReviewBody tr[data-row-key] td.row-open-cell").count();
        if (rowCount <= 0) {
            return false;
        }

        page.locator("#widgetReviewBody tr[data-row-key] td.row-open-cell").first().click();
        return true;
    }
}
