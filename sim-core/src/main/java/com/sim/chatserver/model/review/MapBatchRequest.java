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
import com.sim.chatserver.util.CommonLiteralConstants;

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

    private static final String EMPTY = "";
    private static final String DEFAULT_MODE = CommonLiteralConstants.DEFAULT_MODE_CHAT;
    private static final String BATCH_ID_PREFIX = "batch-";
    private static final String FIELD_REQUEST_ID = CommonLiteralConstants.FIELD_REQUEST_ID;
    private static final String FIELD_TOTAL_SELECTED = CommonLiteralConstants.FIELD_TOTAL_SELECTED;
    private static final String FIELD_TOTAL_BATCHES = CommonLiteralConstants.FIELD_TOTAL_BATCHES;
    private static final String FIELD_BATCH_INDEX = "batchIndex";
    private static final String FIELD_BATCH_ID = "batchId";
    private static final String FIELD_BATCH_SIZE = "batchSize";
    private static final String FIELD_BATCH_CHAT_IDS = "batchChatIds";
    private static final String FIELD_EXPECTED_CHAT_IDS = "expectedChatIds";
    private static final String FIELD_ALL_SELECTED_CHAT_IDS = "authoritativeAllSelectedChatIds";
    private static final String FIELD_TARGET_URL = "targetUrl";
    private static final String FIELD_MODE = CommonLiteralConstants.FIELD_MODE;
    private static final String FIELD_SESSION_ID = CommonLiteralConstants.FIELD_SESSION_ID;
    private static final String FIELD_RESET = com.sim.chatserver.util.CommonLiteralConstants.VALUE_RESET;
    private static final String FIELD_ENTRIES = "entries";
    private static final String FIELD_CONTROLLED_PROMPT = "controlledPrompt";
    private static final String ERR_BATCH_INDEX_GT_TOTAL = "batchIndex cannot be greater than totalBatches";
    private static final String ERR_EXPECTED_SUBSET_PREFIX = "expectedChatIds must be subset of chat IDs present in entries: ";
    private static final String ERR_ALL_IDS_GT_TOTAL = "authoritativeAllSelectedChatIds.size cannot exceed totalSelected";
    private static final String ERR_REQUIRED_SUFFIX = CommonLiteralConstants.ERR_REQUIRED_SUFFIX;
    private static final String ERR_POSITIVE_SUFFIX = " must be > 0";
    private static final String ERR_NON_NEGATIVE_SUFFIX = " must be >= 0";
    private static final String TOSTRING_PREFIX = "MapBatchRequest{";
    private static final String TOSTRING_REQUEST_ID_PREFIX = CommonLiteralConstants.TOSTRING_REQUEST_ID_PREFIX;
    private static final String TOSTRING_TOTAL_SELECTED_PREFIX = ", totalSelected=";
    private static final String TOSTRING_TOTAL_BATCHES_PREFIX = CommonLiteralConstants.TOSTRING_TOTAL_BATCHES_PREFIX;
    private static final String TOSTRING_BATCH_INDEX_PREFIX = ", batchIndex=";
    private static final String TOSTRING_BATCH_ID_PREFIX = ", batchId='";
    private static final String TOSTRING_BATCH_SIZE_PREFIX = ", batchSize=";
    private static final String TOSTRING_EXPECTED_IDS_PREFIX = ", expectedChatIds=";
    private static final String TOSTRING_ALL_IDS_PREFIX = ", authoritativeAllSelectedChatIds=";
    private static final String TOSTRING_MODE_PREFIX = ", mode='";
    private static final String TOSTRING_RESET_PREFIX = ", reset=";

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

    MapBatchRequest(MapBatchRequestBuilder b) {
        this.requestId = nonBlank(b.requestId, FIELD_REQUEST_ID);
        this.totalSelected = positiveOrZero(b.totalSelected, FIELD_TOTAL_SELECTED);
        this.totalBatches = positive(b.totalBatches, FIELD_TOTAL_BATCHES);
        this.batchIndex = positive(b.batchIndex, FIELD_BATCH_INDEX);

        if (this.batchIndex > this.totalBatches) {
            throw new IllegalArgumentException(ERR_BATCH_INDEX_GT_TOTAL);
        }

        this.batchId = defaultIfBlank(b.batchId, BATCH_ID_PREFIX + this.batchIndex);
        this.entries = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(b.entries, FIELD_ENTRIES)));

        this.targetUrl = nonBlank(b.targetUrl, FIELD_TARGET_URL);
        this.mode = defaultIfBlank(b.mode, DEFAULT_MODE);
        this.sessionId = defaultIfNull(b.sessionId, EMPTY);
        this.reset = b.reset;
        this.controlledPrompt = nonBlank(b.controlledPrompt, FIELD_CONTROLLED_PROMPT);

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

    public static MapBatchRequestBuilder builder() {
        return new MapBatchRequestBuilder();
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

    List<String> getExpectedChatIds() {
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

    boolean hasAuthoritativeAllIds() {
        return !authoritativeAllSelectedChatIds.isEmpty();
    }

    JsonObject toJson() {
        JsonArrayBuilder ids = Json.createArrayBuilder();
        for (String id : expectedChatIds) {
            ids.add(id);
        }

        JsonArrayBuilder allIds = Json.createArrayBuilder();
        for (String id : authoritativeAllSelectedChatIds) {
            allIds.add(id);
        }

        return Json.createObjectBuilder()
            .add(FIELD_REQUEST_ID, requestId)
            .add(FIELD_TOTAL_SELECTED, totalSelected)
            .add(FIELD_TOTAL_BATCHES, totalBatches)
            .add(FIELD_BATCH_INDEX, batchIndex)
            .add(FIELD_BATCH_ID, batchId)
            .add(FIELD_BATCH_SIZE, batchSize())
            .add(FIELD_BATCH_CHAT_IDS, ids)
            .add(FIELD_EXPECTED_CHAT_IDS, ids)
            .add(FIELD_ALL_SELECTED_CHAT_IDS, allIds)
            .add(FIELD_TARGET_URL, targetUrl)
            .add(FIELD_MODE, mode)
            .add(FIELD_SESSION_ID, sessionId == null ? EMPTY : sessionId)
            .add(FIELD_RESET, reset)
                .build();
    }

    @Override
    public String toString() {
        return TOSTRING_PREFIX
            + TOSTRING_REQUEST_ID_PREFIX + requestId + '\''
            + TOSTRING_TOTAL_SELECTED_PREFIX + totalSelected
            + TOSTRING_TOTAL_BATCHES_PREFIX + totalBatches
            + TOSTRING_BATCH_INDEX_PREFIX + batchIndex
            + TOSTRING_BATCH_ID_PREFIX + batchId + '\''
            + TOSTRING_BATCH_SIZE_PREFIX + batchSize()
            + TOSTRING_EXPECTED_IDS_PREFIX + expectedChatIds.size()
            + TOSTRING_ALL_IDS_PREFIX + authoritativeAllSelectedChatIds.size()
            + TOSTRING_MODE_PREFIX + mode + '\''
            + TOSTRING_RESET_PREFIX + reset
                + '}';
    }

    private void validateCoverageMetadata() {
        if (!expectedChatIds.isEmpty()) {
            // ensure expected IDs are present in batch entries (strict consistency)
            Set<String> batchDerived = new LinkedHashSet<>(normalizeDistinctIds(rawBatchChatIdsFromEntries(entries)));
            for (String id : expectedChatIds) {
                if (!batchDerived.contains(id)) {
                    throw new IllegalArgumentException(ERR_EXPECTED_SUBSET_PREFIX + id);
                }
            }
        }

        if (!authoritativeAllSelectedChatIds.isEmpty() && totalSelected > 0
                && authoritativeAllSelectedChatIds.size() > totalSelected) {
            throw new IllegalArgumentException(ERR_ALL_IDS_GT_TOTAL);
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
            throw new IllegalArgumentException(field + ERR_POSITIVE_SUFFIX);
        }
        return value;
    }

    private static int positiveOrZero(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + ERR_NON_NEGATIVE_SUFFIX);
        }
        return value;
    }

    private static String nonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + ERR_REQUIRED_SUFFIX);
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

