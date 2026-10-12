// src/main/java/com/sim/chatserver/service/ReviewContextBuilderService.java
package com.sim.chatserver.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.SelectedEntry;
import com.sim.chatserver.util.CommonLiteralConstants;

public class ReviewContextBuilderService {

    private static final Logger log = Logger.getLogger(ReviewContextBuilderService.class.getCanonicalName());

    private static final int DEFAULT_MAX_CONTEXT_CHARS = 52000;
    private static final int MAX_TERMS = 12;

    private static final int PROMPT_SNIPPET_CHARS = 3500;
    private static final int RESPONSE_SNIPPET_CHARS = 5000;

    private static final int MAX_HASH_LINES_SMALL = 6;
    private static final int MAX_HASH_LINES_MEDIUM = 5;
    private static final int MAX_HASH_LINES_LARGE = 4;

    private static final int DEFAULT_MAP_BATCH_SIZE = 50;

    // tighter default reduce payload profile for speed + token safety
    private static final int MAX_MAP_OUTPUT_ITEM_CHARS = 2500;
    private static final int MAX_MAP_OUTPUT_ITEMS_IN_REDUCE = 5;
    private static final int MAX_IDS_PREVIEW_IN_REDUCE = 60;

    private static final int MAX_OMITTED_IDS_LIST = 1200;
    private static final int MAX_SEGMENTS_PER_ENTRY = 256;
    private static final String LINE_FEED = String.valueOf(com.sim.chatserver.util.LineSeparatorUtil.LINE_FEED);
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String DOUBLE_LINE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;
    private static final String VALUE_UNKNOWN = com.sim.chatserver.util.CommonLiteralConstants.MARKER_UNKNOWN;
    private static final String VALUE_NONE = "(none)";
    private static final String VALUE_EMPTY = com.sim.chatserver.util.CommonLiteralConstants.MARKER_EMPTY_PARENS;
    private static final String TRUNCATED_PREFIX = "... (";
    private static final String SECTION_PER_CHAT_EVIDENCE = "Per-chat evidence:";
    private static final String SECTION_MAP_REDUCE_CONTEXT = "Map-Reduce synthesis context";
    private static final String HEADING_CHAT = "### Chat ";
    private static final String LABEL_CREATED_AT = "- Created At: ";
    private static final String LABEL_SESSION_ID = "- Session ID: ";
    private static final String LABEL_PROMPT = "- Prompt:";
    private static final String LABEL_RESPONSE = "- Response:";
    private static final String META_ALL_SELECTED_IDS_COUNT = "- all_selected_ids_count: ";
    private static final String META_MISSING_IDS_COUNT = "- missing_ids_count: ";
    private static final String META_EXACT_TOTAL_SELECTED = "- exact_total_selected: ";
    private static final String META_EXACT_BATCH_IDS = "- exact_batch_ids: ";
    private static final String MSG_MAP_METADATA_TOO_LARGE = "Map batch metadata does not fit maxChars; reduce batch size and retry.";
    private static final String MSG_COVERAGE_METADATA_TOO_LARGE = "Coverage metadata does not fit maxChars; reduce batch size and retry.";
    private static final String EMPTY = "";
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String SECTION_SELECTED_CHATS_CONTEXT = "Selected chats context";
    private static final String LABEL_TOTAL_SELECTED = "- total_selected: ";
    private static final String LABEL_SAMPLED_FOR_EVIDENCE = "- sampled_for_evidence: ";
    private static final String LABEL_OMITTED_INLINE_EVIDENCE = "- omitted_from_inline_evidence: ";
    private static final String SECTION_COVERAGE_INDEX = "Coverage index (sampled subset):";
    private static final String SUFFIX_SAMPLED_TRUNCATED = " sampled chat blocks truncated by size)";
    private static final String SECTION_DETERMINISTIC_COVERAGE = "Deterministic coverage metadata:";
    private static final String LABEL_EXACT_INCLUDED_COUNT = "- exact_included_count: ";
    private static final String LABEL_EXACT_OMITTED_COUNT = "- exact_omitted_count: ";
    private static final String LABEL_EXACT_INCLUDED_IDS = "- exact_included_ids: ";
    private static final String LABEL_EXACT_OMITTED_IDS = "- exact_omitted_ids: ";
    private static final String SECTION_CARRY_FORWARD_IDS = "Carry-forward IDs (not in inline evidence):";
    private static final String SECTION_BATCH_SIGNALS = "Batch signals:";
    private static final String MSG_MAP_BATCH_INVALID_PREFIX = "Map batch invalid: expected IDs not present in batch entries: ";
    private static final String MSG_MAP_BATCH_HEADER_TOO_LARGE = "Map batch header does not fit maxChars; reduce batch size and retry.";
    private static final String LABEL_BATCH = "- batch: ";
    private static final String LABEL_ENTRIES_IN_BATCH = "- entries_in_batch: ";
    private static final String LABEL_QUERY_TERMS = "- query_terms: ";
    private static final String LABEL_EXPECTED_CHAT_IDS = "- expected_chat_ids: ";
    private static final String RULE_STRICT_HEADING = "- strict_heading_rule: use '### Chat [id]' for every expected ID";
    private static final String SECTION_MAP_BATCH_CONTEXT = "Map batch context";
    private static final String MSG_EXPECTED_IDS_LINE_TOO_LARGE = "Expected IDs line does not fit maxChars; reduce batch size and retry.";
    private static final String MSG_STRICT_RULE_LINE_TOO_LARGE = "Strict rule line does not fit maxChars; reduce batch size and retry.";
    private static final String MSG_COVERAGE_HEADER_TOO_LARGE = "Coverage metadata header does not fit maxChars; reduce batch size and retry.";
    private static final String MSG_PER_CHAT_HEADER_TOO_LARGE = "Per-chat evidence header does not fit maxChars; reduce batch size and retry.";
    private static final String MSG_MAP_CONTEXT_TOO_LARGE = "Map batch context too large before including all entries; reduce batch size and retry.";
    private static final String SECTION_DETERMINISTIC_BATCH_COVERAGE = "Deterministic batch coverage metadata:";
    private static final String LABEL_EXACT_BATCH_TOTAL = "- exact_batch_total: ";
    private static final String LABEL_EXACT_BATCH_INCLUDED = "- exact_batch_included: ";
    private static final String LABEL_EXACT_BATCH_OMITTED = "- exact_batch_omitted: ";
    private static final String LABEL_EXACT_EXPECTED_IDS = "- exact_expected_ids: ";
    private static final String LABEL_EXACT_BATCH_INCLUDED_IDS = "- exact_batch_included_ids: ";
    private static final String LABEL_EXACT_BATCH_OMITTED_IDS = "- exact_batch_omitted_ids: ";
    private static final String MSG_MAP_BATCH_COVERAGE_INCOMPLETE = "Map batch coverage incomplete; omitted IDs present. Re-batch required.";
    private static final String LABEL_MAP_OUTPUTS_ZERO = "- map_outputs: 0";
    private static final String LABEL_FAILED_BATCH_INDEXES = "- failed_batch_indexes: ";
    private static final String LABEL_USER_REQUEST = "- user_request: ";
    private static final String LABEL_MAP_OUTPUTS_COUNT = "- map_outputs_count: ";
    private static final String LABEL_ALL_SELECTED_IDS_PREVIEW = "- all_selected_ids_preview: ";
    private static final String LABEL_MISSING_IDS_PREVIEW = "- missing_ids_preview: ";
    private static final String SECTION_MAP_OUTPUTS_TRUNCATED = "Map outputs (truncated):";
    private static final String HEADING_BATCH_OUTPUT = "### Batch Output ";
    private static final String SUFFIX_BATCH_OUTPUTS_OMITTED = " batch outputs omitted/truncated for token safety)";
    private static final String SECTION_DETERMINISTIC_METADATA = "Deterministic metadata (use exactly; do not estimate):";
    private static final String LABEL_EXACT_TOTAL_BATCHES = "- exact_total_batches: ";
    private static final String LABEL_EXACT_BATCH_INDEX = "- exact_batch_index: ";
    private static final String LABEL_EXACT_BATCH_SIZE = "- exact_batch_size: ";
    private static final String SEGMENT_PREFIX = "[segment ";
    private static final String SEGMENT_SUFFIX = "] ";
    private static final String VALUE_QUESTION_MARK = "?";
    private static final String HASH_INDEX_TRUNCATED_AT = "hash index truncated at ";
    private static final String BRACKETS_EMPTY = "[]";
    private static final String BRACKET_OPEN = "[";
    private static final String COMMA_SPACE = ", ";
    private static final String COMMA_ELLIPSIS = ", ...";
    private static final String LIST_NONE = "- (none)";
    private static final String SUFFIX_ADDITIONAL_OMITTED_IDS = " additional omitted IDs not listed)";
    private static final String LIST_BULLET = "- ";
    private static final String LABEL_BATCH_PREFIX = "- Batch ";
    private static final String LABEL_BATCH_SIZE = " size=";
    private static final String LABEL_AVG_PROMPT_CHARS = " avgPromptChars=";
    private static final String LABEL_AVG_RESPONSE_CHARS = " avgResponseChars=";
    private static final String LABEL_MATCHED_TERMS = " matchedTerms=";
    private static final String MSG_BATCH_SIGNALS_OMITTED = "... (remaining batch signals omitted due to size)";
    private static final String REGEX_WHITESPACE = CommonLiteralConstants.REGEX_WHITESPACE;
    private static final String SINGLE_SPACE = " ";
    private static final String ELLIPSIS = CommonLiteralConstants.ELLIPSIS;
    private static final String ALGO_SHA_256 = "SHA-256";
    private static final String MSG_SHA256_UNAVAILABLE = "SHA-256 algorithm unavailable, falling back to hashCode";
        private static final ReviewContextStrategy STRATEGY_SMALL =
            new ReviewContextStrategy(100, 30, 15, 20, MAX_HASH_LINES_SMALL);
        private static final ReviewContextStrategy STRATEGY_MEDIUM =
            new ReviewContextStrategy(140, 35, 20, 35, MAX_HASH_LINES_MEDIUM);
        private static final ReviewContextStrategy STRATEGY_LARGE =
            new ReviewContextStrategy(180, 40, 20, 40, MAX_HASH_LINES_LARGE);

