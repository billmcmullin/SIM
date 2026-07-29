package com.sim.chatserver.web.admin;

import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfigResolver;
import com.sim.chatserver.email.EmailConfigSource;
import com.sim.chatserver.email.EmailAttachment;
import com.sim.chatserver.email.EmailFactory;
import com.sim.chatserver.email.EmailMessage;
import com.sim.chatserver.email.EmailService;
import com.sim.chatserver.email.ResolvedEmailConfig;
import com.sim.chatserver.model.DashboardViewModels.TermSummary;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker.WidgetAvailabilityResult;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.web.admin.AutoEmailAlertConfigStore.AutoEmailAlertConfig;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Background scheduler that evaluates configured alert conditions and sends email notifications.
 */
public class AutoEmailAlertScheduler {

    private static final Logger log = Logger.getLogger(AutoEmailAlertScheduler.class.getName());

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TICK_SECONDS = 30;
    private static final long MAX_HEALTH_ATTACHMENT_BYTES = 5L * 1024L * 1024L;

    private final AutoEmailAlertConfigStore store;
    private final DataSource dataSource;
    private final WidgetAvailabilityChecker availabilityChecker;
    private final TermsStore termsStore;
    private final DbEmailConfigProvider dbEmailConfigProvider;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "auto-email-alerts");
        t.setDaemon(true);
        return t;
    });

    private final AtomicBoolean tickRunning = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);

    private ScheduledFuture<?> future;

    public AutoEmailAlertScheduler(
            AutoEmailAlertConfigStore store,
            DataSource dataSource,
            WidgetAvailabilityChecker availabilityChecker,
            TermsStore termsStore,
            DbEmailConfigProvider dbEmailConfigProvider
    ) {
        this.store = store;
        this.dataSource = dataSource;
        this.availabilityChecker = availabilityChecker;
        this.termsStore = termsStore;
        this.dbEmailConfigProvider = dbEmailConfigProvider;
    }

    public synchronized void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        future = scheduler.scheduleWithFixedDelay(this::runTickSafely, TICK_SECONDS, TICK_SECONDS, TimeUnit.SECONDS);
        log.info("Automatic email alert scheduler started.");
    }

    public synchronized void stop() {
        if (!started.compareAndSet(true, false)) {
            return;
        }
        if (future != null) {
            future.cancel(false);
        }
        scheduler.shutdownNow();
        log.info("Automatic email alert scheduler stopped.");
    }

    TestEmailResult sendHealthTestEmail(AutoEmailAlertConfig cfg) {
        if (cfg == null) {
            return new TestEmailResult(false, "Health test email was not sent: no configuration provided.");
        }

        List<String> recipients = parseRecipients(cfg.getHealthRecipients());
        if (recipients.isEmpty()) {
            return new TestEmailResult(false, "Health test email was not sent: no valid recipients configured.");
        }

        Instant now = Instant.now();
        WidgetAvailabilityResult result = null;
        try {
            result = availabilityChecker.checkNow();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.FINE, "Health test email proceeding without live checker details.", e);
        }

        String subject = defaultIfBlank(cfg.getHealthSubject(), "SIM Test Alert: Widget Healthcheck Offline");
        String textBody = buildHealthBody(cfg, result, now, now)
                + "\n\n[TEST EMAIL] This is a manual healthcheck alert preview.";
        String htmlBody = buildHealthHtmlBody(cfg, result, now, now)
                .replace("</body></html>", "<p><em>[TEST EMAIL] This is a manual healthcheck alert preview.</em></p></body></html>");
        List<EmailAttachment> attachments = resolveHealthAttachments(cfg);

        boolean sent = sendEmail(recipients, subject, textBody, htmlBody, attachments);
        if (!sent) {
            return new TestEmailResult(false, "Health test email failed to send. Verify email configuration and check server logs.");
        }

        return new TestEmailResult(true, "Health test email sent to " + recipients.size() + " recipient(s).");
    }

    private void runTickSafely() {
        if (!tickRunning.compareAndSet(false, true)) {
            return;
        }

        try {
            runTick();
        } catch (RuntimeException e) {
            log.log(Level.WARNING, "Automatic email alert tick failed.", e);
        } finally {
            tickRunning.set(false);
        }
    }

    private void runTick() {
        AutoEmailAlertConfig cfg;
        try {
            cfg = store.load();
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to load automatic email alert config.", e);
            return;
        }

        if (cfg == null) {
            return;
        }

        Instant now = Instant.now();

        if (cfg.isHealthEnabled() && hasRecipients(cfg.getHealthRecipients()) && isDue(cfg.getHealthLastCheckedAt(), cfg.getHealthCheckIntervalSeconds(), now)) {
            evaluateHealthAlert(cfg, now);
        }

        if (cfg.isTermEnabled() && hasRecipients(cfg.getTermRecipients()) && hasText(cfg.getTermName())
                && isDue(cfg.getTermLastCheckedAt(), cfg.getTermCheckIntervalSeconds(), now)) {
            evaluateTermAlert(cfg, now);
        }
    }

    private void evaluateHealthAlert(AutoEmailAlertConfig cfg, Instant now) {
        try {
            WidgetAvailabilityResult result = availabilityChecker.checkNow();
            boolean up = result != null && result.available();

            Instant offlineSince = cfg.getHealthOfflineSince();
            Instant lastAlert = cfg.getHealthLastAlertAt();

            if (up) {
                offlineSince = null;
                lastAlert = null;
            } else {
                if (offlineSince == null) {
                    offlineSince = now;
                }

                boolean delayElapsed = secondsBetween(offlineSince, now) >= Math.max(0, cfg.getHealthOfflineDelaySeconds());
                boolean resendElapsed = lastAlert == null
                        || secondsBetween(lastAlert, now) >= Math.max(30, cfg.getHealthResendIntervalSeconds());

                if (delayElapsed && resendElapsed) {
                    List<String> recipients = parseRecipients(cfg.getHealthRecipients());
                    if (!recipients.isEmpty()) {
                        String subject = defaultIfBlank(cfg.getHealthSubject(), "SIM Alert: Widget Healthcheck Offline");
                        String textBody = buildHealthBody(cfg, result, now, offlineSince);
                        String htmlBody = buildHealthHtmlBody(cfg, result, now, offlineSince);
                        List<EmailAttachment> attachments = resolveHealthAttachments(cfg);
                        if (sendEmail(recipients, subject, textBody, htmlBody, attachments)) {
                            lastAlert = now;
                        }
                    }
                }
            }

            String status = up ? "UP" : "DOWN";
            store.updateHealthState(now, status, offlineSince, lastAlert);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to persist health alert state.", e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Health alert evaluation failed.", e);
        }
    }

    private void evaluateTermAlert(AutoEmailAlertConfig cfg, Instant now) {
        try {
            long currentCount = resolveTermCount(cfg.getTermName());
            if (currentCount < 0L) {
                store.updateTermState(now, cfg.getTermLastCount(), cfg.getTermLastAlertAt());
                return;
            }

            long previousCount = Math.max(0L, cfg.getTermLastCount());
            Instant lastChecked = cfg.getTermLastCheckedAt();
            Instant lastAlert = cfg.getTermLastAlertAt();

            if (lastChecked != null && currentCount > previousCount) {
                long delta = currentCount - previousCount;
                List<String> recipients = parseRecipients(cfg.getTermRecipients());
                if (!recipients.isEmpty()) {
                    String defaultSubject = "SIM Alert: Term increase detected - " + cfg.getTermName();
                    String subject = defaultIfBlank(cfg.getTermSubject(), defaultSubject);
                    String body = buildTermBody(cfg, now, previousCount, currentCount, delta);
                    if (sendEmail(recipients, subject, body, null, List.of())) {
                        lastAlert = now;
                    }
                }
            }

            store.updateTermState(now, currentCount, lastAlert);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to evaluate or persist term alert state.", e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Term alert evaluation failed.", e);
        }
    }

    private long resolveTermCount(String termName) throws SQLException {
        String target = normalizeTermName(termName);
        if (target == null) {
            return -1L;
        }

        List<WidgetEntry> widgets = WidgetStore.list(null);
        if (widgets == null || widgets.isEmpty()) {
            return 0L;
        }

        List<TermDefinition> terms = termsStore.listAll();
        DashboardTermService termService = new DashboardTermService(termsStore);

        try (Connection conn = dataSource.getConnection()) {
            TermSummary summary = termService.buildTermSummary(conn, widgets, terms);
            Map<String, Integer> counts = summary.getTermCounts();
            if (counts == null || counts.isEmpty()) {
                return 0L;
            }

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(target)) {
                    Integer value = entry.getValue();
                    return value == null ? 0L : Math.max(0, value);
                }
            }
        }

        return 0L;
    }

    private boolean sendEmail(
            List<String> recipients,
            String subject,
            String textBody,
            String htmlBody,
            List<EmailAttachment> attachments
    ) {
        try {
            EmailConfigResolver resolver = new EmailConfigResolver(dbEmailConfigProvider);
            ResolvedEmailConfig resolved = resolver.resolve();
            if (!resolved.valid() || resolved.source() == EmailConfigSource.NONE) {
                log.warning("Automatic alert email skipped: no valid email configuration available.");
                return false;
            }

            EmailService service = EmailFactory.forProvider(resolved);
            EmailMessage.Builder builder = EmailMessage.builder()
                    .subject(subject)
                    .textBody(textBody);
            if (hasText(htmlBody)) {
                builder.htmlBody(htmlBody);
            }
            if (attachments != null && !attachments.isEmpty()) {
                builder.attachments(attachments);
            }
            for (String recipient : recipients) {
                builder.to(recipient);
            }

            service.send(builder.build());
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Failed sending automatic alert email.", e);
            return false;
        }
    }

    private String buildHealthBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIM healthcheck alert\n\n");
        sb.append("Status: OFFLINE\n");
        sb.append("Detected at: ").append(formatInstant(now)).append("\n");
        if (offlineSince != null) {
            sb.append("Offline since: ").append(formatInstant(offlineSince)).append("\n");
        }
        if (result != null) {
            sb.append("Checker status: ").append(defaultIfBlank(result.status(), "DOWN")).append("\n");
            sb.append("Checker timestamp: ").append(defaultIfBlank(result.checkedAtIso(), formatInstant(now))).append("\n");
            sb.append("Latency ms: ").append(Math.max(0L, result.latencyMs())).append("\n");
            if (hasText(result.details())) {
                sb.append("Details: ").append(result.details()).append("\n");
            }
        }

        if (hasText(cfg.getHealthMessage())) {
            sb.append("\n").append(cfg.getHealthMessage()).append("\n");
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            sb.append("\nRunbook URL: ").append(runbookUrl).append("\n");
        }

        String runbookAttachmentPath = cfg.getHealthRunbookAttachmentPath();
        if (hasText(runbookAttachmentPath)) {
            sb.append("Runbook attachment path: ").append(runbookAttachmentPath).append("\n");
        }

        sb.append("\nThis alert will resend based on the configured resend timer until healthcheck succeeds.");
        return sb.toString();
    }

    private String buildHealthHtmlBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>SIM healthcheck alert</h2>");
        sb.append("<p><strong>Status:</strong> OFFLINE</p>");
        sb.append("<p><strong>Detected at:</strong> ").append(escapeHtml(formatInstant(now))).append("</p>");
        if (offlineSince != null) {
            sb.append("<p><strong>Offline since:</strong> ").append(escapeHtml(formatInstant(offlineSince))).append("</p>");
        }
        if (result != null) {
            sb.append("<p><strong>Checker status:</strong> ").append(escapeHtml(defaultIfBlank(result.status(), "DOWN"))).append("</p>");
            sb.append("<p><strong>Checker timestamp:</strong> ")
                    .append(escapeHtml(defaultIfBlank(result.checkedAtIso(), formatInstant(now))))
                    .append("</p>");
            sb.append("<p><strong>Latency ms:</strong> ").append(Math.max(0L, result.latencyMs())).append("</p>");
            if (hasText(result.details())) {
                sb.append("<p><strong>Details:</strong> ").append(escapeHtml(result.details())).append("</p>");
            }
        }

        if (hasText(cfg.getHealthMessage())) {
            sb.append("<p>").append(escapeHtml(cfg.getHealthMessage())).append("</p>");
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            sb.append("<p><strong>Runbook:</strong> ")
                    .append("<a href=\"").append(escapeHtml(runbookUrl)).append("\">")
                    .append(escapeHtml(runbookUrl))
                    .append("</a></p>");
        }

        if (hasText(cfg.getHealthRunbookAttachmentPath())) {
            sb.append("<p><strong>Runbook attachment path:</strong> ")
                    .append(escapeHtml(cfg.getHealthRunbookAttachmentPath()))
                    .append("</p>");
        }

        sb.append("<p>This alert will resend based on the configured resend timer until healthcheck succeeds.</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private List<EmailAttachment> resolveHealthAttachments(AutoEmailAlertConfig cfg) {
        String rawPath = cfg.getHealthRunbookAttachmentPath();
        if (!hasText(rawPath)) {
            return List.of();
        }

        try {
            Path path = Paths.get(rawPath.trim()).normalize();
            if (!Files.isRegularFile(path)) {
                log.warning(() -> "Health alert runbook attachment skipped: file not found at " + path);
                return List.of();
            }

            long fileSize = Files.size(path);
            if (fileSize <= 0L || fileSize > MAX_HEALTH_ATTACHMENT_BYTES) {
                log.warning(() -> "Health alert runbook attachment skipped: file size out of bounds (bytes=" + fileSize + ") at " + path);
                return List.of();
            }

            byte[] content = Files.readAllBytes(path);
            String fileName = path.getFileName() == null ? "runbook.bin" : path.getFileName().toString();
            String contentType = Files.probeContentType(path);
            if (!hasText(contentType)) {
                contentType = "application/octet-stream";
            }
            return List.of(new EmailAttachment(fileName, contentType, content));
        } catch (IOException | InvalidPathException e) {
            log.log(Level.WARNING, "Health alert runbook attachment skipped due to read/parse error.", e);
            return List.of();
        }
    }

    private String normalizeRunbookUrl(String raw) {
        if (!hasText(raw)) {
            return null;
        }
        try {
            URI uri = URI.create(raw.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                return null;
            }
            String normalizedScheme = scheme.toLowerCase();
            if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
                return null;
            }
            return uri.toString();
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, "Ignoring invalid runbook URL.", e);
            return null;
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String buildTermBody(AutoEmailAlertConfig cfg, Instant now, long previousCount, long currentCount, long delta) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIM term activity alert\n\n");
        sb.append("Term: ").append(defaultIfBlank(cfg.getTermName(), "(not set)")).append("\n");
        sb.append("Detected at: ").append(formatInstant(now)).append("\n");
        sb.append("Previous count: ").append(previousCount).append("\n");
        sb.append("Current count: ").append(currentCount).append("\n");
        sb.append("Increase: ").append(delta).append("\n");

        if (hasText(cfg.getTermMessage())) {
            sb.append("\n").append(cfg.getTermMessage()).append("\n");
        }

        return sb.toString();
    }

    private boolean hasRecipients(String csv) {
        return !parseRecipients(csv).isEmpty();
    }

    private List<String> parseRecipients(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        String[] raw = csv.split("[,;\\n\\r]+");
        Set<String> deduped = new LinkedHashSet<>();
        for (String token : raw) {
            if (token == null) {
                continue;
            }
            String candidate = token.trim();
            if (candidate.isEmpty()) {
                continue;
            }
            if (EMAIL_RX.matcher(candidate).matches()) {
                deduped.add(candidate);
            }
        }

        return new ArrayList<>(deduped);
    }

    private boolean isDue(Instant lastCheckedAt, int intervalSeconds, Instant now) {
        if (lastCheckedAt == null) {
            return true;
        }
        long elapsed = secondsBetween(lastCheckedAt, now);
        return elapsed >= Math.max(30, intervalSeconds);
    }

    private long secondsBetween(Instant from, Instant to) {
        if (from == null || to == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, Duration.between(from, to).getSeconds());
    }

    private String normalizeTermName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String formatInstant(Instant value) {
        if (value == null) {
            return "";
        }
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC).format(value);
    }

    public record TestEmailResult(boolean sent, String message) {
    }
}
