package com.sim.chatserver.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sim.chatserver.term.TermChatSnapshot;

public final class TermSummary {

    private final Map<String, Integer> termCounts = new LinkedHashMap<>();
    private final Map<String, List<TermChatSnapshot>> termSnapshots = new LinkedHashMap<>();

    public void ensureTerm(String termName) {
        termCounts.putIfAbsent(termName, Integer.valueOf(0));
        termSnapshots.putIfAbsent(termName, new ArrayList<>());
    }

    public void recordMatch(String termName, TermChatSnapshot snapshot) {
        termCounts.merge(termName, Integer.valueOf(1), Integer::sum);
        termSnapshots.computeIfAbsent(termName, k -> new ArrayList<>()).add(snapshot);
    }

    public Map<String, Integer> getTermCounts() {
        return termCounts;
    }

    public Map<String, List<TermChatSnapshot>> getTermSnapshots() {
        return termSnapshots;
    }

    public Map<String, List<TermChatSnapshot>> copyTermSnapshots() {
        Map<String, List<TermChatSnapshot>> copies = new LinkedHashMap<>();
        for (Map.Entry<String, List<TermChatSnapshot>> entry : termSnapshots.entrySet()) {
            copies.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copies;
    }
}
