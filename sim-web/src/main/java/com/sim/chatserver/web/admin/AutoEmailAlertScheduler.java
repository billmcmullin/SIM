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
import com.sim.chatserver.model.TermSummary;
import com.sim.chatserver.service.dashboard.DashboardTermService;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.service.widget.WidgetAvailabilityResult;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.web.admin.AutoEmailAlertConfig;
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

    private static final Logger log = Logger.getLogger(AutoEmailAlertScheduler.class.getCanonicalName());
    private static final String LS = System.lineSeparator();
    static final String EMPTY = "";
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String SCHEDULER_THREAD_NAME = "auto-email-alerts";
    private static final String MSG_SCHEDULER_STARTED = "Automatic email alert scheduler started.";
    private static final String MSG_SCHEDULER_STOPPED = "Automatic email alert scheduler stopped.";
    private static final String MSG_HEALTH_TEST_NO_CHECKER_DETAILS = "Health test email proceeding without live checker details.";
    private static final String SUBJECT_TEST_HEALTHCHECK_OFFLINE = "SIM Test Alert: Widget Healthcheck Offline";
    private static final String TEXT_TEST_EMAIL_PREVIEW = "[TEST EMAIL] This is a manual healthcheck alert preview.";
    private static final String PREFIX_HEALTH_TEST_EMAIL_SENT_TO = "Health test email sent to ";
    private static final String MSG_TICK_FAILED = "Automatic email alert tick failed.";
    private static final String MSG_LOAD_CONFIG_FAILED = "Unable to load automatic email alert config.";
    private static final String MSG_RESTART_SUBMITTED = "Healthcheck offline automation submitted EC2 restart request.";
    private static final String MSG_RESTART_ATTEMPTED_NOT_SUBMITTED = "Healthcheck offline automation attempted EC2 restart but it was not submitted.";
    private static final String MSG_PERSIST_HEALTH_STATE_FAILED = "Failed to persist health alert state.";
    private static final String MSG_HEALTH_EVALUATION_FAILED = "Health alert evaluation failed.";
    private static final String MSG_LOAD_AWS_CONFIG_WITH_REASON = "Unable to load AWS config for automatic healthcheck restart: {0}";
    private static final String MSG_LOAD_AWS_CONFIG = "Unable to load AWS config for automatic healthcheck restart.";
    private static final String MSG_RESTART_SKIPPED_NO_AWS = "Automatic healthcheck restart skipped: no AWS config available.";
    private static final String MSG_RESTART_SKIPPED_INCOMPLETE_AWS = "Automatic healthcheck restart skipped: AWS region/instance/credentials are incomplete.";
    private static final String MSG_RESTART_SKIPPED_NOT_SUBMITTED = "Automatic healthcheck restart skipped: reboot request was not submitted.";
    private static final String MSG_RESTART_SUBMITTED_DETAILS = "Automatic healthcheck restart submitted. region={0}, instanceId={1}, offlineSince={2}, attemptedAt={3}, checkerStatus={4}";
    private static final String MSG_RESTART_FAILED = "Automatic healthcheck restart failed.";
    private static final String PREFIX_TERM_ALERT_SUBJECT = "SIM Alert: Term increase detected - ";
    private static final String MSG_TERM_STATE_FAILED = "Failed to evaluate or persist term alert state.";
    private static final String MSG_TERM_EVALUATION_FAILED = "Term alert evaluation failed.";
    private static final String MSG_AUTO_EMAIL_SKIPPED_NO_CONFIG = "Automatic alert email skipped: no valid email configuration available.";
    private static final String MSG_AUTO_EMAIL_SEND_FAILED = "Failed sending automatic alert email.";
    private static final String PREFIX_STATUS = "Status: ";
    private static final String TERM_OFFLINE = "offline";
    private static final String TEXT_RECIPIENTS_SUFFIX = " recipient(s).";
    private static final String TEXT_CLOSE_PAREN_AT = ") at ";
    private static final String VALUE_NOT_SET = "(not set)";
    private static final String REGEX_OFFLINE_CASE_INSENSITIVE = "(?i)offline";
    private static final String TEXT_DASH_SPACE = " - ";
    private static final String LABEL_VALUE_SEPARATOR = ": ";
    private static final String MSG_RUNBOOK_FILE_NOT_FOUND_PREFIX = "Health alert runbook attachment skipped: file not found at ";
    private static final String MSG_RUNBOOK_FILE_SIZE_INVALID_PREFIX = "Health alert runbook attachment skipped: file size out of bounds (bytes=";
    private static final String MSG_RUNBOOK_ATTACHMENT_PARSE_ERROR = "Health alert runbook attachment skipped due to read/parse error.";
    private static final String FILE_RUNBOOK_BIN = "runbook.bin";
    private static final String URL_SCHEME_HTTP = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTP;
    private static final String URL_SCHEME_HTTPS = com.sim.chatserver.util.CommonLiteralConstants.SCHEME_HTTPS;
    private static final String MSG_INVALID_RUNBOOK_URL = "Ignoring invalid runbook URL.";
    private static final String TERM_ACTIVITY_TITLE = "SIM term activity alert";
    private static final String PREFIX_TERM = "Term: ";
    private static final String PREFIX_PREVIOUS_COUNT = "Previous count: ";
    private static final String PREFIX_CURRENT_COUNT = "Current count: ";
    private static final String PREFIX_INCREASE = "Increase: ";
    private static final String REGEX_RECIPIENT_SPLIT = "(?:[,;]|\\R)+";
    private static final String DURATION_HOURS_SUFFIX = "h ";
    private static final String DURATION_MINUTES_SUFFIX = "m ";
    static final String XML_VERSION = "1.0";
    static final String TAG_HTML = "html";
    static final String TAG_BODY = "body";
    static final String TAG_H2 = "h2";
    static final String TAG_P = "p";
    static final String TAG_EM = "em";
    static final String TAG_STRONG = "strong";
    static final String TAG_A = "a";
    static final String ATTR_HREF = "href";
    static final String MSG_INIT_HTML_BUILDER_FAILED = "Unable to initialize HTML email builder";
    static final String MSG_APPEND_PARAGRAPH_FAILED = "Unable to append paragraph";
    static final String MSG_APPEND_EMPHASIZED_PARAGRAPH_FAILED = "Unable to append emphasized paragraph";
    static final String MSG_APPEND_LABELED_VALUE_FAILED = "Unable to append labeled value";
    static final String MSG_APPEND_LABELED_LINK_FAILED = "Unable to append labeled link";
    static final String MSG_FINALIZE_HTML_BODY_FAILED = "Unable to finalize HTML email body";

    private static final Pattern EMAIL_RX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TICK_SECONDS = 30;
    private static final long MAX_HEALTH_ATTACHMENT_BYTES = 5L * 1024L * 1024L;
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = com.sim.chatserver.util.CommonLiteralConstants.STATUS_DOWN;
    private static final String STATUS_OFFLINE = "OFFLINE";
    private static final String STATUS_ONLINE = "ONLINE";
    private static final String VALUE_UNKNOWN = CommonLiteralConstants.VALUE_UNKNOWN_UPPER;
    private static final String TITLE_HEALTH_ALERT = "SIM healthcheck alert";
    private static final String TITLE_HEALTH_RECOVERY = "SIM healthcheck recovery notification";
    private static final String SUBJECT_HEALTH_OFFLINE_DEFAULT = "SIM Alert: Widget Healthcheck Offline";
    private static final String TEXT_ALERT_RESEND_TIMER = "This alert will resend based on the configured resend timer until healthcheck succeeds.";
    private static final String TEXT_BACK_ONLINE = "Back Online";
    private static final String PREFIX_DETECTED_AT = "Detected at: ";
    private static final String PREFIX_OFFLINE_SINCE = "Offline since: ";
    private static final String PREFIX_RUNBOOK_URL = "Runbook URL: ";
    private static final String PREFIX_RECOVERED_AT = "Recovered at: ";
    private static final String PREFIX_RESTART_ATTEMPTED_AT = "Automatic EC2 restart attempted at: ";
    private static final String PREFIX_RUNBOOK_ATTACHMENT_PATH = "Runbook attachment path: ";
    private static final String PREFIX_ESTIMATED_OUTAGE_DURATION = "Estimated outage duration: ";
    private static final String LABEL_STATUS = "Status";
    private static final String LABEL_OFFLINE_SINCE = "Offline since";
    private static final String LABEL_DETECTED_AT = "Detected at";
    private static final String LABEL_RECOVERED_AT = "Recovered at";
    private static final String LABEL_RESTART_ATTEMPTED_AT = "Automatic EC2 restart attempted at";
    private static final String LABEL_RUNBOOK_ATTACHMENT_PATH = "Runbook attachment path";
    private static final String LABEL_ESTIMATED_OUTAGE_DURATION = "Estimated outage duration";
    private static final String LABEL_CHECKER_STATUS = "Checker status";
    private static final String LABEL_CHECKER_TIMESTAMP = "Checker timestamp";
    private static final String LABEL_LATENCY_MS = "Latency ms";
    private static final String LABEL_DETAILS = "Details";
    private static final String LABEL_RUNBOOK = "Runbook";
    private static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final TestEmailResult TEST_EMAIL_RESULT_NO_CONFIG =
            new TestEmailResult(false, "Health test email was not sent: no configuration provided.");
    private static final TestEmailResult TEST_EMAIL_RESULT_NO_RECIPIENTS =
            new TestEmailResult(false, "Health test email was not sent: no valid recipients configured.");
    private static final TestEmailResult TEST_EMAIL_RESULT_SEND_FAILED =
            new TestEmailResult(false, "Health test email failed to send. Verify email configuration and check server logs.");

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

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, SCHEDULER_THREAD_NAME);
        t.setDaemon(true);
        return t;
    });

    private final AtomicBoolean tickRunning = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Object lifecycleLock = new Object();

    private ScheduledFuture<?> future;

    private AutoEmailAlertScheduler(
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

    static AutoEmailAlertScheduler create(
            AutoEmailAlertConfigStore store,
            DataSource dataSource,
            WidgetAvailabilityChecker availabilityChecker,
            TermsStore termsStore,
            DbEmailConfigProvider dbEmailConfigProvider
    ) {
        return new AutoEmailAlertScheduler(store, dataSource, availabilityChecker, termsStore, dbEmailConfigProvider);
    }

    private AutoEmailAlertScheduler(
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
        log.info(MSG_SCHEDULER_STARTED);
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
        log.info(MSG_SCHEDULER_STOPPED);
    }

    final TestEmailResult sendHealthTestEmail(AutoEmailAlertConfig cfg) {
        if (cfg == null) {
            return TEST_EMAIL_RESULT_NO_CONFIG;
        }

        List<String> recipients = parseRecipients(cfg.getHealthRecipients());
        if (recipients.isEmpty()) {
            return TEST_EMAIL_RESULT_NO_RECIPIENTS;
        }

        Instant now = Instant.now();
        WidgetAvailabilityResult result = null;
        try {
            result = availabilityChecker.checkNow();
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            log.log(Level.FINE, MSG_HEALTH_TEST_NO_CHECKER_DETAILS, e);
        }

        String subject = defaultIfBlank(cfg.getHealthSubject(), SUBJECT_TEST_HEALTHCHECK_OFFLINE);
        String textBody = buildHealthBody(cfg, result, now, now)
            + LS + LS + TEXT_TEST_EMAIL_PREVIEW;
        String htmlBody = buildHealthHtmlBody(
            cfg,
            result,
            now,
            now,
            TEXT_TEST_EMAIL_PREVIEW
        );
        List<EmailAttachment> attachments = resolveHealthAttachments(cfg);

        boolean sent = sendEmail(recipients, subject, textBody, htmlBody, attachments);
        if (!sent) {
            return TEST_EMAIL_RESULT_SEND_FAILED;
        }

        return new TestEmailResult(true, PREFIX_HEALTH_TEST_EMAIL_SENT_TO + recipients.size() + TEXT_RECIPIENTS_SUFFIX);
    }

    private void runTickSafely() {
        if (!tickRunning.compareAndSet(false, true)) {
            return;
        }

        try (var taskLock = com.sim.chatserver.service.WorkflowRuntime.tryTaskLock(() -> dataSource, "automatic-alerts")) {
            if (taskLock == null) {
                return;
            }
            runTick();
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            log.log(Level.WARNING, MSG_TICK_FAILED, e);
        } finally {
            tickRunning.set(false);
        }
    }

    private void runTick() {
        AutoEmailAlertConfig cfg;
        try {
            cfg = store.load();
        } catch (SQLException e) {
            log.log(Level.WARNING, MSG_LOAD_CONFIG_FAILED, e);
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
            boolean wasDown = previousStatus != null && STATUS_DOWN.equalsIgnoreCase(previousStatus.trim());
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
                            log.info(MSG_RESTART_SUBMITTED);
                        } else {
                            log.warning(MSG_RESTART_ATTEMPTED_NOT_SUBMITTED);
                        }
                    }
                } else {
                    boolean postRestartDelayElapsed = secondsBetween(restartAttemptAt, now) >= delaySeconds;
                    boolean resendElapsed = lastAlert == null
                            || secondsBetween(lastAlert, now) >= Math.max(30, cfg.getHealthResendIntervalSeconds());

                    if (postRestartDelayElapsed && resendElapsed) {
                        List<String> recipients = parseRecipients(cfg.getHealthRecipients());
                        if (!recipients.isEmpty()) {
                            String subject = defaultIfBlank(cfg.getHealthSubject(), SUBJECT_HEALTH_OFFLINE_DEFAULT);
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

            String status = up ? STATUS_UP : STATUS_DOWN;
            store.updateHealthState(now, status, offlineSince, lastAlert, restartAttemptAt);
        } catch (SQLException e) {
            log.log(Level.WARNING, MSG_PERSIST_HEALTH_STATE_FAILED, e);
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            log.log(Level.WARNING, MSG_HEALTH_EVALUATION_FAILED, e);
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
                    MSG_LOAD_AWS_CONFIG_WITH_REASON,
                    defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
            return false;
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException ex) {
            log.log(Level.WARNING, MSG_LOAD_AWS_CONFIG, ex);
            return false;
        }

        if (awsCfg == null) {
            log.warning(MSG_RESTART_SKIPPED_NO_AWS);
            return false;
        }

        String region = trimToNull(awsCfg.getAwsRegion());
        String instanceId = trimToNull(awsCfg.getAwsInstanceId());
        String accessKeyId = trimToNull(awsCfg.getAwsAccessKeyId());
        String secretAccessKey = trimToNull(awsCfg.getAwsSecretAccessKey());

        if (!hasText(region) || !hasText(instanceId) || !hasText(accessKeyId) || !hasText(secretAccessKey)) {
            log.warning(MSG_RESTART_SKIPPED_INCOMPLETE_AWS);
            return false;
        }

        try {
            boolean restartSubmitted = rebootEc2InstanceForHealthcheck(region, accessKeyId, secretAccessKey, instanceId);
            if (!restartSubmitted) {
                log.warning(MSG_RESTART_SKIPPED_NOT_SUBMITTED);
                return false;
            }
            log.log(
                    Level.INFO,
                    MSG_RESTART_SUBMITTED_DETAILS,
                    new Object[]{
                        region,
                        instanceId,
                        formatInstant(offlineSince),
                        formatInstant(now),
                        result == null ? VALUE_UNKNOWN : defaultIfBlank(result.status(), VALUE_UNKNOWN)
                    }
            );
            return true;
        } catch (SdkException | IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.WARNING, MSG_RESTART_FAILED, ex);
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
                    String defaultSubject = PREFIX_TERM_ALERT_SUBJECT + cfg.getTermName();
                    String subject = defaultIfBlank(cfg.getTermSubject(), defaultSubject);
                    String body = buildTermBody(cfg, now, previousCount, currentCount, delta);
                    if (sendEmail(recipients, subject, body, null, List.of())) {
                        lastAlert = now;
                    }
                }
            }

            store.updateTermState(now, currentCount, lastAlert);
        } catch (SQLException e) {
            log.log(Level.WARNING, MSG_TERM_STATE_FAILED, e);
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            log.log(Level.WARNING, MSG_TERM_EVALUATION_FAILED, e);
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
            TermSummary summary = termService.computeTermSummaryForDashboard(conn, widgets, terms);
            Map<String, Integer> counts = summary.getTermCounts();
            if (counts == null || counts.isEmpty()) {
                return 0L;
            }

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(target)) {
                    Object value = entry.getValue();
                    int count = 0;
                    if (value != null) {
                        count = Integer.parseInt(value.toString());
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
            ResolvedEmailConfig resolved = EmailConfigResolver.resolveEffectiveConfig(dbEmailConfigProvider);
            if (!resolved.valid() || resolved.source() == EmailConfigSource.NONE) {
                log.warning(MSG_AUTO_EMAIL_SKIPPED_NO_CONFIG);
                return false;
            }

            EmailService service = EmailFactory.createForProvider(resolved);
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
            log.log(Level.WARNING, MSG_AUTO_EMAIL_SEND_FAILED, e);
            return false;
        }
    }

    private String buildHealthBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        StringBuilder sb = new StringBuilder();
        sb.append(TITLE_HEALTH_ALERT).append(LS).append(LS);
        sb.append(PREFIX_STATUS).append(STATUS_OFFLINE).append(LS);
        sb.append(PREFIX_DETECTED_AT).append(formatInstant(now)).append(System.lineSeparator());
        if (offlineSince != null) {
            sb.append(PREFIX_OFFLINE_SINCE).append(formatInstant(offlineSince)).append(System.lineSeparator());
        }
        appendCheckerDetails(sb, result, now, STATUS_DOWN);

        if (cfg.getHealthLastRestartAttemptAt() != null) {
                sb.append(PREFIX_RESTART_ATTEMPTED_AT)
                    .append(formatInstant(cfg.getHealthLastRestartAttemptAt()))
                    .append(System.lineSeparator());
        }

        if (hasText(cfg.getHealthMessage())) {
            sb.append(System.lineSeparator()).append(cfg.getHealthMessage()).append(System.lineSeparator());
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            sb.append(LS).append(PREFIX_RUNBOOK_URL).append(runbookUrl).append(System.lineSeparator());
        }

        String runbookAttachmentPath = cfg.getHealthRunbookAttachmentPath();
        if (hasText(runbookAttachmentPath)) {
            sb.append(PREFIX_RUNBOOK_ATTACHMENT_PATH).append(runbookAttachmentPath).append(System.lineSeparator());
        }

        sb.append(LS).append(TEXT_ALERT_RESEND_TIMER);
        return sb.toString();
    }

    private String buildHealthRecoverySubject(AutoEmailAlertConfig cfg) {
        String configured = defaultIfBlank(cfg.getHealthSubject(), SUBJECT_HEALTH_OFFLINE_DEFAULT);
        if (configured.toLowerCase(Locale.ROOT).contains(TERM_OFFLINE)) {
            return configured.replaceAll(REGEX_OFFLINE_CASE_INSENSITIVE, TEXT_BACK_ONLINE);
        }
        return configured + TEXT_DASH_SPACE + TEXT_BACK_ONLINE;
    }

    private String buildHealthRecoveryBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        StringBuilder sb = new StringBuilder();
        sb.append(TITLE_HEALTH_RECOVERY).append(LS).append(LS);
        sb.append(PREFIX_STATUS).append(STATUS_ONLINE).append(LS);
        sb.append(PREFIX_RECOVERED_AT).append(formatInstant(now)).append(System.lineSeparator());
        if (offlineSince != null) {
            sb.append(PREFIX_OFFLINE_SINCE).append(formatInstant(offlineSince)).append(System.lineSeparator());
            sb.append(PREFIX_ESTIMATED_OUTAGE_DURATION).append(formatDuration(now, offlineSince)).append(System.lineSeparator());
        }
        appendCheckerDetails(sb, result, now, STATUS_UP);

        if (hasText(cfg.getHealthMessage())) {
            sb.append(System.lineSeparator()).append(cfg.getHealthMessage()).append(System.lineSeparator());
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            sb.append(LS).append(PREFIX_RUNBOOK_URL).append(runbookUrl).append(System.lineSeparator());
        }

        return sb.toString();
    }

        private void appendCheckerDetails(
            StringBuilder target,
            WidgetAvailabilityResult result,
            Instant now,
            String fallbackStatus
        ) {
        if (target == null || result == null) {
            return;
        }

        target.append(LABEL_CHECKER_STATUS).append(LABEL_VALUE_SEPARATOR)
            .append(defaultIfBlank(result.status(), fallbackStatus))
            .append(System.lineSeparator());
        target.append(LABEL_CHECKER_TIMESTAMP).append(LABEL_VALUE_SEPARATOR)
            .append(defaultIfBlank(result.checkedAtIso(), formatInstant(now)))
            .append(System.lineSeparator());
        target.append(LABEL_LATENCY_MS).append(LABEL_VALUE_SEPARATOR)
            .append(Math.max(0L, result.latencyMs()))
            .append(System.lineSeparator());
        if (hasText(result.details())) {
            target.append(LABEL_DETAILS).append(LABEL_VALUE_SEPARATOR)
                .append(result.details())
                .append(System.lineSeparator());
        }
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
        HtmlEmailBuilder builder = HtmlEmailBuilder.create(TITLE_HEALTH_ALERT);
        builder.addLabeledValue(LABEL_STATUS, STATUS_OFFLINE);
        builder.addLabeledValue(LABEL_DETECTED_AT, formatInstant(now));

        if (offlineSince != null) {
            builder.addLabeledValue(LABEL_OFFLINE_SINCE, formatInstant(offlineSince));
        }
        if (result != null) {
            builder.addLabeledValue(LABEL_CHECKER_STATUS, defaultIfBlank(result.status(), STATUS_DOWN));
            builder.addLabeledValue(LABEL_CHECKER_TIMESTAMP, defaultIfBlank(result.checkedAtIso(), formatInstant(now)));
            builder.addLabeledValue(LABEL_LATENCY_MS, String.valueOf(Math.max(0L, result.latencyMs())));
            if (hasText(result.details())) {
                builder.addLabeledValue(LABEL_DETAILS, result.details());
            }
        }

        if (cfg.getHealthLastRestartAttemptAt() != null) {
            builder.addLabeledValue(LABEL_RESTART_ATTEMPTED_AT, formatInstant(cfg.getHealthLastRestartAttemptAt()));
        }

        if (hasText(cfg.getHealthMessage())) {
            builder.addParagraph(cfg.getHealthMessage());
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            builder.addLabeledLink(LABEL_RUNBOOK, runbookUrl);
        }

        if (hasText(cfg.getHealthRunbookAttachmentPath())) {
            builder.addLabeledValue(LABEL_RUNBOOK_ATTACHMENT_PATH, cfg.getHealthRunbookAttachmentPath());
        }

        if (hasText(testNotice)) {
            builder.addEmphasizedParagraph(testNotice);
        }

        builder.addParagraph(TEXT_ALERT_RESEND_TIMER);
        return builder.build();
    }

    private String buildHealthRecoveryHtmlBody(AutoEmailAlertConfig cfg, WidgetAvailabilityResult result, Instant now, Instant offlineSince) {
        HtmlEmailBuilder builder = HtmlEmailBuilder.create(TITLE_HEALTH_RECOVERY);
        builder.addLabeledValue(LABEL_STATUS, STATUS_ONLINE);
        builder.addLabeledValue(LABEL_RECOVERED_AT, formatInstant(now));

        if (offlineSince != null) {
            builder.addLabeledValue(LABEL_OFFLINE_SINCE, formatInstant(offlineSince));
            builder.addLabeledValue(LABEL_ESTIMATED_OUTAGE_DURATION, formatDuration(now, offlineSince));
        }
        if (result != null) {
            builder.addLabeledValue(LABEL_CHECKER_STATUS, defaultIfBlank(result.status(), STATUS_UP));
            builder.addLabeledValue(LABEL_CHECKER_TIMESTAMP, defaultIfBlank(result.checkedAtIso(), formatInstant(now)));
            builder.addLabeledValue(LABEL_LATENCY_MS, String.valueOf(Math.max(0L, result.latencyMs())));
            if (hasText(result.details())) {
                builder.addLabeledValue(LABEL_DETAILS, result.details());
            }
        }

        if (hasText(cfg.getHealthMessage())) {
            builder.addParagraph(cfg.getHealthMessage());
        }

        String runbookUrl = normalizeRunbookUrl(cfg.getHealthRunbookUrl());
        if (hasText(runbookUrl)) {
            builder.addLabeledLink(LABEL_RUNBOOK, runbookUrl);
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
                log.warning(() -> MSG_RUNBOOK_FILE_NOT_FOUND_PREFIX + path);
                return List.of();
            }

            long fileSize = Files.size(path);
            if (fileSize <= 0L || fileSize > MAX_HEALTH_ATTACHMENT_BYTES) {
                log.warning(() -> MSG_RUNBOOK_FILE_SIZE_INVALID_PREFIX + fileSize + TEXT_CLOSE_PAREN_AT + path);
                return List.of();
            }

            byte[] content = Files.readAllBytes(path);
            String fileName = path.getFileName() == null ? FILE_RUNBOOK_BIN : path.getFileName().toString();
            String contentType = Files.probeContentType(path);
            if (!hasText(contentType)) {
                contentType = MIME_APPLICATION_OCTET_STREAM;
            }
            return List.of(new EmailAttachment(fileName, contentType, content));
        } catch (IOException | InvalidPathException e) {
            log.log(Level.WARNING, MSG_RUNBOOK_ATTACHMENT_PARSE_ERROR, e);
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
            if (!URL_SCHEME_HTTP.equals(normalizedScheme) && !URL_SCHEME_HTTPS.equals(normalizedScheme)) {
                return null;
            }
            return uri.toString();
        } catch (IllegalArgumentException e) {
            log.log(Level.FINE, MSG_INVALID_RUNBOOK_URL, e);
            return null;
        }
    }

    private String buildTermBody(AutoEmailAlertConfig cfg, Instant now, long previousCount, long currentCount, long delta) {
        StringBuilder sb = new StringBuilder();
        sb.append(TERM_ACTIVITY_TITLE).append(LS).append(LS);
        sb.append(PREFIX_TERM).append(defaultIfBlank(cfg.getTermName(), VALUE_NOT_SET)).append(System.lineSeparator());
        sb.append(PREFIX_DETECTED_AT).append(formatInstant(now)).append(System.lineSeparator());
        sb.append(PREFIX_PREVIOUS_COUNT).append(previousCount).append(System.lineSeparator());
        sb.append(PREFIX_CURRENT_COUNT).append(currentCount).append(System.lineSeparator());
        sb.append(PREFIX_INCREASE).append(delta).append(System.lineSeparator());

        if (hasText(cfg.getTermMessage())) {
            sb.append(System.lineSeparator()).append(cfg.getTermMessage()).append(System.lineSeparator());
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

        String[] raw = csv.split(REGEX_RECIPIENT_SPLIT);
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
                .append(twoDigit(hours)).append(DURATION_HOURS_SUFFIX)
                .append(twoDigit(minutes)).append(DURATION_MINUTES_SUFFIX)
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
            return EMPTY;
        }
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC).format(value);
    }

    public record TestEmailResult(boolean sent, String message) {
    }
}

