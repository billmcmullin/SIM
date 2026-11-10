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
import com.microsoft.playwright.options.BoundingBox;
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