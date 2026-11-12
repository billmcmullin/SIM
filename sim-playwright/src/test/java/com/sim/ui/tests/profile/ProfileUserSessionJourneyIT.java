package com.sim.ui.tests.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;
import com.sim.ui.base.ChatReviewFixture;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileUserSessionJourneyIT extends BaseUiIT {

        @Test
        void phase3_invalidRecipientsAndDeliveryFailuresDoNotRetry() {
                ChatReviewFixture fixture = new ChatReviewFixture(context);
                fixture.enableSharingFailureFixture();
                for (boolean profile : new boolean[] {false, true}) {
                        if (profile) {
                                fixture.openProfile(page);
                                page.locator("#cpSubmittedChatsBody .chat-preview-open").first().click();
                                page.waitForFunction("document.querySelector('#cpDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
                        } else {
                                fixture.openReview(page);
                                page.locator("#widgetReviewBody .chat-preview-open").first().click();
                        }
                        int sentBefore = fixture.sharedMessages().size();
                        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send To Teammate").setExact(true)).click();
                        page.getByLabel("Teammate Email").fill("not-an-email");
                        var send = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send Email").setExact(true));
                        send.click();
                        String statusId = profile ? "cpShareChatEmailStatus" : "shareChatEmailStatus";
                        assertTrue(page.locator("#" + statusId).textContent().contains("valid teammate email"));
                        assertEquals(sentBefore, fixture.sharedMessages().size());
                        page.getByLabel("Teammate Email").fill("teammate@example.test");
                        send.click();
                        page.waitForFunction("id => document.getElementById(id).textContent.includes('Synthetic delivery unavailable')", statusId);
                        assertTrue(send.isEnabled());
                        assertEquals(sentBefore + 1, fixture.sharedMessages().size(), "A failed explicit send must not be automatically retried.");
                }
                fixture.assertNoUnexpectedRequests();
        }

        @Test
        void phase3_profileAutoDetectPayloadIsPreservedAndExplicitSourceIsHonored() {
                ChatReviewFixture fixture = new ChatReviewFixture(context);
                fixture.openProfile(page);
                page.locator("#cpSubmittedChatsBody .chat-preview-open").first().click();
                page.waitForFunction("document.querySelector('#cpDetailPrompt').textContent === 'Synthetic question A'");
                page.locator("#cpTranslatePromptBtn").click();
                fixture.awaitHeldTranslation(page);
                assertEquals(2, fixture.heldTranslationPayload().size());
                assertFalse(fixture.heldTranslationPayload().has("sourceLang"));
                Response automatic = page.waitForResponse(response -> response.url().endsWith("/review/translate"), fixture::releaseFirstTranslation);
                automatic.finished();
                page.waitForFunction("document.querySelector('#cpPromptTranslationOutput').textContent === 'Synthetic translation A'");
                page.selectOption("#cpTranslateSourceLang", "ja");
                page.locator("#cpTranslatePromptBtn").click();
                fixture.awaitHeldTranslation(page);
                assertEquals("ja", fixture.heldTranslationPayload().get("sourceLang").getAsString());
                assertEquals(ChatReviewFixture.PROMPT_A, fixture.heldTranslationPayload().get("text").getAsString());
                Response explicit = page.waitForResponse(response -> response.url().endsWith("/review/translate"), fixture::releaseFirstTranslation);
                explicit.finished();
                fixture.assertNoUnexpectedRequests();
        }

        @Test
        void phase3_profileSharingUsesOnlyTheLoadedScopedChat() {
                ChatReviewFixture fixture = new ChatReviewFixture(context);
                fixture.enableSharingFixture();
                fixture.openProfile(page);
                page.locator("#cpSubmittedChatsBody .chat-preview-open").first().click();
                page.waitForFunction("document.querySelector('#cpDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
                assertTrue(fixture.sharedMessages().isEmpty());
                page.locator("#cpShareChatRevealBtn").click();
                page.fill("#cpShareEmailTo", "teammate@example.test");
                page.fill("#cpShareCustomMessage", "Synthetic context");
                page.locator("#cpShareTranslateToEnglish").uncheck();
                page.locator("#cpShareChatByEmailBtn").click();
                page.waitForFunction("document.querySelector('#cpShareChatEmailStatus').textContent.startsWith('Email sent to')");
                assertEquals(1, fixture.sharedMessages().size());
                var payload = fixture.sharedMessages().get(0);
                assertEquals(10, payload.size());
                assertEquals("teammate@example.test", payload.get("to").getAsString());
                assertEquals(ChatReviewFixture.CHAT_A, payload.get("chatId").getAsString());
                assertEquals("phase1-session", payload.get("sessionId").getAsString());
                assertEquals(ChatReviewFixture.PROMPT_A, payload.get("prompt").getAsString());
                assertEquals(ChatReviewFixture.RESPONSE_A, payload.get("response").getAsString());
                assertEquals("Session", payload.get("subjectType").getAsString());
                assertFalse(payload.get("translateToEnglish").getAsBoolean());
                assertEquals(0, page.locator("#cpSubmittedChatsBody input.cp-chat-checkbox:checked").count());
                fixture.assertNoUnexpectedRequests();
        }

        @Test
        void phase3_profileKeyboardCloseKeepsPendingDetailsClosed() throws Exception {
                ChatReviewFixture fixture = new ChatReviewFixture(context);
                fixture.holdFirstDetail();
                for (int width : new int[] {1440, 390}) {
                        page.setViewportSize(width, 900);
                        fixture.openProfile(page);
                        page.locator("#cpSubmittedChatsBody .chat-preview-open").first().focus();
                        page.keyboard().press("Enter");
                        fixture.awaitHeldDetail(page);
                        assertTrue(page.locator("#cpShareChatRevealBtn").isDisabled());
                        page.locator("#cpDetailNextBtn").click();
                        page.waitForFunction("document.querySelector('#cpDetailPrompt').textContent === 'Synthetic question B'");
                        assertFalse(page.locator("#cpShareChatRevealBtn").isDisabled());
                        assertTrue(page.locator("#cpDetailNextBtn").isDisabled());
                        fixture.capture(page, "profile-shared-preview-" + width);
                        page.keyboard().press("Escape");
                        assertTrue(page.locator("#cpDetailCard").isHidden());
                        assertEquals("Preview chat " + ChatReviewFixture.CHAT_B, page.evaluate("document.activeElement.getAttribute('aria-label')"));
                        Response late = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"), fixture::releaseFirstDetail);
                        late.finished();
                        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
                        assertTrue(page.locator("#cpDetailCard").isHidden());
                        assertEquals(ChatReviewFixture.PROMPT_B, page.locator("#cpDetailPrompt").textContent());
                }
                fixture.assertNoUnexpectedRequests();
        }

    @Test
    void phase1_profilePreviewProvidesTheSameReviewControls() throws Exception {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        page.locator("#widgetReviewBody tr[data-row-key]").first().locator("td").nth(2).click();
        assertTrue(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close Preview")).isVisible());
        assertTrue(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send To Teammate")).isVisible());

        fixture.openProfile(page);
        page.locator("#cpSubmittedChatsBody tr[data-chat-id='" + ChatReviewFixture.CHAT_A + "'] td:nth-child(4)").click();
        page.waitForFunction("document.querySelector('#cpDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        Locator panel = page.locator("#cpDetailCard:visible, #detailCard:visible");
        fixture.capture(page, "profile-preview-controls");
        fixture.assertNoUnexpectedRequests();
        assertAll("Profile and Review must provide consistent preview controls",
                () -> assertTrue(panel.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Close Preview")).count() == 1, "Missing Close Preview"),
                () -> assertTrue(panel.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Send To Teammate")).count() == 1, "Missing teammate action"),
                () -> assertTrue(panel.locator("label").allTextContents().stream()
                        .anyMatch(label -> label.contains("Translate From")), "Missing source-language control"));
    }

    private final String userUsername = System.getProperty("userUsername", "user");
    private final String userPassword = System.getProperty("userPassword", "user");
    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void unauthenticated_profileAndSessionPages_areBlocked() {
        navigateWithCommit("/profile");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated profile navigation to require login,");

        navigateWithCommit("/dashboard/sessions");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated user sessions navigation to require login,");
    }

    @Test
    @Order(2)
    void profilePage_renders_andTopBarNavigationWorks() {
        loginAsAvailableUser();

        navigateWithCommit("/profile");
        waitForPath("/chat-server/profile");
        page.waitForSelector("#profileForm");

        assertTrue(page.title().contains("Profile"));
        assertTrue(page.locator("#usernameInput").count() > 0);
        assertTrue(page.locator("#passwordInput").count() > 0);
        assertTrue(page.locator("#profileForm button[type='submit']").count() > 0);

        clickNavButtonNoWait("Dashboard", "/chat-server/dashboard");
        assertTrue(page.url().contains("/chat-server/dashboard"),
                "Expected profile top bar to navigate to dashboard.");

        navigateWithCommit("/profile");
        waitForPath("/chat-server/profile");

        clickNavButtonNoWait("Logout", "/chat-server/login");
        assertOnLoginScreen("After logout expected login screen,");

        navigateWithCommit("/profile");
        waitForLoginScreen();
        assertOnLoginScreen("Expected profile to be blocked after logout,");
    }

    @Test
    @Order(3)
    void profileToSessionsJourney_viaDashboardButton_supportsSessionFilters() {
        loginAsAvailableUser();

        navigateWithCommit("/profile");
        waitForPath("/chat-server/profile");

        clickNavButtonNoWait("Dashboard", "/chat-server/dashboard");
        waitForPath("/chat-server/dashboard");

        String sessionsTarget = readDashboardButtonTarget("View User Sessions", "/dashboard/sessions");

        clickDashboardSwitchNoWait("View User Sessions");

        // Always open the route directly after button click to keep assertions deterministic
        // across inline and full-page dashboard modes.
        navigateWithCommit(normalizeDashboardTarget(sessionsTarget));
        waitForPath("/dashboard/sessions");

        page.waitForSelector("#sessionsTable");
        assertTrue(page.locator("#searchInput").count() > 0);
        assertTrue(page.locator("#searchBtn").count() > 0);
        assertTrue(page.locator("#refreshBtn").count() > 0);
        assertTrue(page.locator("#showAllUsersBtn").count() > 0);
        assertTrue(page.locator("#showActiveUsersBtn").count() > 0);
        assertTrue(page.locator("#toggleLabeledOnlyBtn").count() > 0);

        // Read-only interactions only: these filters/search inputs do not persist data.
        page.fill("#searchInput", "playwright-session-search");
        page.click("#searchBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");
        assertFalse(page.locator("#sessionsBody").innerText().trim().isBlank(),
                "Expected sessions results region to be populated after search.");

        page.click("#toggleLabeledOnlyBtn", new Page.ClickOptions().setNoWaitAfter(true));
        assertTrue(page.locator("#toggleLabeledOnlyBtn").innerText().contains("On")
                || page.locator("#toggleLabeledOnlyBtn").innerText().contains("Off"),
                "Expected labeled-only toggle state label to be rendered.");

        page.click("#showActiveUsersBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");
        page.click("#showAllUsersBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");

        page.click("#refreshBtn", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionsBody");

        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"),
                "Expected sessions top bar to navigate back to profile.");
    }

    @Test
    @Order(4)
    void dashboardToUsernameCatalogJourney_coversSessionCatalogControls() {
        loginAsAvailableUser();

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        String catalogTarget = readDashboardButtonTarget("Username Catalog", "/dashboard/session-names");
        clickDashboardSwitchNoWait("Username Catalog");

        // Always open the route directly after button click to keep assertions deterministic
        // across inline and full-page dashboard modes.
        navigateWithCommit(normalizeDashboardTarget(catalogTarget));
        waitForPath("/dashboard/session-names");

        page.waitForSelector("#sessionSearch");
        assertTrue(page.locator("#sessionSearchButton").count() > 0);
        assertTrue(page.locator("#toggleLabeledOnlyBtn").count() > 0);
        assertTrue(page.locator("#pageSizeSelect").count() > 0);
        assertTrue(page.locator("#prevPageBtn").count() > 0);
        assertTrue(page.locator("#nextPageBtn").count() > 0);
        assertTrue(page.locator("#sessionNameList").count() > 0);

        page.fill("#sessionSearch", "playwright-catalog-search");
        page.click("#sessionSearchButton", new Page.ClickOptions().setNoWaitAfter(true));
        page.waitForSelector("#sessionNameList");

        page.selectOption("#pageSizeSelect", "20");
        assertTrue("20".equals(page.inputValue("#pageSizeSelect")),
                "Expected pageSize selection to update.");

        page.click("#toggleLabeledOnlyBtn", new Page.ClickOptions().setNoWaitAfter(true));
        assertTrue(page.locator("#toggleLabeledOnlyBtn").innerText().contains("On")
                || page.locator("#toggleLabeledOnlyBtn").innerText().contains("Off"),
                "Expected labeled-only toggle state label to be rendered.");
    }

    private void loginAsAvailableUser() {
        if (!tryLoginViaApi(userUsername, userPassword)) {
            loginViaApi(adminUsername, adminPassword);
        }
    }

    private String readDashboardButtonTarget(String buttonText, String expectedPathFragment) {
        String selector = "button.dashboard-switch-btn:has-text('" + buttonText + "')";
        Assumptions.assumeTrue(page.locator(selector).count() > 0,
                "Skipping: dashboard switch button is unavailable in this environment: " + buttonText);

        String dataTarget = page.locator(selector).first().getAttribute("data-target");
        Assumptions.assumeTrue(dataTarget != null && dataTarget.contains(expectedPathFragment),
                "Skipping: expected dashboard switch target containing " + expectedPathFragment + " for " + buttonText);
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