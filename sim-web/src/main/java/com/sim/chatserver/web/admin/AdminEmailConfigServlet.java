package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfig;
import com.sim.chatserver.email.EmailConfigResolver;
import com.sim.chatserver.email.EmailFactory;
import com.sim.chatserver.email.EmailMessage;
import com.sim.chatserver.email.EmailService;
import com.sim.chatserver.email.ResolvedEmailConfig;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "AdminEmailConfigServlet", urlPatterns = {"/admin/email/config"})
public class AdminEmailConfigServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(AdminEmailConfigServlet.class.getCanonicalName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 32 * 1024;
    private static final String JSON_KEY_HOST = "host";
    private static final String JSON_KEY_PORT = "port";
    private static final String SESSION_USER_KEY = com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER;
    private static final String SESSION_ROLE_KEY = com.sim.chatserver.util.CommonLiteralConstants.ROLE;
    private static final String ADMIN_ROLE = com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN;
    private static final String MSG_INVALID_JSON_PAYLOAD = CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD;
    private static final String LOG_UNHANDLED_DO_POST = CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST;
    private static final String MSG_AUTH_REQUIRED = CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED;
    private static final String MSG_ADMIN_REQUIRED = CommonLiteralConstants.MSG_ADMIN_ROLE_REQUIRED;
    private static final String DESERIALIZATION_NOT_ALLOWED = com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED;

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req, resp)) {
            return;
        }

        DbEmailConfigProvider provider = emailConfigProvider();
        ResolvedEmailConfig resolved = EmailConfigResolver.resolveEffectiveConfig(provider);

        EmailConfig effective = resolved.config();

        JsonObjectBuilder effectiveJson = Json.createObjectBuilder()
                .add("source", safe(resolved.source() == null ? "NONE" : resolved.source().name()))
                .add("valid", resolved.valid())
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, safe(resolved.message()));

        if (effective != null) {
            effectiveJson
                    .add(JSON_KEY_HOST, safe(effective.host()))
                    .add(JSON_KEY_PORT, effective.port())
                    .add("auth", effective.auth())
                    .add("starttls", effective.startTls())
                    .add("ssl", effective.ssl())
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_USERNAME, safe(effective.username()))
                    .add("passwordConfigured", effective.password() != null && !effective.password().isBlank())
                    .add("defaultFrom", safe(effective.defaultFrom()));
        }

        boolean dbConfigured = false;
        try {
            EmailConfig db = provider.load();
            dbConfigured = db != null && hasText(db.host()) && db.port() > 0;
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Failed checking DB SMTP config", e);
        }

        JsonObject response = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add("effective", effectiveJson)
                .add("dbConfigured", dbConfigured)
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, response);
    
        } catch (IllegalStateException | IllegalArgumentException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req, resp)) {
            return;
        }

        JsonObject payload;
        try {
            payload = readValidatedJsonPayload(req);
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Rejected SMTP config payload: {0}", safe(e.getMessage()));
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, MSG_INVALID_JSON_PAYLOAD);
            return;
        }

        String action = payload.getString("action", "save").trim().toLowerCase();
        if ("test".equals(action)) {
            handleTest(payload, resp);
            return;
        }

        handleSave(req, payload, resp);
    
        } catch (IllegalStateException | IllegalArgumentException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, LOG_UNHANDLED_DO_POST, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
                }
            }
        }
    }

    private void handleSave(HttpServletRequest req, JsonObject payload, HttpServletResponse resp) {
        DbEmailConfigProvider provider;
        try {
            provider = emailConfigProvider();
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "DB SMTP provider is not available", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB SMTP provider is not available.");
            return;
        }

        SmtpRequestConfig requestConfig = validateSmtpRequestConfig(payload, resp, "host and valid port are required.");
        if (requestConfig == null) {
            return;
        }

        String finalPassword = requestConfig.password();
        if (!hasText(finalPassword)) {
            EmailConfig existing = null;
            try {
                existing = provider.load();
            } catch (IllegalStateException | IllegalArgumentException e) {
                log.log(Level.WARNING, "Unable to load existing SMTP config.", e);
            }
            finalPassword = existing == null ? "" : safe(existing.password());
        }

        EmailConfig config = new EmailConfig(
                requestConfig.host(),
                requestConfig.port(),
                requestConfig.auth(),
                requestConfig.starttls(),
                requestConfig.ssl(),
                requestConfig.username(),
                finalPassword,
                requestConfig.defaultFrom()
        );

        String updatedBy = getUser(req);

        try {
            log.log(Level.INFO,
                "Saving SMTP config: host={0}, port={1}, auth={2}, starttls={3}, ssl={4}, username={5}, defaultFrom={6}, updatedBy={7}",
                new Object[]{
                        requestConfig.host(),
                        requestConfig.port(),
                        requestConfig.auth(),
                        requestConfig.starttls(),
                        requestConfig.ssl(),
                        requestConfig.username(),
                        requestConfig.defaultFrom(),
                        updatedBy
                });

            provider.save(config, updatedBy);

            JsonObject response = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "SMTP configuration saved.")
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Failed to save SMTP configuration", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save SMTP configuration.");
        }
    }

    private void handleTest(JsonObject payload, HttpServletResponse resp) {
        try {
            DbEmailConfigProvider provider = emailConfigProvider();
            EmailConfig cfg;

            if (hasText(payload.getString("host", ""))) {
                SmtpRequestConfig requestConfig = validateSmtpRequestConfig(
                        payload,
                        resp,
                        "host and valid port are required for test."
                );
                if (requestConfig == null) {
                    return;
                }

                cfg = buildTestConfig(provider, requestConfig);
            } else {
                ResolvedEmailConfig resolved = EmailConfigResolver.resolveEffectiveConfig(provider);
                if (!resolved.valid() || resolved.config() == null) {
                    writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "No valid effective SMTP config to test.");
                    return;
                }
                cfg = resolved.config();
            }

            String testTo = payload.getString("testTo", "").trim();
            if (!hasText(testTo) || !isValidEmail(testTo)) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "A valid testTo email is required.");
                return;
            }

            String from = payload.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_FROM, "").trim();
            if (!hasText(from)) {
                from = safe(cfg.defaultFrom());
            }
            if (!hasText(from) || !isValidEmail(from)) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "A valid from/defaultFrom email is required.");
                return;
            }

            EmailService service = EmailFactory.smtp(cfg);

                EmailMessage message = EmailMessage.create(
                    from,
                    List.of(testTo),
                    List.of(),
                    List.of(),
                    "SIM SMTP Test Email",
                    "This is a test email from SIM Admin SMTP configuration.",
                    null,
                    null,
                    List.of());

            service.send(message);

            JsonObject response = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "SMTP test email sent.")
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "SMTP test failed", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SMTP test failed.");
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_KEY) == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, MSG_AUTH_REQUIRED);
            return false;
        }
        String role = session.getAttribute(SESSION_ROLE_KEY) == null ? "" : session.getAttribute(SESSION_ROLE_KEY).toString();
        if (!ADMIN_ROLE.equalsIgnoreCase(role)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, MSG_ADMIN_REQUIRED);
            return false;
        }
        return true;
    }

    private String getUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return CommonLiteralConstants.VALUE_UNKNOWN_UPPER;
        }
        Object user = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER);
        return user == null ? CommonLiteralConstants.VALUE_UNKNOWN_UPPER : user.toString();
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean isValidEmail(String s) {
        return s != null && EMAIL_RX.matcher(s.trim()).matches();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private SmtpRequestConfig validateSmtpRequestConfig(
            JsonObject payload,
            HttpServletResponse resp,
            String hostAndPortMessage
    ) {
        SmtpRequestConfig requestConfig = readSmtpRequestConfig(payload);
        if (!hasText(requestConfig.host()) || requestConfig.port() < 1 || requestConfig.port() > 65535) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, hostAndPortMessage);
            return null;
        }

        if (hasText(requestConfig.defaultFrom()) && !isValidEmail(requestConfig.defaultFrom())) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "defaultFrom is not a valid email address.");
            return null;
        }

        return requestConfig;
    }

    private EmailConfig buildTestConfig(DbEmailConfigProvider provider, SmtpRequestConfig requestConfig) {
        String username = requestConfig.username();
        String password = requestConfig.password();
        String defaultFrom = requestConfig.defaultFrom();

        // Keep test-mode behavior: fallback to DB values for missing fields.
        if (!hasText(password) || !hasText(username) || !hasText(defaultFrom)) {
            try {
                EmailConfig existing = provider.load();
                if (existing != null) {
                    if (!hasText(password)) {
                        password = safe(existing.password());
                    }
                    if (!hasText(username)) {
                        username = safe(existing.username());
                    }
                    if (!hasText(defaultFrom)) {
                        defaultFrom = safe(existing.defaultFrom());
                    }
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                log.log(Level.WARNING, "Unable to load existing SMTP config for test fallback", e);
            }
        }

        return new EmailConfig(
                requestConfig.host(),
                requestConfig.port(),
                requestConfig.auth(),
                requestConfig.starttls(),
                requestConfig.ssl(),
                username,
                password,
                defaultFrom
        );
    }

    private SmtpRequestConfig readSmtpRequestConfig(JsonObject payload) {
        return new SmtpRequestConfig(
                payload.getString(JSON_KEY_HOST, "").trim(),
                payload.getInt(JSON_KEY_PORT, -1),
                payload.getBoolean("auth", false),
                payload.getBoolean("starttls", false),
                payload.getBoolean("ssl", false),
                payload.getString(com.sim.chatserver.util.CommonLiteralConstants.KEY_USERNAME, "").trim(),
                payload.getString(com.sim.chatserver.util.CommonLiteralConstants.KEY_PASSWORD, ""),
                payload.getString("defaultFrom", "").trim()
        );
    }

    private record SmtpRequestConfig(
            String host,
            int port,
            boolean auth,
            boolean starttls,
            boolean ssl,
            String username,
            String password,
            String defaultFrom
    ) {
    }

    private JsonObject readValidatedJsonPayload(HttpServletRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Missing request");
        }
        long len = req.getContentLengthLong();
        if (len < 0 || len > MAX_JSON_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("Invalid content length");
        }

        try (JsonReader reader = Json.createReader(new StringReader(readRequestBody(req)))) {
            return reader.readObject();
        } catch (JsonException e) {
            throw new IllegalArgumentException(MSG_INVALID_JSON_PAYLOAD, e);
        }
    }

    private String readRequestBody(HttpServletRequest req) {
        try {
            var reader = req.getReader();
            try {
                return ServletRequestParamUtil.readNormalizedBodyText(reader, MAX_JSON_PAYLOAD_BYTES);
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read request body", e);
        }
    }

    private DbEmailConfigProvider emailConfigProvider() {
        return CDI.current().select(DbEmailConfigProvider.class).get();
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        JsonObject obj = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, safe(message))
                .build();
        writeJson(resp, status, obj);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject obj) {
        JsonObject payload = obj == null ? Json.createObjectBuilder().build() : obj;
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to write admin email config response", e);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback admin email config error", sendErrorFailure);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidObjectException(DESERIALIZATION_NOT_ALLOWED);
    }

}