    private final ReviewSamplingService samplingService;

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private ReviewContextBuilderService() {
        this(new ReviewSamplingService());
    }

    public ReviewContextBuilderService(ReviewSamplingService samplingService) {
        this.samplingService = samplingService;
    }

    final String buildContext(String userMessage, List<SelectedEntry> entries) {
        return buildContext(userMessage, entries, DEFAULT_MAX_CONTEXT_CHARS);
    }

    public String buildContext(String userMessage, List<SelectedEntry> entries, int maxChars) {
        if (entries == null || entries.isEmpty() || maxChars <= 0) {
            return EMPTY;
        }

        final int total = entries.size();
        List<String> terms = samplingService.keywordTerms(userMessage, MAX_TERMS);
        ReviewContextStrategy strategy = chooseStrategy(total);

        List<SelectedEntry> sample = samplingService.stratifiedSample(
                entries, userMessage, strategy.topRelevantCount, strategy.newestCount, strategy.oldestCount, strategy.randomCount
        );

        StringBuilder sb = new StringBuilder();

        appendWithinLimit(sb, SECTION_SELECTED_CHATS_CONTEXT + LINE_SEPARATOR, maxChars);
        appendWithinLimit(sb, LABEL_TOTAL_SELECTED + total + LINE_FEED, maxChars);
        appendWithinLimit(sb, LABEL_SAMPLED_FOR_EVIDENCE + sample.size() + LINE_FEED, maxChars);
        appendWithinLimit(sb, LABEL_OMITTED_INLINE_EVIDENCE + Math.max(0, total - sample.size()) + LINE_FEED, maxChars);

        appendWithinLimit(sb, LINE_SEPARATOR + SECTION_COVERAGE_INDEX + LINE_SEPARATOR, maxChars);
        appendWithinLimit(sb, buildHashIndex(sample, strategy.maxHashLines), maxChars);

        appendWithinLimit(sb, LINE_SEPARATOR + SECTION_PER_CHAT_EVIDENCE + LINE_SEPARATOR, maxChars);
        int omittedBlocks = 0;
        List<String> includedIds = new ArrayList<>();
        for (SelectedEntry e : sample) {
            String id = normalizeId(safe(e.getChatId(), VALUE_UNKNOWN));
            String block = formatEvidenceBlockCompressed(e);
            if (appendWithinLimit(sb, block + DOUBLE_LINE_SEPARATOR, maxChars)) {
                includedIds.add(id);
            } else {
                omittedBlocks++;
            }
        }
        if (omittedBlocks > 0) {
            appendWithinLimit(sb, TRUNCATED_PREFIX + omittedBlocks + SUFFIX_SAMPLED_TRUNCATED + LINE_SEPARATOR, maxChars);
        }

        List<String> omittedIds = computeOmittedIds(entries, includedIds);
        if (sb.length() < maxChars - 350) {
            appendWithinLimit(sb, LINE_SEPARATOR + SECTION_DETERMINISTIC_COVERAGE + LINE_SEPARATOR, maxChars);
            appendWithinLimit(sb, META_EXACT_TOTAL_SELECTED + total + LINE_FEED, maxChars);
            appendWithinLimit(sb, LABEL_EXACT_INCLUDED_COUNT + includedIds.size() + LINE_FEED, maxChars);
            appendWithinLimit(sb, LABEL_EXACT_OMITTED_COUNT + omittedIds.size() + LINE_FEED, maxChars);
            appendWithinLimit(sb, LABEL_EXACT_INCLUDED_IDS + toBracketedIds(includedIds, 500) + LINE_FEED, maxChars);
            appendWithinLimit(sb, LABEL_EXACT_OMITTED_IDS + toBracketedIds(omittedIds, MAX_OMITTED_IDS_LIST) + LINE_FEED, maxChars);
        }

        if (sb.length() < maxChars - 300) {
            appendWithinLimit(sb, LINE_SEPARATOR + SECTION_CARRY_FORWARD_IDS + LINE_SEPARATOR, maxChars);
            appendWithinLimit(sb, buildOmittedIds(omittedIds, maxChars - sb.length()), maxChars);
        }

        if (sb.length() < maxChars - 220) {
            appendWithinLimit(sb, LINE_SEPARATOR + SECTION_BATCH_SIGNALS + LINE_SEPARATOR, maxChars);
            appendWithinLimit(sb, buildBatchSignals(sample, terms, maxChars - sb.length()), maxChars);
        }

        return trimTo(sb.toString(), maxChars);
    }

