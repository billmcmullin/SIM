package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfig;
import com.sim.chatserver.email.EmailConfigResolver;
import com.sim.chatserver.email.EmailFactory;
import com.sim.chatserver.email.EmailMessage;
import com.sim.chatserver.email.EmailService;
import com.sim.chatserver.email.ResolvedEmailConfig;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "AdminEmailConfigServlet", urlPatterns = {"/admin/email/config"})
public class AdminEmailConfigServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(AdminEmailConfigServlet.class.getName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 32 * 1024;

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        if (!isAdmin(req, resp)) {
            return;
        }

        DbEmailConfigProvider provider = emailConfigProvider();
        EmailConfigResolver resolver = new EmailConfigResolver(provider);
        ResolvedEmailConfig resolved = resolver.resolve();

        EmailConfig effective = resolved.config();

        JsonObjectBuilder effectiveJson = Json.createObjectBuilder()
                .add("source", safe(resolved.source() == null ? "NONE" : resolved.source().name()))
                .add("valid", resolved.valid())
                .add("message", safe(resolved.message()));

        if (effective != null) {
            effectiveJson
                    .add("host", safe(effective.host()))
                    .add("port", effective.port())
                    .add("auth", effective.auth())
                    .add("starttls", effective.startTls())
                    .add("ssl", effective.ssl())
                    .add("username", safe(effective.username()))
                    .add("passwordConfigured", effective.password() != null && !effective.password().isBlank())
                    .add("defaultFrom", safe(effective.defaultFrom()));
        }

        boolean dbConfigured = false;
        try {
            EmailConfig db = provider.load();
            dbConfigured = db != null && hasText(db.host()) && db.port() > 0;
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Failed checking DB SMTP config", e);
        }

        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("effective", effectiveJson)
                .add("dbConfigured", dbConfigured)
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, response);
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
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
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String action = payload.getString("action", "save").trim().toLowerCase();
        if ("test".equals(action)) {
            handleTest(payload, resp);
            return;
        }

        handleSave(req, payload, resp);
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
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

        String host = payload.getString("host", "").trim();
        int port = payload.getInt("port", -1);
        boolean auth = payload.getBoolean("auth", false);
        boolean starttls = payload.getBoolean("starttls", false);
        boolean ssl = payload.getBoolean("ssl", false);
        String username = payload.getString("username", "").trim();
        String password = payload.getString("password", "");
        String defaultFrom = payload.getString("defaultFrom", "").trim();

        if (!hasText(host) || port < 1 || port > 65535) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "host and valid port are required.");
            return;
        }

        if (hasText(defaultFrom) && !isValidEmail(defaultFrom)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "defaultFrom is not a valid email address.");
            return;
        }

        String finalPassword = password;
        if (!hasText(finalPassword)) {
            EmailConfig existing = null;
            try {
                existing = provider.load();
            } catch (IllegalArgumentException | IllegalStateException e) {
                log.log(Level.WARNING, "Unable to load existing SMTP config.", e);
            }
            finalPassword = existing == null ? "" : safe(existing.password());
        }

        EmailConfig config = new EmailConfig(
                host, port, auth, starttls, ssl, username, finalPassword, defaultFrom
        );

        String updatedBy = getUser(req);

        try {
            log.log(Level.INFO,
                "Saving SMTP config: host={0}, port={1}, auth={2}, starttls={3}, ssl={4}, username={5}, defaultFrom={6}, updatedBy={7}",
                new Object[]{host, port, auth, starttls, ssl, username, defaultFrom, updatedBy});

            provider.save(config, updatedBy);

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "SMTP configuration saved.")
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.SEVERE, "Failed to save SMTP configuration", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save SMTP configuration.");
        }
    }

    private void handleTest(JsonObject payload, HttpServletResponse resp) {
        try {
            DbEmailConfigProvider provider = emailConfigProvider();
            EmailConfig cfg;

            if (hasText(payload.getString("host", ""))) {
                String host = payload.getString("host", "").trim();
                int port = payload.getInt("port", -1);
                boolean auth = payload.getBoolean("auth", false);
                boolean starttls = payload.getBoolean("starttls", false);
                boolean ssl = payload.getBoolean("ssl", false);
                String username = payload.getString("username", "").trim();
                String password = payload.getString("password", "");
                String defaultFrom = payload.getString("defaultFrom", "").trim();

                if (!hasText(host) || port < 1 || port > 65535) {
                    writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "host and valid port are required for test.");
                    return;
                }

                if (hasText(defaultFrom) && !isValidEmail(defaultFrom)) {
                    writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "defaultFrom is not a valid email address.");
                    return;
                }

                // FIX: fallback to DB values when payload fields are blank (especially password)
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
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        log.log(Level.WARNING, "Unable to load existing SMTP config for test fallback", e);
                    }
                }

                cfg = new EmailConfig(host, port, auth, starttls, ssl, username, password, defaultFrom);
            } else {
                EmailConfigResolver resolver = new EmailConfigResolver(provider);
                ResolvedEmailConfig resolved = resolver.resolve();
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

            String from = payload.getString("from", "").trim();
            if (!hasText(from)) {
                from = safe(cfg.defaultFrom());
            }
            if (!hasText(from) || !isValidEmail(from)) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "A valid from/defaultFrom email is required.");
                return;
            }

            EmailService service = EmailFactory.smtp(cfg);

            EmailMessage message = EmailMessage.builder()
                    .from(from)
                    .to(testTo)
                    .subject("SIM SMTP Test Email")
                    .textBody("This is a test email from SIM Admin SMTP configuration.")
                    .build();

            service.send(message);

            JsonObject response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "SMTP test email sent.")
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.SEVERE, "SMTP test failed", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SMTP test failed.");
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    private String getUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return "UNKNOWN";
        }
        Object user = session.getAttribute("user");
        return user == null ? "UNKNOWN" : user.toString();
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
        } catch (IOException | JsonException e) {
            throw new IllegalArgumentException("Invalid JSON payload", e);
        }
    }

    private String readRequestBody(HttpServletRequest req) throws IOException {
        try (var reader = req.getReader()) {
            return ServletRequestParamUtil.readNormalizedBodyText(reader, MAX_JSON_PAYLOAD_BYTES);
        }
    }

    private DbEmailConfigProvider emailConfigProvider() {
        return CDI.current().select(DbEmailConfigProvider.class).get();
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        JsonObject obj = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", safe(message))
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
}
