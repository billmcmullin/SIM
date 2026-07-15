package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfigResolver;
import com.sim.chatserver.email.EmailConfigSource;
import com.sim.chatserver.email.EmailFactory;
import com.sim.chatserver.email.EmailMessage;
import com.sim.chatserver.email.EmailService;
import com.sim.chatserver.email.ResolvedEmailConfig;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
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

    @Inject
    DbEmailConfigProvider dbProvider;

    // Simple email format check (good enough for admin validation)
    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        if (!isValidJsonRequest(req)) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid JSON payload.");
            return;
        }

        JsonObject payload;
        try (JsonReader reader = Json.createReader(req.getReader())) {
            payload = reader.readObject();
        } catch (JsonException | ClassCastException e) {
            log.log(Level.FINE, "Invalid admin email payload", e);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid JSON payload.");
            return;
        }

        try {
            // Resolve SMTP source every request (ENV -> PROPERTIES -> DB)
            EmailConfigResolver resolver = new EmailConfigResolver(dbProvider);
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

            resp.setContentType("application/json");
            resp.getWriter().write(Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "Email sent.")
                    .add("smtpSource", resolved.source().name())
                    .build()
                    .toString());

        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Invalid admin email request", e);
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Invalid email request.");
        } catch (RuntimeException e) {
            log.log(Level.SEVERE, "Failed to send admin email", e);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Failed to send email.");
        }
    }

    private boolean isValidJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            return false;
        }
        long len = req.getContentLengthLong();
        return len >= 0 && len <= MAX_JSON_PAYLOAD_BYTES;
    }

    private List<String> toList(JsonArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) {
            return out;
        }

        arr.forEach(v -> {
            String s = v.toString().replace("\"", "").trim();
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

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

    private void writeJson(HttpServletResponse resp, int status, String result, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write(Json.createObjectBuilder()
                .add("status", safe(result))
                .add("message", safe(message))
                .build()
                .toString());
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
