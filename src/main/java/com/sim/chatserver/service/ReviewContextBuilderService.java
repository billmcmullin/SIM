package com.sim.chatserver.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.sim.chatserver.model.SelectedEntry;

/**
 * Builds high-signal compressed context for selected chat entries.
 *
 * Updated behavior for manual-message reporting: - Prioritizes real
 * prompt/response evidence blocks - Keeps a small hash index only for
 * traceability - Avoids metadata-only context output
 */
public class ReviewContextBuilderService {

    private static final int DEFAULT_MAX_CONTEXT_CHARS = 3400;
    private static final int MAX_TERMS = 12;

    // Per-entry evidence truncation
    private static final int PROMPT_SNIPPET_CHARS = 700;
    private static final int RESPONSE_SNIPPET_CHARS = 900;

    // Keep hash lines minimal (traceability only)
    private static final int MAX_HASH_LINES_SMALL = 30;
    private static final int MAX_HASH_LINES_MEDIUM = 20;
    private static final int MAX_HASH_LINES_LARGE = 12;

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
                entries,
                userMessage,
                strategy.topRelevantCount,
                strategy.newestCount,
                strategy.oldestCount,
                strategy.randomCount
        );

        StringBuilder sb = new StringBuilder();

        appendWithinLimit(sb, "Selected chats context\n", maxChars);
        appendWithinLimit(sb, "- total_selected: " + total + "\n", maxChars);
        appendWithinLimit(sb, "- sampled_for_evidence: " + sample.size() + "\n", maxChars);
        appendWithinLimit(sb, "- omitted_from_inline_evidence: " + Math.max(0, total - sample.size()) + "\n", maxChars);
        appendWithinLimit(sb, "- strategy: " + strategy.name + "\n", maxChars);
        appendWithinLimit(sb, "- query_terms: " + terms + "\n", maxChars);

        // Small traceability index (not primary content)
        appendWithinLimit(sb, "\nCoverage index (sampled subset):\n", maxChars);
        appendWithinLimit(sb, buildHashIndex(sample, strategy.maxHashLines), maxChars);

        // Primary analysis evidence: real chat content
        appendWithinLimit(sb, "\nPer-chat evidence:\n", maxChars);
        int omittedBlocks = 0;
        for (SelectedEntry e : sample) {
            String block = formatEvidenceBlock(e);
            if (!appendWithinLimit(sb, block + "\n\n", maxChars)) {
                omittedBlocks++;
            }
        }
        if (omittedBlocks > 0) {
            appendWithinLimit(sb, "... (" + omittedBlocks + " sampled chat blocks truncated by size)\n", maxChars);
        }

        // Optional compact batch signals if room remains
        if (sb.length() < maxChars - 200) {
            appendWithinLimit(sb, "\nBatch signals:\n", maxChars);
            appendWithinLimit(sb, buildBatchSignals(sample, terms, maxChars - sb.length()), maxChars);
        }

        return trimTo(sb.toString(), maxChars);
    }

    private Strategy chooseStrategy(int total) {
        if (total <= 150) {
            return new Strategy("small", 55, 25, 15, 15, MAX_HASH_LINES_SMALL);
        }
        if (total <= 600) {
            return new Strategy("medium", 60, 20, 10, 20, MAX_HASH_LINES_MEDIUM);
        }
        // large
        return new Strategy("large", 65, 20, 10, 25, MAX_HASH_LINES_LARGE);
    }

    private String formatEvidenceBlock(SelectedEntry e) {
        String id = safe(e.getChatId(), "(unknown)");
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

    private String buildHashIndex(List<SelectedEntry> sample, int maxLines) {
        StringBuilder out = new StringBuilder();
        int limit = Math.max(0, maxLines);
        int count = 0;

        for (SelectedEntry e : sample) {
            if (count >= limit) {
                out.append("... (hash index truncated at ").append(limit).append(")\n");
                break;
            }
            out.append(safe(e.getChatId(), "(unknown)"))
                    .append("|")
                    .append(sha1Hex(safe(e.getPrompt(), "")))
                    .append('\n');
            count++;
        }
        return out.toString();
    }

    private String buildBatchSignals(List<SelectedEntry> sample, List<String> terms, int budget) {
        if (sample.isEmpty() || budget <= 0) {
            return "";
        }

        final int batchSize = 20;
        StringBuilder out = new StringBuilder();

        int total = sample.size();
        int batches = (int) Math.ceil(total / (double) batchSize);

        for (int b = 0; b < batches; b++) {
            int from = b * batchSize;
            int to = Math.min(total, from + batchSize);

            List<SelectedEntry> slice = new ArrayList<>(sample.subList(from, to));
            int pChars = 0;
            int rChars = 0;
            Set<String> matched = new java.util.HashSet<>();

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

    private String sha1Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "sha1_error";
        }
    }

    private static final class Strategy {

        private final String name;
        private final int topRelevantCount;
        private final int newestCount;
        private final int oldestCount;
        private final int randomCount;
        private final int maxHashLines;

        private Strategy(String name, int topRelevantCount, int newestCount, int oldestCount, int randomCount, int maxHashLines) {
            this.name = name;
            this.topRelevantCount = topRelevantCount;
            this.newestCount = newestCount;
            this.oldestCount = oldestCount;
            this.randomCount = randomCount;
            this.maxHashLines = maxHashLines;
        }
    }
}
