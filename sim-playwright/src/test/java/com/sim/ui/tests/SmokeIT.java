package com.sim.ui.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

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
}