final class HtmlEmailBuilder {

    private final StringWriter out;
    private final XMLStreamWriter writer;

    private HtmlEmailBuilder(String title) throws XMLStreamException {
        this.out = new StringWriter();
        this.writer = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), AutoEmailAlertScheduler.XML_VERSION);
        writer.writeStartElement(AutoEmailAlertScheduler.TAG_HTML);
        writer.writeStartElement(AutoEmailAlertScheduler.TAG_BODY);
        writer.writeStartElement(AutoEmailAlertScheduler.TAG_H2);
        writer.writeCharacters(title == null ? AutoEmailAlertScheduler.EMPTY : title);
        writer.writeEndElement();
    }

    static HtmlEmailBuilder create(String title) {
        try {
            return new HtmlEmailBuilder(title);
        } catch (XMLStreamException e) {
            throw new IllegalStateException(AutoEmailAlertScheduler.MSG_INIT_HTML_BUILDER_FAILED, e);
        }
    }

    void addParagraph(String text) {
        try {
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_P);
            writer.writeCharacters(text == null ? AutoEmailAlertScheduler.EMPTY : text);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(AutoEmailAlertScheduler.MSG_APPEND_PARAGRAPH_FAILED, e);
        }
    }

    void addEmphasizedParagraph(String text) {
        try {
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_P);
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_EM);
            writer.writeCharacters(text == null ? AutoEmailAlertScheduler.EMPTY : text);
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(AutoEmailAlertScheduler.MSG_APPEND_EMPHASIZED_PARAGRAPH_FAILED, e);
        }
    }

    void addLabeledValue(String label, String value) {
        try {
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_P);
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_STRONG);
            writer.writeCharacters((label == null ? AutoEmailAlertScheduler.EMPTY : label) + (char) 58);
            writer.writeEndElement();
            writer.writeCharacters(' ' + (value == null ? AutoEmailAlertScheduler.EMPTY : value));
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(AutoEmailAlertScheduler.MSG_APPEND_LABELED_VALUE_FAILED, e);
        }
    }

    void addLabeledLink(String label, String url) {
        try {
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_P);
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_STRONG);
            writer.writeCharacters((label == null ? AutoEmailAlertScheduler.EMPTY : label) + (char) 58);
            writer.writeEndElement();
            writer.writeCharacters(String.valueOf(' '));
            writer.writeStartElement(AutoEmailAlertScheduler.TAG_A);
            writer.writeAttribute(AutoEmailAlertScheduler.ATTR_HREF, url == null ? AutoEmailAlertScheduler.EMPTY : url);
            writer.writeCharacters(url == null ? AutoEmailAlertScheduler.EMPTY : url);
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(AutoEmailAlertScheduler.MSG_APPEND_LABELED_LINK_FAILED, e);
        }
    }

    String build() {
        try {
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            return out.toString();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(AutoEmailAlertScheduler.MSG_FINALIZE_HTML_BODY_FAILED, e);
        }
    }
}


