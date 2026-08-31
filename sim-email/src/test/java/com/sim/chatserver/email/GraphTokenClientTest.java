package com.sim.chatserver.email;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

class GraphTokenClientTest {

    @Test
    @DisplayName("constructor throws when config is null")
    void constructor_nullConfig_throws() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new GraphTokenClient(null));
        assertEquals("GraphEmailConfig is required", ex.getMessage());
    }

    @Test
    @DisplayName("getAccessToken throws EmailException when config is not usable")
    void getAccessToken_configNotUsable_throws() {
        GraphEmailConfig config = new GraphEmailConfig("tenant-1", "", "secret-1", "sender@contoso.com", "login.microsoftonline.com");

        GraphTokenClient client = new GraphTokenClient(config);

        EmailException ex = assertThrows(EmailException.class, client::getAccessToken);
        assertTrue(ex.getMessage().contains("Graph config is incomplete"));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("getAccessToken fetches token successfully and caches it")
    void getAccessToken_success_thenCached() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig(
            "tenant-1",
            "client-1",
            "secret-1",
            "sender@contoso.com",
            "login.microsoftonline.com"
        );

        HttpsURLConnection conn = mock(HttpsURLConnection.class);
        ByteArrayOutputStream postedBody = new ByteArrayOutputStream();

        when(conn.getOutputStream()).thenReturn(postedBody);
        when(conn.getResponseCode()).thenReturn(200);
        when(conn.getInputStream()).thenReturn(
                new ByteArrayInputStream("{\"access_token\":\"tok-abc\",\"expires_in\":3600}".getBytes())
        );

        GraphTokenClient client = new GraphTokenClient(config);
        String t1;
        String t2;
        try (MockedStatic<URI> uriMock = mockUriConnection(conn)) {
            t1 = client.getAccessToken();
            t2 = client.getAccessToken();
        }

        assertEquals("tok-abc", t1);
        assertEquals("tok-abc", t2);

        // First call fetches token; second should use cache (no second HTTP roundtrip)
        verify(conn, times(1)).getResponseCode();
        verify(conn, times(1)).disconnect();

        String form = postedBody.toString();
        assertTrue(form.contains("client_id=client-1"));
        assertTrue(form.contains("client_secret=secret-1"));
        assertTrue(form.contains("grant_type=client_credentials"));
        assertTrue(form.contains("scope=https%3A%2F%2Fgraph.microsoft.com%2F.default"));
    }

    @Test
    @DisplayName("getAccessToken throws EmailException on non-2xx token response")
    void getAccessToken_httpError_throws() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant-1",
                "client-1",
                "secret-1",
                "sender@contoso.com",
                "login.microsoftonline.com"
        );

        HttpsURLConnection conn = mock(HttpsURLConnection.class);
        when(conn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(conn.getResponseCode()).thenReturn(401);
        when(conn.getErrorStream()).thenReturn(new ByteArrayInputStream("{\"error\":\"unauthorized\"}".getBytes()));

        GraphTokenClient client = new GraphTokenClient(config);
        EmailException ex;
        try (MockedStatic<URI> uriMock = mockUriConnection(conn)) {
            ex = assertThrows(EmailException.class, client::getAccessToken);
        }
        assertTrue(ex.getMessage().contains("Failed to acquire Graph token. HTTP 401"));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("graph_token_http_401"));

        verify(conn, times(1)).disconnect();
    }

    @Test
    @DisplayName("getAccessToken throws EmailException when access_token is missing")
    void getAccessToken_missingAccessToken_throws() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant-1",
                "client-1",
                "secret-1",
                "sender@contoso.com",
                "login.microsoftonline.com"
        );

        HttpsURLConnection conn = mock(HttpsURLConnection.class);
        when(conn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(conn.getResponseCode()).thenReturn(200);
        when(conn.getInputStream()).thenReturn(new ByteArrayInputStream("{\"expires_in\":3600}".getBytes()));

        GraphTokenClient client = new GraphTokenClient(config);
        EmailException ex;
        try (MockedStatic<URI> uriMock = mockUriConnection(conn)) {
            ex = assertThrows(EmailException.class, client::getAccessToken);
        }
        assertTrue(ex.getMessage().contains("Graph token response missing access_token"));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("missing_access_token"));

        verify(conn, times(1)).disconnect();
    }

    @Test
    @DisplayName("getAccessToken wraps unexpected exceptions")
    void getAccessToken_unexpectedException_wrapped() throws Exception {
        GraphEmailConfig config = new GraphEmailConfig(
                "tenant-1",
                "client-1",
                "secret-1",
                "sender@contoso.com",
                "login.microsoftonline.com"
        );

        HttpsURLConnection conn = mock(HttpsURLConnection.class);
        when(conn.getOutputStream()).thenThrow(new IllegalArgumentException("boom"));

        GraphTokenClient client = new GraphTokenClient(config);
        EmailException ex;
        try (MockedStatic<URI> uriMock = mockUriConnection(conn)) {
            ex = assertThrows(EmailException.class, client::getAccessToken);
        }
        assertEquals("Graph token acquisition failed", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());

        verify(conn, times(1)).disconnect();
    }

    private static MockedStatic<URI> mockUriConnection(HttpsURLConnection connection) throws Exception {
        MockedStatic<URI> uriMock = Mockito.mockStatic(URI.class);
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        when(url.openConnection()).thenReturn(connection);
        when(uri.toURL()).thenReturn(url);
        uriMock.when(() -> URI.create(anyString())).thenReturn(uri);
        return uriMock;
    }
}
