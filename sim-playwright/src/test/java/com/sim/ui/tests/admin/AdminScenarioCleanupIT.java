package com.sim.ui.tests.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminScenarioCleanupIT extends BaseUiIT {

    private static final Pattern NUMERIC_ARG = Pattern.compile("\\((\\d+)\\)");
    private static final Pattern QUOTED_ARG = Pattern.compile("'([^']+)'");

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    @Order(1)
    void adminTopButtons_andTabs_areNavigable() {
        login(adminUsername, adminPassword);

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");

        navigateWithCommit("/admin");
        waitForPath("/chat-server/admin");
        page.waitForSelector("#serverConfigForm");

        int tabCount = (int) page.locator("#adminTabs .admin-tab-btn").count();
        if (tabCount > 0) {
            for (int i = 0; i < tabCount; i++) {
            Locator tab = page.locator("#adminTabs .admin-tab-btn").nth(i);
            String tabText = tab.innerText().trim();
            tab.click();
            assertTrue(tab.getAttribute("class").contains("active"),
                "Expected active admin tab after click: " + tabText);
            }
        }

        assertButtonExists("#saveWidgetEntryBtn");
        assertButtonExists("#clearWidgetFormBtn");
        assertButtonExists("#deleteSelectedWidgetsBtn");
        assertButtonExists("#syncWidgetTablesBtn");
        assertButtonExists("#saveSyncIntervalBtn");
        assertButtonExists("#saveSummarySettingsBtn");
        assertButtonExists("#saveWorkspaceBtn");
        assertButtonExists("#saveTermBtn");
        assertButtonExists("#userCreateForm button[type='submit']");

        clickNavButtonNoWait("Dashboard", "/chat-server/dashboard");
        assertTrue(page.url().contains("/chat-server/dashboard"),
                "Expected dashboard navigation from admin top bar.");

        navigateWithCommit("/admin");
        waitForPath("/chat-server/admin");

        clickNavButtonNoWait("Profile", "/chat-server/profile");
        assertTrue(page.url().contains("/chat-server/profile"),
                "Expected profile navigation from admin top bar.");

        navigateWithCommit("/dashboard");
        waitForPath("/chat-server/dashboard");
        navigateWithCommit("/admin");
        waitForPath("/chat-server/admin");
        clickNavButtonNoWait("Back", "/chat-server/dashboard");
        assertTrue(page.url().contains("/chat-server/dashboard"),
                "Expected back navigation to dashboard from admin page.");
    }

    @Test
    @Order(2)
    void createAndDeleteUser_throughAdminUi_withCleanupGuarantee() {
        login(adminUsername, adminPassword);
        page.onDialog(dialog -> dialog.accept());

        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String username = "pw_user_" + uniqueSuffix;
        String password = "Pw!" + uniqueSuffix;
        String createdUserId = null;

        List<String> cleanupFailures = new ArrayList<>();
        try {
            openAdminPage();
            activateAdminTab("Users", "Terms");
            ensureSectionVisible("#userCreateForm");

            page.fill("#newUsername", username);
            page.fill("#newPassword", password);
            page.selectOption("#roleSelect", "USER");
            page.click("#userCreateForm button[type='submit']");

            page.waitForSelector("#userResult");
            assertFalse(page.locator("#userResult").innerText().trim().isBlank(),
                    "Expected user creation result message.");

            Locator row = page.locator("#userTableBody tr:has-text('" + username + "')");
            row.first().waitFor();
            assertTrue(row.count() > 0, "Expected created user row in table.");

            createdUserId = extractQuotedArg(row.first().locator("button:has-text('Delete')").first().getAttribute("onclick"));
            assertTrue(createdUserId != null && !createdUserId.isBlank(), "Expected created user id for cleanup.");

            row.first().locator("button:has-text('Delete')").first().click();
            page.waitForTimeout(300);
            assertTrue(page.locator("#userTableBody tr:has-text('" + username + "')").count() == 0,
                    "Expected created user row to be removed after delete.");
            createdUserId = null;
        } finally {
            if (createdUserId != null && !createdUserId.isBlank()) {
                if (!deleteByApi("/admin/users?userId=" + urlEncode(createdUserId))) {
                    cleanupFailures.add("user:" + createdUserId);
                }
            }
            assertTrue(cleanupFailures.isEmpty(), "Cleanup failed for entities: " + String.join(", ", cleanupFailures));
        }
    }

    @Test
    @Order(3)
    void createAndDeleteTerm_andWidget_throughAdminUi_withCleanupGuarantee() {
        login(adminUsername, adminPassword);
        page.onDialog(dialog -> dialog.accept());

        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String termName = "pw_term_" + uniqueSuffix;
        String widgetIdValue = "pw_widget_" + uniqueSuffix;
        String widgetDisplayName = "PW Widget " + uniqueSuffix;

        String createdTermId = null;
        String createdWidgetDbId = null;
        List<String> cleanupFailures = new ArrayList<>();

        try {
            openAdminPage();

            activateAdminTab("Users", "Terms");
            ensureSectionVisible("#termCreateForm");

            page.fill("#termName", termName);
            page.fill("#termDescription", "Playwright term " + uniqueSuffix);
            page.fill("#termPattern", "*pw-it-" + uniqueSuffix + "*");
            page.selectOption("#termType", "WILDCARD");
            page.click("#saveTermBtn");

            Locator termRow = page.locator("#termTableBody tr:has-text('" + termName + "')");
            termRow.first().waitFor();
            assertTrue(termRow.count() > 0, "Expected created term row in table.");

            String termDeleteOnclick = termRow.first().locator("button:has-text('Delete')").first().getAttribute("onclick");
            createdTermId = extractNumericArg(termDeleteOnclick);
            assertTrue(createdTermId != null && !createdTermId.isBlank(), "Expected created term id for cleanup.");

            termRow.first().locator("button:has-text('Delete')").first().click();
            page.waitForTimeout(300);
            assertTrue(page.locator("#termTableBody tr:has-text('" + termName + "')").count() == 0,
                    "Expected created term row to be removed after delete.");
            createdTermId = null;

            activateAdminTab("Widgets", "Widget");
            ensureSectionVisible("#widgetIdInput");

            page.fill("#widgetIdInput", widgetIdValue);
            page.fill("#widgetNameInput", widgetDisplayName);
            page.click("#saveWidgetEntryBtn");

            Locator widgetRow = page.locator("#widgetTableBody tr:has-text('" + widgetIdValue + "')");
            widgetRow.first().waitFor();
            assertTrue(widgetRow.count() > 0, "Expected created widget row in table.");

            createdWidgetDbId = widgetRow.first().locator(".widget-select").first().getAttribute("value");
            assertTrue(createdWidgetDbId != null && !createdWidgetDbId.isBlank(), "Expected created widget DB id for cleanup.");

            widgetRow.first().locator("button:has-text('Delete')").first().click();
            page.waitForTimeout(300);
            assertTrue(page.locator("#widgetTableBody tr:has-text('" + widgetIdValue + "')").count() == 0,
                    "Expected created widget row to be removed after delete.");
            createdWidgetDbId = null;
        } finally {
            if (createdTermId != null && !createdTermId.isBlank()) {
                if (!deleteByApi("/admin/terms?id=" + urlEncode(createdTermId))) {
                    cleanupFailures.add("term:" + createdTermId);
                }
            }
            if (createdWidgetDbId != null && !createdWidgetDbId.isBlank()) {
                if (!deleteByApi("/admin/widgets?ids=" + urlEncode(createdWidgetDbId))) {
                    cleanupFailures.add("widget:" + createdWidgetDbId);
                }
            }
            assertTrue(cleanupFailures.isEmpty(), "Cleanup failed for entities: " + String.join(", ", cleanupFailures));
        }
    }

    private void openAdminPage() {
        navigateWithCommit("/admin");
        waitForPath("/chat-server/admin");
        page.waitForSelector("#serverConfigForm");
    }

    private void activateAdminTab(String preferredLabel, String fallbackLabel) {
        if (page.locator("#adminTabs .admin-tab-btn").count() == 0) {
            return;
        }
        String[] labels = { preferredLabel, fallbackLabel };
        for (String label : labels) {
            String selector = "#adminTabs .admin-tab-btn:has-text('" + label + "')";
            if (page.locator(selector).count() > 0) {
                page.click(selector, new Page.ClickOptions().setNoWaitAfter(true));
                return;
            }
        }
        throw new AssertionError("Expected one of admin tab labels: " + preferredLabel + " or " + fallbackLabel);
    }

    private void ensureSectionVisible(String selector) {
        if (page.locator(selector).count() == 0) {
            throw new AssertionError("Expected element not found: " + selector);
        }

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
        assertTrue(page.locator(selector).first().isVisible(), "Expected visible element: " + selector);
    }

    private void assertButtonExists(String selector) {
        assertTrue(page.locator(selector).count() > 0,
                "Expected admin button/control: " + selector);
    }

    private boolean deleteByApi(String relativeUrl) {
        APIResponse response = page.request().delete(baseUrl + relativeUrl);
        int status = response.status();
        if (status == 200 || status == 204 || status == 404) {
            return true;
        }
        String body = response.text();
        return body != null && body.contains("already") && body.contains("deleted");
    }

    private String extractNumericArg(String onclickText) {
        if (onclickText == null) {
            return null;
        }
        Matcher m = NUMERIC_ARG.matcher(onclickText);
        return m.find() ? m.group(1) : null;
    }

    private String extractQuotedArg(String onclickText) {
        if (onclickText == null) {
            return null;
        }
        Matcher m = QUOTED_ARG.matcher(onclickText);
        return m.find() ? m.group(1) : null;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void login(String username, String password) {
        loginViaApi(username, password);
    }
}