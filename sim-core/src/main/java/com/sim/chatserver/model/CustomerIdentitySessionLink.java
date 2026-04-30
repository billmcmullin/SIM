package com.sim.chatserver.model;

import java.time.OffsetDateTime;

public class CustomerIdentitySessionLink {

    private String sessionId;
    private Long identityId;
    private String displayNameSnapshot;
    private String contactEmailSnapshot;
    private OffsetDateTime linkedAt;
    private OffsetDateTime updatedAt;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getIdentityId() {
        return identityId;
    }

    public void setIdentityId(Long identityId) {
        this.identityId = identityId;
    }

    public String getDisplayNameSnapshot() {
        return displayNameSnapshot;
    }

    public void setDisplayNameSnapshot(String displayNameSnapshot) {
        this.displayNameSnapshot = displayNameSnapshot;
    }

    public String getContactEmailSnapshot() {
        return contactEmailSnapshot;
    }

    public void setContactEmailSnapshot(String contactEmailSnapshot) {
        this.contactEmailSnapshot = contactEmailSnapshot;
    }

    public OffsetDateTime getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(OffsetDateTime linkedAt) {
        this.linkedAt = linkedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