    private List<List<SelectedEntry>> splitForMap(List<SelectedEntry> entries, int batchSize) {
        List<List<SelectedEntry>> out = new ArrayList<>();
        if (entries == null || entries.isEmpty()) {
            return out;
        }

        int size = batchSize <= 0 ? DEFAULT_MAP_BATCH_SIZE : batchSize;
        int totalEntries = entries.size();
        for (int i = 0; i < totalEntries; i += size) {
            out.add(new ArrayList<>(entries.subList(i, Math.min(totalEntries, i + size))));
        }
        return out;
    }

    final List<List<SelectedEntry>> splitForMapAdaptive(List<SelectedEntry> entries, int preferredBatchSize, int minBatchSize) {
        int safeMin = Math.max(1, minBatchSize);
        int safePreferred = Math.max(safeMin, preferredBatchSize);
        return splitForMap(entries, safePreferred);
    }

    final String buildMapBatchContext(String userMessage, List<SelectedEntry> batch, int batchIndex, int totalBatches, int maxChars) {
        return buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, null);
    }

    final String buildMapBatchContext(
            String userMessage,
            List<SelectedEntry> batch,
            int batchIndex,
            int totalBatches,
            int maxChars,
            List<String> expectedChatIds
    ) {
        if (batch == null || batch.isEmpty() || maxChars <= 0) {
            return EMPTY;
        }

        List<String> terms = samplingService.keywordTerms(userMessage, MAX_TERMS);

        List<String> batchIds = extractKnownIds(batch);
        List<String> expected = expectedChatIds == null || expectedChatIds.isEmpty() ? batchIds : distinctIds(expectedChatIds);

        Set<String> batchSet = new LinkedHashSet<>(batchIds);
        Set<String> unexpected = new LinkedHashSet<>(expected);
        for (String batchId : batchSet) {
            unexpected.remove(batchId);
        }
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException(MSG_MAP_BATCH_INVALID_PREFIX + unexpected);
        }

        StringBuilder sb = new StringBuilder();

    mustAppendWithinLimit(sb, SECTION_MAP_BATCH_CONTEXT + LINE_SEPARATOR, maxChars, MSG_MAP_BATCH_HEADER_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_BATCH + batchIndex + '/' + totalBatches + LINE_FEED, maxChars, MSG_MAP_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_ENTRIES_IN_BATCH + batch.size() + LINE_FEED, maxChars, MSG_MAP_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_QUERY_TERMS + terms + LINE_FEED, maxChars, MSG_MAP_METADATA_TOO_LARGE);
    mustAppendWithinLimit(sb, LABEL_EXPECTED_CHAT_IDS + toBracketedIds(expected, 5000) + LINE_FEED, maxChars, MSG_EXPECTED_IDS_LINE_TOO_LARGE);
    mustAppendWithinLimit(sb, RULE_STRICT_HEADING + LINE_SEPARATOR, maxChars, MSG_STRICT_RULE_LINE_TOO_LARGE);

        mustAppendWithinLimit(sb, LINE_SEPARATOR + SECTION_PER_CHAT_EVIDENCE + LINE_SEPARATOR, maxChars, MSG_PER_CHAT_HEADER_TOO_LARGE);

        List<String> includedIds = new ArrayList<>();
        for (SelectedEntry e : batch) {
            String id = normalizeId(safe(e.getChatId(), VALUE_UNKNOWN));
            String block = formatEvidenceBlockFull(e) + DOUBLE_LINE_SEPARATOR;

            mustAppendWithinLimit(
                    sb, block, maxChars,
                    MSG_MAP_CONTEXT_TOO_LARGE
            );
            includedIds.add(id);
        }

        List<String> omittedIds = computeOmittedIdsByBatch(batchIds, includedIds);

        mustAppendWithinLimit(sb, LINE_SEPARATOR + SECTION_DETERMINISTIC_BATCH_COVERAGE + LINE_SEPARATOR, maxChars, MSG_COVERAGE_HEADER_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_EXACT_BATCH_TOTAL + batchIds.size() + LINE_FEED, maxChars, MSG_COVERAGE_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_EXACT_BATCH_INCLUDED + includedIds.size() + LINE_FEED, maxChars, MSG_COVERAGE_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_EXACT_BATCH_OMITTED + omittedIds.size() + LINE_FEED, maxChars, MSG_COVERAGE_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, META_EXACT_BATCH_IDS + toBracketedIds(batchIds, 5000) + LINE_FEED, maxChars, MSG_COVERAGE_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_EXACT_EXPECTED_IDS + toBracketedIds(expected, 5000) + LINE_FEED, maxChars, MSG_COVERAGE_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_EXACT_BATCH_INCLUDED_IDS + toBracketedIds(includedIds, 5000) + LINE_FEED, maxChars, MSG_COVERAGE_METADATA_TOO_LARGE);
        mustAppendWithinLimit(sb, LABEL_EXACT_BATCH_OMITTED_IDS + toBracketedIds(omittedIds, 5000) + LINE_FEED, maxChars, MSG_COVERAGE_METADATA_TOO_LARGE);

        if (!omittedIds.isEmpty()) {
            throw new IllegalArgumentException(MSG_MAP_BATCH_COVERAGE_INCOMPLETE);
        }

        return trimTo(sb.toString(), maxChars);
    }

    final String buildReduceContext(String userMessage, List<String> mapOutputs, int maxChars) {
        return buildReduceContext(userMessage, mapOutputs, List.of(), maxChars);
    }

    private String buildReduceContext(String userMessage, List<String> mapOutputs, List<Integer> failedBatchIndexes, int maxChars) {
        return buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, List.of(), List.of(), maxChars);
    }

    final String buildReduceContext(
            String userMessage,
            List<String> mapOutputs,
            List<Integer> failedBatchIndexes,
            List<String> allSelectedIds,
            List<String> missingIds,
            int maxChars
    ) {
        if (maxChars <= 0) {
            return EMPTY;
        }

        List<String> allDistinct = distinctIds(allSelectedIds);
        List<String> missingDistinct = distinctIds(missingIds);

        if (mapOutputs == null || mapOutputs.isEmpty()) {
            StringBuilder fallback = new StringBuilder();
            appendWithinLimit(fallback, SECTION_MAP_REDUCE_CONTEXT + LINE_SEPARATOR, maxChars);
            appendWithinLimit(fallback, LABEL_MAP_OUTPUTS_ZERO + LINE_SEPARATOR, maxChars);
            appendWithinLimit(fallback, LABEL_FAILED_BATCH_INDEXES + safeListInt(failedBatchIndexes) + LINE_FEED, maxChars);
            appendWithinLimit(fallback, META_ALL_SELECTED_IDS_COUNT + allDistinct.size() + LINE_FEED, maxChars);
            appendWithinLimit(fallback, META_MISSING_IDS_COUNT + missingDistinct.size() + LINE_FEED, maxChars);
            appendWithinLimit(fallback, LABEL_USER_REQUEST + safe(userMessage, EMPTY) + LINE_FEED, maxChars);
            appendWithinLimit(fallback, """

                    Synthesis instruction:
                    - No batch outputs were available.
                    - Report that analysis is blocked by batch-processing failures.
                    - Include failed batch indexes as reason chats were not used.
                    """, maxChars);
            return trimTo(fallback.toString(), maxChars);
        }

        StringBuilder sb = new StringBuilder();
        appendWithinLimit(sb, SECTION_MAP_REDUCE_CONTEXT + LINE_SEPARATOR, maxChars);
        appendWithinLimit(sb, LABEL_MAP_OUTPUTS_COUNT + mapOutputs.size() + LINE_FEED, maxChars);
        appendWithinLimit(sb, LABEL_FAILED_BATCH_INDEXES + safeListInt(failedBatchIndexes) + LINE_FEED, maxChars);
        appendWithinLimit(sb, META_ALL_SELECTED_IDS_COUNT + allDistinct.size() + LINE_FEED, maxChars);
        appendWithinLimit(sb, META_MISSING_IDS_COUNT + missingDistinct.size() + LINE_FEED, maxChars);

        appendWithinLimit(sb, LABEL_ALL_SELECTED_IDS_PREVIEW + toBracketedIds(allDistinct, MAX_IDS_PREVIEW_IN_REDUCE) + LINE_FEED, maxChars);
        appendWithinLimit(sb, LABEL_MISSING_IDS_PREVIEW + toBracketedIds(missingDistinct, MAX_IDS_PREVIEW_IN_REDUCE) + LINE_FEED, maxChars);
        appendWithinLimit(sb, LABEL_USER_REQUEST + safe(userMessage, EMPTY) + LINE_FEED, maxChars);

        appendWithinLimit(sb, LINE_SEPARATOR + SECTION_MAP_OUTPUTS_TRUNCATED + LINE_SEPARATOR, maxChars);

        int included = 0;
        int omitted = 0;
        int idx = 1;

        for (String out : mapOutputs) {
            if (included >= MAX_MAP_OUTPUT_ITEMS_IN_REDUCE) {
                omitted += (mapOutputs.size() - idx + 1);
                break;
            }

            String clean = trimTo(safe(out, EMPTY), MAX_MAP_OUTPUT_ITEM_CHARS);
            String block = HEADING_BATCH_OUTPUT + idx + LINE_FEED + clean + DOUBLE_LINE_SEPARATOR;
            if (!appendWithinLimit(sb, block, maxChars)) {
                omitted++;
            } else {
                included++;
            }
            idx++;
        }

        if (omitted > 0 || included < mapOutputs.size()) {
            int notIncluded = Math.max(0, mapOutputs.size() - included);
            appendWithinLimit(sb, TRUNCATED_PREFIX + notIncluded + SUFFIX_BATCH_OUTPUTS_OMITTED + LINE_SEPARATOR, maxChars);
        }

        appendWithinLimit(sb, """

            Synthesis instruction:
            - Merge and de-duplicate findings across included batch outputs.
            - Keep required markdown structure.
            - Prefer concrete evidence over generic statements.
            - Preserve coverage accounting and carry-forward IDs.
            - If deterministic counts/IDs are present, use them exactly (do not estimate).
            - Treat failed_batch_indexes as not-processed batches and include that reason.
            - If missing_ids is non-empty, report them exactly under coverage and carry-forward.
            - If evidence is truncated, say so briefly and avoid fabrication.
            """, maxChars);

        return trimTo(sb.toString(), maxChars);
    }

    final String buildBatchDeterministicHeader(int totalSelected, int totalBatches, int batchIndex, List<SelectedEntry> batch) {
        List<String> batchIds = extractKnownIds(batch);
        StringBuilder sb = new StringBuilder(192);
        sb.append(SECTION_DETERMINISTIC_METADATA).append(LINE_SEPARATOR);
        sb.append(META_EXACT_TOTAL_SELECTED).append(totalSelected).append(System.lineSeparator());
        sb.append(LABEL_EXACT_TOTAL_BATCHES).append(totalBatches).append(System.lineSeparator());
        sb.append(LABEL_EXACT_BATCH_INDEX).append(batchIndex).append(System.lineSeparator());
        sb.append(LABEL_EXACT_BATCH_SIZE).append(batchIds.size()).append(System.lineSeparator());
        sb.append(META_EXACT_BATCH_IDS).append(batchIds).append(System.lineSeparator());
        return sb.toString();
    }

    private List<SelectedEntry> explodeLargeEntryToSegments(SelectedEntry entry, int promptChunkChars, int responseChunkChars) {
        if (entry == null) {
            return List.of();
        }

        int pSize = Math.max(200, promptChunkChars);
        int rSize = Math.max(200, responseChunkChars);

        List<String> pSegs = splitText(entry.getPrompt(), pSize);
        List<String> rSegs = splitText(entry.getResponse(), rSize);

        int segments = Math.max(pSegs.size(), rSegs.size());
        if (segments == 0) {
            segments = 1;
        }
        segments = Math.min(segments, MAX_SEGMENTS_PER_ENTRY);

        List<SelectedEntry> out = new ArrayList<>(segments);
        for (int i = 0; i < segments; i++) {
            String p = i < pSegs.size() ? pSegs.get(i) : EMPTY;
            String r = i < rSegs.size() ? rSegs.get(i) : EMPTY;

            out.add(new SelectedEntry(
                    entry.getChatId(),
                    SEGMENT_PREFIX + (i + 1) + '/' + segments + SEGMENT_SUFFIX + p,
                    SEGMENT_PREFIX + (i + 1) + '/' + segments + SEGMENT_SUFFIX + r,
                    entry.getCreatedAt(),
                    entry.getSessionId()
            ));
        }
        return out;
    }

    final List<SelectedEntry> explodeLargeEntriesToSegments(List<SelectedEntry> entries, int promptChunkChars, int responseChunkChars) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<SelectedEntry> out = new ArrayList<>();
        for (SelectedEntry e : entries) {
            out.addAll(explodeLargeEntryToSegments(e, promptChunkChars, responseChunkChars));
        }
        return out;
    }

    private List<String> splitText(String text, int chunkSize) {
        String normalized = safe(text, EMPTY).trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        if (normalized.length() <= chunkSize) {
            return List.of(normalized);
        }

        List<String> parts = new ArrayList<>();
        int i = 0;
        while (i < normalized.length()) {
            int end = Math.min(normalized.length(), i + chunkSize);
            parts.add(normalized.substring(i, end));
            i = end;
            if (parts.size() >= MAX_SEGMENTS_PER_ENTRY) {
                break;
            }
        }
        return parts;
    }

    private ReviewContextStrategy chooseStrategy(int total) {
        if (total <= 120) {
            return STRATEGY_SMALL;
        }
        if (total <= 500) {
            return STRATEGY_MEDIUM;
        }
        return STRATEGY_LARGE;
    }

    private String formatEvidenceBlockCompressed(SelectedEntry e) {
        String id = normalizeId(safe(e.getChatId(), VALUE_UNKNOWN));
        String createdAt = safe(e.getCreatedAt(), VALUE_QUESTION_MARK);
        String sessionId = safe(e.getSessionId(), VALUE_NONE);
        String prompt = compressText(e.getPrompt(), PROMPT_SNIPPET_CHARS);
        String response = compressText(e.getResponse(), RESPONSE_SNIPPET_CHARS);

        return HEADING_CHAT + id + LINE_FEED
                + LABEL_CREATED_AT + createdAt + LINE_FEED
                + LABEL_SESSION_ID + sessionId + LINE_FEED
            + LABEL_PROMPT + LINE_SEPARATOR + prompt + LINE_FEED
            + LABEL_RESPONSE + LINE_SEPARATOR + response;
    }

    private String formatEvidenceBlockFull(SelectedEntry e) {
        String id = normalizeId(safe(e.getChatId(), VALUE_UNKNOWN));
        String createdAt = safe(e.getCreatedAt(), VALUE_QUESTION_MARK);
        String sessionId = safe(e.getSessionId(), VALUE_NONE);
        String prompt = safe(e.getPrompt(), VALUE_EMPTY).trim();
        String response = safe(e.getResponse(), VALUE_EMPTY).trim();

        return HEADING_CHAT + id + LINE_FEED
                + LABEL_CREATED_AT + createdAt + LINE_FEED
                + LABEL_SESSION_ID + sessionId + LINE_FEED
            + LABEL_PROMPT + LINE_SEPARATOR + prompt + LINE_FEED
            + LABEL_RESPONSE + LINE_SEPARATOR + response;
    }

    private String buildHashIndex(List<SelectedEntry> sample, int maxLines) {
        StringBuilder out = new StringBuilder();
        int limit = Math.max(0, maxLines);
        int count = 0;

        for (SelectedEntry e : sample) {
            if (count >= limit) {
                out.append(TRUNCATED_PREFIX).append(HASH_INDEX_TRUNCATED_AT).append(limit).append(')').append(LINE_SEPARATOR);
                break;
            }
            out.append(normalizeId(safe(e.getChatId(), VALUE_UNKNOWN)))
                    .append('|')
                    .append(sha1Hex(safe(e.getPrompt(), EMPTY)))
                    .append(System.lineSeparator());
            count++;
        }
        return out.toString();
    }

    private List<String> extractKnownIds(List<SelectedEntry> entries) {
        Set<String> ids = new LinkedHashSet<>();
        if (entries == null) {
            return new ArrayList<>(ids);
        }
        for (SelectedEntry e : entries) {
            String id = normalizeId(safe(e.getChatId(), EMPTY));
            if (!id.isBlank()) {
                ids.add(id);
            }
        }
        return new ArrayList<>(ids);
    }

    private List<String> distinctIds(List<String> ids) {
        Set<String> out = new LinkedHashSet<>();
        if (ids != null) {
            for (String id : ids) {
                String n = normalizeId(id);
                if (!n.isBlank()) {
                    out.add(n);
                }
            }
        }
        return new ArrayList<>(out);
    }

    private List<String> computeOmittedIds(List<SelectedEntry> all, List<String> includedIds) {
        Set<String> included = new HashSet<>(distinctIds(includedIds));
        List<String> omitted = new ArrayList<>();
        for (SelectedEntry e : all) {
            String id = normalizeId(safe(e.getChatId(), EMPTY));
            if (id.isBlank()) {
                continue;
            }
            if (!included.contains(id)) {
                omitted.add(id);
            }
        }
        return distinctIds(omitted);
    }

    private List<String> computeOmittedIdsByBatch(List<String> batchIds, List<String> includedIds) {
        Set<String> included = new HashSet<>(distinctIds(includedIds));
        List<String> omitted = new ArrayList<>();
        for (String id : distinctIds(batchIds)) {
            if (!included.contains(id)) {
                omitted.add(id);
            }
        }
        return omitted;
    }

    private String toBracketedIds(List<String> ids, int max) {
        List<String> clean = distinctIds(ids);
        if (clean.isEmpty()) {
            return BRACKETS_EMPTY;
        }

        StringBuilder sb = new StringBuilder(BRACKET_OPEN);
        int count = 0;
        for (String id : clean) {
            if (count > 0) {
                sb.append(COMMA_SPACE);
            }
            sb.append(id);
            count++;
            if (count >= max) {
                if (count < clean.size()) {
                    sb.append(COMMA_ELLIPSIS);
                }
                break;
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private String safeListInt(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return BRACKETS_EMPTY;
        }
        return values.toString();
    }

    private String buildOmittedIds(List<String> omittedIds, int budget) {
        if (budget <= 0) {
            return EMPTY;
        }
        List<String> clean = distinctIds(omittedIds);
        if (clean.isEmpty()) {
            return LIST_NONE + LINE_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        int emitted = 0;
        for (String id : clean) {
            if (emitted >= MAX_OMITTED_IDS_LIST) {
                String tail = TRUNCATED_PREFIX + (clean.size() - emitted) + SUFFIX_ADDITIONAL_OMITTED_IDS + LINE_SEPARATOR;
                if (sb.length() + tail.length() <= budget) {
                    sb.append(tail);
                }
                break;
            }
            String line = LIST_BULLET + id + LINE_FEED;
            if (sb.length() + line.length() > budget) {
                break;
            }
            sb.append(line);
            emitted++;
        }
        return sb.toString();
    }

    private String buildBatchSignals(List<SelectedEntry> sample, List<String> terms, int budget) {
        if (sample.isEmpty() || budget <= 0) {
            return EMPTY;
        }

        final int batchSize = 25;
        StringBuilder out = new StringBuilder();

        int total = sample.size();
        int batches = (total + batchSize - 1) / batchSize;

        for (int b = 0; b < batches; b++) {
            int from = b * batchSize;
            int to = Math.min(total, from + batchSize);

            List<SelectedEntry> slice = sample.subList(from, to);
            int pChars = 0;
            int rChars = 0;
            Set<String> matched = new HashSet<>();

            for (SelectedEntry e : slice) {
                String p = safe(e.getPrompt(), EMPTY);
                String r = safe(e.getResponse(), EMPTY);
                pChars += p.length();
                rChars += r.length();
                matched.addAll(samplingService.matchedTerms(e, terms));
            }

                int avgPromptChars = slice.isEmpty() ? 0 : (pChars / slice.size());
                int avgResponseChars = slice.isEmpty() ? 0 : (rChars / slice.size());
                String line = new StringBuilder(128)
                    .append(LABEL_BATCH_PREFIX).append(b + 1).append('/').append(batches)
                    .append(LABEL_BATCH_SIZE).append(slice.size())
                    .append(LABEL_AVG_PROMPT_CHARS).append(avgPromptChars)
                    .append(LABEL_AVG_RESPONSE_CHARS).append(avgResponseChars)
                    .append(LABEL_MATCHED_TERMS).append(matched)
                    .append(System.lineSeparator())
                    .toString();

            if (out.length() + line.length() > budget) {
                out.append(MSG_BATCH_SIGNALS_OMITTED).append(LINE_SEPARATOR);
                break;
            }
            out.append(line);
        }

        return out.toString();
    }

    private boolean appendWithinLimit(StringBuilder sb, String text, int maxChars) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        if (sb.length() >= maxChars) {
            return false;
        }

        int room = maxChars - sb.length();
        if (text.length() <= room) {
            sb.append(text);
            return true;
        }

        sb.append(text, 0, room);
        return false;
    }

    private void mustAppendWithinLimit(StringBuilder sb, String text, int maxChars, String onFailMessage) {
        if (!appendWithinLimit(sb, text, maxChars)) {
            throw new IllegalArgumentException(onFailMessage);
        }
    }

    private String compressText(String text, int maxChars) {
        if (text == null || text.isBlank()) {
            return VALUE_EMPTY;
        }
        String normalized = text.replaceAll(REGEX_WHITESPACE, SINGLE_SPACE).trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        if (maxChars <= ELLIPSIS.length()) {
            return new String(ELLIPSIS.toCharArray(), 0, Math.max(0, maxChars));
        }
        return new String(normalized.toCharArray(), 0, maxChars - ELLIPSIS.length()) + ELLIPSIS;
    }

    private String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return EMPTY;
        }
        return value.length() <= maxChars ? value : new String(value.toCharArray(), 0, maxChars);
    }

    private String safe(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String normalizeId(String id) {
        return id == null ? EMPTY : id.trim().toLowerCase(Locale.ROOT);
    }

    private String sha1Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGO_SHA_256);
            byte[] bytes = md.digest((value == null ? EMPTY : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                int v = b & 0xFF;
                appendHexByte(sb, v);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            log.log(Level.FINE, MSG_SHA256_UNAVAILABLE, ex);
            return Integer.toHexString((value == null ? EMPTY : value).hashCode());
        }
    }

    private static void appendHexByte(StringBuilder sb, int value) {
        sb.append(Character.forDigit((value >>> 4) & 0x0F, 16));
        sb.append(Character.forDigit(value & 0x0F, 16));
    }



}

