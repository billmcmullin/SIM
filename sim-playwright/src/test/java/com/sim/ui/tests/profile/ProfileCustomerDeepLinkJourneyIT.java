package com.sim.ui.tests.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;
import com.sim.ui.base.ChatReviewFixture;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileCustomerDeepLinkJourneyIT extends BaseUiIT {

    @Test
    void phase2_lateFailureCannotReplaceTheActiveChat() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.holdFirstDetail();
        fixture.openProfile(page);
        page.locator("tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        fixture.awaitHeldDetail(page);
        page.locator("tr[data-chat-id='" + ChatReviewFixture.CHAT_B + "'] td:nth-child(4)").click();
        page.waitForFunction("document.querySelector('#cpDetailPrompt').textContent === 'Synthetic question B'");
        Response late = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"),
                fixture::releaseFirstDetailFailure);
        late.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals(ChatReviewFixture.PROMPT_B, page.locator("#cpDetailPrompt").textContent());
        assertEquals(ChatReviewFixture.RESPONSE_B, page.locator("#cpDetailResponse").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_hiddenPreviewStaysHiddenAfterItsRequestCompletes() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.holdFirstDetail();
        fixture.openProfile(page);
        page.locator("tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        fixture.awaitHeldDetail(page);
        page.evaluate("document.querySelector('#cpDetailCard').style.display = 'none'");
        Response late = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"),
                fixture::releaseFirstDetail);
        late.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertTrue(page.locator("#cpDetailCard").isHidden());
        assertEquals("Loading prompt...", page.locator("#cpDetailPrompt").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_languageChangeInvalidatesPendingTranslation() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openProfile(page);
        page.locator("tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        page.waitForFunction("document.querySelector('#cpDetailPrompt').textContent === 'Synthetic question A'");
        page.locator("#cpTranslatePromptBtn").click();
        fixture.awaitHeldTranslation(page);
        page.selectOption("#cpTranslateTargetLang", "fr");
        Response late = page.waitForResponse(response -> response.url().endsWith("/review/translate"),
                fixture::releaseFirstTranslation);
        late.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals("", page.locator("#cpPromptTranslationMeta").textContent());
        assertEquals("", page.locator("#cpPromptTranslationOutput").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_lateTranslationCannotOverwriteTheActiveChatTranslation() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openProfile(page);
        page.locator("tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        page.waitForFunction("document.querySelector('#cpDetailPrompt').textContent === 'Synthetic question A'");
        page.locator("#cpTranslatePromptBtn").click();
        fixture.awaitHeldTranslation(page);
        page.locator("tr[data-chat-id='" + ChatReviewFixture.CHAT_B + "'] td:nth-child(4)").click();
        page.waitForFunction("document.querySelector('#cpDetailPrompt').textContent === 'Synthetic question B'");
        page.locator("#cpTranslatePromptBtn").click();
        page.waitForFunction("document.querySelector('#cpPromptTranslationOutput').textContent === 'Synthetic translation B'");
        Response late = page.waitForResponse(response -> response.url().endsWith("/review/translate"),
                fixture::releaseFirstTranslation);
        late.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals("Synthetic translation B", page.locator("#cpPromptTranslationOutput").textContent());
        assertEquals(ChatReviewFixture.PROMPT_B, page.locator("#cpDetailPrompt").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_detachedPreviewCannotOverwriteItsReplacement() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.holdFirstDetail();
        fixture.openProfile(page);
        page.locator("tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        fixture.awaitHeldDetail(page);
        page.evaluate("() => { const old = document.querySelector('#cpDetailCard'); const replacement = old.cloneNode(true); replacement.querySelector('#cpDetailPrompt').textContent = 'Replacement panel'; old.replaceWith(replacement); }");
        Response late = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"),
                fixture::releaseFirstDetail);
        late.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals("Replacement panel", page.locator("#cpDetailPrompt").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase1_lateFirstPreviewCannotOverwriteTheActiveChat() throws Exception {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.holdFirstDetail();
        fixture.openProfile(page);
        page.locator("#cpSubmittedChatsBody tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        fixture.awaitHeldDetail(page);
        page.locator("#cpSubmittedChatsBody tr[data-chat-id='" + ChatReviewFixture.CHAT_B + "'] td:nth-child(4)").click();
        page.waitForFunction("document.querySelector('#cpDetailPrompt').textContent === 'Synthetic question B'");

        Response lateResponse = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"),
                fixture::releaseFirstDetail);
        lateResponse.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        fixture.capture(page, "profile-delayed-response");
        fixture.assertNoUnexpectedRequests();
        assertEquals(ChatReviewFixture.PROMPT_B, page.locator("#cpDetailPrompt").textContent(),
                "A late response for chat A must not replace the active chat B.");
        assertEquals(ChatReviewFixture.RESPONSE_B, page.locator("#cpDetailResponse").textContent());
    }

    @Test
    void phase1_profilePreviewPreservesOriginalTextWithoutSelectingChat() throws Exception {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openProfile(page);
        page.locator("#cpSubmittedChatsBody tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        page.waitForFunction("document.querySelector('#cpDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(ChatReviewFixture.PROMPT_A, page.locator("#cpDetailPrompt").textContent());
        assertEquals(ChatReviewFixture.RESPONSE_A, page.locator("#cpDetailResponse").textContent());
        assertFalse(page.locator("#cpSubmittedChatsBody input.cp-chat-checkbox").first().isChecked());
        fixture.capture(page, "profile-original-text");
        fixture.assertNoUnexpectedRequests();
    }

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
        clickDashboardSwitchNoWait("View User Sessions");

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
        clickDashboardSwitchNoWait("Username Catalog");

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

        page.locator("#sessionNameList a.customer-profile-link").first().waitFor(
            new com.microsoft.playwright.Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));
        int catalogLinkCount = page.locator("#sessionNameList a.customer-profile-link").count();
        assertTrue(catalogLinkCount > 0,
                "A customer-profile link is required to certify the live catalog deep-link journey.");

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
        page.locator(selector).first().waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED));

        String dataTarget = page.locator(selector).first().getAttribute("data-target");
        assertTrue(dataTarget != null && dataTarget.contains(expectedPathFragment),
            "Expected dashboard switch target containing " + expectedPathFragment + " for " + buttonText);
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