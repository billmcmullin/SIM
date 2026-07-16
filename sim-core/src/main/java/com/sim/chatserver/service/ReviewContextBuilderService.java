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

import com.sim.chatserver.model.SelectedEntry;

public class ReviewContextBuilderService {

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

    private final ReviewSamplingService samplingService;

    public ReviewContextBuilderService() {
        this(new ReviewSamplingService());
    }

    public ReviewContextBuilderService(ReviewSamplingService samplingService) {
        this.samplingService = samplingService;
    }

    public String buildContext(String userMessage, List<SelectedEntry> entries) {
        return buildContext(userMessage, entries, DEFAULT_MAX_CONTEXT_CHARS);
    }

    public String buildContext(String userMessage, List<SelectedEntry> entries, int maxChars) {
        if (entries == null || entries.isEmpty() || maxChars <= 0) {
            return "";
        }

        final int total = entries.size();
        List<String> terms = samplingService.keywordTerms(userMessage, MAX_TERMS);
        Strategy strategy = chooseStrategy(total);

        List<SelectedEntry> sample = samplingService.stratifiedSample(
                entries, userMessage, strategy.topRelevantCount, strategy.newestCount, strategy.oldestCount, strategy.randomCount
        );

        StringBuilder sb = new StringBuilder();

        appendWithinLimit(sb, "Selected chats context\n", maxChars);
        appendWithinLimit(sb, "- total_selected: " + total + "\n", maxChars);
        appendWithinLimit(sb, "- sampled_for_evidence: " + sample.size() + "\n", maxChars);
        appendWithinLimit(sb, "- omitted_from_inline_evidence: " + Math.max(0, total - sample.size()) + "\n", maxChars);

        appendWithinLimit(sb, "\nCoverage index (sampled subset):\n", maxChars);
        appendWithinLimit(sb, buildHashIndex(sample, strategy.maxHashLines), maxChars);

        appendWithinLimit(sb, "\nPer-chat evidence:\n", maxChars);
        int omittedBlocks = 0;
        List<String> includedIds = new ArrayList<>();
        for (SelectedEntry e : sample) {
            String id = normalizeId(safe(e.getChatId(), "(unknown)"));
            String block = formatEvidenceBlockCompressed(e);
            if (appendWithinLimit(sb, block + "\n\n", maxChars)) {
                includedIds.add(id);
            } else {
                omittedBlocks++;
            }
        }
        if (omittedBlocks > 0) {
            appendWithinLimit(sb, "... (" + omittedBlocks + " sampled chat blocks truncated by size)\n", maxChars);
        }

        List<String> omittedIds = computeOmittedIds(entries, includedIds);
        if (sb.length() < maxChars - 350) {
            appendWithinLimit(sb, "\nDeterministic coverage metadata:\n", maxChars);
            appendWithinLimit(sb, "- exact_total_selected: " + total + "\n", maxChars);
            appendWithinLimit(sb, "- exact_included_count: " + includedIds.size() + "\n", maxChars);
            appendWithinLimit(sb, "- exact_omitted_count: " + omittedIds.size() + "\n", maxChars);
            appendWithinLimit(sb, "- exact_included_ids: " + toBracketedIds(includedIds, 500) + "\n", maxChars);
            appendWithinLimit(sb, "- exact_omitted_ids: " + toBracketedIds(omittedIds, MAX_OMITTED_IDS_LIST) + "\n", maxChars);
        }

        if (sb.length() < maxChars - 300) {
            appendWithinLimit(sb, "\nCarry-forward IDs (not in inline evidence):\n", maxChars);
            appendWithinLimit(sb, buildOmittedIds(omittedIds, maxChars - sb.length()), maxChars);
        }

        if (sb.length() < maxChars - 220) {
            appendWithinLimit(sb, "\nBatch signals:\n", maxChars);
            appendWithinLimit(sb, buildBatchSignals(sample, terms, maxChars - sb.length()), maxChars);
        }

        return trimTo(sb.toString(), maxChars);
    }

