package com.sim.chatserver.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.net.http.HttpClient;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SalesforceAuthClientTest {

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

    @SuppressWarnings("unchecked")
    private <T> T invoke(Object target, String methodName, Class<?>[] signature, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, signature);
        method.setAccessible(true);
        return (T) method.invoke(target, args);
    }
}
