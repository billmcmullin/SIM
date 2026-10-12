package com.sim.chatserver.web.admin;

import java.time.Instant;

final class AutoEmailAlertConfig {

    private int id;

    private boolean healthEnabled;
    private int healthCheckIntervalSeconds;
    private int healthOfflineDelaySeconds;
    private int healthResendIntervalSeconds;
    private String healthRecipients;
    private String healthSubject;
    private String healthMessage;
    private String healthRunbookUrl;
    private String healthRunbookAttachmentPath;

    private boolean termEnabled;
    private int termCheckIntervalSeconds;
    private String termName;
    private String termRecipients;
    private String termSubject;
    private String termMessage;

    private String healthLastStatus;
    private Instant healthLastCheckedAt;
    private Instant healthOfflineSince;
    private Instant healthLastAlertAt;
    private Instant healthLastRestartAttemptAt;

    private Instant termLastCheckedAt;
    private long termLastCount;
    private Instant termLastAlertAt;

    private String updatedBy;
    private Instant updatedAt;

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    boolean isHealthEnabled() {
        return healthEnabled;
    }

    void setHealthEnabled(boolean healthEnabled) {
        this.healthEnabled = healthEnabled;
    }

    int getHealthCheckIntervalSeconds() {
        return healthCheckIntervalSeconds;
    }

    void setHealthCheckIntervalSeconds(int healthCheckIntervalSeconds) {
        this.healthCheckIntervalSeconds = healthCheckIntervalSeconds;
    }

    int getHealthOfflineDelaySeconds() {
        return healthOfflineDelaySeconds;
    }

    void setHealthOfflineDelaySeconds(int healthOfflineDelaySeconds) {
        this.healthOfflineDelaySeconds = healthOfflineDelaySeconds;
    }

    int getHealthResendIntervalSeconds() {
        return healthResendIntervalSeconds;
    }

    void setHealthResendIntervalSeconds(int healthResendIntervalSeconds) {
        this.healthResendIntervalSeconds = healthResendIntervalSeconds;
    }

    String getHealthRecipients() {
        return healthRecipients;
    }

    void setHealthRecipients(String healthRecipients) {
        this.healthRecipients = healthRecipients;
    }

    String getHealthSubject() {
        return healthSubject;
    }

    void setHealthSubject(String healthSubject) {
        this.healthSubject = healthSubject;
    }

    String getHealthMessage() {
        return healthMessage;
    }

    void setHealthMessage(String healthMessage) {
        this.healthMessage = healthMessage;
    }

    String getHealthRunbookUrl() {
        return healthRunbookUrl;
    }

    void setHealthRunbookUrl(String healthRunbookUrl) {
        this.healthRunbookUrl = healthRunbookUrl;
    }

    String getHealthRunbookAttachmentPath() {
        return healthRunbookAttachmentPath;
    }

    void setHealthRunbookAttachmentPath(String healthRunbookAttachmentPath) {
        this.healthRunbookAttachmentPath = healthRunbookAttachmentPath;
    }

    boolean isTermEnabled() {
        return termEnabled;
    }

    void setTermEnabled(boolean termEnabled) {
        this.termEnabled = termEnabled;
    }

    int getTermCheckIntervalSeconds() {
        return termCheckIntervalSeconds;
    }

    void setTermCheckIntervalSeconds(int termCheckIntervalSeconds) {
        this.termCheckIntervalSeconds = termCheckIntervalSeconds;
    }

    String getTermName() {
        return termName;
    }

    void setTermName(String termName) {
        this.termName = termName;
    }

    String getTermRecipients() {
        return termRecipients;
    }

    void setTermRecipients(String termRecipients) {
        this.termRecipients = termRecipients;
    }

    String getTermSubject() {
        return termSubject;
    }

    void setTermSubject(String termSubject) {
        this.termSubject = termSubject;
    }

    String getTermMessage() {
        return termMessage;
    }

    void setTermMessage(String termMessage) {
        this.termMessage = termMessage;
    }

    String getHealthLastStatus() {
        return healthLastStatus;
    }

    void setHealthLastStatus(String healthLastStatus) {
        this.healthLastStatus = healthLastStatus;
    }

    Instant getHealthLastCheckedAt() {
        return healthLastCheckedAt;
    }

    void setHealthLastCheckedAt(Instant healthLastCheckedAt) {
        this.healthLastCheckedAt = healthLastCheckedAt;
    }

    Instant getHealthOfflineSince() {
        return healthOfflineSince;
    }

    void setHealthOfflineSince(Instant healthOfflineSince) {
        this.healthOfflineSince = healthOfflineSince;
    }

    Instant getHealthLastAlertAt() {
        return healthLastAlertAt;
    }

    void setHealthLastAlertAt(Instant healthLastAlertAt) {
        this.healthLastAlertAt = healthLastAlertAt;
    }

    Instant getHealthLastRestartAttemptAt() {
        return healthLastRestartAttemptAt;
    }

    void setHealthLastRestartAttemptAt(Instant healthLastRestartAttemptAt) {
        this.healthLastRestartAttemptAt = healthLastRestartAttemptAt;
    }

    Instant getTermLastCheckedAt() {
        return termLastCheckedAt;
    }

    void setTermLastCheckedAt(Instant termLastCheckedAt) {
        this.termLastCheckedAt = termLastCheckedAt;
    }

    long getTermLastCount() {
        return termLastCount;
    }

    void setTermLastCount(long termLastCount) {
        this.termLastCount = termLastCount;
    }

    Instant getTermLastAlertAt() {
        return termLastAlertAt;
    }

    void setTermLastAlertAt(Instant termLastAlertAt) {
        this.termLastAlertAt = termLastAlertAt;
    }

    String getUpdatedBy() {
        return updatedBy;
    }

    void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
