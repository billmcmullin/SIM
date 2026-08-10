package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class SaveConfigServletTest {

    @Test
    void doPost_savesExplicitValues() throws Exception {
        SaveConfigServlet servlet = new SaveConfigServlet();
        ServerConfig existing = existingConfig();

        Map<String, String[]> params = new HashMap<>();
        params.put("serverHost", new String[]{"new-host"});
        params.put("serverPort", new String[]{"1234"});
        params.put("connectionInfo", new String[]{"new-connection"});
        params.put("apiKey", new String[]{"new-api-key"});
        params.put("workspaceName", new String[]{"new-workspace"});
        params.put("salesforceInstanceUrl", new String[]{"https://instance.example.com"});
        params.put("salesforceApiKey", new String[]{"new-sf-key"});

        HttpServletRequest req = requestWithParams(params);
        HttpServletResponse resp = writableResponse();
        AtomicReference<ServerConfig> savedRef = new AtomicReference<>();

        try (MockedStatic<EncryptedDbConfigStore> cfgMock = mockStatic(EncryptedDbConfigStore.class)) {
            cfgMock.when(EncryptedDbConfigStore::load).thenReturn(existing);
            cfgMock.when(() -> EncryptedDbConfigStore.save(any(ServerConfig.class))).thenAnswer(invocation -> {
                savedRef.set(invocation.getArgument(0, ServerConfig.class));
                return null;
            });

            servlet.doPost(req, resp);

            cfgMock.verify(() -> EncryptedDbConfigStore.save(any(ServerConfig.class)));
            ServerConfig saved = savedRef.get();

            assertNotNull(saved);
            assertEquals("new-host", saved.getServerHost());
            assertEquals(1234, saved.getServerPort());
            assertEquals("new-connection", saved.getConnectionInfo());
            assertEquals("new-api-key", saved.getApiKey());
            assertEquals("new-workspace", saved.getWorkspaceName());
            assertEquals("https://instance.example.com", saved.getSalesforceInstanceUrl());
            assertEquals("new-sf-key", saved.getSalesforceApiKey());
            verify(resp).setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Test
    void doPost_preservesExistingValues_forBlankOrInvalidInputs() throws Exception {
        SaveConfigServlet servlet = new SaveConfigServlet();
        ServerConfig existing = existingConfig();

        Map<String, String[]> params = new HashMap<>();
        params.put("serverHost", new String[]{"   "});
        params.put("serverPort", new String[]{"not-a-number"});
        params.put("connectionInfo", new String[]{""});
        params.put("apiKey", new String[]{""});
        params.put("workspaceName", new String[]{"  "});
        params.put("salesforceInstanceUrl", new String[]{""});
        params.put("salesforceApiKey", new String[]{""});
        params.put("salesforceClientId", new String[]{""});
        params.put("salesforceClientSecret", new String[]{""});
        params.put("salesforceRefreshToken", new String[]{""});
        params.put("salesforceUsername", new String[]{""});
        params.put("salesforcePassword", new String[]{""});
        params.put("salesforceApiToken", new String[]{""});

        HttpServletRequest req = requestWithParams(params);
        HttpServletResponse resp = writableResponse();
        AtomicReference<ServerConfig> savedRef = new AtomicReference<>();

        try (MockedStatic<EncryptedDbConfigStore> cfgMock = mockStatic(EncryptedDbConfigStore.class)) {
            cfgMock.when(EncryptedDbConfigStore::load).thenReturn(existing);
            cfgMock.when(() -> EncryptedDbConfigStore.save(any(ServerConfig.class))).thenAnswer(invocation -> {
                savedRef.set(invocation.getArgument(0, ServerConfig.class));
                return null;
            });

            servlet.doPost(req, resp);

            cfgMock.verify(() -> EncryptedDbConfigStore.save(any(ServerConfig.class)));
            ServerConfig saved = savedRef.get();

            assertNotNull(saved);
            assertEquals(existing.getServerHost(), saved.getServerHost());
            assertEquals(existing.getServerPort(), saved.getServerPort());
            assertEquals(existing.getConnectionInfo(), saved.getConnectionInfo());
            assertEquals(existing.getApiKey(), saved.getApiKey());
            assertEquals(existing.getWorkspaceName(), saved.getWorkspaceName());
            assertEquals(existing.getSalesforceInstanceUrl(), saved.getSalesforceInstanceUrl());
            assertEquals(existing.getSalesforceApiKey(), saved.getSalesforceApiKey());
            assertEquals(existing.getSalesforceClientId(), saved.getSalesforceClientId());
            assertEquals(existing.getSalesforceClientSecret(), saved.getSalesforceClientSecret());
            assertEquals(existing.getSalesforceRefreshToken(), saved.getSalesforceRefreshToken());
            assertEquals(existing.getSalesforceUsername(), saved.getSalesforceUsername());
            assertEquals(existing.getSalesforcePassword(), saved.getSalesforcePassword());
            assertEquals(existing.getSalesforceApiToken(), saved.getSalesforceApiToken());
        }
    }

    @Test
    void doPost_sends500_whenLoadFails() throws Exception {
        SaveConfigServlet servlet = new SaveConfigServlet();

        HttpServletRequest req = requestWithParams(Map.of());
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.isCommitted()).thenReturn(false);

        try (MockedStatic<EncryptedDbConfigStore> cfgMock = mockStatic(EncryptedDbConfigStore.class)) {
            cfgMock.when(EncryptedDbConfigStore::load).thenThrow(new SQLException("db down"));

            servlet.doPost(req, resp);

            verify(resp).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private static HttpServletRequest requestWithParams(Map<String, String[]> params) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameterValues(any(String.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            return params.get(key);
        });
        return req;
    }

    private static HttpServletResponse writableResponse() throws Exception {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getOutputStream()).thenThrow(new IllegalStateException("no output stream"));
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(resp.isCommitted()).thenReturn(false);
        return resp;
    }

    private static ServerConfig existingConfig() {
        ServerConfig cfg = new ServerConfig("old-host", 4321, "old-connection", "old-api-key", "old-workspace");
        cfg.setSalesforceInstanceUrl("https://old.instance");
        cfg.setSalesforceApiKey("old-sf-key");
        cfg.setSalesforceLoginUrl("https://login.salesforce.com");
        cfg.setSalesforceClientId("old-client-id");
        cfg.setSalesforceClientSecret("old-client-secret");
        cfg.setSalesforceRefreshToken("old-refresh-token");
        cfg.setSalesforceUsername("old-username");
        cfg.setSalesforcePassword("old-password");
        cfg.setSalesforceApiToken("old-api-token");
        return cfg;
    }
}
