package com.sim.chatserver.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SessionTimeline {

    private final List<String> labels;
    private final Map<String, List<Integer>> countsBySession;

    public SessionTimeline(List<String> labels, Map<String, List<Integer>> countsBySession) {
        this.labels = List.copyOf(labels);
        this.countsBySession = new LinkedHashMap<>(countsBySession);
    }

    public List<String> getLabels() {
        return labels;
    }

    public Map<String, List<Integer>> getCountsBySession() {
        return countsBySession;
    }
}
