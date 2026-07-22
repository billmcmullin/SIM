package com.sim.chatserver.security;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class SalesforceAuthClient {

    private static final Logger log = Logger.getLogger(SalesforceAuthClient.class.getName());
    private static final Pattern XML_TAG_PATTERN = Pattern.compile("<(?:\\w+:)?%s>(.*?)</(?:\\w+:)?%s>", Pattern.DOTALL);

    private final HttpClient httpClient;

    public SalesforceAuthClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public SalesforceAuthClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public AuthResult refreshAccessToken() throws Exception {
        ServerConfig cfg = EncryptedDbConfigStore.load();
        if (cfg == null) {
            throw new IllegalStateException("ServerConfig not found.");
        }
        return refreshAccessToken(cfg, true);
    }

    public AuthResult refreshAccessToken(ServerConfig cfg) throws Exception {
        return refreshAccessToken(cfg, false);
    }

    private AuthResult refreshAccessToken(ServerConfig cfg, boolean persistResult) throws Exception {
        if (cfg == null) {
            throw new IllegalStateException("ServerConfig not found.");
        }

        boolean hasRefreshFlow = hasRefreshTokenConfig(cfg);
        boolean hasApiTokenFlow = hasApiTokenLoginConfig(cfg);

        if (hasRefreshFlow) {
            try {
                return refreshAccessTokenWithRefreshToken(cfg, persistResult);
            } catch (Exception e) {
                if (!hasApiTokenFlow) {
                    throw e;
                }
                log.log(Level.WARNING,
                        "Salesforce refresh-token flow failed; attempting username/password/API-token fallback.", e);
            }
        }

        if (hasApiTokenFlow) {
            return loginWithApiToken(cfg, persistResult);
        }

        throw new IllegalStateException("Missing Salesforce auth configuration. Configure OAuth refresh or username/password/API token.");
    }

    private AuthResult refreshAccessTokenWithRefreshToken(ServerConfig cfg, boolean persistResult) throws Exception {

        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String clientId = trimToNull(cfg.getSalesforceClientId());
        String clientSecret = trimToNull(cfg.getSalesforceClientSecret());
        String refreshToken = trimToNull(cfg.getSalesforceRefreshToken());

        if (loginUrl == null || clientId == null || clientSecret == null || refreshToken == null) {
            throw new IllegalStateException("Missing Salesforce OAuth refresh configuration.");
        }

        String tokenUrl = normalizeBaseUrl(loginUrl) + "/services/oauth2/token";

        String form = "grant_type=refresh_token"
                + "&client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&refresh_token=" + enc(refreshToken);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IllegalStateException("Salesforce token refresh failed: HTTP " + res.statusCode() + " body=" + res.body());
        }

        AuthResult result = parseAuthResult(res.body());
        if (result == null || isBlank(result.accessToken) || isBlank(result.instanceUrl)) {
            throw new IllegalStateException("Salesforce token refresh returned incomplete payload.");
        }

        // Persist new access token + instance URL
        if (persistResult) {
            cfg.setSalesforceApiKey(result.accessToken);
            cfg.setSalesforceInstanceUrl(result.instanceUrl);
            EncryptedDbConfigStore.save(cfg);
        }

        return result;
    }

    private AuthResult loginWithApiToken(ServerConfig cfg, boolean persistResult) throws Exception {
        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String username = trimToNull(cfg.getSalesforceUsername());
        String password = trimToNull(cfg.getSalesforcePassword());
        String apiToken = trimToNull(cfg.getSalesforceApiToken());

        if (loginUrl == null || username == null || password == null || apiToken == null) {
            throw new IllegalStateException("Missing Salesforce username/password/API token configuration.");
        }

        String endpoint = normalizeBaseUrl(loginUrl) + "/services/Soap/u/61.0";
        String combinedPassword = password + apiToken;
        String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<env:Envelope xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<env:Body><n1:login xmlns:n1=\"urn:partner.soap.sforce.com\">"
                + "<n1:username>" + xmlEscape(username) + "</n1:username>"
                + "<n1:password>" + xmlEscape(combinedPassword) + "</n1:password>"
                + "</n1:login></env:Body></env:Envelope>";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "text/xml; charset=UTF-8")
                .header("SOAPAction", "login")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String payload = res.body();

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            String fault = trimToNull(extractXmlTag(payload, "faultstring"));
            String message = fault != null ? fault : ("Salesforce SOAP login failed: HTTP " + res.statusCode());
            throw new IllegalStateException(message);
        }

        String sessionId = trimToNull(extractXmlTag(payload, "sessionId"));
        String serverUrl = trimToNull(extractXmlTag(payload, "serverUrl"));
        String instanceUrl = toInstanceUrl(serverUrl);

        if (sessionId == null || instanceUrl == null) {
            throw new IllegalStateException("Salesforce SOAP login returned incomplete payload.");
        }

        AuthResult result = new AuthResult();
        result.accessToken = sessionId;
        result.instanceUrl = instanceUrl;

        if (persistResult) {
            cfg.setSalesforceApiKey(result.accessToken);
            cfg.setSalesforceInstanceUrl(result.instanceUrl);
            EncryptedDbConfigStore.save(cfg);
        }

        return result;
    }

    private AuthResult parseAuthResult(String body) {
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject o = reader.readObject();
            AuthResult r = new AuthResult();
            r.accessToken = o.getString("access_token", null);
            r.instanceUrl = o.getString("instance_url", null);
            return r;
        }
    }

    private String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private boolean hasRefreshTokenConfig(ServerConfig cfg) {
        return trimToNull(cfg.getSalesforceLoginUrl()) != null
                && trimToNull(cfg.getSalesforceClientId()) != null
                && trimToNull(cfg.getSalesforceClientSecret()) != null
                && trimToNull(cfg.getSalesforceRefreshToken()) != null;
    }

    private boolean hasApiTokenLoginConfig(ServerConfig cfg) {
        return trimToNull(cfg.getSalesforceLoginUrl()) != null
                && trimToNull(cfg.getSalesforceUsername()) != null
                && trimToNull(cfg.getSalesforcePassword()) != null
                && trimToNull(cfg.getSalesforceApiToken()) != null;
    }

    private String toInstanceUrl(String serverUrl) {
        if (serverUrl == null) {
            return null;
        }
        try {
            URI uri = URI.create(serverUrl);
            String scheme = trimToNull(uri.getScheme());
            String host = trimToNull(uri.getHost());
            if (scheme == null || host == null) {
                return null;
            }
            int port = uri.getPort();
            boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443);
            if (port > 0 && !defaultPort) {
                return scheme + "://" + host + ":" + port;
            }
            return scheme + "://" + host;
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid Salesforce serverUrl from SOAP login", ex);
            return null;
        }
    }

    private String extractXmlTag(String xml, String tagName) {
        if (xml == null || tagName == null || tagName.isBlank()) {
            return null;
        }
        String pattern = String.format(XML_TAG_PATTERN.pattern(), Pattern.quote(tagName), Pattern.quote(tagName));
        Matcher matcher = Pattern.compile(pattern, Pattern.DOTALL).matcher(xml);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private String xmlEscape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String normalizeBaseUrl(String url) {
        String x = url.trim();
        if (!x.startsWith("http://") && !x.startsWith("https://")) {
            x = "https://" + x;
        }
        return x.replaceAll("/+$", "");
    }

    private String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    public static final class AuthResult {

        public String accessToken;
        public String instanceUrl;
    }
}
