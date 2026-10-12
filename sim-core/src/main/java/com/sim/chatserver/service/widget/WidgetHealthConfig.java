package com.sim.chatserver.service.widget;

import java.time.Instant;

public class WidgetHealthConfig {

    private int id;
    private String healthcheckUrl;
    private boolean healthcheckEnabled = true;
    private int checkIntervalSeconds = 300;
    private String method;
    private int timeoutMs;
    private String expectJsonField;
    private String expectJsonValue;
    private String widgetId;

    private String requestOrigin;
    private String requestReferer;
    private String requestUserAgent;
    private String requestCookie;

    private String apiKeyHeaderName;
    private String apiKeyValue;

    private String updatedBy;
    private Instant updatedAt;

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    public String getHealthcheckUrl() {
        return healthcheckUrl;
    }

    public void setHealthcheckUrl(String healthcheckUrl) {
        this.healthcheckUrl = healthcheckUrl;
    }

    public boolean isHealthcheckEnabled() {
        return healthcheckEnabled;
    }

    public void setHealthcheckEnabled(boolean healthcheckEnabled) {
        this.healthcheckEnabled = healthcheckEnabled;
    }

    public int getCheckIntervalSeconds() {
        return checkIntervalSeconds;
    }

    public void setCheckIntervalSeconds(int checkIntervalSeconds) {
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getExpectJsonField() {
        return expectJsonField;
    }

    public void setExpectJsonField(String expectJsonField) {
        this.expectJsonField = expectJsonField;
    }

    public String getExpectJsonValue() {
        return expectJsonValue;
    }

    public void setExpectJsonValue(String expectJsonValue) {
        this.expectJsonValue = expectJsonValue;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getRequestOrigin() {
        return requestOrigin;
    }

    public void setRequestOrigin(String requestOrigin) {
        this.requestOrigin = requestOrigin;
    }

    public String getRequestReferer() {
        return requestReferer;
    }

    public void setRequestReferer(String requestReferer) {
        this.requestReferer = requestReferer;
    }

    public String getRequestUserAgent() {
        return requestUserAgent;
    }

    public void setRequestUserAgent(String requestUserAgent) {
        this.requestUserAgent = requestUserAgent;
    }

    public String getRequestCookie() {
        return requestCookie;
    }

    public void setRequestCookie(String requestCookie) {
        this.requestCookie = requestCookie;
    }

    public String getApiKeyHeaderName() {
        return apiKeyHeaderName;
    }

    public void setApiKeyHeaderName(String apiKeyHeaderName) {
        this.apiKeyHeaderName = apiKeyHeaderName;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
