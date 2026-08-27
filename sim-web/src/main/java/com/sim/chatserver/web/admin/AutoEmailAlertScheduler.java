package com.sim.chatserver.web.admin;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.email.EmailConfigResolver;
import com.sim.chatserver.email.EmailConfigSource;
import com.sim.chatserver.email.EmailAttachment;
import com.sim.chatserver.email.EmailException;
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

import jakarta.enterprise.inject.spi.CDI;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import software.amazon.awssdk.core.exception.SdkException;

/**
 * Background scheduler that evaluates configured alert conditions and sends email notifications.
 */
public class AutoEmailAlertScheduler {

    private static final Logger log = Logger.getLogger(AutoEmailAlertScheduler.class.getName());

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TICK_SECONDS = 30;
    private static final long MAX_HEALTH_ATTACHMENT_BYTES = 5L * 1024L * 1024L;

    @FunctionalInterface
    interface AwsConfigLoader {
        ServerConfig load() throws SQLException;
    }

    @FunctionalInterface
    interface Ec2RestartInvoker {
        void reboot(String region, String accessKeyId, String secretAccessKey, String instanceId);
    }

    private final AutoEmailAlertConfigStore store;
    private final DataSource dataSource;
    private final WidgetAvailabilityChecker availabilityChecker;
    private final TermsStore termsStore;
    private final DbEmailConfigProvider dbEmailConfigProvider;
    private final AwsConfigLoader awsConfigLoader;
    private final Ec2RestartInvoker ec2RestartInvoker;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "auto-email-alerts");
        t.setDaemon(true);
        return t;
    });

    private final AtomicBoolean tickRunning = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Object lifecycleLock = new Object();

    private ScheduledFuture<?> future;

    AutoEmailAlertScheduler(
            AutoEmailAlertConfigStore store,
            DataSource dataSource,
            WidgetAvailabilityChecker availabilityChecker,
            TermsStore termsStore,
            DbEmailConfigProvider dbEmailConfigProvider
    ) {
        this(
                store,
                dataSource,
                availabilityChecker,
                termsStore,
                dbEmailConfigProvider,
                AutoEmailAlertScheduler::loadAwsConfigFromStore,
                AutoEmailAlertScheduler::invokeEc2Restart
        );
    }

    AutoEmailAlertScheduler(
            AutoEmailAlertConfigStore store,
            DataSource dataSource,
            WidgetAvailabilityChecker availabilityChecker,
            TermsStore termsStore,
            DbEmailConfigProvider dbEmailConfigProvider,
            AwsConfigLoader awsConfigLoader,
            Ec2RestartInvoker ec2RestartInvoker
    ) {
        this.store = store;
        this.dataSource = dataSource;
        this.availabilityChecker = availabilityChecker;
        this.termsStore = termsStore;
        this.dbEmailConfigProvider = dbEmailConfigProvider;
        this.awsConfigLoader = awsConfigLoader == null
                ? AutoEmailAlertScheduler::loadAwsConfigFromStore
                : awsConfigLoader;
        this.ec2RestartInvoker = ec2RestartInvoker == null
                ? AutoEmailAlertScheduler::invokeEc2Restart
                : ec2RestartInvoker;
    }

    private static ServerConfig loadAwsConfigFromStore() throws SQLException {
        return EncryptedDbConfigStore.load();
    }

    private static void invokeEc2Restart(String region, String accessKeyId, String secretAccessKey, String instanceId) {
        new AwsEc2RestartServlet().rebootEc2Instance(region, accessKeyId, secretAccessKey, instanceId);
    }

    final void start() {
        int tickSeconds = TICK_SECONDS;
        synchronized (lifecycleLock) {
            if (!started.compareAndSet(false, true)) {
                return;
            }
            future = scheduler.scheduleWithFixedDelay(this::runTickSafely, tickSeconds, tickSeconds, TimeUnit.SECONDS);
        }
        log.info("Automatic email alert scheduler started.");
    }

    final void stop() {
        ScheduledFuture<?> localFuture;
        synchronized (lifecycleLock) {
            if (!started.compareAndSet(true, false)) {
                return;
            }
            localFuture = future;
        }
        if (localFuture != null) {
            localFuture.cancel(false);
        }
        scheduler.shutdownNow();
        log.info("Automatic email alert scheduler stopped.");
    }

    final TestEmailResult sendHealthTestEmail(AutoEmailAlertConfig cfg) {
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
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            log.log(Level.FINE, "Health test email proceeding without live checker details.", e);
        }

        String subject = defaultIfBlank(cfg.getHealthSubject(), "SIM Test Alert: Widget Healthcheck Offline");
        String textBody = buildHealthBody(cfg, result, now, now)
                + "\n\n[TEST EMAIL] This is a manual healthcheck alert preview.";
        String htmlBody = buildHealthHtmlBody(
            cfg,
            result,
            now,
            now,
            "[TEST EMAIL] This is a manual healthcheck alert preview."
        );
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
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
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

        if (cfg.isHealthEnabled() && isDue(cfg.getHealthLastCheckedAt(), cfg.getHealthCheckIntervalSeconds(), now)) {
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

            String previousStatus = cfg.getHealthLastStatus();
            boolean wasDown = previousStatus != null && "DOWN".equalsIgnoreCase(previousStatus.trim());
            Instant offlineSince = cfg.getHealthOfflineSince();
            Instant lastAlert = cfg.getHealthLastAlertAt();
            Instant restartAttemptAt = cfg.getHealthLastRestartAttemptAt();
            boolean offlineAlertWasSent = lastAlert != null;

            if (up) {
                if (wasDown && offlineAlertWasSent) {
                    List<String> recipients = parseRecipients(cfg.getHealthRecipients());
                    if (!recipients.isEmpty()) {
                        String subject = buildHealthRecoverySubject(cfg);
                        String textBody = buildHealthRecoveryBody(cfg, result, now, offlineSince);
                        String htmlBody = buildHealthRecoveryHtmlBody(cfg, result, now, offlineSince);
                        sendEmail(recipients, subject, textBody, htmlBody, List.of());
                    }
                }
                offlineSince = null;
                lastAlert = null;
                restartAttemptAt = null;
            } else {
                if (offlineSince == null) {
                    offlineSince = now;
                }

                int delaySeconds = Math.max(0, cfg.getHealthOfflineDelaySeconds());

                if (restartAttemptAt == null) {
                    boolean restartDelayElapsed = secondsBetween(offlineSince, now) >= delaySeconds;
                    if (restartDelayElapsed) {
                        boolean restartSubmitted = attemptAutomaticAwsRestart(cfg, now, offlineSince, result);
                        restartAttemptAt = now;
                        if (restartSubmitted) {
                            log.info("Healthcheck offline automation submitted EC2 restart request.");
                        } else {
                            log.warning("Healthcheck offline automation attempted EC2 restart but it was not submitted.");
                        }
                    }
                } else {
                    boolean postRestartDelayElapsed = secondsBetween(restartAttemptAt, now) >= delaySeconds;
                    boolean resendElapsed = lastAlert == null
                            || secondsBetween(lastAlert, now) >= Math.max(30, cfg.getHealthResendIntervalSeconds());

                    if (postRestartDelayElapsed && resendElapsed) {
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
            }

            String status = up ? "UP" : "DOWN";
            store.updateHealthState(now, status, offlineSince, lastAlert, restartAttemptAt);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to persist health alert state.", e);
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            log.log(Level.WARNING, "Health alert evaluation failed.", e);
        }
    }

    private final boolean rebootEc2InstanceForHealthcheck(String region,
            String accessKeyId,
            String secretAccessKey,
            String instanceId) {
        ec2RestartInvoker.reboot(region, accessKeyId, secretAccessKey, instanceId);
        return true;
    }

    private final ServerConfig loadAwsConfigForHealthcheck() throws SQLException {
        return awsConfigLoader.load();
    }

    private boolean attemptAutomaticAwsRestart(
            AutoEmailAlertConfig cfg,
            Instant now,
            Instant offlineSince,
            WidgetAvailabilityResult result
    ) {
        ServerConfig awsCfg;
        try {
            awsCfg = loadAwsConfigForHealthcheck();
        } catch (SQLException ex) {
            log.log(Level.WARNING,
                    "Unable to load AWS config for automatic healthcheck restart: {0}",
                    defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
            return false;
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException ex) {
            log.log(Level.WARNING, "Unable to load AWS config for automatic healthcheck restart.", ex);
            return false;
        }

        if (awsCfg == null) {
            log.warning("Automatic healthcheck restart skipped: no AWS config available.");
            return false;
        }

        String region = trimToNull(awsCfg.getAwsRegion());
        String instanceId = trimToNull(awsCfg.getAwsInstanceId());
        String accessKeyId = trimToNull(awsCfg.getAwsAccessKeyId());
        String secretAccessKey = trimToNull(awsCfg.getAwsSecretAccessKey());

        if (!hasText(region) || !hasText(instanceId) || !hasText(accessKeyId) || !hasText(secretAccessKey)) {
            log.warning("Automatic healthcheck restart skipped: AWS region/instance/credentials are incomplete.");
            return false;
        }

        try {
            rebootEc2InstanceForHealthcheck(region, accessKeyId, secretAccessKey, instanceId);
            log.log(
                    Level.INFO,
                    "Automatic healthcheck restart submitted. region={0}, instanceId={1}, offlineSince={2}, attemptedAt={3}, checkerStatus={4}",
                    new Object[]{
                        region,
                        instanceId,
                        formatInstant(offlineSince),
                        formatInstant(now),
                        result == null ? "UNKNOWN" : defaultIfBlank(result.status(), "UNKNOWN")
                    }
            );
            return true;
        } catch (SdkException | IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.WARNING, "Automatic healthcheck restart failed.", ex);
            return false;
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
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
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
        DashboardTermService termService = CDI.current().select(DashboardTermService.class).get();

        try (Connection conn = dataSource.getConnection()) {
            TermSummary summary = termService.buildTermSummaryForDashboard(conn, widgets, terms);
            Map<String, Integer> counts = summary.getTermCounts();
            if (counts == null || counts.isEmpty()) {
                return 0L;
            }

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(target)) {
                    Integer value = entry.getValue();
                    int count = 0;
                    if (value != null) {
                        count = value.intValue();
                    }
                    return Math.max(0L, count);
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
            EmailConfigResolver resolver = EmailConfigResolver.create(dbEmailConfigProvider);
            ResolvedEmailConfig resolved = resolver.resolve();
            if (!resolved.valid() || resolved.source() == EmailConfigSource.NONE) {
                log.warning("Automatic alert email skipped: no valid email configuration available.");
                return false;
            }

            EmailService service = EmailFactory.forProvider(resolved);
            String htmlBodyValue = hasText(htmlBody) ? htmlBody : null;
            List<EmailAttachment> safeAttachments = attachments == null ? List.of() : attachments;
            EmailMessage message = EmailMessage.create(
                    null,
                    recipients,
                    List.of(),
                    List.of(),
                    subject,
                    textBody,
                    htmlBodyValue,
                    null,
                    safeAttachments);

            service.send(message);
            return true;
        } catch (EmailException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Failed sending automatic alert email.", e);
            return false;
        }
    }

    private String buildHealthBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIM healthcheck alert\n\n");
        sb.append("Status: OFFLINE\n");
        sb.append("Detected at: ").append(formatInstant(now)).append('\n');
        if (offlineSince != null) {
            sb.append("Offline since: ").append(formatInstant(offlineSince)).append('\n');
        }
        if (result != null) {
            sb.append("Checker status: ").append(defaultIfBlank(result.status(), "DOWN")).append('\n');
            sb.append("Checker timestamp: ").append(defaultIfBlank(result.checkedAtIso(), formatInstant(now))).append('\n');
            sb.append("Latency ms: ").append(Math.max(0L, result.latencyMs())).append('\n');
            if (hasText(result.details())) {
                sb.append("Details: ").append(result.details()).append('\n');
            }
        }

        if (cfg.getHealthLastRestartAttemptAt() != null) {
            sb.append("Automatic EC2 restart attempted at: ")
                    .append(formatInstant(cfg.getHealthLastRestartAttemptAt()))
                    .append('\n');
        }

        if (hasText(cfg.getHealthMessage())) {
            sb.append('\n').append(cfg.getHealthMessage()).append('\n');
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            sb.append("\nRunbook URL: ").append(runbookUrl).append('\n');
        }

        String runbookAttachmentPath = cfg.getHealthRunbookAttachmentPath();
        if (hasText(runbookAttachmentPath)) {
            sb.append("Runbook attachment path: ").append(runbookAttachmentPath).append('\n');
        }

        sb.append("\nThis alert will resend based on the configured resend timer until healthcheck succeeds.");
        return sb.toString();
    }

    private String buildHealthRecoverySubject(AutoEmailAlertConfig cfg) {
        String configured = defaultIfBlank(cfg.getHealthSubject(), "SIM Alert: Widget Healthcheck Offline");
        if (configured.toLowerCase(Locale.ROOT).contains("offline")) {
            return configured.replaceAll("(?i)offline", "Back Online");
        }
        return configured + " - Back Online";
    }

    private String buildHealthRecoveryBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIM healthcheck recovery notification\n\n");
        sb.append("Status: ONLINE\n");
        sb.append("Recovered at: ").append(formatInstant(now)).append('\n');
        if (offlineSince != null) {
            sb.append("Offline since: ").append(formatInstant(offlineSince)).append('\n');
            sb.append("Estimated outage duration: ").append(formatDuration(now, offlineSince)).append('\n');
        }
        if (result != null) {
            sb.append("Checker status: ").append(defaultIfBlank(result.status(), "UP")).append('\n');
            sb.append("Checker timestamp: ").append(defaultIfBlank(result.checkedAtIso(), formatInstant(now))).append('\n');
            sb.append("Latency ms: ").append(Math.max(0L, result.latencyMs())).append('\n');
            if (hasText(result.details())) {
                sb.append("Details: ").append(result.details()).append('\n');
            }
        }

        if (hasText(cfg.getHealthMessage())) {
            sb.append('\n').append(cfg.getHealthMessage()).append('\n');
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            sb.append("\nRunbook URL: ").append(runbookUrl).append('\n');
        }

        return sb.toString();
    }

    private String buildHealthHtmlBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        return buildHealthHtmlBody(cfg, result, now, offlineSince, null);
    }

    private String buildHealthHtmlBody(
            AutoEmailAlertConfig cfg,
            WidgetAvailabilityResult result,
            Instant now,
            Instant offlineSince,
            String testNotice
    ) {
        HtmlEmailBuilder builder = HtmlEmailBuilder.create("SIM healthcheck alert");
        builder.addLabeledValue("Status", "OFFLINE");
        builder.addLabeledValue("Detected at", formatInstant(now));

        if (offlineSince != null) {
            builder.addLabeledValue("Offline since", formatInstant(offlineSince));
        }
        if (result != null) {
            builder.addLabeledValue("Checker status", defaultIfBlank(result.status(), "DOWN"));
            builder.addLabeledValue("Checker timestamp", defaultIfBlank(result.checkedAtIso(), formatInstant(now)));
            builder.addLabeledValue("Latency ms", String.valueOf(Math.max(0L, result.latencyMs())));
            if (hasText(result.details())) {
                builder.addLabeledValue("Details", result.details());
            }
        }

        if (cfg.getHealthLastRestartAttemptAt() != null) {
            builder.addLabeledValue("Automatic EC2 restart attempted at", formatInstant(cfg.getHealthLastRestartAttemptAt()));
        }

        if (hasText(cfg.getHealthMessage())) {
            builder.addParagraph(cfg.getHealthMessage());
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            builder.addLabeledLink("Runbook", runbookUrl);
        }

        if (hasText(cfg.getHealthRunbookAttachmentPath())) {
            builder.addLabeledValue("Runbook attachment path", cfg.getHealthRunbookAttachmentPath());
        }

        if (hasText(testNotice)) {
            builder.addEmphasizedParagraph(testNotice);
        }

        builder.addParagraph("This alert will resend based on the configured resend timer until healthcheck succeeds.");
        return builder.build();
    }

    private String buildHealthRecoveryHtmlBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        HtmlEmailBuilder builder = HtmlEmailBuilder.create("SIM healthcheck recovery notification");
        builder.addLabeledValue("Status", "ONLINE");
        builder.addLabeledValue("Recovered at", formatInstant(now));

        if (offlineSince != null) {
            builder.addLabeledValue("Offline since", formatInstant(offlineSince));
            builder.addLabeledValue("Estimated outage duration", formatDuration(now, offlineSince));
        }
        if (result != null) {
            builder.addLabeledValue("Checker status", defaultIfBlank(result.status(), "UP"));
            builder.addLabeledValue("Checker timestamp", defaultIfBlank(result.checkedAtIso(), formatInstant(now)));
            builder.addLabeledValue("Latency ms", String.valueOf(Math.max(0L, result.latencyMs())));
            if (hasText(result.details())) {
                builder.addLabeledValue("Details", result.details());
            }
        }

        if (hasText(cfg.getHealthMessage())) {
            builder.addParagraph(cfg.getHealthMessage());
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            builder.addLabeledLink("Runbook", runbookUrl);
        }

        return builder.build();
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

    private String buildTermBody(AutoEmailAlertConfig cfg, Instant now, long previousCount, long currentCount, long delta) {
        StringBuilder sb = new StringBuilder();
        sb.append("SIM term activity alert\n\n");
        sb.append("Term: ").append(defaultIfBlank(cfg.getTermName(), "(not set)")).append('\n');
        sb.append("Detected at: ").append(formatInstant(now)).append('\n');
        sb.append("Previous count: ").append(previousCount).append('\n');
        sb.append("Current count: ").append(currentCount).append('\n');
        sb.append("Increase: ").append(delta).append('\n');

        if (hasText(cfg.getTermMessage())) {
            sb.append('\n').append(cfg.getTermMessage()).append('\n');
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

    private String formatDuration(Instant end, Instant start) {
        long seconds = Math.max(0L, secondsBetween(start, end));
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return new StringBuilder(24)
                .append(twoDigit(hours)).append("h ")
                .append(twoDigit(minutes)).append("m ")
                .append(twoDigit(secs)).append('s')
                .toString();
    }

    private String twoDigit(long value) {
        long safe = Math.max(0L, value);
        if (safe < 10L) {
            return new StringBuilder(3).append('0').append(safe).toString();
        }
        return Long.toString(safe);
    }

    private String normalizeTermName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private static final class HtmlEmailBuilder {

        private final StringWriter out;
        private final XMLStreamWriter writer;

        private HtmlEmailBuilder(String title) throws XMLStreamException {
            this.out = new StringWriter();
            this.writer = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            writer.writeStartElement("html");
            writer.writeStartElement("body");
            writer.writeStartElement("h2");
            writer.writeCharacters(title == null ? "" : title);
            writer.writeEndElement();
        }

        private static HtmlEmailBuilder create(String title) {
            try {
                return new HtmlEmailBuilder(title);
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable to initialize HTML email builder", e);
            }
        }

        private void addParagraph(String text) {
            try {
                writer.writeStartElement("p");
                writer.writeCharacters(text == null ? "" : text);
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable to append paragraph", e);
            }
        }

        private void addEmphasizedParagraph(String text) {
            try {
                writer.writeStartElement("p");
                writer.writeStartElement("em");
                writer.writeCharacters(text == null ? "" : text);
                writer.writeEndElement();
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable to append emphasized paragraph", e);
            }
        }

        private void addLabeledValue(String label, String value) {
            try {
                writer.writeStartElement("p");
                writer.writeStartElement("strong");
                writer.writeCharacters((label == null ? "" : label) + ':');
                writer.writeEndElement();
                writer.writeCharacters(' ' + (value == null ? "" : value));
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable to append labeled value", e);
            }
        }

        private void addLabeledLink(String label, String url) {
            try {
                writer.writeStartElement("p");
                writer.writeStartElement("strong");
                writer.writeCharacters((label == null ? "" : label) + ':');
                writer.writeEndElement();
                writer.writeCharacters(String.valueOf(' '));
                writer.writeStartElement("a");
                writer.writeAttribute("href", url == null ? "" : url);
                writer.writeCharacters(url == null ? "" : url);
                writer.writeEndElement();
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable to append labeled link", e);
            }
        }

        private String build() {
            try {
                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndDocument();
                writer.flush();
                writer.close();
                return out.toString();
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable to finalize HTML email body", e);
            }
        }
    }
}
