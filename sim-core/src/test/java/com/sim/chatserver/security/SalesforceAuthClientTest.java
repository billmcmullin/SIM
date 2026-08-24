package com.sim.chatserver.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sim.chatserver.config.ServerConfig;

class SalesforceAuthClientTest {

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> mockResponse(int status, String body) {
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }

    @Test
    void normalizeBaseUrl_addsSchemeAndRemovesTrailingSlashes() throws Exception {
        SalesforceAuthClient client = new SalesforceAuthClient(Mockito.mock(HttpClient.class));

        assertEquals("https://example.com", invoke(client, "normalizeBaseUrl", new Class<?>[]{String.class}, "example.com"));
        assertEquals("https://example.com", invoke(client, "normalizeBaseUrl", new Class<?>[]{String.class}, "https://example.com///"));
    }

    @Test
    void toInstanceUrl_extractsOriginAndHandlesInvalidValues() throws Exception {
        SalesforceAuthClient client = new SalesforceAuthClient(Mockito.mock(HttpClient.class));

        assertEquals("https://my.salesforce.com", invoke(client, "toInstanceUrl", new Class<?>[]{String.class}, "https://my.salesforce.com/services/Soap/u/61.0"));
        assertEquals("https://my.salesforce.com:8443", invoke(client, "toInstanceUrl", new Class<?>[]{String.class}, "https://my.salesforce.com:8443/services/Soap/u/61.0"));
        assertNull(invoke(client, "toInstanceUrl", new Class<?>[]{String.class}, "not a uri"));
    }

    @Test
    void extractXmlTag_andRedactOauthPayload_behaveSafely() throws Exception {
        SalesforceAuthClient client = new SalesforceAuthClient(Mockito.mock(HttpClient.class));

        String xml = "<env:Envelope><env:Body><n1:sessionId>abc123</n1:sessionId></env:Body></env:Envelope>";
        assertEquals("abc123", invoke(client, "extractXmlTag", new Class<?>[]{String.class, String.class}, xml, "sessionId"));
        assertNull(invoke(client, "extractXmlTag", new Class<?>[]{String.class, String.class}, xml, "missing"));

        String payload = "{\"access_token\":\"secret-token\",\"refresh_token\":\"secret-refresh\",\"instance_url\":\"https://x\"}";
        String redacted = invoke(client, "redactOauthPayload", new Class<?>[]{String.class}, payload);
        assertTrue(redacted.contains("\"access_token\":\"[REDACTED]\""));
        assertTrue(redacted.contains("\"refresh_token\":\"[REDACTED]\""));
    }

    @Test
    void parseAuthResult_readsTokenAndInstanceUrl() throws Exception {
        SalesforceAuthClient client = new SalesforceAuthClient(Mockito.mock(HttpClient.class));

        Object result = invoke(client, "parseAuthResult", new Class<?>[]{String.class},
                "{\"access_token\":\"token123\",\"instance_url\":\"https://instance\"}");

        assertNotNull(result);
        assertTrue(result instanceof SalesforceAuthClient.AuthResult);
        SalesforceAuthClient.AuthResult authResult = (SalesforceAuthClient.AuthResult) result;
        assertEquals("token123", authResult.accessToken);
        assertEquals("https://instance", authResult.instanceUrl);
    }

    @Test
    void buildSoapLoginBody_containsExpectedFields() throws Exception {
        SalesforceAuthClient client = new SalesforceAuthClient(Mockito.mock(HttpClient.class));

        String body = invoke(client, "buildSoapLoginBody", new Class<?>[]{String.class, String.class}, "user@example.com", "pwToken");

        assertTrue(body.contains("<n1:login"));
        assertTrue(body.contains("<n1:username>user@example.com</n1:username>"));
        assertTrue(body.contains("<n1:password>pwToken</n1:password>"));
    }

    @Test
    void refreshAccessToken_withNullConfig_throws() {
        SalesforceAuthClient client = new SalesforceAuthClient(Mockito.mock(HttpClient.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> client.refreshAccessToken((ServerConfig) null));
        assertTrue(ex.getMessage().contains("ServerConfig not found"));
    }

    @Test
    void refreshAccessToken_withRefreshTokenFlow_returnsAccessToken() throws Exception {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse<String> success = mockResponse(200, "{\"access_token\":\"acc\",\"instance_url\":\"https://inst\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(success);

        SalesforceAuthClient client = new SalesforceAuthClient(httpClient);
        ServerConfig cfg = new ServerConfig();
        cfg.setSalesforceLoginUrl("https://login.salesforce.com");
        cfg.setSalesforceClientId("cid");
        cfg.setSalesforceClientSecret("csec");
        cfg.setSalesforceRefreshToken("rft");

        SalesforceAuthClient.AuthResult result = client.refreshAccessToken(cfg);

        assertNotNull(result);
        assertEquals("acc", result.accessToken);
        assertEquals("https://inst", result.instanceUrl);
    }

    @Test
    void refreshAccessToken_fallsBackToApiTokenFlow_whenRefreshFails() throws Exception {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        String soapSuccess = "<env:Envelope><env:Body><n1:loginResponse><result>"
                + "<sessionId>session-123</sessionId>"
                + "<serverUrl>https://my.salesforce.com/services/Soap/u/61.0</serverUrl>"
                + "</result></n1:loginResponse></env:Body></env:Envelope>";
        HttpResponse<String> refreshFailure = mockResponse(500, "{\"error\":\"server_error\"}");
        HttpResponse<String> soapFallbackSuccess = mockResponse(200, soapSuccess);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(
                refreshFailure,
                soapFallbackSuccess);

        SalesforceAuthClient client = new SalesforceAuthClient(httpClient);
        ServerConfig cfg = new ServerConfig();
        cfg.setSalesforceLoginUrl("https://login.salesforce.com");
        cfg.setSalesforceClientId("cid");
        cfg.setSalesforceClientSecret("csec");
        cfg.setSalesforceRefreshToken("rft");
        cfg.setSalesforceUsername("user@example.com");
        cfg.setSalesforcePassword("pw");
        cfg.setSalesforceApiToken("token");

        SalesforceAuthClient.AuthResult result = client.refreshAccessToken(cfg);

        assertNotNull(result);
        assertEquals("session-123", result.accessToken);
        assertEquals("https://my.salesforce.com", result.instanceUrl);
    }

    @Test
    void refreshAccessToken_withNoConfiguredFlow_throws() {
        SalesforceAuthClient client = new SalesforceAuthClient(Mockito.mock(HttpClient.class));
        ServerConfig cfg = new ServerConfig();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> client.refreshAccessToken(cfg));
        assertTrue(ex.getMessage().contains("Missing Salesforce auth configuration"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(Object target, String methodName, Class<?>[] signature, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        return (T) method.invoke(target, args);
    }
}
