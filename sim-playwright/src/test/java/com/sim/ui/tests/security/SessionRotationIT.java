package com.sim.ui.tests.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.RequestOptions;
import com.sim.ui.base.BaseUiIT;

public class SessionRotationIT extends BaseUiIT {

    private final String adminUsername = System.getProperty("adminUsername", "admin");
    private final String adminPassword = System.getProperty("adminPassword", "admin");

    @Test
    void loginTwice_rotatesSessionCookie() {
        login(adminUsername, adminPassword);
        String firstSessionCookie = findSessionCookieValue();

        APIResponse logoutResponse = page.request().get(baseUrl + "/logout");
        assertTrue(logoutResponse.status() == 200 || logoutResponse.status() == 302,
            "Expected successful logout status, got: " + logoutResponse.status());

        login(adminUsername, adminPassword);
        String secondSessionCookie = findSessionCookieValue();

        assertNotEquals(
                firstSessionCookie,
                secondSessionCookie,
                "Expected session cookie value to rotate after re-login."
        );
    }

    @Test
    void staleCookieIsRejected_afterSessionRotation() {
        login(adminUsername, adminPassword);
        Cookie firstSessionCookie = findSessionCookie();

        APIResponse relogin = page.request().post(
            baseUrl + "/api/auth/login",
            RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginPayload(adminUsername, adminPassword))
        );

        assertEquals(200, relogin.status(), "Expected API login to succeed for session rotation.");

        Cookie rotatedSessionCookie = findSessionCookieByName(firstSessionCookie.name);
        assertNotEquals(
                firstSessionCookie.value,
                rotatedSessionCookie.value,
                "Expected session cookie value to change after re-login."
        );

        APIRequestContext staleContext = playwright.request().newContext(
            new APIRequest.NewContextOptions()
                .setIgnoreHTTPSErrors(ignoreHttpsErrors)
                .setExtraHTTPHeaders(Map.of("Cookie", firstSessionCookie.name + "=" + firstSessionCookie.value))
        );

        APIResponse staleResponse = staleContext.get(baseUrl + "/dashboard/sessions/data");
        assertEquals(
                401,
                staleResponse.status(),
                "Expected stale pre-rotation session cookie to be rejected."
        );
        assertTrue(
                staleResponse.text().contains("Authentication required"),
                "Expected authentication error payload when stale cookie is used."
        );
        staleContext.dispose();
            }

    private void login(String username, String password) {
            loginViaApi(username, password);
    }

    private String findSessionCookieValue() {
        Cookie sessionCookie = findSessionCookie();
        return sessionCookie.value;
    }

    private Cookie findSessionCookie() {
        List<Cookie> cookies = context.cookies(baseUrl);
        assertFalse(cookies.isEmpty(), "Expected browser cookies to be present after login.");

        Cookie sessionCookie = null;
        for (Cookie cookie : cookies) {
            if (cookie != null && cookie.name != null && cookie.name.toUpperCase().startsWith("JSESSIONID")) {
                sessionCookie = cookie;
                break;
            }
        }

        String cookieNames = cookies.stream()
                .filter(c -> c != null && c.name != null)
                .map(c -> c.name)
                .collect(Collectors.joining(", "));

        assertNotNull(sessionCookie, "Expected a JSESSIONID* cookie, found: " + cookieNames);
        assertNotNull(sessionCookie.value, "Expected session cookie value to be non-null.");
        assertFalse(sessionCookie.value.isBlank(), "Expected session cookie value to be non-blank.");
        return sessionCookie;
    }

    private Cookie findSessionCookieByName(String cookieName) {
        List<Cookie> cookies = context.cookies(baseUrl);
        for (Cookie cookie : cookies) {
            if (cookie != null && cookie.name != null && cookie.name.equals(cookieName)) {
                assertNotNull(cookie.value, "Expected session cookie value to be non-null.");
                assertFalse(cookie.value.isBlank(), "Expected session cookie value to be non-blank.");
                return cookie;
            }
        }
        String cookieNames = cookies.stream()
                .filter(c -> c != null && c.name != null)
                .map(c -> c.name)
                .collect(Collectors.joining(", "));
        throw new AssertionError("Expected cookie named '" + cookieName + "', found: " + cookieNames);
    }

    private String loginPayload(String username, String password) {
        String safeUsername = username == null ? "" : username.replace("\\", "\\\\").replace("\"", "\\\"");
        String safePassword = password == null ? "" : password.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"username\":\"" + safeUsername + "\",\"password\":\"" + safePassword + "\"}";
    }
}