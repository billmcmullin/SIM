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

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

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

    final String getEmail() {
        return email;
    }

    final void setEmail(String email) {
        this.email = email;
    }

    final String getPhone() {
        return phone;
    }

    final void setPhone(String phone) {
        this.phone = phone;
    }

    final String getTitle() {
        return title;
    }

    final void setTitle(String title) {
        this.title = title;
    }

    final String getDepartment() {
        return department;
    }

    final void setDepartment(String department) {
        this.department = department;
    }

    final String getRawJson() {
        return rawJson;
    }

    final void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    final String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    final OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    final void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    final OffsetDateTime getUpdatedAt() {
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
