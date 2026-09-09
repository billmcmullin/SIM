package com.sim.ui.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import com.sim.ui.base.BaseUiIT;

public class SmokeIT extends BaseUiIT {

    @Test
    void homePageLoads() {
        navigateWithCommit(baseUrl);

        String title = page.title();
        assertFalse(title == null || title.isBlank(), "Page title should not be blank");

        // Basic page sanity check
        String url = page.url();
        assertTrue(url.startsWith(baseUrl), "Expected URL to start with baseUrl, but was: " + url);
    }

    @Test
    void htmlIsReturned() {
        navigateWithCommit(baseUrl);

        String content = page.content();
        assertTrue(content.toLowerCase().contains("<html"), "Expected HTML content in response");
    }

        @Test
        void apiLoginRejectsInvalidCredentials() {
        APIResponse response = page.request().post(
            baseUrl + "/api/auth/login",
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData("{\"username\":\"invalid-user\",\"password\":\"invalid-pass\"}")
        );

        assertTrue(response.status() >= 400,
            "Expected invalid credentials to be rejected, got status=" + response.status());

        boolean hasSessionCookie = context.cookies(baseUrl).stream()
            .anyMatch(c -> c != null
                && c.name != null
                && c.name.toUpperCase().startsWith("JSESSIONID")
                && c.value != null
                && !c.value.isBlank());
        assertFalse(hasSessionCookie,
            "Invalid login should not create an authenticated session cookie.");
        }
}
