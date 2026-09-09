package com.sim.chatserver.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sim.chatserver.model.CustomerIdentity;
import com.sim.chatserver.model.CustomerIdentitySessionLink;
import com.sim.chatserver.model.CustomerIdentityStore;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.util.SessionLabelStore.SessionLabel;

public class CustomerIdentityService {

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    public CustomerIdentity resolveOrCreateBySessionId(String sessionId) throws SQLException {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }

        // 1) existing mapping
        CustomerIdentity existing = CustomerIdentityStore.findBySessionId(sessionId);
        if (existing != null) {
            return existing;
        }

        // 2) enrich from session_labels
        Map<String, SessionLabel> labels = SessionLabelStore.mapDisplayNames(List.of(sessionId));
        SessionLabel label = labels.get(sessionId);

        String displayName = label != null ? blankToNull(label.getDisplayName()) : null;
        String email = label != null ? blankToNull(label.getEmail()) : null;

        // 3) identity resolution priority: email > friendlyName > create new
        CustomerIdentity identity = null;
        String confidence = "low";

        if (email != null) {
            identity = CustomerIdentityStore.findByCanonicalEmail(email);
            confidence = "high";
        }

        if (identity == null && displayName != null) {
            identity = CustomerIdentityStore.findByCanonicalName(displayName);
            confidence = email != null ? "high" : "medium";
        }

        long identityId = -1L;
        if (identity == null) {
            long newIdentityId = CustomerIdentityStore.insertIdentity(email, displayName, confidence);
            identityId = newIdentityId;
            identity = new CustomerIdentity();
            identity.setIdentityId(Long.valueOf(newIdentityId));
            identity.setCanonicalEmail(email);
            identity.setCanonicalName(displayName);
            identity.setConfidence(confidence);
        } else {
            String identityIdText = Objects.toString(identity.getIdentityId(), "");
            if (!identityIdText.isBlank()) {
                try {
                    identityId = Long.parseLong(identityIdText);
                } catch (NumberFormatException ex) {
                    throw new SQLException("Unable to resolve identity id for session link", ex);
                }
            }
        }

        if (identityId <= 0L) {
            throw new SQLException("Unable to resolve identity id for session link");
        }

        CustomerIdentityStore.upsertSessionLink(identityId, sessionId, displayName, email);
        return identity;
    }

    public List<CustomerIdentitySessionLink> listLinkedSessions(long identityId) throws SQLException {
        return CustomerIdentityStore.listSessionLinks(identityId);
    }

    private String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
