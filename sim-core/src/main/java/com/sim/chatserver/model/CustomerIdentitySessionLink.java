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

    void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getIdentityId() {
        return identityId;
    }

    void setIdentityId(Long identityId) {
        this.identityId = identityId;
    }

    public String getDisplayNameSnapshot() {
        return displayNameSnapshot;
    }

    void setDisplayNameSnapshot(String displayNameSnapshot) {
        this.displayNameSnapshot = displayNameSnapshot;
    }

    public String getContactEmailSnapshot() {
        return contactEmailSnapshot;
    }

    void setContactEmailSnapshot(String contactEmailSnapshot) {
        this.contactEmailSnapshot = contactEmailSnapshot;
    }

    public OffsetDateTime getLinkedAt() {
        return linkedAt;
    }

    void setLinkedAt(OffsetDateTime linkedAt) {
        this.linkedAt = linkedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
