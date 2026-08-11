package com.sim.chatserver.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sim.chatserver.model.SelectedEntry;

/**
 * Sampling + ranking service to keep context representative at large scale.
 *
 * NOTE: This currently references SelectedEntry from your servlet. Best next
 * step is to move SelectedEntry to a shared model package.
 */
public class ReviewSamplingService {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "into", "about",
            "what", "when", "where", "which", "have", "has", "had", "you", "your",
            "are", "was", "were", "how", "why", "can", "could", "would", "should"
    );

    private final SecureRandom secureRandom = new SecureRandom();

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    final List<String> keywordTerms(String text, int maxTerms) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        int limit = Math.max(1, maxTerms);

        return List.of(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .stream()
                .filter(s -> s.length() >= 3 && !STOP_WORDS.contains(s))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private int scoreEntry(SelectedEntry e, List<String> terms) {
        if (e == null || terms == null || terms.isEmpty()) {
            return 0;
        }
        String hay = (safe(e.getPrompt()) + ' ' + safe(e.getResponse())).toLowerCase(Locale.ROOT);

        int score = 0;
        for (String t : terms) {
            if (hay.contains(t)) {
                score++;
            }
        }
        return score;
    }

    private List<SelectedEntry> topRelevant(List<SelectedEntry> entries, List<String> terms, int n) {
        if (entries == null || entries.isEmpty() || n <= 0) {
            return List.of();
        }

        List<SelectedEntry> copy = new ArrayList<>(entries);
        copy.sort((a, b) -> Integer.compare(scoreEntry(b, terms), scoreEntry(a, terms)));
        return copy.subList(0, Math.min(n, copy.size()));
    }

    private List<SelectedEntry> newest(List<SelectedEntry> entries, int n) {
        if (entries == null || entries.isEmpty() || n <= 0) {
            return List.of();
        }

        List<SelectedEntry> copy = new ArrayList<>(entries);
        copy.sort(Comparator.comparing((SelectedEntry e) -> safe(e.getCreatedAt())).reversed());
        return copy.subList(0, Math.min(n, copy.size()));
    }

    private List<SelectedEntry> oldest(List<SelectedEntry> entries, int n) {
        if (entries == null || entries.isEmpty() || n <= 0) {
            return List.of();
        }

        List<SelectedEntry> copy = new ArrayList<>(entries);
        copy.sort(Comparator.comparing((SelectedEntry e) -> safe(e.getCreatedAt())));
        return copy.subList(0, Math.min(n, copy.size()));
    }

    private List<SelectedEntry> randomSample(List<SelectedEntry> entries, int n) {
        if (entries == null || entries.isEmpty() || n <= 0) {
            return List.of();
        }

        List<SelectedEntry> copy = new ArrayList<>(entries);
        Collections.shuffle(copy, secureRandom);
        return copy.subList(0, Math.min(n, copy.size()));
    }

    /**
     * Stratified sample for large selections: - relevance - newest - oldest -
     * random Then dedup by chatId while preserving insertion order.
     */
    final List<SelectedEntry> stratifiedSample(
            List<SelectedEntry> entries,
            String userMessage,
            int topRelevantCount,
            int newestCount,
            int oldestCount,
            int randomCount
    ) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        List<String> terms = keywordTerms(userMessage, 12);

        List<SelectedEntry> merged = new ArrayList<>();
        merged.addAll(topRelevant(entries, terms, Math.max(0, topRelevantCount)));
        merged.addAll(newest(entries, Math.max(0, newestCount)));
        merged.addAll(oldest(entries, Math.max(0, oldestCount)));
        merged.addAll(randomSample(entries, Math.max(0, randomCount)));

        return dedupeByChatId(merged);
    }

    private List<SelectedEntry> dedupeByChatId(List<SelectedEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        Map<String, SelectedEntry> out = new LinkedHashMap<>();
        int nullKeyCounter = 0;

        for (SelectedEntry e : entries) {
            if (e == null) {
                continue;
            }
            String key = safe(e.getChatId());
            if (key.isBlank()) {
                key = "__null__" + (nullKeyCounter++);
            }
            out.putIfAbsent(key, e);
        }
        return new ArrayList<>(out.values());
    }

    final Set<String> matchedTerms(SelectedEntry e, List<String> terms) {
        Set<String> out = new HashSet<>();
        if (e == null || terms == null || terms.isEmpty()) {
            return out;
        }

        String hay = (safe(e.getPrompt()) + ' ' + safe(e.getResponse())).toLowerCase(Locale.ROOT);
        for (String t : terms) {
            if (hay.contains(t)) {
                out.add(t);
            }
        }
        return out;
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
