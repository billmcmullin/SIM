package com.sim.chatserver.model;

import java.time.OffsetDateTime;

public class CustomerProfile {

    private String sessionId;
    private String friendlyName;
    private String salesforceContactId;
    private String salesforceAccountId;
    private String email;
    private String phone;
    private String title;
    private String department;
    private String rawJson;
    private OffsetDateTime lastSyncedAt;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getSalesforceContactId() {
        return salesforceContactId;
    }

    public void setSalesforceContactId(String salesforceContactId) {
        this.salesforceContactId = salesforceContactId;
    }

    public String getSalesforceAccountId() {
        return salesforceAccountId;
    }

    public void setSalesforceAccountId(String salesforceAccountId) {
        this.salesforceAccountId = salesforceAccountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public OffsetDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(OffsetDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public boolean isEmpty() {
        return sessionId == null
                && friendlyName == null
                && salesforceContactId == null
                && salesforceAccountId == null
                && email == null
                && phone == null
                && title == null
                && department == null
                && rawJson == null
                && lastSyncedAt == null;
    }
}
