package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfigResolver;
import com.sim.chatserver.email.EmailConfigSource;
import com.sim.chatserver.email.EmailFactory;
import com.sim.chatserver.email.EmailException;
import com.sim.chatserver.email.EmailMessage;
import com.sim.chatserver.email.EmailService;
import com.sim.chatserver.email.ResolvedEmailConfig;
import com.sim.chatserver.util.JsonRequestParserUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "AdminEmailServlet", urlPatterns = {"/admin/email/send"})
public class AdminEmailServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminEmailServlet.class.getName());

    // Simple email format check (good enough for admin validation)
    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req, resp)) {
            return;
        }

        if (!isValidJsonRequest(req)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid JSON payload.");
            return;
        }

        JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (payload == null || payload.isEmpty()) {
            log.fine("Invalid admin email payload");
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid JSON payload.");
            return;
        }

        try {
            // Resolve SMTP source every request (ENV -> PROPERTIES -> DB)
            EmailConfigResolver resolver = new EmailConfigResolver(resolveDbProvider());
            ResolvedEmailConfig resolved = resolver.resolve();
            if (!resolved.valid() || resolved.config() == null || resolved.source() == EmailConfigSource.NONE) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error",
                        "No valid SMTP configuration found (ENV, properties, DB).");
                return;
            }

            EmailService emailService = EmailFactory.smtp(resolved.config());

            String from = payload.getString("from", "").trim();
            String subject = payload.getString("subject", "").trim();
            String textBody = payload.getString("textBody", "");
            String htmlBody = payload.getString("htmlBody", "");
            String markdownBody = payload.getString("markdownBody", "");

            List<String> to = toList(payload.getJsonArray("to"));
            List<String> cc = toList(payload.getJsonArray("cc"));
            List<String> bcc = toList(payload.getJsonArray("bcc"));

            if (to.isEmpty()) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "At least one 'to' recipient is required.");
                return;
            }
            if (subject.isBlank()) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Subject is required.");
                return;
            }
            if (from.isBlank() && (resolved.config().defaultFrom() == null || resolved.config().defaultFrom().isBlank())) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error",
                        "From address is required (payload.from or configured defaultFrom).");
                return;
            }

            validateEmails("from", from.isBlank() ? null : List.of(from));
            validateEmails("to", to);
            validateEmails("cc", cc);
            validateEmails("bcc", bcc);

            EmailMessage.Builder b = EmailMessage.builder()
                    .subject(subject)
                    .textBody(textBody)
                    .htmlBody(htmlBody)
                    .markdownBody(markdownBody);

            if (!from.isBlank()) {
                b.from(from);
            }
            to.forEach(b::to);
            cc.forEach(b::cc);
            bcc.forEach(b::bcc);

            emailService.send(b.build());

            JsonObject ok = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "Email sent.")
                    .add("smtpSource", resolved.source().name())
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, ok);

        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid admin email request", e);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid email request.");
        } catch (EmailException | IllegalStateException e) {
            log.log(Level.SEVERE, "Failed to send admin email", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Failed to send email.");
        }
    }

    private boolean isValidJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        long len = req.getContentLengthLong();
        return len >= 0 && len <= MAX_JSON_PAYLOAD_BYTES;
    }

    private DbEmailConfigProvider resolveDbProvider() {
        return CDI.current().select(DbEmailConfigProvider.class).get();
    }

    private List<String> toList(JsonArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) {
            return out;
        }

        arr.forEach(v -> {
            String s = v.getValueType() == JsonValue.ValueType.STRING
                    ? ((JsonString) v).getString().trim()
                    : "";
            if (!s.isBlank()) {
                out.add(s);
            }
        });
        return out;
    }

    private void validateEmails(String label, List<String> emails) {
        if (emails == null) {
            return;
        }
        for (String e : emails) {
            if (e == null || e.isBlank()) {
                continue;
            }
            if (!EMAIL_RX.matcher(e.trim()).matches()) {
                throw new IllegalArgumentException("Invalid email in " + label + ": " + e);
            }
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            writeJson(resp, HttpServletResponse.SC_FORBIDDEN, "error", "Admin role required.");
            return false;
        }
        return true;
    }

    private void writeJson(HttpServletResponse resp, int status, String result, String message) {
        JsonObject payload = Json.createObjectBuilder()
                .add("status", safe(result))
                .add("message", safe(message))
                .build();
        writeJson(resp, status, payload);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        try {
            try (JsonWriter writer = Json.createWriter(resp.getWriter())) {
                writer.writeObject(payload);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write admin email response", ex);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback admin email response", sendErrorFailure);
            }
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
