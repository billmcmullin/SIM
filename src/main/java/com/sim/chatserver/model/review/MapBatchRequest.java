// src/main/java/com/sim/chatserver/model/review/MapBatchRequest.java
package com.sim.chatserver.model.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.sim.chatserver.model.SelectedEntry;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

/**
 * Immutable request model for one map batch execution.
 *
 * Strict coverage metadata: - authoritativeAllSelectedChatIds - expectedChatIds
 * (for this batch)
 */
public final class MapBatchRequest {

    private final String requestId;
    private final int totalSelected;
    private final int totalBatches;
    private final int batchIndex;
    private final String batchId;
    private final List<SelectedEntry> entries;

    private final String targetUrl;
    private final String mode;
    private final String sessionId;
    private final boolean reset;
    private final String controlledPrompt;

    // strict coverage metadata
    private final List<String> authoritativeAllSelectedChatIds;
    private final List<String> expectedChatIds;

    private MapBatchRequest(Builder b) {
        this.requestId = nonBlank(b.requestId, "requestId");
        this.totalSelected = positiveOrZero(b.totalSelected, "totalSelected");
        this.totalBatches = positive(b.totalBatches, "totalBatches");
        this.batchIndex = positive(b.batchIndex, "batchIndex");

        if (this.batchIndex > this.totalBatches) {
            throw new IllegalArgumentException("batchIndex cannot be greater than totalBatches");
        }

        this.batchId = defaultIfBlank(b.batchId, "batch-" + this.batchIndex);
        this.entries = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(b.entries, "entries")));

        this.targetUrl = nonBlank(b.targetUrl, "targetUrl");
        this.mode = defaultIfBlank(b.mode, "chat");
        this.sessionId = defaultIfNull(b.sessionId, "");
        this.reset = b.reset;
        this.controlledPrompt = nonBlank(b.controlledPrompt, "controlledPrompt");

        // authoritative ids across all selected entries
        this.authoritativeAllSelectedChatIds = normalizeDistinctIds(
                b.authoritativeAllSelectedChatIds == null ? List.of() : b.authoritativeAllSelectedChatIds
        );

        // expected ids for THIS batch (if absent, derive from entries)
        List<String> derivedBatchIds = normalizeDistinctIds(rawBatchChatIdsFromEntries(this.entries));
        this.expectedChatIds = normalizeDistinctIds(
                (b.expectedChatIds == null || b.expectedChatIds.isEmpty()) ? derivedBatchIds : b.expectedChatIds
        );

        validateCoverageMetadata();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getRequestId() {
        return requestId;
    }

    public int getTotalSelected() {
        return totalSelected;
    }

    public int getTotalBatches() {
        return totalBatches;
    }

    public int getBatchIndex() {
        return batchIndex;
    }

    public String getBatchId() {
        return batchId;
    }

    public List<SelectedEntry> getEntries() {
        return entries;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getMode() {
        return mode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isReset() {
        return reset;
    }

    public String getControlledPrompt() {
        return controlledPrompt;
    }

    public List<String> getAuthoritativeAllSelectedChatIds() {
        return authoritativeAllSelectedChatIds;
    }

    public List<String> getExpectedChatIds() {
        return expectedChatIds;
    }

    public int batchSize() {
        return entries.size();
    }

    /**
     * Legacy-compatible getter used by orchestration code. Returns
     * expectedChatIds (authoritative for batch coverage checks).
     */
    public List<String> batchChatIds() {
        return expectedChatIds;
    }

    public boolean hasAuthoritativeAllIds() {
        return !authoritativeAllSelectedChatIds.isEmpty();
    }

    public JsonObject toJson() {
        JsonArrayBuilder ids = Json.createArrayBuilder();
        for (String id : expectedChatIds) {
            ids.add(id);
        }

        JsonArrayBuilder allIds = Json.createArrayBuilder();
        for (String id : authoritativeAllSelectedChatIds) {
            allIds.add(id);
        }

        return Json.createObjectBuilder()
                .add("requestId", requestId)
                .add("totalSelected", totalSelected)
                .add("totalBatches", totalBatches)
                .add("batchIndex", batchIndex)
                .add("batchId", batchId)
                .add("batchSize", batchSize())
                .add("batchChatIds", ids)
                .add("expectedChatIds", ids)
                .add("authoritativeAllSelectedChatIds", allIds)
                .add("targetUrl", targetUrl)
                .add("mode", mode)
                .add("sessionId", sessionId == null ? "" : sessionId)
                .add("reset", reset)
                .build();
    }

    @Override
    public String toString() {
        return "MapBatchRequest{"
                + "requestId='" + requestId + '\''
                + ", totalSelected=" + totalSelected
                + ", totalBatches=" + totalBatches
                + ", batchIndex=" + batchIndex
                + ", batchId='" + batchId + '\''
                + ", batchSize=" + batchSize()
                + ", expectedChatIds=" + expectedChatIds.size()
                + ", authoritativeAllSelectedChatIds=" + authoritativeAllSelectedChatIds.size()
                + ", mode='" + mode + '\''
                + ", reset=" + reset
                + '}';
    }

    public static final class Builder {

        private String requestId;
        private int totalSelected;
        private int totalBatches;
        private int batchIndex;
        private String batchId;
        private List<SelectedEntry> entries = new ArrayList<>();

        private String targetUrl;
        private String mode;
        private String sessionId;
        private boolean reset;
        private String controlledPrompt;

        private List<String> authoritativeAllSelectedChatIds = new ArrayList<>();
        private List<String> expectedChatIds = new ArrayList<>();

        private Builder() {
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder totalSelected(int totalSelected) {
            this.totalSelected = totalSelected;
            return this;
        }

        public Builder totalBatches(int totalBatches) {
            this.totalBatches = totalBatches;
            return this;
        }

        public Builder batchIndex(int batchIndex) {
            this.batchIndex = batchIndex;
            return this;
        }

        public Builder batchId(String batchId) {
            this.batchId = batchId;
            return this;
        }

        public Builder entries(List<SelectedEntry> entries) {
            this.entries = entries == null ? new ArrayList<>() : entries;
            return this;
        }

        public Builder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder reset(boolean reset) {
            this.reset = reset;
            return this;
        }

        public Builder controlledPrompt(String controlledPrompt) {
            this.controlledPrompt = controlledPrompt;
            return this;
        }

        public Builder authoritativeAllSelectedChatIds(List<String> authoritativeAllSelectedChatIds) {
            this.authoritativeAllSelectedChatIds
                    = authoritativeAllSelectedChatIds == null ? new ArrayList<>() : authoritativeAllSelectedChatIds;
            return this;
        }

        public Builder expectedChatIds(List<String> expectedChatIds) {
            this.expectedChatIds = expectedChatIds == null ? new ArrayList<>() : expectedChatIds;
            return this;
        }

        public MapBatchRequest build() {
            return new MapBatchRequest(this);
        }
    }

    private void validateCoverageMetadata() {
        if (!expectedChatIds.isEmpty()) {
            // ensure expected IDs are present in batch entries (strict consistency)
            Set<String> batchDerived = new LinkedHashSet<>(normalizeDistinctIds(rawBatchChatIdsFromEntries(entries)));
            for (String id : expectedChatIds) {
                if (!batchDerived.contains(id)) {
                    throw new IllegalArgumentException("expectedChatIds must be subset of chat IDs present in entries: " + id);
                }
            }
        }

        if (!authoritativeAllSelectedChatIds.isEmpty() && totalSelected > 0
                && authoritativeAllSelectedChatIds.size() > totalSelected) {
            throw new IllegalArgumentException("authoritativeAllSelectedChatIds.size cannot exceed totalSelected");
        }
    }

    private static List<String> rawBatchChatIdsFromEntries(List<SelectedEntry> entries) {
        List<String> ids = new ArrayList<>();
        if (entries == null) {
            return ids;
        }
        for (SelectedEntry e : entries) {
            if (e == null) {
                continue;
            }
            String id = e.getChatId();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static List<String> normalizeDistinctIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                out.add(id.trim().toLowerCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(out));
    }

    private static int positive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be > 0");
        }
        return value;
    }

    private static int positiveOrZero(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
        }
        return value;
    }

    private static String nonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String defaultIfNull(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
