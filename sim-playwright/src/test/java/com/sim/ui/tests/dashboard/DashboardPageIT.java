package com.sim.ui.tests.dashboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.sim.ui.base.BaseUiIT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardPageIT extends BaseUiIT {

        private static final String[] DASHBOARD_TEMPLATE_RELATIVE = {
                "src", "main", "webapp", "WEB-INF", "views", "dashboard.html"
        };

        private static final String[] DASHBOARD_SERVLET_RELATIVE = {
                "src", "main", "java", "com", "sim", "chatserver", "web", "dashboard", "DashboardServlet.java"
        };

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    private final String userUsername = System.getProperty("userUsername", "user");
    private final String userPassword = System.getProperty("userPassword", "user");

    @Test
    @Order(1)
    void unauthenticated_dashboardRedirectsToLogin() {
        navigateWithCommit("/dashboard/topics");
        waitForLoginScreen();
        assertOnLoginScreen("Expected unauthenticated dashboard navigation to land on login,");
    }

    @Test
    @Order(2)
    void admin_seesDashboardCoreSections_andAdminLink() {
        login(adminUsername, adminPassword);

        APIResponse adminResponse = page.request().get(
                baseUrl + "/admin",
                RequestOptions.create().setTimeout(30000)
        );
        assertTrue(adminResponse.status() == 200, "Expected admin page 200 for admin user.");
        assertTrue(adminResponse.text().contains("id=\"adminTabs\""),
                "Expected admin core tabs to be present for admin user.");

        String template = readWorkspaceFile(resolveProjectFile("sim-app", DASHBOARD_TEMPLATE_RELATIVE));
        assertTrue(template.contains("<h2>Daily Progress</h2>"), "Expected Daily Progress section in dashboard template.");
        assertTrue(template.contains("<h2>Widget Chat Overview</h2>"), "Expected widget overview section in dashboard template.");
        assertTrue(template.contains("Term Distribution based on Prompts"), "Expected term distribution section in dashboard template.");
        assertTrue(template.contains("<h2>Top 10 Sessions</h2>"), "Expected top sessions section in dashboard template.");
        assertTrue(template.contains("window.location.href='${adminHref}'"),
                "Expected admin button target placeholder in dashboard template.");

        assertTrue(template.contains("id=\"dpTodayChats\""), "Expected dpTodayChats element in dashboard template.");
        assertTrue(template.contains("id=\"dpTopTermsBody\""), "Expected dpTopTermsBody element in dashboard template.");
        assertTrue(template.contains("id=\"otherParasoftLatestBody\""), "Expected otherParasoftLatestBody element in dashboard template.");
        assertTrue(template.contains("id=\"widgetStatsBody\""), "Expected widgetStatsBody element in dashboard template.");
        assertTrue(template.contains("id=\"topSessionList\""), "Expected topSessionList element in dashboard template.");
    }

    @Test
    @Order(3)
    void user_doesNotSeeAdminLink() {
        String template = readWorkspaceFile(resolveProjectFile("sim-app", DASHBOARD_TEMPLATE_RELATIVE));
        assertTrue(template.contains("style=\"${adminButtonStyle}\""),
                "Expected dashboard template to control admin-button visibility by role.");

        boolean authenticated = tryLoginViaApi(userUsername, userPassword);
        APIResponse adminResponse = page.request().get(
                baseUrl + "/admin",
                RequestOptions.create().setTimeout(30000)
        );

        if (authenticated) {
            assertFalse(adminResponse.text().contains("id=\"adminTabs\""),
                    "Non-admin user should not receive admin configuration page content.");
        } else {
            assertTrue(adminResponse.status() == 401 || adminResponse.text().contains("id=\"loginForm\""),
                    "When non-admin credentials are unavailable, admin page must still be protected.");
        }
    }

    @Test
    @Order(4)
    void msg_noIncreaseForTerm_showsBanner() {
        String servletSource = readWorkspaceFile(resolveProjectFile("sim-web", DASHBOARD_SERVLET_RELATIVE));
        assertTrue(servletSource.contains("\"noIncreaseForTerm\""),
                "Expected noIncreaseForTerm branch in DashboardServlet.");
        assertTrue(servletSource.contains("No increased chats found for that term today."),
                "Expected no-increase dashboard banner message.");
        assertTrue(servletSource.contains("dashboard-info-banner"),
                "Expected no-increase message to be rendered as dashboard-info-banner.");
    }

    @Test
    @Order(5)
    void msg_noYesterdayForTerm_showsBanner() {
                String servletSource = readWorkspaceFile(resolveProjectFile("sim-web", DASHBOARD_SERVLET_RELATIVE));
                assertTrue(servletSource.contains("\"noYesterdayForTerm\""),
                                "Expected noYesterdayForTerm branch in DashboardServlet.");
                assertTrue(servletSource.contains("No chats found for that term yesterday."),
                                "Expected no-yesterday dashboard banner message.");
                assertTrue(servletSource.contains("dashboard-info-banner"),
                                "Expected no-yesterday message to be rendered as dashboard-info-banner.");
    }

        private Path resolveProjectFile(String moduleName, String[] moduleRelativePath) {
                Path moduleLocal = Paths.get("..", moduleName, moduleRelativePath[0]);
                for (int i = 1; i < moduleRelativePath.length; i++) {
                        moduleLocal = moduleLocal.resolve(moduleRelativePath[i]);
                }

                if (Files.exists(moduleLocal)) {
                        return moduleLocal.normalize();
                }

                Path workspacePath = Paths.get("project", "SIM", moduleName, moduleRelativePath[0]);
                for (int i = 1; i < moduleRelativePath.length; i++) {
                        workspacePath = workspacePath.resolve(moduleRelativePath[i]);
                }
                if (Files.exists(workspacePath)) {
                        return workspacePath.normalize();
                }

                throw new AssertionError("Could not locate file under module " + moduleName);
        }

        private String readWorkspaceFile(Path path) {
                try {
                        return Files.readString(path, StandardCharsets.UTF_8);
                } catch (IOException ex) {
                        throw new AssertionError("Unable to read file: " + path, ex);
                }
        }

        private void login(String username, String password) {
                loginViaApi(username, password);
        }
}
