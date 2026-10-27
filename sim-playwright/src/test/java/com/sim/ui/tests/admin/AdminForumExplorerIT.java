package com.sim.ui.tests.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import com.sim.ui.base.BaseUiIT;

public class AdminForumExplorerIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    void forumExplorerTab_showsUpdatedControls_andTopBarButtonsRemain() {
        loginViaApi(adminUsername, adminPassword);

        navigateWithCommit("/admin");
        waitForPath("/chat-server/admin");
        page.waitForSelector("#adminTabs",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));

        activateForumExplorerTabIfPresent();
        ensureSectionVisible("#forumDbExplorerForm");

        assertControlExists("#forumDbConnectBtn");
        assertControlExists("#forumDbLoadBtn");
        assertControlExists("#forumDbSaveConfigBtn");
        assertControlExists("#forumDbExportThreadsBtn");

        assertControlExists("#forumDbHumanPageJumpInput");
        assertControlExists("#forumDbHumanPageJumpBtn");

        // Export is page-level now and should not exist inside the overlay.
        assertEquals(0, page.locator("#forumDbOverlayExportBtn").count(),
                "Overlay export button should be removed.");

        assertControlExists("button:has-text('Dashboard')");
        assertControlExists("button:has-text('Profile')");
        assertControlExists("button:has-text('Logout')");
    }

    @Test
    void forumDisplayFilters_changeVisibleDiscussions_withoutExporting() throws IOException {
        loadForumSourceFixture();
        Locator rows = page.locator("#forumDbHumanBody tr[data-absolute-index]");
        assertThat(rows).hasCount(4);
        assertEquals(0, page.locator("#forumDbShowAllMessages").count());
        assertEquals(0, page.locator("#forumDbApplySpamFilter").count());
        assertThat(rows.filter(new Locator.FilterOptions().setHasText("You have been sent this communication from"))).hasCount(2);

        page.locator("#forumDbConfirmedOnly").check();
        assertThat(rows).hasCount(2);
        page.locator("#forumDbHasRepliesOnly").check();
        assertThat(rows).hasCount(1);
        assertThat(rows.first()).containsText("Parasoft API question");
        page.locator("#forumDbConfirmedOnly").uncheck();
        page.locator("#forumDbHasRepliesOnly").uncheck();
        assertThat(rows).hasCount(4);

        assertThat(rows.filter(new Locator.FilterOptions().setHasText("You have been sent this communication from"))).hasCount(2);
        assertThat(page.locator("#forumDbHumanSummary")).containsText("Discussion starters: 4");
    }

    @Test
    void forumSelection_popupAndClear_staySynchronizedWithDisplayFilters() throws IOException {
        loadForumSourceFixture();
        page.locator("#forumDbHumanBody .forum-human-message-cell").first().click();
        assertThat(page.locator("#forumDbHumanDetail")).containsText("Software support answer");
        page.locator("#forumDbOverlaySelectPost").check();
        assertThat(page.locator("#forumDbHumanBody input[data-export-index='0']")).isChecked();
        assertThat(page.locator("#forumDbSelectionCount")).hasText("1 selected");
        page.locator("#forumDbOverlayClearSelectionBtn").click();
        assertThat(page.locator("#forumDbOverlaySelectPost")).not().isChecked();
        assertThat(page.locator("#forumDbSelectionCount")).hasText("0 selected");
        assertThat(page.locator("#forumDbHumanOverlay")).isVisible();
        page.locator("#forumDbOverlayCloseBtn").click();

        page.locator("#forumDbExportScope").selectOption("selected");
        assertThat(page.locator("#forumDbHumanBody tr[data-absolute-index]")).hasCount(4);
        page.locator("#forumDbHumanBody input[data-export-index='2']").check();
        Download download = page.waitForDownload(() -> page.locator("#forumDbExportThreadsBtn").click());
        String csv = Files.readString(download.path());
        assertTrue(csv.contains("You have been sent this communication from Spam Sender"));
        assertTrue(csv.contains("Unsolicited promotion"));
        assertTrue(!csv.contains("Parasoft API question"));
    }

    @Test
    void forumText_preservesUnicodePunctuation_inTablePopupAndCsv() throws IOException {
        loadForumSourceFixture();
        String expected = "Parasoft\u2019s release \u2014 here\u2019s what\u2019s new: \u201cAPI\u201d \u2026 Caf\u00e9. Is this correct???";
        page.evaluate("""
            text => {
                const explorer = window.AdminPage.ForumDbExplorer;
                explorer.loadedRows[0].message_text = text;
                explorer.applyClientFilter();
            }
            """, expected);
        assertThat(page.locator("#forumDbHumanBody .forum-human-message-cell").first()).hasText(expected);
        page.locator("#forumDbHumanBody .forum-human-message-cell").first().click();
        assertThat(page.locator("#forumDbHumanDetail pre").first()).hasText(expected);
        page.locator("#forumDbOverlaySelectPost").check();
        page.locator("#forumDbOverlayCloseBtn").click();
        page.locator("#forumDbExportScope").selectOption("selected");
        Download download = page.waitForDownload(() -> page.locator("#forumDbExportThreadsBtn").click());
        assertTrue(Files.readString(download.path()).contains(expected));
        assertEquals("Parasoft\u2019s \u201cAPI\u201d", page.evaluate("""
            () => window.AdminPage.ForumDbExplorer.cleanMessageText('Parasoft&#8217;s &ldquo;API&rdquo;')
            """));
    }

    @Test
    void forumText_repairsApostrophePatterns_withoutChangingOtherQuestionMarks() throws IOException {
        loadForumSourceFixture();
        String raw = "Parasoft???s release. Here???s what???s new: you???re ready, we???ve tested it, and it won???t fail. Why??? A ??? B; What????; ???s";
        String expected = "Parasoft\u2019s release. Here\u2019s what\u2019s new: you\u2019re ready, we\u2019ve tested it, and it won\u2019t fail. Why??? A ??? B; What????; ???s";
        page.evaluate("""
            text => {
                const explorer = window.AdminPage.ForumDbExplorer;
                explorer.loadedRows[0].message_text = text;
                explorer.applyClientFilter();
            }
            """, raw);
        assertThat(page.locator("#forumDbHumanBody .forum-human-message-cell").first()).hasText(expected);
        page.locator("#forumDbHumanBody .forum-human-message-cell").first().click();
        assertThat(page.locator("#forumDbHumanDetail pre").first()).hasText(expected);
        page.locator("#forumDbOverlaySelectPost").check();
        page.locator("#forumDbOverlayCloseBtn").click();
        page.locator("#forumDbExportScope").selectOption("selected");
        Download download = page.waitForDownload(() -> page.locator("#forumDbExportThreadsBtn").click());
        assertTrue(Files.readString(download.path()).contains(expected));
        assertEquals(raw, page.evaluate("window.AdminPage.ForumDbExplorer.loadedRows[0].message_text"));
    }

    @Test
    void forumText_repairsKnownPunctuationPatterns_andPreservesAmbiguousRuns() throws IOException {
        loadForumSourceFixture();
        String raw = "dotTEST 2026.1 ??? a landmark release. Feedback is invaluable???please share it. Extract ???12345??? from URL ???/12345/pvaname???. T??V S??D and T??V support. Why??? A ??? B; Compiler v9.50???";
        String expected = "dotTEST 2026.1 \u2014 a landmark release. Feedback is invaluable \u2014 please share it. Extract \u201c12345\u201d from URL \u201c/12345/pvaname\u201d. T\u00dcV S\u00dcD and T\u00dcV support. Why??? A ??? B; Compiler v9.50???";
        page.evaluate("""
            text => {
                const explorer = window.AdminPage.ForumDbExplorer;
                explorer.loadedRows[0].message_text = text;
                explorer.applyClientFilter();
            }
            """, raw);
        assertThat(page.locator("#forumDbHumanBody .forum-human-message-cell").first()).hasText(expected);
        page.locator("#forumDbHumanBody .forum-human-message-cell").first().click();
        assertThat(page.locator("#forumDbHumanDetail pre").first()).hasText(expected);
        page.locator("#forumDbOverlaySelectPost").check();
        page.locator("#forumDbOverlayCloseBtn").click();
        page.locator("#forumDbExportScope").selectOption("selected");
        Download download = page.waitForDownload(() -> page.locator("#forumDbExportThreadsBtn").click());
        String csv = Files.readString(download.path());
        assertTrue(csv.contains(expected.replace("\"", "\"\"")));
        assertEquals(raw, page.evaluate("window.AdminPage.ForumDbExplorer.loadedRows[0].message_text"));
    }

    @Test
    void forumText_preservesLongPostAndReply_inPopupAndCsv() throws IOException {
        loadForumSourceFixture();
        String post = "Post start " + "x".repeat(19_935) + " POST-COMPLETE-END";
        String reply = "Reply start " + "y".repeat(43_483) + " REPLY-COMPLETE-END";
        page.evaluate("""
            ([post, reply]) => {
                const explorer = window.AdminPage.ForumDbExplorer;
                explorer.loadedRows[0].message_text = post;
                explorer.loadedRows.find(row => row.role === 'reply').message_text = reply;
                for (let index = 0; index < 14; index++) {
                    explorer.loadedRows.push({...explorer.loadedRows[4], message_id: String(100 + index),
                        message_text: 'Additional reply ' + index});
                }
                window.AdminPage.Api.postUrlEncoded = async () => ({ok: true, payload: {status: 'ok', replies: [
                    {created_at: '2026-01-05', actor: 'Support', message_text: reply}
                ]}});
                explorer.applyClientFilter();
            }
            """, List.of(post, reply));
        page.locator("#forumDbHumanBody .forum-human-message-cell").first().click();
        assertThat(page.locator("#forumDbHumanDetail pre").first()).hasText(post);
        assertThat(page.locator("#forumDbHumanDetail")).containsText("REPLY-COMPLETE-END");
        assertThat(page.locator("#forumDbHumanDetail .forum-human-chain-list li")).hasCount(15);
        page.locator("#forumDbOverlaySelectPost").check();
        page.locator("#forumDbOverlayCloseBtn").click();
        page.locator("#forumDbExportScope").selectOption("selected");
        Download download = page.waitForDownload(() -> page.locator("#forumDbExportThreadsBtn").click());
        String csv = Files.readString(download.path());
        assertTrue(csv.contains(post));
        assertTrue(csv.contains(reply));
    }

    private void loadForumSourceFixture() throws IOException {
        Path webRoot = Path.of("..", "sim-app", "src", "main", "webapp");
        if (!Files.isDirectory(webRoot)) {
            webRoot = Path.of("sim-app", "src", "main", "webapp");
        }
        String template = Files.readString(webRoot.resolve("WEB-INF/views/admin_config.html"));
        String isolatedTemplate = (String) page.evaluate("""
            html => {
                const parsed = new DOMParser().parseFromString(html, 'text/html');
                parsed.querySelectorAll('script, link[rel="stylesheet"]').forEach(element => element.remove());
                return parsed.documentElement.outerHTML;
            }
            """, template);
        page.setContent(isolatedTemplate);
        page.addStyleTag(new Page.AddStyleTagOptions().setPath(webRoot.resolve("assets/css/app.css")));
        page.evaluate("window.AdminPage = {Api: {postUrlEncoded: async () => ({ok: false})}};");
        page.addScriptTag(new Page.AddScriptTagOptions().setPath(webRoot.resolve("assets/js/admin_utils/forum_db_explorer.js")));
        page.evaluate("""
            () => {
                const explorer = window.AdminPage.ForumDbExplorer;
                window.AdminPage.Api.postUrlEncoded = async (url, params) => params.get('action') === 'threadReplies'
                    ? {ok: true, payload: {status: 'ok', replies: [
                        {created_at: '2026-01-05', actor: 'Support', message_text: 'Software support answer'}
                    ]}}
                    : {ok: false};
                explorer.init({});
                const makeRow = (id, discussion, subject, body, confirmed = '0', role = 'user') => ({
                    message_id: id, conversation_id: discussion, chain_key: discussion,
                    subject, message_text: body, community_confirmed: confirmed,
                    actor: 'Member', role, created_at: '2026-01-01'
                });
                explorer.loadedColumns = ['message_id', 'conversation_id', 'chain_key', 'subject',
                    'message_text', 'community_confirmed', 'actor', 'role', 'created_at'];
                explorer.loadedHints = {messageTextColumn: 'message_text', actorColumn: 'actor',
                    roleColumn: 'role', conversationColumn: 'conversation_id', createdAtColumn: 'created_at'};
                explorer.loadedRows = [
                    makeRow('1', '1', 'Software support', 'Parasoft API question', '1'),
                    makeRow('2', '2', 'Other software', 'Computer software question', '1'),
                    makeRow('3', '3', 'You have been sent this communication from Spam Sender', 'Unsolicited promotion'),
                    makeRow('4', '4', 'Forwarded message', 'You have been sent this communication from Spam Sender'),
                    makeRow('5', '1', 'Software support', 'Software support answer', '1', 'reply')
                ];
                explorer.applyClientFilter();
                document.getElementById('forumDbExplorerForm').closest('section').scrollIntoView();
            }
            """);
    }

    private void activateForumExplorerTabIfPresent() {
        Locator tab = page.locator("#adminTabs .admin-tab-btn:has-text('Forum Database Explorer')").first();
        if (tab.count() == 0) {
            return;
        }
        if (tab.isVisible()) {
            tab.click(new Locator.ClickOptions().setNoWaitAfter(true));
        } else {
            page.evaluate("sel => { const el = document.querySelector(sel); if (el) el.click(); }",
                    "#adminTabs .admin-tab-btn:has-text('Forum Database Explorer')");
        }
    }

    private void ensureSectionVisible(String selector) {
        assertTrue(page.locator(selector).count() > 0,
                "Expected section control not found: " + selector);

        if (!page.locator(selector).first().isVisible()) {
            page.evaluate("(sel) => {"
                    + "const el = document.querySelector(sel);"
                    + "if (!el) return;"
                    + "const section = el.closest('section.section');"
                    + "if (section) {"
                    + "  section.style.display = '';"
                    + "  section.classList.add('active');"
                    + "}"
                    + "}", selector);
        }

        page.waitForSelector(selector);
        assertTrue(page.locator(selector).first().isVisible(), "Expected visible section: " + selector);
    }

    private void assertControlExists(String selector) {
        assertTrue(page.locator(selector).count() > 0,
                "Expected control: " + selector);
    }
}
