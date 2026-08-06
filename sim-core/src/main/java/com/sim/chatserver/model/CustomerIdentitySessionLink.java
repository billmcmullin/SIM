package com.sim.chatserver.model;

import java.time.OffsetDateTime;

public class CustomerIdentitySessionLink {

    private String sessionId;
    private Long identityId;
    private String displayNameSnapshot;
    private String contactEmailSnapshot;
    private OffsetDateTime linkedAt;
    private OffsetDateTime updatedAt;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public String getSessionId() {
        return sessionId;
    }

    final void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getIdentityId() {
        return identityId;
    }

    final void setIdentityId(Long identityId) {
        this.identityId = identityId;
    }

    public String getDisplayNameSnapshot() {
        return displayNameSnapshot;
    }

    final void setDisplayNameSnapshot(String displayNameSnapshot) {
        this.displayNameSnapshot = displayNameSnapshot;
    }

    public String getContactEmailSnapshot() {
        return contactEmailSnapshot;
    }

    final void setContactEmailSnapshot(String contactEmailSnapshot) {
        this.contactEmailSnapshot = contactEmailSnapshot;
    }

    public OffsetDateTime getLinkedAt() {
        return linkedAt;
    }

    final void setLinkedAt(OffsetDateTime linkedAt) {
        this.linkedAt = linkedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    final void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
