package com.sim.chatserver.model;

import java.time.OffsetDateTime;

public class CustomerIdentity {

    private Long identityId;
    private String canonicalEmail;
    private String canonicalName;

    private String salesforceContactId;
    private String salesforceAccountId;

    private String email;
    private String phone;
    private String title;
    private String department;
    private String rawJson;

    private String confidence; // high|medium|low
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime lastSyncedAt;

    public Long getIdentityId() {
        return identityId;
    }

    public void setIdentityId(Long identityId) {
        this.identityId = identityId;
    }

    public String getCanonicalEmail() {
        return canonicalEmail;
    }

    public void setCanonicalEmail(String canonicalEmail) {
        this.canonicalEmail = canonicalEmail;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getSalesforceContactId() {
        return salesforceContactId;
    }

    final void setSalesforceContactId(String salesforceContactId) {
        this.salesforceContactId = salesforceContactId;
    }

    public String getSalesforceAccountId() {
        return salesforceAccountId;
    }

    final void setSalesforceAccountId(String salesforceAccountId) {
        this.salesforceAccountId = salesforceAccountId;
    }

    public String getEmail() {
        return email;
    }

    final void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    final void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    final void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    final void setDepartment(String department) {
        this.department = department;
    }

    public String getRawJson() {
        return rawJson;
    }

    final void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    final void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    final void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    final void setLastSyncedAt(OffsetDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
