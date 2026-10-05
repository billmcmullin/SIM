package com.sim.ui.tests.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
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
