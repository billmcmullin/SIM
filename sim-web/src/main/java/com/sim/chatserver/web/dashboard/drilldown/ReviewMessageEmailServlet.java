package com.sim.chatserver.web.dashboard.drilldown;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfigResolver;
import com.sim.chatserver.email.EmailConfigSource;
import com.sim.chatserver.email.EmailException;
import com.sim.chatserver.email.EmailFactory;
import com.sim.chatserver.email.EmailMessage;
import com.sim.chatserver.email.EmailService;
import com.sim.chatserver.email.ResolvedEmailConfig;
import com.sim.chatserver.service.translation.DefaultTranslationService;
import com.sim.chatserver.service.translation.TranslationService;
import com.sim.chatserver.service.translation.TranslationService.TranslationResult;
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ReviewMessageEmailServlet", urlPatterns = {"/dashboard/widgets/drilldown/review/email"})
public class ReviewMessageEmailServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ReviewMessageEmailServlet.class.getName());
    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final int MAX_JSON_PAYLOAD_BYTES = 96 * 1024;
    private static final int MAX_EMAIL_LENGTH = 320;
    private static final int MAX_SUBJECT_LENGTH = 180;
    private static final int MAX_LABEL_LENGTH = 140;
    private static final int MAX_TEXT_LENGTH = 12_000;
    private static final int MAX_CUSTOM_MESSAGE_LENGTH = 4_000;

    private static final ZoneId DISPLAY_ZONE = ZoneOffset.UTC;
    private static final DateTimeFormatter DISPLAY_TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (!setRequestEncoding(req, resp)) {
            return;
        }

        try {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
                return;
            }

            if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
                return;
            }

            JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
            if (payload == null || payload.isEmpty()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
                return;
            }

            String recipient = normalizeSimple(JsonRequestParserUtil.getString(payload, "to", ""), MAX_EMAIL_LENGTH);
            if (recipient == null || !EMAIL_RX.matcher(recipient).matches()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid recipient email is required.");
                return;
            }

            String prompt = normalizeText(JsonRequestParserUtil.getString(payload, "prompt", ""), MAX_TEXT_LENGTH);
            String responseText = normalizeText(JsonRequestParserUtil.getString(payload, "response", ""), MAX_TEXT_LENGTH);
            if (prompt == null && responseText == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Either prompt or response is required.");
                return;
            }

            String chatId = normalizeSimple(JsonRequestParserUtil.getString(payload, "chatId", ""), 120);
            String createdAtRaw = normalizeSimple(JsonRequestParserUtil.getString(payload, "createdAt", ""), 200);
            String sessionId = normalizeSimple(JsonRequestParserUtil.getString(payload, "sessionId", ""), 200);
            String subjectLabel = normalizeText(JsonRequestParserUtil.getString(payload, "subjectLabel", ""), MAX_LABEL_LENGTH);
            String subjectType = normalizeSimple(JsonRequestParserUtil.getString(payload, "subjectType", ""), 40);
            String customMessage = normalizeText(JsonRequestParserUtil.getString(payload, "customMessage", ""), MAX_CUSTOM_MESSAGE_LENGTH);

            String subject = normalizeText(JsonRequestParserUtil.getString(payload, "subject", ""), MAX_SUBJECT_LENGTH);
            if (subject == null) {
                subject = buildDefaultSubject(subjectType, subjectLabel, chatId);
            }

            boolean translateToEnglish = parseBoolean(payload, "translateToEnglish", true);
            TranslationService translationService = resolveTranslationService();

            PreparedText preparedPrompt = prepareText(prompt, translateToEnglish, translationService);
            PreparedText preparedResponse = prepareText(responseText, translateToEnglish, translationService);

            String sharedBy = String.valueOf(session.getAttribute("user"));
            String textBody = buildTextBody(
                    sharedBy,
                    chatId,
                    createdAtRaw,
                    sessionId,
                    subjectType,
                    subjectLabel,
                    customMessage,
                    translateToEnglish,
                    preparedPrompt,
                    preparedResponse);

            ResolvedEmailConfig resolved = resolveEmailConfig();
            if (resolved == null || !resolved.valid() || resolved.source() == EmailConfigSource.NONE) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "No valid email configuration found. Ask an administrator to configure email settings.");
                return;
            }

            EmailService emailService = resolveEmailService(resolved);
            EmailMessage email = EmailMessage.create(
                    null,
                    List.of(recipient),
                    List.of(),
                    List.of(),
                    subject,
                    textBody,
                    "",
                    "",
                    List.of());
            emailService.send(email);

            JsonObjectBuilder success = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("message", "Email sent.")
                    .add("translatedPrompt", preparedPrompt.wasTranslated)
                    .add("translatedResponse", preparedResponse.wasTranslated)
                    .add("promptSourceLang", preparedPrompt.sourceLang)
                    .add("responseSourceLang", preparedResponse.sourceLang)
                    .add("emailConfigSource", resolved.source().name());

            writeJson(resp, HttpServletResponse.SC_OK, success.build());

        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid review email request", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid review email request.");
        } catch (EmailException | IllegalStateException | UnsupportedOperationException ex) {
            log.log(Level.SEVERE, "Failed to send review email", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to send email.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST required.");
    }

    DbEmailConfigProvider resolveDbProvider() {
        return CDI.current().select(DbEmailConfigProvider.class).get();
    }

    ResolvedEmailConfig resolveEmailConfig() {
        return EmailConfigResolver.resolveEffectiveConfig(resolveDbProvider());
    }

    EmailService resolveEmailService(ResolvedEmailConfig resolved) {
        return EmailFactory.createForProvider(resolved);
    }

    TranslationService resolveTranslationService() {
        return new DefaultTranslationService();
    }

    private PreparedText prepareText(String input, boolean translateToEnglish, TranslationService translationService) {
        if (input == null) {
            return PreparedText.empty();
        }

        if (!translateToEnglish || translationService == null) {
            return PreparedText.untranslated(input);
        }

        try {
            TranslationResult result = translationService.detectAndTranslate(input, "en");
            if (result == null || !result.isSuccess()) {
                return PreparedText.failed(input, safe(result == null ? "" : result.getMessage()));
            }

            String translated = normalizeText(result.getTranslatedText(), MAX_TEXT_LENGTH);
            if (translated == null) {
                translated = input;
            }

            String source = normalizeLang(result.getSourceLang());
            boolean translatedFlag = source != null && !source.startsWith("en") && !translated.isBlank();
            return new PreparedText(input, translated, source == null ? "" : source, translatedFlag, "");
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException ex) {
            log.log(Level.FINE, "Translation failed; falling back to original text", ex);
            return PreparedText.failed(input, "Translation unavailable");
        }
    }

    private String buildTextBody(
            String sharedBy,
            String chatId,
            String createdAtRaw,
            String sessionId,
            String subjectType,
            String subjectLabel,
            String customMessage,
            boolean translateToEnglish,
            PreparedText prompt,
            PreparedText response) {

        StringBuilder sb = new StringBuilder(2048);
        sb.append("SIM Chat Review Share").append('\n').append('\n');
        sb.append("Shared By: ").append(safe(sharedBy)).append('\n');
        if (chatId != null && !chatId.isBlank()) {
            sb.append("Chat ID: ").append(chatId).append('\n');
        }
        if (createdAtRaw != null && !createdAtRaw.isBlank()) {
            String chatTs = toHumanReadableTimestamp(createdAtRaw);
            if (!chatTs.isBlank()) {
                sb.append("Chat Timestamp: ").append(chatTs).append('\n');
            }
        }
        if (sessionId != null && !sessionId.isBlank()) {
            sb.append("Session ID: ").append(sessionId).append('\n');
        }
        if (subjectLabel != null && !subjectLabel.isBlank()) {
            sb.append("Reviewed ")
                    .append(subjectType == null || subjectType.isBlank() ? "Context" : subjectType)
                    .append(": ")
                    .append(subjectLabel)
                    .append('\n');
        }
        sb.append("Shared At: ").append(formatDisplayInstant(Instant.now())).append('\n');
        sb.append("Translation To English: ").append(translateToEnglish ? "Enabled" : "Disabled").append('\n');

        if (customMessage != null && !customMessage.isBlank()) {
            sb.append('\n').append("Custom Note from Reviewer").append('\n');
            sb.append("-------------------------").append('\n');
            sb.append(customMessage).append('\n');
        }

        appendSection(sb, "Prompt", prompt, translateToEnglish);
        appendSection(sb, "Response", response, translateToEnglish);

        return sb.toString();
    }

    private void appendSection(StringBuilder sb, String label, PreparedText text, boolean translateToEnglish) {
        if (text == null || text.original == null || text.original.isBlank()) {
            return;
        }

        sb.append('\n').append(label).append(" (Original)").append('\n');
        sb.append("----------------").append('\n');
        sb.append(text.original).append('\n');

        if (!translateToEnglish) {
            return;
        }

        if (text.wasTranslated) {
            sb.append('\n').append(label).append(" (English Translation from ")
                    .append(text.sourceLang)
                    .append(')')
                    .append('\n');
            sb.append("----------------").append('\n');
            sb.append(text.english).append('\n');
            return;
        }

        if (text.translationIssue != null && !text.translationIssue.isBlank()) {
            sb.append("\nNote: ").append(label).append(" translation could not be completed. Original text was included.")
                    .append('\n');
            return;
        }

        if (text.sourceLang != null && text.sourceLang.startsWith("en")) {
            sb.append("\nNote: ").append(label).append(" detected as English; no translation needed.")
                    .append('\n');
        }
    }

    private String buildDefaultSubject(String subjectType, String subjectLabel, String chatId) {
        String type = subjectType == null || subjectType.isBlank() ? "Chat" : subjectType;
        StringBuilder sb = new StringBuilder("SIM Review Share: ").append(type);
        if (subjectLabel != null && !subjectLabel.isBlank()) {
            sb.append(" - ").append(subjectLabel);
        }
        if (chatId != null && !chatId.isBlank()) {
            sb.append(" (Chat ").append(chatId).append(')');
        }
        String raw = sb.toString();
        return raw.length() > MAX_SUBJECT_LENGTH ? raw.substring(0, MAX_SUBJECT_LENGTH) : raw;
    }

    private boolean parseBoolean(JsonObject payload, String key, boolean defaultValue) {
        if (payload == null || key == null || !payload.containsKey(key)) {
            return defaultValue;
        }

        JsonValue value = payload.get(key);
        if (value == null) {
            return defaultValue;
        }

        return switch (value.getValueType()) {
            case TRUE -> true;
            case FALSE -> false;
            case STRING -> Boolean.parseBoolean(payload.getString(key, String.valueOf(defaultValue)).trim());
            default -> defaultValue;
        };
    }

    private String normalizeSimple(String text, int maxLen) {
        return ServletRequestParamUtil.normalizeValue(text, maxLen, true, true);
    }

    private String normalizeText(String text, int maxLen) {
        return ServletRequestParamUtil.normalizeBodyText(text, maxLen, true);
    }

    private String normalizeLang(String language) {
        if (language == null || language.isBlank()) {
            return "";
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        return normalized.length() > 24 ? normalized.substring(0, 24) : normalized;
    }

    private String toHumanReadableTimestamp(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) {
            return "";
        }

        String raw = rawTimestamp.trim();
        try {
            return formatDisplayInstant(Instant.parse(raw));
        } catch (DateTimeParseException ex) {
            logTimestampParseFailure("Instant", raw, ex);
            // try next parser
        }

        try {
            return formatDisplayInstant(OffsetDateTime.parse(raw).toInstant());
        } catch (DateTimeParseException ex) {
            logTimestampParseFailure("OffsetDateTime", raw, ex);
            // try next parser
        }

        try {
            return formatDisplayInstant(LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException ex) {
            logTimestampParseFailure("LocalDateTime", raw, ex);
            // try fallback with spaced timestamp form
        }

        try {
            String isoLike = raw.replace(' ', 'T');
            return formatDisplayInstant(LocalDateTime.parse(isoLike).toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException ex) {
            logTimestampParseFailure("LocalDateTime(isoLike)", raw, ex);
            return raw;
        }
    }

    private String formatDisplayInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return DISPLAY_TS_FORMAT.format(instant.atZone(DISPLAY_ZONE));
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\r', ' ').replace('\n', ' ');
    }

    private void logTimestampParseFailure(String parser, String value, DateTimeParseException ex) {
        log.log(Level.FINER, "Timestamp parse failed using " + parser + " for value: " + safe(value), ex);
    }

    private boolean setRequestEncoding(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return true;
        } catch (UnsupportedEncodingException | IllegalStateException ex) {
            log.log(Level.WARNING, "Failed to set request encoding", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request encoding.");
            return false;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException | IllegalStateException ex) {
            log.log(Level.WARNING, "Failed to write JSON response", ex);
            try {
                resp.sendError(status);
            } catch (IOException ioEx) {
                log.log(Level.WARNING, "Failed to send fallback error response", ioEx);
            }
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        writeJson(resp,
                status,
                Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", safe(message))
                        .build());
    }

    private static final class PreparedText {

        final String original;
        final String english;
        final String sourceLang;
        final boolean wasTranslated;
        final String translationIssue;

        private PreparedText(String original, String english, String sourceLang, boolean wasTranslated, String translationIssue) {
            this.original = original == null ? "" : original;
            this.english = english == null ? "" : english;
            this.sourceLang = sourceLang == null ? "" : sourceLang;
            this.wasTranslated = wasTranslated;
            this.translationIssue = translationIssue == null ? "" : translationIssue;
        }

        private static PreparedText empty() {
            return new PreparedText("", "", "", false, "");
        }

        private static PreparedText untranslated(String original) {
            return new PreparedText(original, original, "", false, "");
        }

        private static PreparedText failed(String original, String issue) {
            return new PreparedText(original, original, "", false, issue);
        }
    }
}