    public List<List<SelectedEntry>> splitForMap(List<SelectedEntry> entries, int batchSize) {
        List<List<SelectedEntry>> out = new ArrayList<>();
        if (entries == null || entries.isEmpty()) {
            return out;
        }

        int size = batchSize <= 0 ? DEFAULT_MAP_BATCH_SIZE : batchSize;
        for (int i = 0; i < entries.size(); i += size) {
            out.add(new ArrayList<>(entries.subList(i, Math.min(entries.size(), i + size))));
        }
        return out;
    }

    public List<List<SelectedEntry>> splitForMapAdaptive(List<SelectedEntry> entries, int preferredBatchSize, int minBatchSize) {
        int safeMin = Math.max(1, minBatchSize);
        int safePreferred = Math.max(safeMin, preferredBatchSize);
        return splitForMap(entries, safePreferred);
    }

    public String buildMapBatchContext(String userMessage, List<SelectedEntry> batch, int batchIndex, int totalBatches, int maxChars) {
        return buildMapBatchContext(userMessage, batch, batchIndex, totalBatches, maxChars, null);
    }

    public String buildMapBatchContext(
            String userMessage,
            List<SelectedEntry> batch,
            int batchIndex,
            int totalBatches,
            int maxChars,
            List<String> expectedChatIds
    ) {
        if (batch == null || batch.isEmpty() || maxChars <= 0) {
            return "";
        }

        List<String> terms = samplingService.keywordTerms(userMessage, MAX_TERMS);

        List<String> batchIds = extractKnownIds(batch);
        List<String> expected = expectedChatIds == null || expectedChatIds.isEmpty() ? batchIds : distinctIds(expectedChatIds);

        Set<String> batchSet = new LinkedHashSet<>(batchIds);
        Set<String> unexpected = new LinkedHashSet<>(expected);
        unexpected.removeAll(batchSet);
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException("Map batch invalid: expected IDs not present in batch entries: " + unexpected);
        }

        StringBuilder sb = new StringBuilder();

        mustAppendWithinLimit(sb, "Map batch context\n", maxChars, "Map batch header does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- batch: " + batchIndex + "/" + totalBatches + "\n", maxChars, "Map batch metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- entries_in_batch: " + batch.size() + "\n", maxChars, "Map batch metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- query_terms: " + terms + "\n", maxChars, "Map batch metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- expected_chat_ids: " + toBracketedIds(expected, 5000) + "\n", maxChars, "Expected IDs line does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- strict_heading_rule: use '### Chat <id>' for every expected ID\n", maxChars, "Strict rule line does not fit maxChars; reduce batch size and retry.");

        mustAppendWithinLimit(sb, "\nPer-chat evidence:\n", maxChars, "Per-chat evidence header does not fit maxChars; reduce batch size and retry.");

        List<String> includedIds = new ArrayList<>();
        for (SelectedEntry e : batch) {
            String id = normalizeId(safe(e.getChatId(), "(unknown)"));
            String block = formatEvidenceBlockFull(e) + "\n\n";

            mustAppendWithinLimit(
                    sb, block, maxChars,
                    "Map batch context too large before including all entries; reduce batch size and retry."
            );
            includedIds.add(id);
        }

        List<String> omittedIds = computeOmittedIdsByBatch(batchIds, includedIds);

        mustAppendWithinLimit(sb, "\nDeterministic batch coverage metadata:\n", maxChars, "Coverage metadata header does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- exact_batch_total: " + batchIds.size() + "\n", maxChars, "Coverage metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- exact_batch_included: " + includedIds.size() + "\n", maxChars, "Coverage metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- exact_batch_omitted: " + omittedIds.size() + "\n", maxChars, "Coverage metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- exact_batch_ids: " + toBracketedIds(batchIds, 5000) + "\n", maxChars, "Coverage metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- exact_expected_ids: " + toBracketedIds(expected, 5000) + "\n", maxChars, "Coverage metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- exact_batch_included_ids: " + toBracketedIds(includedIds, 5000) + "\n", maxChars, "Coverage metadata does not fit maxChars; reduce batch size and retry.");
        mustAppendWithinLimit(sb, "- exact_batch_omitted_ids: " + toBracketedIds(omittedIds, 5000) + "\n", maxChars, "Coverage metadata does not fit maxChars; reduce batch size and retry.");

