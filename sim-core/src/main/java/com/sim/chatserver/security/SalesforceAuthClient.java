package com.sim.chatserver.security;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.ServerDiagnosticsLog;
import com.sim.chatserver.util.TextIoSanitizerUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class SalesforceAuthClient {

    private static final Logger log = Logger.getLogger(SalesforceAuthClient.class.getCanonicalName());
    private static final Pattern XML_TAG_PATTERN = Pattern.compile("<(?:\\w+:)?%s>(.*?)</(?:\\w+:)?%s>", Pattern.DOTALL);
    private static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String SOAP_PARTNER_NS = "urn:partner.soap.sforce.com";
    private static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    private static final String XML_SCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String LS = System.lineSeparator();
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String EMPTY = "";
    private static final String MSG_SERVER_CONFIG_NOT_FOUND = "ServerConfig not found.";
    private static final String LOG_PRIMARY_AUTH_FALLBACK = "Primary Salesforce auth flow failed; attempting configured fallback flow.";
    private static final String MSG_MISSING_AUTH_CONFIG = "Missing Salesforce auth configuration. Configure OAuth refresh or username/password/API token.";
    private static final String MSG_MISSING_REFRESH_CONFIG = "Missing Salesforce OAuth refresh configuration.";
    private static final String TOKEN_PATH = "/services/oauth2/token";
    private static final String FORM_GRANT_TYPE_REFRESH = "grant_type=refresh_token";
    private static final String FORM_CLIENT_ID = "&client_id=";
    private static final String FORM_CLIENT_SECRET = "&client_secret=";
    private static final String FORM_REFRESH_TOKEN = "&refresh_token=";
    private static final String HEADER_CONTENT_TYPE = CommonLiteralConstants.HEADER_CONTENT_TYPE;
    private static final String HEADER_ACCEPT = CommonLiteralConstants.HEADER_ACCEPT;
    private static final String HEADER_SOAP_ACTION = "SOAPAction";
    private static final String MIME_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String MIME_JSON = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String MIME_TEXT_XML_UTF8 = "text/xml; charset=UTF-8";
    private static final String COMPONENT_SALESFORCE_AUTH_CLIENT = "salesforce-auth-client";
    private static final String EVENT_TOKEN_REQUEST = "token-request";
    private static final String EVENT_TOKEN_RESPONSE = "token-response";
    private static final String KV_METHOD_POST = CommonLiteralConstants.LOG_KV_METHOD_POST;
    private static final String KV_URL = CommonLiteralConstants.LOG_KV_URL;
    private static final String KV_STATUS = CommonLiteralConstants.LOG_KV_STATUS;
    private static final String KV_BODY = CommonLiteralConstants.LOG_KV_BODY;
    private static final String MSG_TOKEN_REFRESH_FAILED_PREFIX = "Salesforce token refresh failed: HTTP ";
    private static final String MSG_TOKEN_REFRESH_FAILED_BODY_PREFIX = " body=";
    private static final String MSG_TOKEN_REFRESH_INCOMPLETE = "Salesforce token refresh returned incomplete payload.";
    private static final String MSG_MISSING_API_TOKEN_CONFIG = "Missing Salesforce API-token login configuration.";
    private static final String SOAP_LOGIN_PATH = "/services/Soap/u/61.0";
    private static final String SOAP_ACTION_LOGIN = "login";
    private static final String SOAP_TAG_FAULTSTRING = "faultstring";
    private static final String SOAP_TAG_SESSION_ID = CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String SOAP_TAG_SERVER_URL = "serverUrl";
    private static final String MSG_SOAP_LOGIN_FAILED_PREFIX = "Salesforce SOAP login failed: HTTP ";
    private static final String MSG_SOAP_LOGIN_INCOMPLETE = "Salesforce SOAP login returned incomplete payload.";
    private static final String JSON_ACCESS_TOKEN = "access_token";
    private static final String JSON_INSTANCE_URL = "instance_url";
    private static final String SCHEME_HTTP = CommonLiteralConstants.SCHEME_HTTP;
    private static final String SCHEME_HTTPS = CommonLiteralConstants.SCHEME_HTTPS;
    private static final String SCHEME_SEPARATOR = CommonLiteralConstants.SCHEME_SEPARATOR;
    private static final String LOG_INVALID_SERVER_URL = "Invalid Salesforce serverUrl from SOAP login";
    private static final String XML_VERSION = "1.0";
    private static final String XML_PREFIX_ENV = "env";
    private static final String XML_PREFIX_XSD = "xsd";
    private static final String XML_PREFIX_XSI = "xsi";
    private static final String XML_PREFIX_N1 = "n1";
    private static final String SOAP_TAG_ENVELOPE = "Envelope";
    private static final String SOAP_TAG_BODY = "Body";
    private static final String SOAP_TAG_LOGIN = "login";
    private static final String SOAP_TAG_USERNAME = CommonLiteralConstants.KEY_USERNAME;
    private static final String SOAP_TAG_PASSWORD = CommonLiteralConstants.KEY_PASSWORD;
    private static final String MSG_SOAP_PAYLOAD_BUILD_FAILED = "Unable to build Salesforce SOAP payload.";
    private static final String URL_PREFIX_HTTP = CommonLiteralConstants.URL_PREFIX_HTTP;
    private static final String URL_PREFIX_HTTPS = CommonLiteralConstants.URL_PREFIX_HTTPS;
    private static final String REGEX_TRAILING_SLASHES = CommonLiteralConstants.REGEX_TRAILING_SLASHES;
    private static final String REGEX_REDACT_ACCESS_TOKEN = "\"access_token\"\\s*:\\s*\"[^\"]*\"";
    private static final String REGEX_REDACT_REFRESH_TOKEN = "\"refresh_token\"\\s*:\\s*\"[^\"]*\"";
    private static final String REPLACEMENT_REDACT_ACCESS_TOKEN = "\"access_token\":\"[REDACTED]\"";
    private static final String REPLACEMENT_REDACT_REFRESH_TOKEN = "\"refresh_token\":\"[REDACTED]\"";
    private static final String ELLIPSIS = CommonLiteralConstants.ELLIPSIS;

    private final HttpClient httpClient;

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    public SalesforceAuthClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    public SalesforceAuthClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public AuthResult refreshAccessToken() throws IOException, InterruptedException, SQLException {
        ServerConfig cfg = EncryptedDbConfigStore.load();
        if (cfg == null) {
            throw new IllegalStateException(MSG_SERVER_CONFIG_NOT_FOUND);
        }
        return refreshAccessToken(cfg, true);
    }

    public AuthResult refreshAccessToken(ServerConfig cfg) throws IOException, InterruptedException, SQLException {
        return refreshAccessToken(cfg, false);
    }

        private AuthResult refreshAccessToken(ServerConfig cfg, boolean persistResult)
            throws IOException, InterruptedException, SQLException {
        if (cfg == null) {
            throw new IllegalStateException(MSG_SERVER_CONFIG_NOT_FOUND);
        }

        boolean hasRefreshFlow = hasRefreshTokenConfig(cfg);
        boolean hasApiTokenFlow = hasApiTokenLoginConfig(cfg);

        if (hasRefreshFlow) {
            try {
                return refreshAccessTokenWithRefreshToken(cfg, persistResult);
            } catch (IOException | SQLException | IllegalStateException e) {
                if (!hasApiTokenFlow) {
                    throw e;
                }
                log.log(Level.WARNING,
                        LOG_PRIMARY_AUTH_FALLBACK, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }

        if (hasApiTokenFlow) {
            return loginWithApiToken(cfg, persistResult);
        }

        throw new IllegalStateException(MSG_MISSING_AUTH_CONFIG);
    }

        private AuthResult refreshAccessTokenWithRefreshToken(ServerConfig cfg, boolean persistResult)
                throws IOException, InterruptedException, SQLException {

        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String clientId = trimToNull(cfg.getSalesforceClientId());
        String clientSecret = trimToNull(cfg.getSalesforceClientSecret());
        String refreshToken = trimToNull(cfg.getSalesforceRefreshToken());

        if (loginUrl == null || clientId == null || clientSecret == null || refreshToken == null) {
            throw new IllegalStateException(MSG_MISSING_REFRESH_CONFIG);
        }

        String tokenUrl = normalizeBaseUrl(loginUrl) + TOKEN_PATH;

        String form = FORM_GRANT_TYPE_REFRESH
            + FORM_CLIENT_ID + enc(clientId)
            + FORM_CLIENT_SECRET + enc(clientSecret)
            + FORM_REFRESH_TOKEN + enc(refreshToken);
        String requestId = UUID.randomUUID().toString();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(Duration.ofSeconds(15))
            .header(HEADER_CONTENT_TYPE, MIME_FORM_URLENCODED)
            .header(HEADER_ACCEPT, MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        ServerDiagnosticsLog.write(
            COMPONENT_SALESFORCE_AUTH_CLIENT,
            requestId,
            EVENT_TOKEN_REQUEST,
            KV_METHOD_POST + LS + KV_URL + tokenUrl
        );

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String responseBody = res.body() == null ? EMPTY : res.body();

        ServerDiagnosticsLog.write(
            COMPONENT_SALESFORCE_AUTH_CLIENT,
            requestId,
            EVENT_TOKEN_RESPONSE,
            KV_STATUS + res.statusCode() + LS + KV_BODY + redactOauthPayload(responseBody)
        );

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IllegalStateException(MSG_TOKEN_REFRESH_FAILED_PREFIX + res.statusCode()
                    + MSG_TOKEN_REFRESH_FAILED_BODY_PREFIX + redactOauthPayload(responseBody));
        }

        AuthResult result = parseAuthResult(responseBody);
        if (result == null || isBlank(result.accessToken) || isBlank(result.instanceUrl)) {
            throw new IllegalStateException(MSG_TOKEN_REFRESH_INCOMPLETE);
        }

        // Persist new access token + instance URL
        if (persistResult) {
            cfg.setSalesforceApiKey(result.accessToken);
            cfg.setSalesforceInstanceUrl(result.instanceUrl);
            EncryptedDbConfigStore.save(cfg);
        }

        return result;
    }

        private AuthResult loginWithApiToken(ServerConfig cfg, boolean persistResult)
            throws IOException, InterruptedException, SQLException {
        String loginUrl = trimToNull(cfg.getSalesforceLoginUrl());
        String username = trimToNull(cfg.getSalesforceUsername());
        String password = trimToNull(cfg.getSalesforcePassword());
        String apiToken = trimToNull(cfg.getSalesforceApiToken());

        if (loginUrl == null || username == null || password == null || apiToken == null) {
            throw new IllegalStateException(MSG_MISSING_API_TOKEN_CONFIG);
        }

        String endpoint = normalizeBaseUrl(loginUrl) + SOAP_LOGIN_PATH;
        String combinedPassword = password + apiToken;
        String body = buildSoapLoginBody(username, combinedPassword);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
            .header(HEADER_CONTENT_TYPE, MIME_TEXT_XML_UTF8)
            .header(HEADER_SOAP_ACTION, SOAP_ACTION_LOGIN)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String payload = res.body();

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            String fault = trimToNull(extractXmlTag(payload, SOAP_TAG_FAULTSTRING));
            String message = fault != null ? fault : (MSG_SOAP_LOGIN_FAILED_PREFIX + res.statusCode());
            throw new IllegalStateException(message);
        }

        String sessionId = trimToNull(extractXmlTag(payload, SOAP_TAG_SESSION_ID));
        String serverUrl = trimToNull(extractXmlTag(payload, SOAP_TAG_SERVER_URL));
        String instanceUrl = toInstanceUrl(serverUrl);

        if (sessionId == null || instanceUrl == null) {
            throw new IllegalStateException(MSG_SOAP_LOGIN_INCOMPLETE);
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
            r.accessToken = o.getString(JSON_ACCESS_TOKEN, null);
            r.instanceUrl = o.getString(JSON_INSTANCE_URL, null);
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
            boolean defaultPort = (SCHEME_HTTP.equalsIgnoreCase(scheme) && port == 80)
                    || (SCHEME_HTTPS.equalsIgnoreCase(scheme) && port == 443);
            if (port > 0 && !defaultPort) {
                return scheme + SCHEME_SEPARATOR + host + (char) 58 + port;
            }
            return scheme + SCHEME_SEPARATOR + host;
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, LOG_INVALID_SERVER_URL, ex);
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
        int start = matcher.start(1);
        int end = matcher.end(1);
        return TextIoSanitizerUtil.safeSlice(xml, start, end);
    }

    private String buildSoapLoginBody(String username, String combinedPassword) {
        try {
            StringWriter writer = new StringWriter(512);
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            XMLStreamWriter xml = factory.createXMLStreamWriter(writer);

            xml.writeStartDocument(StandardCharsets.UTF_8.name(), XML_VERSION);
            xml.setPrefix(XML_PREFIX_ENV, SOAP_ENV_NS);
            xml.setPrefix(XML_PREFIX_XSD, XML_SCHEMA_NS);
            xml.setPrefix(XML_PREFIX_XSI, XML_SCHEMA_INSTANCE_NS);

            xml.writeStartElement(XML_PREFIX_ENV, SOAP_TAG_ENVELOPE, SOAP_ENV_NS);
            xml.writeNamespace(XML_PREFIX_ENV, SOAP_ENV_NS);
            xml.writeNamespace(XML_PREFIX_XSD, XML_SCHEMA_NS);
            xml.writeNamespace(XML_PREFIX_XSI, XML_SCHEMA_INSTANCE_NS);

            xml.writeStartElement(XML_PREFIX_ENV, SOAP_TAG_BODY, SOAP_ENV_NS);
            xml.writeStartElement(XML_PREFIX_N1, SOAP_TAG_LOGIN, SOAP_PARTNER_NS);
            xml.writeNamespace(XML_PREFIX_N1, SOAP_PARTNER_NS);

            xml.writeStartElement(XML_PREFIX_N1, SOAP_TAG_USERNAME, SOAP_PARTNER_NS);
            xml.writeCharacters(username);
            xml.writeEndElement();

            xml.writeStartElement(XML_PREFIX_N1, SOAP_TAG_PASSWORD, SOAP_PARTNER_NS);
            xml.writeCharacters(combinedPassword);
            xml.writeEndElement();

            xml.writeEndElement();
            xml.writeEndElement();
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
            return writer.toString();
        } catch (XMLStreamException ex) {
            throw new IllegalStateException(MSG_SOAP_PAYLOAD_BUILD_FAILED, ex);
        }
    }

    private String normalizeBaseUrl(String url) {
        String x = url.trim();
        if (!x.startsWith(URL_PREFIX_HTTP) && !x.startsWith(URL_PREFIX_HTTPS)) {
            x = URL_PREFIX_HTTPS + x;
        }
        return x.replaceAll(REGEX_TRAILING_SLASHES, EMPTY);
    }

    private String redactOauthPayload(String payload) {
        String text = payload == null ? EMPTY : payload;
        text = text.replaceAll(REGEX_REDACT_ACCESS_TOKEN, REPLACEMENT_REDACT_ACCESS_TOKEN);
        text = text.replaceAll(REGEX_REDACT_REFRESH_TOKEN, REPLACEMENT_REDACT_REFRESH_TOKEN);
        return text.length() > 1024 ? TextIoSanitizerUtil.safeSlice(text, 0, 1024) + ELLIPSIS : text;
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



}

