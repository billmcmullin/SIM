package com.sim.chatserver.web.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.email.DbEmailConfigProvider;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.web.admin.AutoEmailAlertConfigStore.AutoEmailAlertConfig;
 class AutoEmailAlertSchedulerTest {

    @Test
    void sendHealthTestEmail_coversNullConfigAndFailurePaths() {
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        WidgetAvailabilityChecker checker = mock(WidgetAvailabilityChecker.class);
        AutoEmailAlertScheduler scheduler = new AutoEmailAlertScheduler(
                store,
                mock(DataSource.class),
                checker,
                mock(TermsStore.class),
                mock(DbEmailConfigProvider.class)) {
            @Override
            ServerConfig loadAwsConfigForHealthcheck() {
                return new ServerConfig();
            }
        };

        AutoEmailAlertScheduler.TestEmailResult missingCfg = scheduler.sendHealthTestEmail(null);
        assertFalse(missingCfg.sent());

        AutoEmailAlertConfig noRecipients = new AutoEmailAlertConfig();
        AutoEmailAlertScheduler.TestEmailResult missingRecipients = scheduler.sendHealthTestEmail(noRecipients);
        assertFalse(missingRecipients.sent());

        AutoEmailAlertConfig withRecipients = new AutoEmailAlertConfig();
        withRecipients.setHealthRecipients("ops@example.com");
        when(checker.checkNow()).thenThrow(new IllegalStateException("checker unavailable"));

        AutoEmailAlertScheduler.TestEmailResult failedSend = scheduler.sendHealthTestEmail(withRecipients);
        assertFalse(failedSend.sent());
        assertTrue(failedSend.message().toLowerCase().contains("failed"));
    }

    @Test
    void evaluateHealthAlert_downAndUpPaths_updateState() throws Exception {
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        WidgetAvailabilityChecker checker = mock(WidgetAvailabilityChecker.class);
        AutoEmailAlertScheduler scheduler = new AutoEmailAlertScheduler(
                store,
                mock(DataSource.class),
                checker,
                mock(TermsStore.class),
                mock(DbEmailConfigProvider.class)) {
            @Override
            ServerConfig loadAwsConfigForHealthcheck() {
                return new ServerConfig();
            }
        };

        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();
        cfg.setHealthRecipients("ops@example.com");
        cfg.setHealthOfflineDelaySeconds(0);
        cfg.setHealthResendIntervalSeconds(30);
        cfg.setHealthSubject("Offline alert");

        when(checker.checkNow()).thenReturn(new WidgetAvailabilityChecker.WidgetAvailabilityResult(false, "DOWN", "", 10L, "d"));
        Instant firstCheck = Instant.parse("2026-08-07T12:00:00Z");
        invokePrivate(scheduler, "evaluateHealthAlert", new Class<?>[]{AutoEmailAlertConfig.class, Instant.class}, cfg, firstCheck);

        ArgumentCaptor<Instant> firstOfflineSince = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> firstLastAlert = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> firstRestartAttempt = ArgumentCaptor.forClass(Instant.class);
        verify(store).updateHealthState(eq(firstCheck), eq("DOWN"), firstOfflineSince.capture(), firstLastAlert.capture(), firstRestartAttempt.capture());
        assertNotNull(firstOfflineSince.getValue());
        assertNull(firstLastAlert.getValue());
        assertNotNull(firstRestartAttempt.getValue());

        setPrivateField(cfg, "healthLastStatus", "DOWN");
        setPrivateField(cfg, "healthOfflineSince", Instant.parse("2026-08-07T11:50:00Z"));
        setPrivateField(cfg, "healthLastAlertAt", Instant.parse("2026-08-07T11:55:00Z"));
        setPrivateField(cfg, "healthLastRestartAttemptAt", Instant.parse("2026-08-07T11:56:00Z"));
        when(checker.checkNow()).thenReturn(new WidgetAvailabilityChecker.WidgetAvailabilityResult(true, "UP", "", 5L, "ok"));
        invokePrivate(scheduler, "evaluateHealthAlert", new Class<?>[]{AutoEmailAlertConfig.class, Instant.class}, cfg, Instant.parse("2026-08-07T12:01:00Z"));
        verify(store).updateHealthState(any(), eq("UP"), eq(null), eq(null), eq(null));
    }

    @Test
    void evaluateHealthAlert_downBeforeOfflineDelay_doesNotAttemptRestart() throws Exception {
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        WidgetAvailabilityChecker checker = mock(WidgetAvailabilityChecker.class);
        AtomicReference<String> rebootCapture = new AtomicReference<>();

        AutoEmailAlertScheduler scheduler = new AutoEmailAlertScheduler(
                store,
                mock(DataSource.class),
                checker,
                mock(TermsStore.class),
                mock(DbEmailConfigProvider.class)) {
            @Override
            ServerConfig loadAwsConfigForHealthcheck() {
                ServerConfig cfg = new ServerConfig();
                cfg.setAwsRegion("us-east-1");
                cfg.setAwsInstanceId("i-123");
                cfg.setAwsAccessKeyId("AKIA");
                cfg.setAwsSecretAccessKey("secret");
                return cfg;
            }

            @Override
            boolean rebootEc2InstanceForHealthcheck(String region, String accessKeyId, String secretAccessKey, String instanceId) {
                rebootCapture.set(region + ":" + instanceId);
                return true;
            }
        };

        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();
        cfg.setHealthRecipients("ops@example.com");
        cfg.setHealthOfflineDelaySeconds(300);
        cfg.setHealthResendIntervalSeconds(30);

        when(checker.checkNow()).thenReturn(new WidgetAvailabilityChecker.WidgetAvailabilityResult(false, "DOWN", "", 10L, "d"));
        Instant now = Instant.parse("2026-08-07T12:00:00Z");
        invokePrivate(scheduler, "evaluateHealthAlert", new Class<?>[]{AutoEmailAlertConfig.class, Instant.class}, cfg, now);

        ArgumentCaptor<Instant> offlineSince = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> lastAlert = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> restartAttempt = ArgumentCaptor.forClass(Instant.class);
        verify(store).updateHealthState(eq(now), eq("DOWN"), offlineSince.capture(), lastAlert.capture(), restartAttempt.capture());

        assertEquals(now, offlineSince.getValue());
        assertNull(lastAlert.getValue());
        assertNull(restartAttempt.getValue());
        assertNull(rebootCapture.get());
    }

    @Test
    void attemptAutomaticAwsRestart_withValidAwsConfig_rebootsInstance() throws Exception {
        AtomicReference<String> capture = new AtomicReference<>();

        AutoEmailAlertScheduler scheduler = new AutoEmailAlertScheduler(
                mock(AutoEmailAlertConfigStore.class),
                mock(DataSource.class),
                mock(WidgetAvailabilityChecker.class),
                mock(TermsStore.class),
                mock(DbEmailConfigProvider.class)) {
            @Override
            ServerConfig loadAwsConfigForHealthcheck() {
                ServerConfig cfg = new ServerConfig();
                cfg.setAwsRegion("us-west-2");
                cfg.setAwsInstanceId("i-0123456789abcdef0");
                cfg.setAwsAccessKeyId("AKIATEST123");
                cfg.setAwsSecretAccessKey("secret");
                return cfg;
            }

            @Override
            boolean rebootEc2InstanceForHealthcheck(String region, String accessKeyId, String secretAccessKey, String instanceId) {
                capture.set(region + ":" + instanceId + ":" + accessKeyId + ":" + secretAccessKey);
                return true;
            }
        };

        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();
        Object result = new WidgetAvailabilityChecker.WidgetAvailabilityResult(false, "DOWN", "", 12L, "offline");
        boolean restarted = (boolean) invokePrivate(
                scheduler,
                "attemptAutomaticAwsRestart",
                new Class<?>[]{AutoEmailAlertConfig.class, Instant.class, Instant.class, WidgetAvailabilityChecker.WidgetAvailabilityResult.class},
                cfg,
                Instant.parse("2026-08-07T12:00:00Z"),
                Instant.parse("2026-08-07T11:50:00Z"),
                result);

        assertTrue(restarted);
        assertEquals("us-west-2:i-0123456789abcdef0:AKIATEST123:secret", capture.get());
    }

    @Test
    void evaluateHealthAlert_afterRestartBeforePostDelay_keepsAlertSuppressed() throws Exception {
        AutoEmailAlertConfigStore store = mock(AutoEmailAlertConfigStore.class);
        WidgetAvailabilityChecker checker = mock(WidgetAvailabilityChecker.class);

        AutoEmailAlertScheduler scheduler = new AutoEmailAlertScheduler(
                store,
                mock(DataSource.class),
                checker,
                mock(TermsStore.class),
                mock(DbEmailConfigProvider.class)) {
            @Override
            ServerConfig loadAwsConfigForHealthcheck() {
                ServerConfig cfg = new ServerConfig();
                cfg.setAwsRegion("us-east-1");
                cfg.setAwsInstanceId("i-xyz");
                cfg.setAwsAccessKeyId("AKIA");
                cfg.setAwsSecretAccessKey("secret");
                return cfg;
            }
        };

        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();
        cfg.setHealthRecipients("ops@example.com");
        cfg.setHealthOfflineDelaySeconds(300);
        cfg.setHealthResendIntervalSeconds(60);
        setPrivateField(cfg, "healthLastStatus", "DOWN");
        setPrivateField(cfg, "healthOfflineSince", Instant.parse("2026-08-07T11:50:00Z"));
        Instant priorRestartAttempt = Instant.parse("2026-08-07T11:59:00Z");
        setPrivateField(cfg, "healthLastRestartAttemptAt", priorRestartAttempt);

        when(checker.checkNow()).thenReturn(new WidgetAvailabilityChecker.WidgetAvailabilityResult(false, "DOWN", "", 10L, "d"));
        Instant now = Instant.parse("2026-08-07T12:00:00Z");
        invokePrivate(scheduler, "evaluateHealthAlert", new Class<?>[]{AutoEmailAlertConfig.class, Instant.class}, cfg, now);

        ArgumentCaptor<Instant> lastAlert = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> restartAttempt = ArgumentCaptor.forClass(Instant.class);
        verify(store).updateHealthState(eq(now), eq("DOWN"), any(), lastAlert.capture(), restartAttempt.capture());
        assertNull(lastAlert.getValue());
        assertEquals(priorRestartAttempt, restartAttempt.getValue());
    }

    @Test
    void parseRecipients_andHasRecipients_handleInvalidAndDuplicates() throws Exception {
        AutoEmailAlertScheduler scheduler = newScheduler();

        @SuppressWarnings("unchecked")
        List<String> recipients = (List<String>) invokePrivate(
                scheduler,
                "parseRecipients",
                new Class<?>[]{String.class},
                "a@example.com;bad;A@example.com\n b@example.com , a@example.com");

        assertEquals(3, recipients.size());
        assertTrue(recipients.contains("a@example.com"));
        assertTrue(recipients.contains("A@example.com"));
        assertTrue(recipients.contains("b@example.com"));

        assertTrue((boolean) invokePrivate(scheduler, "hasRecipients", new Class<?>[]{String.class}, "a@example.com"));
        assertFalse((boolean) invokePrivate(scheduler, "hasRecipients", new Class<?>[]{String.class}, " , ; \n"));
    }

    @Test
    void normalizeRunbookUrl_acceptsHttpHttps_only() throws Exception {
        AutoEmailAlertScheduler scheduler = newScheduler();
        assertEquals("https://example.test/a", invokePrivate(scheduler, "normalizeRunbookUrl", new Class<?>[]{String.class}, "https://example.test/a"));
        assertNull(invokePrivate(scheduler, "normalizeRunbookUrl", new Class<?>[]{String.class}, "ftp://example.test"));
        assertNull(invokePrivate(scheduler, "normalizeRunbookUrl", new Class<?>[]{String.class}, "not a url"));
    }

    @Test
    void resolveHealthAttachments_negativePaths_returnEmpty() throws Exception {
        AutoEmailAlertScheduler scheduler = newScheduler();
        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();

        cfg.setHealthRunbookAttachmentPath("C:/definitely/missing/file.txt");
        @SuppressWarnings("unchecked")
        List<Object> missingResult = (List<Object>) invokePrivate(scheduler, "resolveHealthAttachments", new Class<?>[]{AutoEmailAlertConfig.class}, cfg);
        assertTrue(missingResult.isEmpty());

        cfg.setHealthRunbookAttachmentPath("\u0000badpath");
        @SuppressWarnings("unchecked")
        List<Object> invalidPathResult = (List<Object>) invokePrivate(scheduler, "resolveHealthAttachments", new Class<?>[]{AutoEmailAlertConfig.class}, cfg);
        assertTrue(invalidPathResult.isEmpty());

        cfg.setHealthRunbookAttachmentPath(" ");
        @SuppressWarnings("unchecked")
        List<Object> blankResult = (List<Object>) invokePrivate(scheduler, "resolveHealthAttachments", new Class<?>[]{AutoEmailAlertConfig.class}, cfg);
        assertTrue(blankResult.isEmpty());
    }

    @Test
    void resolveHealthAttachments_validFile_returnsAttachment() throws Exception {
        AutoEmailAlertScheduler scheduler = newScheduler();
        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();

        Path tmp = Files.createTempFile("runbook-", ".txt");
        try {
            Files.writeString(tmp, "hello");
            cfg.setHealthRunbookAttachmentPath(tmp.toAbsolutePath().toString());
            @SuppressWarnings("unchecked")
            List<Object> attachments = (List<Object>) invokePrivate(scheduler, "resolveHealthAttachments", new Class<?>[]{AutoEmailAlertConfig.class}, cfg);
            assertEquals(1, attachments.size());
            assertNotNull(attachments.get(0));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void timeAndFormattingHelpers_coverBoundaries() throws Exception {
        AutoEmailAlertScheduler scheduler = newScheduler();
        Instant now = Instant.parse("2026-08-07T11:00:00Z");
        Instant before = now.minusSeconds(65);

        assertTrue((boolean) invokePrivate(scheduler, "isDue", new Class<?>[]{Instant.class, int.class, Instant.class}, null, 10, now));
        assertTrue((boolean) invokePrivate(scheduler, "isDue", new Class<?>[]{Instant.class, int.class, Instant.class}, before, 30, now));
        assertFalse((boolean) invokePrivate(scheduler, "isDue", new Class<?>[]{Instant.class, int.class, Instant.class}, now.minusSeconds(10), 60, now));

        assertEquals(65L, invokePrivate(scheduler, "secondsBetween", new Class<?>[]{Instant.class, Instant.class}, before, now));
        assertEquals(Long.MAX_VALUE, invokePrivate(scheduler, "secondsBetween", new Class<?>[]{Instant.class, Instant.class}, null, now));
        assertEquals("01h 01m 01s", invokePrivate(scheduler, "formatDuration", new Class<?>[]{Instant.class, Instant.class}, now, now.minusSeconds(3661)));
        assertEquals("09", invokePrivate(scheduler, "twoDigit", new Class<?>[]{long.class}, 9L));
        assertEquals("12", invokePrivate(scheduler, "twoDigit", new Class<?>[]{long.class}, 12L));
        assertEquals("", invokePrivate(scheduler, "formatInstant", new Class<?>[]{Instant.class}, new Object[]{null}));
        assertNotNull(invokePrivate(scheduler, "formatInstant", new Class<?>[]{Instant.class}, now));
    }

    @Test
    void bodyBuilders_includeExpectedSections() throws Exception {
        AutoEmailAlertScheduler scheduler = newScheduler();
        AutoEmailAlertConfig cfg = new AutoEmailAlertConfig();
        cfg.setHealthMessage("custom note");
        cfg.setHealthRunbookUrl("https://runbook.local/doc");
        cfg.setHealthRunbookAttachmentPath("C:/x/runbook.pdf");
        cfg.setHealthSubject("Offline");
        cfg.setTermName("term-A");
        cfg.setTermMessage("term note");
        setPrivateField(cfg, "healthLastRestartAttemptAt", Instant.parse("2026-08-07T10:59:00Z"));

        Object result = new WidgetAvailabilityChecker.WidgetAvailabilityResult(false, "DOWN", "2026-08-07T11:00:00Z", -5L, "details");
        Instant now = Instant.parse("2026-08-07T11:00:00Z");

        String healthBody = (String) invokePrivate(scheduler, "buildHealthBody", new Class<?>[]{AutoEmailAlertConfig.class, WidgetAvailabilityChecker.WidgetAvailabilityResult.class, Instant.class, Instant.class}, cfg, result, now, now.minusSeconds(30));
        assertTrue(healthBody.contains("OFFLINE"));
        assertTrue(healthBody.contains("custom note"));
        assertTrue(healthBody.contains("Automatic EC2 restart attempted at"));

        String healthHtml = (String) invokePrivate(scheduler, "buildHealthHtmlBody", new Class<?>[]{AutoEmailAlertConfig.class, WidgetAvailabilityChecker.WidgetAvailabilityResult.class, Instant.class, Instant.class}, cfg, result, now, now.minusSeconds(30));
        assertTrue(healthHtml.contains("<html"));
        assertTrue(healthHtml.contains("Runbook"));
        assertTrue(healthHtml.contains("Automatic EC2 restart attempted at"));

        String recoverySubject = (String) invokePrivate(scheduler, "buildHealthRecoverySubject", new Class<?>[]{AutoEmailAlertConfig.class}, cfg);
        assertTrue(recoverySubject.toLowerCase().contains("back online"));

        String recoveryBody = (String) invokePrivate(scheduler, "buildHealthRecoveryBody", new Class<?>[]{AutoEmailAlertConfig.class, WidgetAvailabilityChecker.WidgetAvailabilityResult.class, Instant.class, Instant.class}, cfg, result, now, now.minusSeconds(120));
        assertTrue(recoveryBody.contains("ONLINE"));

        String recoveryHtml = (String) invokePrivate(scheduler, "buildHealthRecoveryHtmlBody", new Class<?>[]{AutoEmailAlertConfig.class, WidgetAvailabilityChecker.WidgetAvailabilityResult.class, Instant.class, Instant.class}, cfg, result, now, now.minusSeconds(120));
        assertTrue(recoveryHtml.contains("recovery"));

        String termBody = (String) invokePrivate(scheduler, "buildTermBody", new Class<?>[]{AutoEmailAlertConfig.class, Instant.class, long.class, long.class, long.class}, cfg, now, 10L, 20L, 10L);
        assertTrue(termBody.contains("term-A"));
        assertTrue(termBody.contains("Increase: 10"));
    }

    private AutoEmailAlertScheduler newScheduler() {
        return new AutoEmailAlertScheduler(
                mock(AutoEmailAlertConfigStore.class),
                mock(DataSource.class),
                mock(WidgetAvailabilityChecker.class),
                mock(TermsStore.class),
                mock(DbEmailConfigProvider.class)) {
            @Override
            ServerConfig loadAwsConfigForHealthcheck() {
                return new ServerConfig();
            }
        };
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Object invokePrivate(Object target, String method, Class<?>[] paramTypes, Object... args) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Method m = type.getDeclaredMethod(method, paramTypes);
                m.setAccessible(true);
                return m.invoke(target, args);
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchMethodException(method);
    }
}