        if (!omittedIds.isEmpty()) {
            throw new IllegalArgumentException("Map batch coverage incomplete; omitted IDs present. Re-batch required.");
        }

        return trimTo(sb.toString(), maxChars);
    }

    public String buildReduceContext(String userMessage, List<String> mapOutputs, int maxChars) {
        return buildReduceContext(userMessage, mapOutputs, List.of(), maxChars);
    }

    public String buildReduceContext(String userMessage, List<String> mapOutputs, List<Integer> failedBatchIndexes, int maxChars) {
        return buildReduceContext(userMessage, mapOutputs, failedBatchIndexes, List.of(), List.of(), maxChars);
    }

    public String buildReduceContext(
            String userMessage,
            List<String> mapOutputs,
            List<Integer> failedBatchIndexes,
            List<String> allSelectedIds,
            List<String> missingIds,
            int maxChars
    ) {
        if (maxChars <= 0) {
            return "";
        }

        List<String> allDistinct = distinctIds(allSelectedIds);
        List<String> missingDistinct = distinctIds(missingIds);

        if (mapOutputs == null || mapOutputs.isEmpty()) {
            StringBuilder fallback = new StringBuilder();
            appendWithinLimit(fallback, "Map-Reduce synthesis context\n", maxChars);
            appendWithinLimit(fallback, "- map_outputs: 0\n", maxChars);
            appendWithinLimit(fallback, "- failed_batch_indexes: " + safeListInt(failedBatchIndexes) + "\n", maxChars);
            appendWithinLimit(fallback, "- all_selected_ids_count: " + allDistinct.size() + "\n", maxChars);
            appendWithinLimit(fallback, "- missing_ids_count: " + missingDistinct.size() + "\n", maxChars);
            appendWithinLimit(fallback, "- user_request: " + safe(userMessage, "") + "\n", maxChars);
            appendWithinLimit(fallback, "\nSynthesis instruction:\n"
                    + "- No batch outputs were available.\n"
                    + "- Report that analysis is blocked by batch-processing failures.\n"
                    + "- Include failed batch indexes as reason chats were not used.\n", maxChars);
            return trimTo(fallback.toString(), maxChars);
        }

        StringBuilder sb = new StringBuilder();
        appendWithinLimit(sb, "Map-Reduce synthesis context\n", maxChars);
        appendWithinLimit(sb, "- map_outputs_count: " + mapOutputs.size() + "\n", maxChars);
        appendWithinLimit(sb, "- failed_batch_indexes: " + safeListInt(failedBatchIndexes) + "\n", maxChars);
        appendWithinLimit(sb, "- all_selected_ids_count: " + allDistinct.size() + "\n", maxChars);
        appendWithinLimit(sb, "- missing_ids_count: " + missingDistinct.size() + "\n", maxChars);

        appendWithinLimit(sb, "- all_selected_ids_preview: " + toBracketedIds(allDistinct, MAX_IDS_PREVIEW_IN_REDUCE) + "\n", maxChars);
        appendWithinLimit(sb, "- missing_ids_preview: " + toBracketedIds(missingDistinct, MAX_IDS_PREVIEW_IN_REDUCE) + "\n", maxChars);
        appendWithinLimit(sb, "- user_request: " + safe(userMessage, "") + "\n", maxChars);

        appendWithinLimit(sb, "\nMap outputs (truncated):\n", maxChars);

        int included = 0;
        int omitted = 0;
        int idx = 1;

        for (String out : mapOutputs) {
            if (included >= MAX_MAP_OUTPUT_ITEMS_IN_REDUCE) {
                omitted += (mapOutputs.size() - idx + 1);
                break;
            }

            String clean = trimTo(safe(out, ""), MAX_MAP_OUTPUT_ITEM_CHARS);
            String block = "### Batch Output " + idx + "\n" + clean + "\n\n";
            if (!appendWithinLimit(sb, block, maxChars)) {
                omitted++;
            } else {
                included++;
            }
            idx++;
        }

        if (omitted > 0 || included < mapOutputs.size()) {
            int notIncluded = Math.max(0, mapOutputs.size() - included);
            appendWithinLimit(sb, "... (" + notIncluded + " batch outputs omitted/truncated for token safety)\n", maxChars);
        }

        appendWithinLimit(sb, "\nSynthesis instruction:\n"
                + "- Merge and de-duplicate findings across included batch outputs.\n"
                + "- Keep required markdown structure.\n"
                + "- Prefer concrete evidence over generic statements.\n"
                + "- Preserve coverage accounting and carry-forward IDs.\n"
                + "- If deterministic counts/IDs are present, use them exactly (do not estimate).\n"
                + "- Treat failed_batch_indexes as not-processed batches and include that reason.\n"
                + "- If missing_ids is non-empty, report them exactly under coverage and carry-forward.\n"
                + "- If evidence is truncated, say so briefly and avoid fabrication.\n", maxChars);

        return trimTo(sb.toString(), maxChars);
    }

    public String buildBatchDeterministicHeader(int totalSelected, int totalBatches, int batchIndex, List<SelectedEntry> batch) {
        List<String> batchIds = extractKnownIds(batch);
        return """
                Deterministic metadata (use exactly; do not estimate):
                - exact_total_selected: %d
                - exact_total_batches: %d
                - exact_batch_index: %d
                - exact_batch_size: %d
                - exact_batch_ids: %s
                """.formatted(totalSelected, totalBatches, batchIndex, batchIds.size(), batchIds);
    }

    public List<SelectedEntry> explodeLargeEntryToSegments(SelectedEntry entry, int promptChunkChars, int responseChunkChars) {
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
            String p = i < pSegs.size() ? pSegs.get(i) : "";
            String r = i < rSegs.size() ? rSegs.get(i) : "";

            out.add(new SelectedEntry(
                    entry.getChatId(),
                    "[segment " + (i + 1) + "/" + segments + "] " + p,
                    "[segment " + (i + 1) + "/" + segments + "] " + r,
                    entry.getCreatedAt(),
                    entry.getSessionId()
            ));
        }
        return out;
    }

    public List<SelectedEntry> explodeLargeEntriesToSegments(List<SelectedEntry> entries, int promptChunkChars, int responseChunkChars) {
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
        String normalized = safe(text, "").trim();
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

    private Strategy chooseStrategy(int total) {
        if (total <= 120) {
            return new Strategy("small", 100, 30, 15, 20, MAX_HASH_LINES_SMALL);
        }
        if (total <= 500) {
            return new Strategy("medium", 140, 35, 20, 35, MAX_HASH_LINES_MEDIUM);
        }
        return new Strategy("large", 180, 40, 20, 40, MAX_HASH_LINES_LARGE);
    }

    private String formatEvidenceBlockCompressed(SelectedEntry e) {
        String id = normalizeId(safe(e.getChatId(), "(unknown)"));
        String createdAt = safe(e.getCreatedAt(), "?");
        String sessionId = safe(e.getSessionId(), "(none)");
        String prompt = compressText(e.getPrompt(), PROMPT_SNIPPET_CHARS);
        String response = compressText(e.getResponse(), RESPONSE_SNIPPET_CHARS);

        return "### Chat " + id + "\n"
                + "- Created At: " + createdAt + "\n"
                + "- Session ID: " + sessionId + "\n"
                + "- Prompt:\n" + prompt + "\n"
                + "- Response:\n" + response;
    }

    private String formatEvidenceBlockFull(SelectedEntry e) {
        String id = normalizeId(safe(e.getChatId(), "(unknown)"));
        String createdAt = safe(e.getCreatedAt(), "?");
        String sessionId = safe(e.getSessionId(), "(none)");
        String prompt = safe(e.getPrompt(), "(empty)").trim();
        String response = safe(e.getResponse(), "(empty)").trim();

        return "### Chat " + id + "\n"
                + "- Created At: " + createdAt + "\n"
                + "- Session ID: " + sessionId + "\n"
                + "- Prompt:\n" + prompt + "\n"
                + "- Response:\n" + response;
    }

    private String buildHashIndex(List<SelectedEntry> sample, int maxLines) {
        StringBuilder out = new StringBuilder();
        int limit = Math.max(0, maxLines);
        int count = 0;

        for (SelectedEntry e : sample) {
            if (count >= limit) {
                out.append("... (hash index truncated at ").append(limit).append(")\n");
                break;
            }
            out.append(normalizeId(safe(e.getChatId(), "(unknown)")))
                    .append("|")
                    .append(sha1Hex(safe(e.getPrompt(), "")))
                    .append('\n');
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
            String id = normalizeId(safe(e.getChatId(), ""));
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
            String id = normalizeId(safe(e.getChatId(), ""));
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
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        int count = 0;
        for (String id : clean) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append(id);
            count++;
            if (count >= max) {
                if (count < clean.size()) {
                    sb.append(", ...");
                }
                break;
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String safeListInt(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        return values.toString();
    }

    private String buildOmittedIds(List<String> omittedIds, int budget) {
        if (budget <= 0) {
            return "";
        }
        List<String> clean = distinctIds(omittedIds);
        if (clean.isEmpty()) {
            return "- (none)\n";
        }

        StringBuilder sb = new StringBuilder();
        int emitted = 0;
        for (String id : clean) {
            if (emitted >= MAX_OMITTED_IDS_LIST) {
                String tail = "... (" + (clean.size() - emitted) + " additional omitted IDs not listed)\n";
                if (sb.length() + tail.length() <= budget) {
                    sb.append(tail);
                }
                break;
            }
            String line = "- " + id + "\n";
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
            return "";
        }

        final int batchSize = 25;
        StringBuilder out = new StringBuilder();

        int total = sample.size();
        int batches = (int) Math.ceil(total / (double) batchSize);

        for (int b = 0; b < batches; b++) {
            int from = b * batchSize;
            int to = Math.min(total, from + batchSize);

            List<SelectedEntry> slice = new ArrayList<>(sample.subList(from, to));
            int pChars = 0;
            int rChars = 0;
            Set<String> matched = new HashSet<>();

            for (SelectedEntry e : slice) {
                String p = safe(e.getPrompt(), "");
                String r = safe(e.getResponse(), "");
                pChars += p.length();
                rChars += r.length();
                matched.addAll(samplingService.matchedTerms(e, terms));
            }

            String line = String.format(
                    Locale.ROOT,
                    "- Batch %d/%d size=%d avgPromptChars=%d avgResponseChars=%d matchedTerms=%s%n",
                    b + 1, batches, slice.size(),
                    slice.isEmpty() ? 0 : (pChars / slice.size()),
                    slice.isEmpty() ? 0 : (rChars / slice.size()),
                    matched
            );

            if (out.length() + line.length() > budget) {
                out.append("... (remaining batch signals omitted due to size)\n");
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
            return "(empty)";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    private String trimTo(String value, int maxChars) {
        if (value == null || maxChars <= 0) {
            return "";
        }
        return value.length() <= maxChars ? value : value.substring(0, maxChars);
    }

    private String safe(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private String sha1Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            final char[] hex = "0123456789abcdef".toCharArray();
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                int v = b & 0xFF;
                sb.append(hex[v >>> 4]).append(hex[v & 0x0F]);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString((value == null ? "" : value).hashCode());
        }
    }

    private static final class Strategy {

        @SuppressWarnings("unused")
        final String name;
        final int topRelevantCount;
        final int newestCount;
        final int oldestCount;
        final int randomCount;
        final int maxHashLines;

        Strategy(String name, int topRelevantCount, int newestCount, int oldestCount, int randomCount, int maxHashLines) {
            this.name = name;
            this.topRelevantCount = topRelevantCount;
            this.newestCount = newestCount;
            this.oldestCount = oldestCount;
            this.randomCount = randomCount;
            this.maxHashLines = maxHashLines;
        }
    }
}
