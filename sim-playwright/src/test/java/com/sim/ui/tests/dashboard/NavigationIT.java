package com.sim.ui.tests.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.BoundingBox;
import com.sim.ui.base.ChatReviewFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NavigationIT {
    private static final String ORIGIN = "http://sim.test";
    private static final String CONTEXT = "/chat-server";
    private static final Pattern SCRIPTS = Pattern.compile("<script\\b[^>]*>.*?</script>", Pattern.DOTALL);
    private static final Map<String, String> VIEWS = new LinkedHashMap<>();
    private static Playwright playwright;
    private static Browser browser;
    private static Path webRoot;
    private BrowserContext context;
    private Page page;
    private boolean includeBootScripts;

    @Test
    void phase9_reviewProfileAndSessionsUseRightSidePreviewWithoutPageMovement() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        for (String view : java.util.List.of("review", "profile", "sessions", "review-mobile", "profile-mobile", "sessions-mobile")) {
            int width = view.endsWith("-mobile") ? 390 : 1440;
            page.setViewportSize(width, 900);
            String rootId;
            String rowSelector;
            String prefix;
            if (view.startsWith("review")) {
                fixture.openReview(page);
                rootId = "detailCard";
                rowSelector = "#widgetReviewBody .chat-preview-open";
                prefix = "";
            } else if (view.startsWith("profile")) {
                fixture.openProfile(page);
                rootId = "cpDetailCard";
                rowSelector = "#cpSubmittedChatsBody .chat-preview-open";
                prefix = "cp";
            } else {
                page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
                page.locator("#sessionsBody button:has-text('Expand')").first().click();
                rootId = "detailCard";
                rowSelector = ".session-chats-row .chat-preview-open";
                prefix = "session";
            }
            var button = page.locator(rowSelector).first();
            button.scrollIntoViewIfNeeded();
            Object beforeScroll = page.evaluate("() => [scrollX, scrollY]");
            var beforeRow = button.locator("xpath=ancestor::tr[1]").boundingBox();
            button.click();
            page.waitForFunction("rootId => document.getElementById(rootId).getAttribute('aria-busy') === 'false'", rootId);
            var root = page.locator("#" + rootId);
            assertEquals("fixed", root.evaluate("element => getComputedStyle(element).position"), view);
            var panel = root.boundingBox();
                assertTrue(panel.width >= Math.min(500, width - 32) && panel.x + panel.width <= width + 1
                    && panel.x + panel.width >= width - 20, view);
            assertTrue(panel.y >= -1 && panel.y + panel.height <= 901, view);
            assertEquals(beforeScroll, page.evaluate("() => [scrollX, scrollY]"), view + " must not scroll when opened/loaded.");
            assertEquals(beforeRow.y, button.locator("xpath=ancestor::tr[1]").boundingBox().y, 1, view);
            String sourceId = prefix.isEmpty() ? "translateSourceLang" : prefix + "TranslateSourceLang";
            assertTrue(page.locator("#" + sourceId).isVisible(), view + " must retain translation controls.");
            fixture.capture(page, "all-chat-right-panel-" + view);
            page.keyboard().press("Escape");
            assertTrue(root.isHidden(), view);
            assertEquals(beforeScroll, page.evaluate("() => [scrollX, scrollY]"), view + " must not scroll when closed.");
        }
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase8_languageChangeRejectsPendingTranslationAndKeepsControlsUsable() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        page.locator("#widgetTableBody .chat-preview-open").first().click();
        page.locator("#widgetTranslatePromptBtn").click();
        fixture.awaitHeldTranslation(page);
        page.locator("#widgetTranslateSourceLang").selectOption("ja");
        assertFalse(page.locator("#widgetTranslatePromptBtn").isDisabled());
        Response old = page.waitForResponse(response -> response.url().endsWith("/review/translate"), fixture::releaseFirstTranslation);
        old.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertTrue(page.locator("#widgetPromptTranslationOutput").isHidden());
        page.locator("#widgetTranslateResponseBtn").click();
        page.waitForFunction("document.querySelector('#widgetResponseTranslationOutput').style.display !== 'none'");
        assertEquals("ja", fixture.translationRequests().get(1).get("sourceLang").getAsString());
        assertEquals(ChatReviewFixture.RESPONSE_A, page.locator("#widgetDetailResponse").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase8_translationFailurePreservesOriginalTextAndDoesNotRetry() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        var requests = new java.util.concurrent.atomic.AtomicInteger();
        context.route("**/dashboard/widgets/drilldown/review/translate", route -> {
            requests.incrementAndGet();
            route.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/json")
                    .setBody("{\"status\":\"error\",\"message\":\"Synthetic translation unavailable\"}"));
        });
        fixture.openWidget(page);
        page.locator("#widgetTableBody .chat-preview-open").first().click();
        page.locator("#widgetTranslateResponseBtn").click();
        page.waitForFunction("document.querySelector('#widgetResponseTranslationMeta').textContent.includes('translation failed') && !document.querySelector('#widgetTranslateResponseBtn').disabled");
        assertEquals(1, requests.get());
        assertTrue(page.locator("#widgetResponseTranslationOutput").isHidden());
        assertEquals(ChatReviewFixture.RESPONSE_A, page.locator("#widgetDetailResponse").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase8_sessionTranslationSupportsManualSourceAndStaysClosedAfterCollapse() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        page.locator(".session-chats-row .chat-preview-open").first().click();
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals("auto", page.locator("#sessionTranslateSourceLang").inputValue());
        assertEquals("en", page.locator("#sessionTranslateTargetLang").inputValue());
        page.locator("#sessionTranslateSourceLang").selectOption("ja");
        page.locator("#sessionTranslateResponseBtn").click();
        page.waitForFunction("document.querySelector('#sessionResponseTranslationOutput').style.display !== 'none'");
        assertEquals("ja", fixture.translationRequests().get(0).get("sourceLang").getAsString());
        assertEquals(ChatReviewFixture.RESPONSE_A, fixture.translationRequests().get(0).get("text").getAsString());
        assertEquals(ChatReviewFixture.RESPONSE_A, page.locator("#sessionDetailResponse").textContent());
        page.locator("#sessionTranslatePromptBtn").click();
        fixture.awaitHeldTranslation(page);
        page.locator("#sessionsBody button:has-text('Collapse')").first().click();
        Response old = page.waitForResponse(response -> response.url().endsWith("/review/translate"), fixture::releaseFirstTranslation);
        old.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertTrue(page.locator("#detailCard").isHidden());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase8_widgetPreviewIsWideAnchoredAndTranslatesCompleteText() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        String source = "Bonjour, pouvez-vous expliquer la configuration du serveur LDAP pour notre equipe? Voici le contexte complet.";
        fixture.useWidgetResponse(source);
        page.setViewportSize(1440, 900);
        fixture.openWidget(page);
        var button = page.locator("#widgetTableBody .chat-preview-open").first();
        button.scrollIntoViewIfNeeded();
        Object beforeScroll = page.evaluate("() => [scrollX, scrollY]");
        button.click();
        assertEquals("fixed", page.locator("#detailCard").evaluate("element => getComputedStyle(element).position"));
        assertTrue(page.locator("#detailCard").boundingBox().width >= 500, "The side viewer should offer a wider reading area.");
        assertEquals(beforeScroll, page.evaluate("() => [scrollX, scrollY]"));
        assertTrue(page.locator("#widgetTranslateSourceLang").isVisible());
        assertEquals("auto", page.locator("#widgetTranslateSourceLang").inputValue());
        assertEquals("en", page.locator("#widgetTranslateTargetLang").inputValue());
        assertTrue(fixture.translationRequests().isEmpty(), "Opening a preview must not start translation.");
        page.locator("#widgetTranslateResponseBtn").click();
        page.waitForFunction("document.querySelector('#widgetResponseTranslationOutput').style.display !== 'none'");
        assertEquals(source, fixture.translationRequests().get(0).get("text").getAsString());
        assertEquals("auto", fixture.translationRequests().get(0).get("sourceLang").getAsString());
        assertEquals("en", fixture.translationRequests().get(0).get("targetLang").getAsString());
        assertEquals(source, page.locator("#widgetDetailResponse").textContent());
        page.locator("#widgetTranslateSourceLang").selectOption("fr");
        page.locator("#widgetTranslatePromptBtn").click();
        fixture.awaitHeldTranslation(page);
        assertEquals("fr", fixture.heldTranslationPayload().get("sourceLang").getAsString());
        assertEquals(ChatReviewFixture.PROMPT_A, fixture.heldTranslationPayload().get("text").getAsString());
        fixture.releaseFirstTranslation();
        page.waitForFunction("document.querySelector('#widgetPromptTranslationOutput').style.display !== 'none'");
        assertEquals(beforeScroll, page.evaluate("() => [scrollX, scrollY]"), "Translation must not move the document.");
        fixture.capture(page, "widget-translation-side-panel");
        page.setViewportSize(390, 900);
        page.waitForFunction("document.querySelector('#detailCard').getBoundingClientRect().right <= innerWidth");
        var mobile = page.locator("#detailCard").boundingBox();
        assertTrue(mobile.x >= -1 && mobile.x + mobile.width <= 391 && mobile.y + mobile.height <= 901);
        fixture.capture(page, "widget-translation-side-panel-mobile");
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase8_dashboardTranslationWaitsForDetailsAndRejectsStaleResults() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.holdFirstDetail();
        page.navigate(ChatReviewFixture.BASE + "/dashboard");
        page.locator("#otherParasoftLatestBody .chat-preview-open").first().click();
        fixture.awaitHeldDetail(page);
        assertTrue(page.locator("#dashboardTranslatePromptBtn").isVisible());
        assertTrue(page.locator("#dashboardTranslatePromptBtn").isDisabled());
        fixture.releaseFirstDetail();
        page.waitForFunction("document.querySelector('#dashboardTranslatePromptBtn').disabled === false");
        page.locator("#dashboardTranslatePromptBtn").click();
        fixture.awaitHeldTranslation(page);
        page.locator("#dashboardDetailNextBtn").click();
        page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer B, preserved **verbatim**.'");
        Response old = page.waitForResponse(response -> response.url().endsWith("/review/translate"), fixture::releaseFirstTranslation);
        old.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertTrue(page.locator("#dashboardPromptTranslationOutput").isHidden());
        assertEquals(ChatReviewFixture.PROMPT_B, page.locator("#dashboardDetailPrompt").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase7_dashboardPromptHoverHasNoFilledHighlight() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.navigate(ChatReviewFixture.BASE + "/dashboard");
        var button = page.locator("#otherParasoftLatestBody .chat-preview-open").first();
        button.scrollIntoViewIfNeeded();
        button.hover();
        assertEquals("rgba(0, 0, 0, 0)", button.evaluate("element => getComputedStyle(element).backgroundColor"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase7_dashboardPreviewStaysBesideItsRowWithoutPageMovement() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        for (int width : new int[] {1440, 390}) {
            page.setViewportSize(width, 900);
            page.navigate(ChatReviewFixture.BASE + "/dashboard");
            var button = page.locator("#otherParasoftLatestBody .chat-preview-open").first();
            button.scrollIntoViewIfNeeded();
            Object beforeScroll = page.evaluate("() => [scrollX, scrollY]");
            var beforeRow = button.locator("xpath=ancestor::tr").boundingBox();
            button.click();
            page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
            assertEquals(beforeScroll, page.evaluate("() => [scrollX, scrollY]"), "Opening/loading must not scroll the page.");
            assertEquals(beforeRow.y, button.locator("xpath=ancestor::tr").boundingBox().y, 1);
            var panel = page.locator("#dashboardChatPreview").boundingBox();
            assertTrue(panel.x >= -1 && panel.x + panel.width <= width + 1);
            assertTrue(panel.y >= -1 && panel.y + panel.height <= 901);
            if (width >= 1000) {
                var anchor = button.boundingBox();
                assertTrue(panel.x >= anchor.x + anchor.width, "Desktop preview must sit to the right of the prompt.");
                assertEquals(beforeRow.y, panel.y, 1, "Preview should align with the clicked row.");
            }
            fixture.capture(page, "anchored-dashboard-" + width);
            page.keyboard().press("Escape");
            assertTrue(page.locator("#dashboardChatPreview").isHidden());
            assertEquals(beforeScroll, page.evaluate("() => [scrollX, scrollY]"), "Closing must not scroll the page.");
        }
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase6_dashboardCreatedColumnContainsTheFullTimestamp() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        for (int width : new int[] {1366, 1440, 390}) {
            page.setViewportSize(width, 900);
            page.navigate(ChatReviewFixture.BASE + "/dashboard");
            var time = page.locator("#otherParasoftLatestBody time.chat-created-at").first();
            time.waitFor();
            assertEquals(Boolean.TRUE, time.evaluate("""
                    element => {
                        const cell = element.closest('td');
                        const bounds = cell.getBoundingClientRect();
                        const range = document.createRange();
                        range.selectNodeContents(element);
                        return Array.from(range.getClientRects()).every(rectangle =>
                            rectangle.left >= bounds.left - 1 && rectangle.right <= bounds.right + 1
                            && rectangle.top >= bounds.top - 1 && rectangle.bottom <= bounds.bottom + 1);
                    }
                    """), "The full timestamp and time-zone suffix must fit inside the Created cell at width " + width);
            assertTrue(time.getAttribute("title").contains("UTC: 2026-11-10T12:00:00.000Z"));
            time.scrollIntoViewIfNeeded();
            assertEquals(Boolean.TRUE, time.evaluate("""
                    element => {
                        const bounds = element.closest('.table-scroll').getBoundingClientRect();
                        return bounds.left >= -1 && bounds.right <= innerWidth + 1;
                    }
                    """), "Scrolling to the Created column must not move its table container outside the viewport.");
            fixture.capture(page, "created-column-" + width);
        }
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase5_rawTimestampAndDateScopeSurviveExplicitSharing() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        String original = "2026-11-10T17:30:00+05:30";
        fixture.useChatTimestamp(ChatReviewFixture.CHAT_A, original);
        fixture.enableSharingFixture();
        fixture.openWidget(page);
        page.locator("#widgetTableBody input.row-select").first().check();
        page.locator("#reviewSelectedBtn").click();
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        assertEquals("2026-11-10", fixture.lastSelectionPayload().get("date").getAsString());
        page.locator("#widgetReviewBody .chat-preview-open").first().click();
        assertEquals("2026-11-10T12:00:00.000Z", page.locator("#detailCreatedAt").getAttribute("datetime"));
        page.locator("#shareChatRevealBtn").click();
        page.locator("#shareEmailTo").fill("phase5@example.test");
        page.locator("#shareChatByEmailBtn").click();
        page.waitForCondition(() -> fixture.sharedMessages().size() == 1);
        assertEquals(original, fixture.sharedMessages().get(0).get("createdAt").getAsString());
        assertEquals(ChatReviewFixture.RESPONSE_A, fixture.sharedMessages().get(0).get("response").getAsString());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase5_sessionCardsUseTheSameUnknownDatePolicy() {
        page.addInitScript("document.addEventListener('DOMContentLoaded', () => { const wrapper = document.querySelector('#sessionsTable')?.closest('.session-table-wrapper'); if (wrapper) { const cards = document.createElement('div'); cards.id = 'sessionsContainer'; wrapper.replaceWith(cards); } });");
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.useChatTimestamp(ChatReviewFixture.CHAT_A, null);
        page.setViewportSize(390, 900);
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsContainer button:has-text('Expand')").first().click();
        assertEquals("Unknown", page.locator("#sessionsContainer time.chat-created-at").first().textContent());
        page.locator("#sessionsContainer .chat-preview-open").first().press("Enter");
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals("Unknown", page.locator("#sessionDetailCreatedAt").textContent());
        assertEquals(null, page.locator("#sessionDetailCreatedAt").getAttribute("datetime"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase5_markdownAssessmentConfirmsEscapedFallbackOnly() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        Object evaluated = page.evaluate("""
                async () => {
                    const { renderMarkdown } = await import('/chat-server/assets/js/widget_review_util/render_util.js');
                    const element = document.createElement('div');
                    const source = '**Source** <img src=x onerror="throw 1"> [unsafe](javascript:alert(1))';
                    renderMarkdown(element, source);
                    return { source, text: element.textContent, images: element.querySelectorAll('img').length,
                        links: element.querySelectorAll('a').length, scripts: element.querySelectorAll('script').length,
                        librariesAvailable: Boolean(window.marked && window.DOMPurify) };
                }
                """);
        assertTrue(evaluated instanceof Map<?, ?>);
        Map<?, ?> result = (Map<?, ?>) evaluated;
        assertEquals(result.get("source"), result.get("text"));
        assertEquals(0, ((Number) result.get("images")).intValue());
        assertEquals(0, ((Number) result.get("links")).intValue());
        assertEquals(0, ((Number) result.get("scripts")).intValue());
        assertEquals(Boolean.FALSE, result.get("librariesAvailable"), "This case proves the fallback, not real-library sanitizer behavior.");
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase5_sameInstantHasIdenticalDatesAcrossAllChatSurfaces() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        String expected = page.locator("#widgetTableBody time.chat-created-at").first().textContent();
        page.locator("#widgetTableBody .chat-preview-open").first().click();
        assertEquals(expected, page.locator("#widgetDetailCreatedAt").textContent());
        assertTrue(page.locator("#widgetDetailCreatedAt").getAttribute("title").contains("UTC: 2026-11-10T12:00:00.000Z"));
        fixture.capture(page, "phase5-widget-dates-desktop");
        fixture.openProfile(page);
        assertEquals(expected, page.locator("#cpSubmittedChatsBody time.chat-created-at").first().textContent());
        page.locator("#cpSubmittedChatsBody .chat-preview-open").first().click();
        page.waitForFunction("document.querySelector('#cpDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(expected, page.locator("#cpDetailCreatedAt").textContent());
        fixture.openReview(page);
        assertEquals(expected, page.locator("#widgetReviewBody time.chat-created-at").first().textContent());
        page.locator("#widgetReviewBody .chat-preview-open").first().click();
        assertEquals(expected, page.locator("#detailCreatedAt").textContent());
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        assertEquals(expected, page.locator(".session-chats-row time.chat-created-at").first().textContent());
        page.locator(".session-chats-row .chat-preview-open").first().click();
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(expected, page.locator("#sessionDetailCreatedAt").textContent());
        page.setViewportSize(390, 900);
        page.navigate(ChatReviewFixture.BASE + "/dashboard");
        assertEquals(expected, page.locator("#otherParasoftLatestBody time.chat-created-at").first().textContent());
        page.locator("#otherParasoftLatestBody .chat-preview-open").first().click();
        page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(expected, page.locator("#dashboardDetailCreatedAt").textContent());
        assertEquals(Boolean.TRUE, page.locator("#dashboardDetailCreatedAt").evaluate("element => element.getBoundingClientRect().right <= element.parentElement.getBoundingClientRect().right + 1"));
        fixture.capture(page, "phase5-dashboard-dates-mobile");
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase5_missingDatesStayUnknownWithoutEpochDefaults() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.useChatTimestamp(ChatReviewFixture.CHAT_A, null);
        fixture.openWidget(page);
        assertEquals("Unknown", page.locator("#widgetTableBody time.chat-created-at").first().textContent());
        page.locator("#widgetTableBody .chat-preview-open").first().click();
        assertEquals("Unknown", page.locator("#widgetDetailCreatedAt").textContent());
        assertEquals(null, page.locator("#widgetDetailCreatedAt").getAttribute("datetime"));
        fixture.openReview(page);
        assertEquals("Unknown", page.locator("#widgetReviewBody time.chat-created-at").first().textContent());
        fixture.openProfile(page);
        assertEquals("Unknown", page.locator("#cpSubmittedChatsBody time.chat-created-at").first().textContent());
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        assertEquals("Unknown", page.locator(".session-chats-row time.chat-created-at").first().textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase5_viewerTimezoneShowsDstAndDateBoundariesExplicitly() {
        try (BrowserContext timezoneContext = browser.newContext(new Browser.NewContextOptions()
                .setTimezoneId("America/New_York").setLocale("en-US"))) {
            Page timezonePage = timezoneContext.newPage();
            ChatReviewFixture fixture = new ChatReviewFixture(timezoneContext);
            fixture.openReview(timezonePage);
            Object evaluated = timezonePage.evaluate("""
                    () => ['2026-11-01T05:30:00Z', '2026-11-01T06:30:00Z',
                        '2026-03-08T06:59:00Z', '2026-03-08T07:00:00Z', '2026-11-10T00:30:00Z']
                        .map(window.SimChatPreview.formatTimestamp)
                    """);
            assertTrue(evaluated instanceof java.util.List<?>);
            java.util.List<?> dates = (java.util.List<?>) evaluated;
            assertTrue(dates.get(0).toString().contains("1:30 AM GMT-4"));
            assertTrue(dates.get(1).toString().contains("1:30 AM GMT-5"));
            assertTrue(dates.get(2).toString().contains("1:59 AM GMT-5"));
            assertTrue(dates.get(3).toString().contains("3:00 AM GMT-4"));
            assertTrue(dates.get(4).toString().contains("Nov 9, 2026"));
            fixture.assertNoUnexpectedRequests();
        }
    }

    @Test
    void phase5_timestampPolicyUsesExplicitOffsetsAndRejectsAmbiguousValues() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        Object evaluated = page.evaluate("""
                () => {
                    const format = window.SimChatPreview.formatTimestamp;
                    const expected = new Intl.DateTimeFormat(undefined, {
                        year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
                        hour12: true, timeZoneName: 'shortOffset'
                    }).format(new Date('2026-11-10T12:00:00Z'));
                    const time = document.createElement('time');
                    window.SimChatPreview.setTimestamp(time, '2026-11-10T07:00:00-05:00');
                    const tooltip = time.title;
                    const dateTime = time.dateTime;
                    window.SimChatPreview.setTimestamp(time, null);
                    return { utc: format('2026-11-10T12:00:00Z'), offset: format('2026-11-10T07:00:00-05:00'),
                        positiveOffset: format('2026-11-10T17:30:00+05:30'), expected,
                        validLeapDay: format('2024-02-29T12:00:00Z') !== 'Unknown',
                        invalid: [null, '', 'not-a-date', '2026-02-30T12:00:00Z', '2026-11-10 12:00:00', 0,
                            '2026-02-29T12:00:00Z', '2026-11-10'].map(format),
                        tooltip, dateTime, clearedDateTime: time.getAttribute('datetime') };
                }
                """);
        assertTrue(evaluated instanceof Map<?, ?>);
        Map<?, ?> result = (Map<?, ?>) evaluated;
        assertEquals(result.get("expected"), result.get("utc"));
        assertEquals(result.get("utc"), result.get("offset"));
        assertEquals(result.get("utc"), result.get("positiveOffset"));
        assertEquals(Boolean.TRUE, result.get("validLeapDay"));
        assertEquals(null, result.get("clearedDateTime"));
        assertEquals(java.util.List.of("Unknown", "Unknown", "Unknown", "Unknown", "Unknown", "Unknown", "Unknown", "Unknown"), result.get("invalid"));
        assertEquals("2026-11-10T12:00:00.000Z", result.get("dateTime"));
        assertTrue(String.valueOf(result.get("tooltip")).contains("Original timestamp: 2026-11-10T07:00:00-05:00"));
        assertTrue(String.valueOf(result.get("tooltip")).contains("UTC: 2026-11-10T12:00:00.000Z"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_cppTopicDayRetainsExactlyFourSelectedChats() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        var ids = java.util.List.of(ChatReviewFixture.CHAT_A, ChatReviewFixture.CHAT_B, "phase4-topic-c", "phase4-topic-d");
        fixture.useTopicData("C/C++Test", "2026-11-10", "2026-11-10");
        fixture.useTopicChatIds(ids);
        page.navigate(ChatReviewFixture.BASE + "/dashboard/topics?day=2026-11-10");
        page.locator("#globalTopicsBody button").first().click();
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        assertEquals("Popular Topics | Topic: C/C++Test | Day: 2026-11-10", page.locator("#reviewOrigin").textContent());
        assertTrue(page.locator("p.helper-note").first().textContent().contains("Currently reviewing Topic:"));
        assertEquals(4, page.locator("#widgetReviewBody tr[data-row-key]").count());
        assertEquals(ids, fixture.lastSelectionPayload().getAsJsonArray("selectedChatIds").asList().stream()
                .map(com.google.gson.JsonElement::getAsString).toList());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_topicRangeContextRemainsTextAndPreservesSelectedIds() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        String topic = "Synthetic <img src=x onerror=alert(1)> & topic";
        fixture.useTopicData(topic, "2026-11-01", "2026-11-10");
        page.setViewportSize(390, 900);
        page.navigate(ChatReviewFixture.BASE + "/dashboard/topics?start=2026-11-01&end=2026-11-10");
        page.locator("#globalTopicsBody button").first().click();
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        assertEquals("Popular Topics | Topic: " + topic + " | Range: 2026-11-01 to 2026-11-10",
                page.locator("#reviewOrigin").textContent());
        assertEquals(0, page.locator("#reviewOrigin img").count());
        assertEquals(2, fixture.lastSelectionPayload().size());
        var selected = fixture.lastSelectionPayload().getAsJsonArray("selectedChatIds");
        assertEquals(2, selected.size());
        assertEquals(ChatReviewFixture.CHAT_A, selected.get(0).getAsString());
        assertEquals(ChatReviewFixture.CHAT_B, selected.get(1).getAsString());
        assertEquals(2, page.locator("#widgetReviewBody tr[data-row-key]").count());
        assertEquals(Boolean.TRUE, page.locator("#reviewOrigin").evaluate("element => element.scrollWidth <= element.clientWidth"));
        fixture.capture(page, "phase4-topic-range-review-mobile");
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_legacyReviewWithoutContextRetainsPreviewBehavior() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openReview(page);
        assertTrue(page.locator("#reviewOrigin").isHidden());
        page.locator("#widgetReviewBody .chat-preview-open").first().press("Enter");
        assertEquals(ChatReviewFixture.RESPONSE_A, page.locator("#detailResponse").textContent());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_dashboardPreviewUsesStableIdentityAndPreservesProfileLinks() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.navigate(ChatReviewFixture.BASE + "/dashboard");
        assertEquals(2, page.locator("#otherParasoftLatestBody .chat-preview-open").count());
        assertEquals(0, fixture.selectionRequests());
        page.locator("#otherParasoftLatestBody tr").nth(2).locator("td").nth(2).click();
        assertEquals(0, fixture.selectionRequests(), "Legacy rows without identity must not guess a chat.");
        String source = page.url();
        page.locator("#otherParasoftLatestBody .chat-preview-open").first().press("Enter");
        page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals("phase1-widget", fixture.lastSelectionPayload().get("widgetId").getAsString());
        assertEquals(ChatReviewFixture.CHAT_A, fixture.lastSelectionPayload().getAsJsonArray("selectedChatIds").get(0).getAsString());
        assertEquals(2, fixture.lastSelectionPayload().size());
        assertTrue(page.locator("#dashboardShareOpenBtn").isHidden());
        page.locator("#dashboardDetailNextBtn").click();
        page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer B, preserved **verbatim**.'");
        page.locator("#dashboardDetailPreviousBtn").click();
        page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(2, fixture.selectionRequests());
        assertEquals(source, page.url());
        fixture.capture(page, "phase4-dashboard-preview-desktop");
        page.keyboard().press("Escape");
        assertTrue(page.locator("#dashboardChatPreview").isHidden());
        assertEquals("/chat-server/customer-profile?sessionId=phase1-session",
                page.locator("#otherParasoftLatestBody a").first().getAttribute("href"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_dashboardPreviewIgnoresLateDetailsAndRebindsReplacedRows() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.holdFirstDetail();
        page.setViewportSize(390, 900);
        page.navigate(ChatReviewFixture.BASE + "/dashboard");
        page.locator("#otherParasoftLatestBody .chat-preview-open").first().click();
        fixture.awaitHeldDetail(page);
        page.locator("#otherParasoftLatestBody .chat-preview-open").nth(1).click();
        page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer B, preserved **verbatim**.'");
        Response old = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"), fixture::releaseFirstDetailFailure);
        old.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals(ChatReviewFixture.RESPONSE_B, page.locator("#dashboardDetailResponse").textContent());
        page.evaluate("() => { const body = document.querySelector('#otherParasoftLatestBody'); body.replaceChildren(...Array.from(body.children, node => node.cloneNode(true))); }");
        page.waitForFunction("document.querySelector('#dashboardChatPreview').style.display === 'none'");
        assertEquals(2, fixture.selectionRequests());
        page.locator("#otherParasoftLatestBody .chat-preview-open").nth(1).click();
        page.waitForFunction("document.querySelector('#dashboardDetailResponse').textContent === 'Synthetic answer B, preserved **verbatim**.'");
        assertEquals(3, fixture.selectionRequests(), "Replaced rows must have exactly one delegated preview handler.");
        fixture.capture(page, "phase4-dashboard-preview-mobile");
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_sessionPreviewLoadsOwnedDetailsWithoutSelectingChats() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        page.locator(".session-chats-row input.chat-checkbox").first().check();
        assertTrue(page.locator("#detailCard").isHidden());
        page.locator(".session-chats-row input.chat-checkbox").first().uncheck();
        String source = page.url();
        page.locator(".session-chats-row .chat-preview-open").first().click();
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(1, fixture.selectionRequests());
        assertEquals(0, page.locator(".session-chats-row input.chat-checkbox:checked").count());
        page.locator("#sessionDetailNextBtn").click();
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer B, preserved **verbatim**.'");
        page.locator("#sessionDetailPreviousBtn").click();
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(2, fixture.selectionRequests(), "Reopening an owned cached row must not create duplicate snapshots.");
        assertEquals(source, page.url());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_sessionPreviewRejectsLateDetailsAndCollapsedSessions() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.holdFirstDetail();
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        page.locator(".session-chats-row .chat-preview-open").first().click();
        fixture.awaitHeldDetail(page);
        page.locator(".session-chats-row .chat-preview-open").nth(1).click();
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer B, preserved **verbatim**.'");
        Response old = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"), fixture::releaseFirstDetail);
        old.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals(ChatReviewFixture.RESPONSE_B, page.locator("#sessionDetailResponse").textContent());
        page.locator(".session-chats-row .chat-preview-open").first().click();
        fixture.awaitHeldDetail(page);
        page.locator("#sessionsBody button:has-text('Collapse')").first().click();
        Response collapsed = page.waitForResponse(response -> response.url().contains("selectionId=selection-a"), fixture::releaseFirstDetail);
        collapsed.finished();
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertTrue(page.locator("#detailCard").isHidden());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_sessionCardRendererUsesTheSameReadOnlyPreview() {
        page.addInitScript("document.addEventListener('DOMContentLoaded', () => { const wrapper = document.querySelector('#sessionsTable')?.closest('.session-table-wrapper'); if (wrapper) { const cards = document.createElement('div'); cards.id = 'sessionsContainer'; wrapper.replaceWith(cards); } });");
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.setViewportSize(390, 900);
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsContainer button:has-text('Expand')").first().click();
        page.locator("#sessionsContainer .chat-preview-open").first().focus();
        page.keyboard().press("Enter");
        page.waitForFunction("document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer A, preserved **verbatim**.'");
        assertEquals(0, page.locator("#sessionsContainer input.chat-checkbox:checked").count());
        assertTrue(page.locator("#sessionShareChatRevealBtn").isHidden());
        page.keyboard().press("Escape");
        assertTrue(page.locator("#detailCard").isHidden());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_largeSessionKeepsAll74ChatsAndBulkIds() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.useSessionChatCount(74);
        page.onDialog(dialog -> dialog.accept());
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions");
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        page.waitForFunction("document.querySelectorAll('.session-chats-row tr[data-preview-key]').length === 74");
        page.locator("#sessionsBody > tr").first().locator("button:has-text('Select All')").click();
        page.waitForFunction("document.querySelectorAll('.session-chats-row input.chat-checkbox:checked').length === 74");
        page.locator(".session-chats-row .chat-preview-open").last().click();
        page.waitForFunction("document.querySelector('#sessionDetailTitle').textContent.includes('phase4-chat-73') && document.querySelector('#sessionDetailResponse').textContent === 'Synthetic answer B, preserved **verbatim**.'");
        assertTrue(page.locator("#sessionDetailNextBtn").isDisabled());
        page.locator("#sessionDetailCardCloseBtn").click();
        assertEquals(74, page.locator(".session-chats-row input.chat-checkbox:checked").count());
        page.locator("#reviewSelectedBtn").click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.waitForFunction("document.querySelector('#pageInfo').textContent.includes('74 result')");
        assertEquals(74, fixture.lastSelectionPayload().getAsJsonArray("selectedChatIds").size());
        assertTrue(page.locator("p.helper-note").first().textContent().contains("Currently reviewing Session:"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase4_widgetPreviewUsesCompleteLoadedTextWithoutSelectingOrPosting() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        String response = "Complete preserved **source** line.\n".repeat(100);
        fixture.useWidgetResponse(response);
        fixture.openWidget(page);
        String sourceUrl = page.url();
        page.locator("#widgetTableBody .chat-preview-open").first().focus();
        page.keyboard().press("Enter");
        assertEquals(response, page.locator("#widgetDetailResponse").textContent());
        assertTrue(page.locator("#widgetTableBody tr").first().locator("td").nth(3).textContent().length() <= 220);
        assertEquals(0, fixture.selectionRequests(), "Loaded widget text must not require a new review snapshot.");
        assertEquals(0, page.locator("#widgetTableBody input.row-select:checked").count());
        assertTrue(page.locator("#widgetShareChatRevealBtn").isHidden());
        page.locator("#widgetDetailNextBtn").click();
        assertEquals(ChatReviewFixture.RESPONSE_B, page.locator("#widgetDetailResponse").textContent());
        page.keyboard().press("Escape");
        assertTrue(page.locator("#detailCard").isHidden());
        assertEquals(sourceUrl, page.url());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_logoutInvalidatesSavedSelections() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        page.locator("#widgetTableBody input.row-select").first().check();
        page.locator("button:has-text('Logout')").click();
        page.locator("#loginForm").waitFor();
        page.goBack();
        page.locator("#widgetTableBody input.row-select").first().waitFor();
        assertFalse(page.locator("#widgetTableBody input.row-select").first().isChecked());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_topicDayReviewReturnsToTheSameInlineDay() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.navigate(ChatReviewFixture.BASE + "/dashboard");
        page.evaluate("() => window.DashboardInlineView.openOrNavigate('/chat-server/dashboard/topics', 'Topics')");
        page.locator("#globalTopicsBody button").first().waitFor();
        page.fill("#topicDayInput", "2026-11-10");
        page.waitForURL(url -> url.contains("/dashboard/topics?") && url.contains("day=2026-11-10"));
        page.locator("#globalTopicsBody button").first().click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        assertEquals(2, page.locator("#widgetReviewBody tr[data-row-key]").count());
        assertTrue(page.locator("#dashboardInlineSection").isVisible());
        assertEquals("Popular Topics | Topic: Synthetic topic | Day: 2026-11-10", page.locator("#reviewOrigin").textContent());
        page.locator("[data-sim-back]").click();
        page.waitForURL(url -> url.contains("/dashboard/topics?") && url.contains("day=2026-11-10"));
        page.locator("#globalTopicsBody button").first().waitFor();
        assertEquals("2026-11-10", page.inputValue("#topicDayInput"));
        assertTrue(page.locator("#dashboardInlineSection").isVisible());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_widgetPageSelectionAndScrollSurviveReload() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.enableWidgetPagination();
        page.setViewportSize(1440, 450);
        fixture.openWidget(page);
        page.locator("#nextPageBtn").click();
        page.waitForFunction("document.querySelector('#pageInfo').textContent.includes('Page 2 of 3')");
        page.locator("#widgetTableBody input.row-select").first().check();
        page.evaluate("window.scrollTo(0, 200)");
        double scroll = ((Number) page.evaluate("window.scrollY")).doubleValue();
        assertTrue(scroll > 0, "The paging fixture must exercise a nonzero scroll offset.");
        page.waitForFunction("history.state?.simLists?.['widget:phase1-widget:2026-11-10']?.value.position.y === " + scroll);
        page.reload();
        page.locator("#widgetTableBody input.row-select").first().waitFor();
        page.waitForFunction("document.querySelector('#pageInfo').textContent.includes('Page 2 of 3')");
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals(scroll, ((Number) page.evaluate("window.scrollY")).doubleValue(), 1.0,
            () -> "Saved scroll state: " + page.evaluate("JSON.stringify(history.state)"));
        assertTrue(page.locator("#widgetTableBody input.row-select").first().isChecked());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_historyWriteDeniedDoesNotBlockReviewNavigation() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        page.locator("#widgetTableBody input.row-select").first().check();
        page.evaluate("() => { history.replaceState = () => { throw new DOMException('Denied', 'SecurityError'); }; }");
        page.locator("#reviewSelectedBtn").click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        assertTrue(page.locator("#widgetReviewBody").isVisible());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_loginPageInvalidatesPreviousListSelections() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        page.locator("#widgetTableBody input.row-select").first().check();
        page.locator("#reviewSelectedBtn").click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        page.navigate(ChatReviewFixture.BASE + "/login");
        page.locator("#loginForm").waitFor();
        page.goBack();
        page.locator("[data-sim-back]").click();
        page.waitForURL("**/dashboard/widgets/view?**");
        page.locator("#widgetTableBody input.row-select").first().waitFor();
        assertFalse(page.locator("#widgetTableBody input.row-select").first().isChecked(),
                "A new sign-in boundary must not restore the prior selection.");
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_storageDeniedDoesNotBreakNormalListAndReviewNavigation() {
        page.addInitScript("Object.defineProperty(window, 'sessionStorage', {get() { throw new DOMException('Denied', 'SecurityError'); }});");
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        page.locator("#widgetTableBody input.row-select").first().check();
        page.locator("#reviewSelectedBtn").click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        page.locator("[data-sim-back]").click();
        page.waitForURL("**/dashboard/widgets/view?**");
        page.locator("#widgetTableBody input.row-select").first().waitFor();
        assertTrue(page.locator("#widgetTableBody").isVisible());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_sessionSearchAndSelectionSurviveReviewAndBack() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.navigate(ChatReviewFixture.BASE + "/dashboard/sessions?activity=active&activeDays=7");
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        page.locator(".session-chats-row input.chat-checkbox").first().check();
        page.fill("#searchInput", "Synthetic");
        page.locator("#searchBtn").click();
        page.locator("#sessionsBody button:has-text('Expand')").first().waitFor();
        page.locator("#sessionsBody button:has-text('Expand')").first().click();
        page.locator(".session-chats-row input.chat-checkbox").first().waitFor();
        page.locator("#reviewSelectedBtn").click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        page.locator("[data-sim-back]").click();
        page.waitForURL("**/dashboard/sessions?**");
        page.locator(".session-chats-row input.chat-checkbox").first().waitFor();
        assertEquals("Synthetic", page.inputValue("#searchInput"));
        assertTrue(page.url().contains("activity=active"));
        assertTrue(page.locator(".session-chats-row input.chat-checkbox").first().isChecked());
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_inlineReviewRoundTripKeepsTheShellAndSelection() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        page.navigate(ChatReviewFixture.BASE + "/dashboard");
        page.evaluate("() => window.DashboardInlineView.openOrNavigate('/chat-server/dashboard/widgets/view?widgetId=phase1-widget&date=2026-11-10', 'Fixture widget')");
        for (int visit = 0; visit < 2; visit++) {
            page.locator("#widgetTableBody input.row-select").first().waitFor();
            page.locator("#widgetTableBody input.row-select").first().check();
            page.locator("#reviewSelectedBtn").click();
            page.waitForURL("**/dashboard/widgets/drilldown/review?**");
            page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
            assertTrue(page.locator("#dashboardInlineSection").isVisible());
            if (visit == 0) {
                page.goBack();
            } else {
                page.locator("[data-sim-back]").click();
            }
            page.waitForURL("**/dashboard/widgets/view?**");
            page.locator("#widgetTableBody input.row-select").first().waitFor();
            assertTrue(page.locator("#widgetTableBody input.row-select").first().isChecked());
            assertTrue(page.locator("#dashboardInlineSection").isVisible());
            if (visit == 0) {
                page.goForward();
                page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
                assertTrue(page.locator("#dashboardInlineSection").isVisible());
                page.goBack();
                page.locator("#widgetTableBody input.row-select").first().waitFor();
            }
        }
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase2_widgetFiltersSurviveBackWithoutPersistingChatContent() {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        page.selectOption("#widgetTablePageSize", "20");
        page.fill("#globalSearchInput", "Synthetic");
        page.fill("#filterPrompt", "question");
        page.fill("#filterResponse", "verbatim");
        page.locator("#widgetTableBody input.row-select").first().check();
        page.locator("#reviewSelectedBtn").click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        page.locator("[data-sim-back]").click();
        page.waitForURL("**/dashboard/widgets/view?**");
        page.locator("#widgetTableBody input.row-select").first().waitFor();
        assertEquals("Synthetic", page.inputValue("#globalSearchInput"));
        assertEquals("question", page.inputValue("#filterPrompt"));
        assertEquals("verbatim", page.inputValue("#filterResponse"));
        assertEquals("20", page.inputValue("#widgetTablePageSize"));
        String stored = (String) page.evaluate("JSON.stringify(history.state)");
        assertFalse(stored.contains(ChatReviewFixture.PROMPT_A));
        assertFalse(stored.contains(ChatReviewFixture.RESPONSE_A));
        assertFalse((Boolean) page.evaluate("Object.keys(localStorage).some(key => key.startsWith('sim.navigation'))"));
        fixture.assertNoUnexpectedRequests();
    }

    @Test
    void phase1_widgetRowsOfferPreviewWithoutBulkReviewNavigation() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        String sourceUrl = page.url();
        page.locator("#widgetTableBody tr").first().locator("td").nth(2).click();
        fixture.capture(page, "widget-row-preview");
        fixture.assertNoUnexpectedRequests();
        assertEquals(sourceUrl, page.url(), "Preview must not require navigating to bulk review.");
        assertTrue(page.locator("#detailCard:visible, #cpDetailCard:visible, [role='dialog']:visible").count() > 0,
                "Individual widget chats must have an in-place preview.");
        assertFalse(page.locator("#widgetTableBody input.row-select").first().isChecked());
    }

    @Test
    void phase1_widgetSelectionSurvivesReviewAndBack() throws IOException {
        ChatReviewFixture fixture = new ChatReviewFixture(context);
        fixture.openWidget(page);
        page.locator("#widgetTableBody input.row-select").first().check();
        assertTrue(page.locator("#reviewSelectedBtn").innerText().contains("(1)"));
        page.locator("#reviewSelectedBtn").click();
        page.waitForURL("**/dashboard/widgets/drilldown/review?**");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
        page.locator("[data-sim-back]").click();
        page.waitForURL("**/dashboard/widgets/view?**");
        page.locator("#widgetTableBody input.row-select").first().waitFor();
        fixture.capture(page, "widget-selection-after-back");
        fixture.assertNoUnexpectedRequests();
        assertTrue(page.locator("#widgetTableBody input.row-select").first().isChecked(),
                "Returning from review must retain the selected chat, not reset it to zero.");
    }

    @BeforeAll
    static void openBrowser() {
        webRoot = Path.of(System.getProperty("basedir", "."))
                .resolve("../sim-app/src/main/webapp").normalize();
        VIEWS.put("/admin", "admin_config.html");
        VIEWS.put("/dashboard/sessions", "all_sessions.html");
        VIEWS.put("/customer-profile", "customer_profile.html");
        VIEWS.put("/dashboard", "dashboard.html");
        VIEWS.put("/dashboard/new-users", "dashboard_new_users.html");
        VIEWS.put("/dashboard/new-users/drilldown", "dashboard_new_users_drilldown.html");
        VIEWS.put("/dashboard/summary/markdown", "dashboard_summary_markdown.html");
        VIEWS.put("/dashboard/topics", "dashboard_topics.html");
        VIEWS.put("/dashboard/trends", "dashboard_trends.html");
        VIEWS.put("/dashboard/inactive-users", "inactive_users.html");
        VIEWS.put("/dashboard/inactive-users/list", "inactive_users_list.html");
        VIEWS.put("/profile", "profile.html");
        VIEWS.put("/dashboard/session-names", "session-names.html");
        VIEWS.put("/dashboard/widgets/drilldown/review", "widget_review.html");
        VIEWS.put("/dashboard/widgets/view", "widget_table_view.html");
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext();
        context.route("**/*", this::serveFixture);
        page = context.newPage();
    }

    @AfterEach
    void closePage() {
        context.close();
    }

    @Test
    void summaryConfiguration_alignsLabelsAndInputsAcrossResponsiveRows() throws IOException {
        Files.createDirectories(Path.of("target/navigation"));
        for (int width : new int[] {1440, 1280, 1024, 768, 390}) {
            page.setViewportSize(width, 900);
            page.navigate(ORIGIN + CONTEXT + "/admin");
            assertTrue((Boolean) page.evaluate("""
                    () => {
                        const grid = document.querySelector('.summary-settings-grid');
                        const bounds = grid.getBoundingClientRect();
                        const fields = Array.from(grid.children, field => ({
                            field: field.getBoundingClientRect(),
                            label: field.querySelector('label').getBoundingClientRect(),
                            input: field.querySelector('input').getBoundingClientRect()
                        }));
                        return fields.every(current =>
                            current.label.bottom <= current.input.top + 1 &&
                            current.input.left >= bounds.left - 1 &&
                            current.input.right <= bounds.right + 1 &&
                            fields.every(other => Math.abs(current.field.top - other.field.top) > 1 ||
                                (Math.abs(current.label.top - other.label.top) <= 1 &&
                                 Math.abs(current.input.top - other.input.top) <= 1 &&
                                 Math.abs(current.input.height - other.input.height) <= 1)));
                    }
                    """), "Summary fields should align without overlap at width " + width);
            if (width == 1440 || width == 390) {
                page.locator("section:has(#summaryMaxMessageChars)").screenshot(new Locator.ScreenshotOptions()
                    .setStyle(".top-bar { visibility: hidden !important; }")
                        .setPath(Path.of("target/navigation", width + "-summary-configuration.png")));
            }
        }
    }

    @Test
    void connectionAndInferenceActions_areIndependentAndNeverResubmit() throws IOException {
        Files.createDirectories(Path.of("target/navigation"));
        for (int width : new int[] {1440, 390}) {
            page.setViewportSize(width, 900);
            page.navigate(ORIGIN + CONTEXT + "/admin");
            page.evaluate("""
                    () => {
                        window.adminPageConfig = {contextPath:'/chat-server', apiKeyStored:true};
                        window.probeRequests = [];
                        window.AdminPage = {Api:{fetchJson:async (url, options) => {
                            const parameters = new URLSearchParams(options.body);
                            window.probeRequests.push(Object.fromEntries(parameters));
                            const inference = parameters.get('testInference') === 'true';
                            const result = inference
                                ? {status:502,payload:{status:'error',probe:'chat',connectivityVerified:true,
                                    message:'Inference test timed out. No automatic retry was sent.',errorRef:'test-reference'}}
                                : {status:200,payload:{status:'ok',probe:'system',connectivityVerified:true,
                                    message:'Connection and API authentication successful. No inference request was sent.'}};
                            if (window.holdProbe) {
                                return new Promise(resolve => { window.finishProbe = () => resolve(result); });
                            }
                            return result;
                        }}};
                    }
                    """);
            page.addScriptTag(new Page.AddScriptTagOptions()
                    .setPath(webRoot.resolve("assets/js/admin_page.js")));
            page.evaluate("document.dispatchEvent(new Event('DOMContentLoaded'))");
            page.locator("button").filter(new Locator.FilterOptions().setHasText("Server Configuration")).first().click();
            page.locator("#serverHost").fill("https://example.test");
            page.locator("#serverPort").fill("443");
            assertEquals("", page.locator("#apiKey").inputValue());
            page.locator("#testConnectionBtn").click();
            page.waitForFunction("window.probeRequests.length === 1 && !document.querySelector('#testConnectionBtn').disabled");
            assertFalse((Boolean) page.evaluate("'testInference' in window.probeRequests[0]"));
            assertFalse((Boolean) page.evaluate("'apiKey' in window.probeRequests[0]"));
            assertTrue(page.locator("#testResult").innerText().contains("No inference request was sent"));
            assertTrue(page.locator("#saveConfigBtn").isEnabled());
            page.locator("#testInferenceBtn").click();
            page.waitForFunction("window.probeRequests.length === 2 && !document.querySelector('#testInferenceBtn').disabled");
            assertEquals("true", page.evaluate("window.probeRequests[1].testInference"));
            assertTrue(page.locator("#inferenceTestResult").innerText().contains("timed out"));
            assertTrue(page.locator("#inferenceTestResult").innerText().contains("test-reference"));
            assertTrue(page.locator("#saveConfigBtn").isEnabled());
            page.locator("section:has(#serverConfigForm)").screenshot(new Locator.ScreenshotOptions()
                    .setStyle(".top-bar { visibility: hidden !important; }")
                    .setPath(Path.of("target/navigation", width + "-connection-and-inference.png")));
            page.evaluate("window.holdProbe = true");
            page.locator("#testConnectionBtn").click();
            page.waitForFunction("typeof window.finishProbe === 'function'");
            assertTrue(page.locator("#testConnectionBtn").isDisabled());
            assertTrue(page.locator("#testInferenceBtn").isDisabled());
            page.evaluate("document.querySelector('#testConnectionBtn').dispatchEvent(new Event('click'))");
            assertEquals(3, ((Number) page.evaluate("window.probeRequests.length")).intValue());
            page.locator("#serverHost").fill("https://changed.example.test");
            page.evaluate("window.finishProbe()");
            page.waitForFunction("!document.querySelector('#testConnectionBtn').disabled");
            assertTrue(page.locator("#saveConfigBtn").isDisabled());
            assertTrue(page.locator("#testResult").innerText().contains("Settings changed"));
        }
    }

    @Test
    void dashboardSummary_distinguishesNoDataFailedReadsAndOlderFailures() throws IOException {
        Files.createDirectories(Path.of("target/navigation"));
        for (int width : new int[] {1440, 390}) {
            page.setViewportSize(width, 900);
            page.navigate(ORIGIN + CONTEXT + "/dashboard");
            page.addScriptTag(new Page.AddScriptTagOptions().setPath(webRoot.resolve("assets/js/dashboard_util/core.js")));
            page.addScriptTag(new Page.AddScriptTagOptions().setPath(webRoot.resolve("assets/js/dashboard_util/summary.js")));
            page.evaluate("""
                    () => window.DashboardSummary.hydrateFromBootstrap('/chat-server', {
                        status:'ok',summary:{overall:'<html><body>504 Gateway Time-out</body></html>',
                            quality:'Unavailable',response:'Unavailable',usage:'Unavailable',entryCount:6},
                        meta:{statusText:'error',progressPct:100,inProgress:false,fromFallback:true,
                            day:'2026-11-09',slot:2,requestedDay:'2026-11-10',requestedSlot:0,
                            message:'Summary failed: upstream gateway timeout (HTTP 504). No automatic retry was sent.',
                            updatedAt:'2026-11-09T23:55:00Z'}})
                    """);
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("No summary was generated"));
            assertFalse(page.locator("#dailySummaryBody").innerText().contains("<html>"));
            assertFalse(page.locator("#dailySummaryCopyText").inputValue().contains("<html>"));
            assertTrue(page.locator("#dailySummaryMeta").innerText().contains("2026-11-09"));
            assertTrue(page.locator("#dailySummaryMeta").innerText().contains("2026-11-10"));
            assertTrue(page.locator("#dailySummaryMeta").innerText().contains("Chats collected: 6"));
            assertFalse(page.locator("#dailySummaryMeta").innerText().contains("Entries analyzed"));
            page.locator("#dailySummaryBody").locator("..").screenshot(new Locator.ScreenshotOptions()
                    .setStyle(".top-bar { visibility: hidden !important; }")
                    .setPath(Path.of("target/navigation", width + "-summary-failed.png")));
            page.evaluate("""
                    () => window.DashboardSummary.hydrateFromBootstrap('/chat-server', {
                        status:'ok',summary:{overall:'No chats available.',entryCount:0},
                        meta:{statusText:'success',progressPct:100,inProgress:false,noData:true,
                            day:'2026-11-10',slot:0,updatedAt:'2026-11-10T15:00:00Z'}})
                    """);
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("No chats available for 2026-11-10"));
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("No request was sent"));
            assertFalse(page.locator("#dailySummaryMeta").innerText().contains("Generated:"));
            assertFalse(page.locator("#manualSummaryRetryBtn").isVisible());
            page.evaluate("""
                    () => window.DashboardSummary.hydrateFromBootstrap('/chat-server', {
                        status:'error',message:'Unable to load summary data.'})
                    """);
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("Unable to load summary data"));
            assertFalse(page.locator("#dailySummaryBody").innerText().contains("No chats"));
            assertEquals("", page.locator("#dailySummaryCopyText").inputValue());
            page.evaluate("""
                    () => window.DashboardSummary.hydrateFromBootstrap('/chat-server', {
                        status:'ok',summary:{overall:'Normal summary',quality:'Good quality',
                            response:'Useful responses',usage:'Normal usage',entryCount:6},
                        meta:{statusText:'success',progressPct:100,inProgress:false,day:'2026-11-10',slot:0}})
                    """);
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("Good quality"));
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("Useful responses"));
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("Normal usage"));
            assertTrue(page.locator("#dailySummaryBody h4").allTextContents().contains("Quality"));
            page.evaluate("""
                    () => window.DashboardSummary.hydrateFromBootstrap('/chat-server', {
                        status:'ok',summary:{entryCount:4,
                            overall:'Users asked about license-server connectivity and report generation.',
                            quality:'Topic: license-server connectivity (E1)\\n- Asked: Why can the client not connect?\\n- Answered: The excerpt suggests checking the server.\\n- Gap: Concrete diagnostics are not visible.\\n- Confidence: limited excerpts; factual correctness not independently verified.',
                            response:'Candidate article: License-server connectivity checklist\\n- Evidence: E1\\n- Add: prerequisites, supported configuration and diagnostic steps.\\n- Verify the existing documentation and knowledge base before treating this as missing content.',
                            usage:'Priority: high\\n- Add or link the verified checklist.\\n- Benefit: users can diagnose the failure before escalation.\\n- Validate with a representative connection failure.',
                            suggestedNextAction:'Review the existing licensing documentation and add verified troubleshooting steps for E1.'},
                        meta:{statusText:'success',progressPct:100,inProgress:false,format:'question-review-v1',
                            day:'2026-11-10',slot:1}})
                    """);
            for (String heading : new String[] {"What Users Asked", "Questions and Answers", "Candidate Knowledge Gaps", "Recommended Improvements"}) {
                assertTrue(page.locator("#dailySummaryBody h4").allTextContents().contains(heading));
                assertTrue(page.locator("#dailySummaryCopyText").inputValue().contains(heading));
            }
            assertFalse(page.locator("#dailySummaryBody h4").allTextContents().contains("Quality"));
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("diagnostics"));
            assertTrue(page.locator("#dailySummaryBody").innerText().contains("verified troubleshooting steps for E1"));
            assertFalse(page.locator("#dailySummaryBody").innerText().contains("tighten prompt"));
            page.locator("#dailySummaryBody").locator("..").screenshot(new Locator.ScreenshotOptions()
                    .setStyle(".top-bar { visibility: hidden !important; }")
                    .setPath(Path.of("target/navigation", width + "-question-review-summary.png")));
        }
    }

    @Test
    void fullAnalysisControls_requireExplicitUncertaintyConfirmationAndRenderTextSafely() throws IOException {
        Files.createDirectories(Path.of("target/navigation"));
        for (int width : new int[] {1440, 390}) {
            page.setViewportSize(width, 900);
            page.navigate(ORIGIN + CONTEXT + "/admin");
            page.evaluate("""
                    () => {
                        window.fullAnalysisPosts = [];
                        window.fullAnalysisJob = null;
                        const jobId = '00000000-0000-0000-0000-000000000011';
                        window.AdminPage = {Api:{
                            fetchJson: async url => {
                                const query = new URL(url, location.origin).searchParams;
                                if (query.get('action') === 'list') {
                                    return {ok:true,payload:{status:'ok',today:'2026-11-10',jobs:window.fullAnalysisJob ? [window.fullAnalysisJob] : []}};
                                }
                                if (query.get('action') === 'status') {
                                    return {ok:true,payload:{status:'ok',job:window.fullAnalysisJob}};
                                }
                                const offset = Number(query.get('offset') || 0);
                                const items = Array.from({length:Math.min(5, 6-offset)}, (_, index) => ({
                                    id:'step-'+(offset+index),kind:'map',chatId:'widget/chat-1',part:offset+index+1,parts:6,state:'COMPLETE',
                                    text:'Question: <script>window.analysisInjected = true</script>\\nAnswer: complete stored text without truncation.',
                                    result:{overview:'User asked about licensing',questions:[{asked:'How is the server configured?',answerSummary:'The answer suggests reviewing configuration',answerCoverage:'partial',sourceQuote:'complete stored text',gapKind:'candidate_kb_gap',gapDescription:'Concrete diagnostic steps are not shown',improvement:'Verify and document diagnostics',confidence:'limited'}]}
                                }));
                                return {ok:true,payload:{job:window.fullAnalysisJob,offset,total:6,items}};
                            },
                            postJson: async (url, payload) => {
                                window.fullAnalysisPosts.push(payload);
                                if (payload.action === 'start') {
                                    window.fullAnalysisJob = {id:jobId,day:'2026-11-10',createdAt:1,state:'PAUSED',phase:'map',
                                        message:'Upstream timed out. No automatic retry was sent. Review logs before resending.',
                                        uncertain:true,totalChats:2,completedChats:1,mapSteps:6,mapCompleted:3,requests:4};
                                    return {ok:true,payload:{status:'accepted',jobId}};
                                }
                                if (payload.action === 'resume') {
                                    window.fullAnalysisJob = {...window.fullAnalysisJob,state:'RUNNING',uncertain:false,message:'Continuing pending work; completed results are retained.'};
                                }
                                return {ok:true,payload:{status:'ok',job:window.fullAnalysisJob}};
                            }
                        }};
                    }
                    """);
            page.addScriptTag(new Page.AddScriptTagOptions().setPath(webRoot.resolve("assets/js/admin_utils/full_analysis.js")));
            page.evaluate("window.AdminPage.FullAnalysis.init('/chat-server')");
            page.waitForFunction("document.querySelector('#faDay').value === '2026-11-10'");
            page.locator("#faStart").click();
            page.waitForFunction("document.querySelector('#faStatus').textContent.startsWith('PAUSED:')");
            assertEquals(1, ((Number) page.evaluate("window.fullAnalysisPosts.length")).intValue());
            assertTrue(page.locator("#faCoverage").innerText().contains("Complete chats: 1/2"));
            assertTrue(page.locator("#faCoverage").innerText().contains("Uncertain requests: 1"));
            assertTrue(page.locator("#faResume").isDisabled());
            assertTrue(page.locator("#faConfirmIdle").isDisabled());
            page.evaluate("document.querySelector('#faResume').dispatchEvent(new Event('click'))");
            assertEquals(1, ((Number) page.evaluate("window.fullAnalysisPosts.length")).intValue());
            page.locator("#faDetails details summary").first().click();
            assertTrue(page.locator("#faDetails pre").first().innerText().contains("<script>"));
            assertTrue(page.locator("#faDetails").innerText().contains("Response provided: The answer suggests reviewing configuration"));
            assertEquals(null, page.evaluate("window.analysisInjected || null"));
            page.locator("#faConfirmedIdle").check();
            assertTrue(page.locator("#faResume").isEnabled());
            page.evaluate("document.querySelector('#faResume').dispatchEvent(new Event('click')); document.querySelector('#faResume').dispatchEvent(new Event('click'))");
            page.waitForFunction("window.fullAnalysisPosts.length === 2 && document.querySelector('#faStatus').textContent.startsWith('RUNNING:')");
            assertEquals(Boolean.TRUE, page.evaluate("window.fullAnalysisPosts[1].confirmedIdle"));
            assertFalse(page.locator("#faConfirmedIdle").isChecked());
            page.evaluate("window.fullAnalysisJob = {...window.fullAnalysisJob,state:'COMPLETED',completedChats:2,mapCompleted:6,message:'All source segments processed.',overview:{overview:'Licensing and reporting questions',themes:['Licensing'],candidateGaps:['Candidate diagnostic checklist; verify existing documentation'],improvements:['Add validated troubleshooting steps']}};");
            page.locator("#faRefresh").click();
            page.waitForFunction("document.querySelector('#faStatus').textContent.startsWith('COMPLETED:')");
            assertTrue(page.locator("#faOverview").innerText().contains("Licensing and reporting questions"));
            assertTrue(page.locator("#faCoverage").innerText().contains("Complete chats: 2/2"));
            assertTrue(page.locator("#faExport").getAttribute("href").contains("action=export"));
            page.locator("#faNext").click();
            page.waitForFunction("document.querySelector('#faPage').textContent.startsWith('6-6')");
            assertTrue(page.locator("#faPrevious").isEnabled());
            assertTrue(page.locator("#faNext").isDisabled());
            page.locator("#fullAnalysisSection").screenshot(new Locator.ScreenshotOptions()
                    .setStyle(".top-bar { visibility: hidden !important; }")
                    .setPath(Path.of("target/navigation", width + "-full-analysis.png")));
            assertEquals(2, ((Number) page.evaluate("window.fullAnalysisPosts.length")).intValue());
        }
    }

    @Test
    void everySignedInView_hasVisibleBackAndReturnsToItsSource() throws IOException {
        for (int width : new int[] { 1440, 390 }) {
            page.setViewportSize(width, 900);
            for (Map.Entry<String, String> view : VIEWS.entrySet()) {
                String source = "/profile".equals(view.getKey()) ? "/admin" : "/profile";
                page.navigate(ORIGIN + CONTEXT + source);
                page.evaluate("""
                        destination => {
                            const anchor = document.createElement('a');
                            anchor.href = destination;
                            document.body.appendChild(anchor);
                            anchor.click();
                        }
                        """, CONTEXT + view.getKey());
                page.waitForURL(url -> URI.create(url).getPath().equals(CONTEXT + view.getKey()));
                assertEquals(1, page.locator("[data-sim-back]").count(), view.getValue());
                assertTrue(page.locator("[data-sim-back]").isVisible(), view.getValue());
                assertTrue(page.locator("[data-sim-back]").isEnabled(), view.getValue());
                BoundingBox bounds = page.locator("[data-sim-back]").boundingBox();
                assertTrue(bounds.x >= 0 && bounds.x + bounds.width <= width, view.getValue());
                assertTrue(page.locator(".container").isVisible(), view.getValue());
                Path screenshot = Path.of("target/navigation", width + "-" + view.getValue() + ".png");
                Files.createDirectories(screenshot.getParent());
                page.screenshot(new Page.ScreenshotOptions().setPath(screenshot));
                page.locator("[data-sim-back]").click();
                page.waitForURL(ORIGIN + CONTEXT + source);
            }
        }
    }

    @Test
    void inlineBackAndForward_restoreTheMatchingViewAndQuery() {
        page.navigate(ORIGIN + CONTEXT + "/dashboard");
        page.evaluate("() => window.DashboardInlineView.openOrNavigate('/chat-server/dashboard/topics?days=30', 'Terms')");
        page.evaluate("() => window.DashboardInlineView.openOrNavigate('/chat-server/dashboard/trends?days=7', 'Trends')");
        page.locator("[data-sim-back]").click();
        page.waitForURL("**/dashboard/topics?days=30");
        page.waitForFunction("() => document.querySelector('#dashboardInlineTitle').textContent === 'Terms'"
                + " && document.querySelector('#dashboardInlineContent h1')?.textContent.includes('Popular Topics')");
        page.locator("[data-sim-back]").click();
        page.waitForURL(ORIGIN + CONTEXT + "/dashboard");
        assertFalse(page.locator("#dashboardInlineSection").isVisible());
        page.goForward();
        page.waitForURL("**/dashboard/topics?days=30");
        page.waitForFunction("() => document.querySelector('#dashboardInlineContent h1')?.textContent.includes('Popular Topics')");
        assertTrue(page.locator("#dashboardInlineSection").isVisible());
    }

    @Test
    void dashboardViewSwitches_keepHeaderVisibleAndLoadingInsideDetail() throws IOException {
        includeBootScripts = true;
        Files.createDirectories(Path.of("target/navigation"));
        for (int width : new int[] { 1440, 390 }) {
            page.setViewportSize(width, 900);
            page.navigate(ORIGIN + CONTEXT + "/dashboard");
                for (String route : new String[] { "/dashboard/topics", "/dashboard/new-users", "/dashboard/sessions",
                    "/dashboard/session-names", "/dashboard/widgets/view" }) {
                page.evaluate("route => window.DashboardInlineView.openOrNavigate('/chat-server' + route)", route);
                assertTrue(page.locator(".top-bar").isVisible(), route);
                assertTrue(page.locator("#dashboardHomeSection").isVisible(), route);
                page.evaluate("document.documentElement.classList.add('page-boot-loading')");
                assertTrue(page.locator(".top-bar").isVisible(), route);
                page.evaluate("window.navigationTestLoading = window.PageBootLoading.begin({title: 'Loading detail'})");
                assertTrue(page.locator(".top-bar").isVisible(), route);
                assertTrue(page.locator("#dashboardInlineContent > #inlinePageBootOverlay").isVisible(), route);
                assertFalse((Boolean) page.evaluate("document.documentElement.classList.contains('page-boot-loading')"));
                assertEquals("true", page.locator("#dashboardInlineContent").getAttribute("aria-busy"));
                if ("/dashboard/topics".equals(route)) {
                    page.screenshot(new Page.ScreenshotOptions()
                            .setPath(Path.of("target/navigation", width + "-dashboard-inline-loading.png")));
                }
                page.evaluate("window.navigationTestLoading.finish()");
                assertEquals("false", page.locator("#dashboardInlineContent").getAttribute("aria-busy"));
                assertEquals(0, ((Number) page.evaluate("window.scrollY")).intValue(), route);
            }
            page.evaluate("window.navigationTestLoading = window.PageBootLoading.begin({title: 'Loading detail'})");
            page.evaluate("window.DashboardInlineView.showDefaultSections()");
            assertTrue(page.locator(".top-bar").isVisible());
            assertFalse((Boolean) page.evaluate("document.documentElement.classList.contains('page-boot-loading')"));
            assertFalse(page.locator("#dashboardInlineSection").isVisible());
            Path screenshot = Path.of("target/navigation", width + "-dashboard-loading-cleanup.png");
            page.screenshot(new Page.ScreenshotOptions().setPath(screenshot));
        }
    }

    @Test
    void standalonePageLoading_remainsFullPageAndCanRestart() {
        includeBootScripts = true;
        page.navigate(ORIGIN + CONTEXT + "/dashboard/topics");
        assertFalse(page.locator(".top-bar").isVisible());
        page.evaluate("window.navigationTestLoading = window.PageBootLoading.begin({title: 'Loading page'})");
        assertTrue(page.locator("#pageBootOverlay").isVisible());
        assertEquals(0, page.locator("#inlinePageBootOverlay").count());
        page.evaluate("window.navigationTestLoading.finish()");
        assertTrue(page.locator(".top-bar").isVisible());
        assertFalse(page.locator("#pageBootOverlay").isVisible());
        page.evaluate("window.navigationTestLoading = window.PageBootLoading.begin({title: 'Loading again'})");
        assertTrue(page.locator("#pageBootOverlay").isVisible());
        page.evaluate("window.navigationTestLoading.finish()");
        assertTrue(page.locator(".top-bar").isVisible());
    }

    @Test
    void newTabWithoutReferrer_returnsToFilteredSourceAfterReload() {
        String source = ORIGIN + CONTEXT + "/dashboard/topics?days=30&page=2";
        page.navigate(source);
        Page popup = page.waitForPopup(() -> page.evaluate("""
                () => {
                    const anchor = document.createElement('a');
                    anchor.href = '/chat-server/customer-profile?sessionId=example';
                    anchor.target = '_blank';
                    anchor.rel = 'noreferrer';
                    document.body.appendChild(anchor);
                    anchor.click();
                }
                """));
        popup.waitForLoadState();
        assertEquals("", popup.evaluate("document.referrer"));
        assertTrue(popup.url().contains("returnTo="));
        popup.reload();
        popup.locator("[data-sim-back]").click();
        popup.waitForURL(source);
        assertTrue(popup.locator(".container").isVisible());
    }

    @Test
    void directEntryAndUnsafeReturnTargets_doNotSendUsersToDashboardOrOutsideApp() {
        for (String suffix : new String[] { "", "?returnTo=https%3A%2F%2Fevil.example%2F",
                "?returnTo=%2Fother-app%2Fpage", "?returnTo=%2Fchat-server%2Flogout" }) {
            Page direct = context.newPage();
            direct.navigate(ORIGIN + CONTEXT + "/profile" + suffix);
                assertTrue(direct.locator("[data-sim-back]").isDisabled(), () -> String.valueOf(direct.evaluate(
                    "({length: history.length, referrer: document.referrer, entries: window.navigation?.entries().map(entry => entry.url)})")));
            direct.evaluate("window.SimNavigation.back()");
            assertEquals(ORIGIN + CONTEXT + "/profile" + suffix, direct.url());
            direct.close();
        }
        page.navigate(ORIGIN + CONTEXT + "/admin");
        page.navigate(ORIGIN + CONTEXT + "/profile");
        assertTrue(page.locator("[data-sim-back]").isEnabled());
        page.locator("[data-sim-back]").click();
        page.waitForURL(ORIGIN + CONTEXT + "/admin");
    }

    private void serveFixture(Route route) {
        URI uri = URI.create(route.request().url());
        if (!"sim.test".equals(uri.getHost())) {
            route.abort();
            return;
        }
        String path = uri.getPath().substring(CONTEXT.length());
        try {
                if (path.equals("/assets/js/navigation.js") || path.equals("/assets/js/dashboard_util/inlineView.js")
                    || path.equals("/assets/js/page_boot_loading.js")) {
                route.fulfill(new Route.FulfillOptions().setContentType("application/javascript")
                        .setBody(Files.readString(webRoot.resolve(path.substring(1)))));
            } else if (path.equals("/assets/css/app.css")) {
                route.fulfill(new Route.FulfillOptions().setContentType("text/css")
                        .setBody(Files.readString(webRoot.resolve(path.substring(1)))));
            } else if (path.endsWith(".js")) {
                route.fulfill(new Route.FulfillOptions().setContentType("application/javascript").setBody(""));
            } else if (VIEWS.containsKey(path)) {
                String html = Files.readString(webRoot.resolve("WEB-INF/views/" + VIEWS.get(path)));
                html = SCRIPTS.matcher(html).replaceAll(match -> match.group().contains("/assets/js/navigation.js")
                    || match.group().contains("/assets/js/dashboard_util/inlineView.js")
                    || (includeBootScripts && (match.group().contains("page-boot-loading")
                        || match.group().contains("/assets/js/page_boot_loading.js")))
                    ? Matcher.quoteReplacement(match.group()) : "");
                html = html.replace("${contextPath}", CONTEXT).replace("${user}", "Navigation test user")
                        .replace("${role}", "ADMIN").replaceAll("\\$\\{[^}]+}", "")
                        .replaceAll("<%@.*?%>", "");
                html = html.replace("</head>", "<script>window.dashboardConfig={contextPath:'/chat-server'};</script></head>");
                route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(html));
            } else {
                route.fulfill(new Route.FulfillOptions().setContentType("application/json").setBody("{}"));
            }
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load navigation fixture: " + path, error);
        }
    }
}