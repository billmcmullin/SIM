package com.sim.chatserver.web.dashboard.topics;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class TopicsAggregation {

    final Map<String, Integer> globalCounts = new LinkedHashMap<>();
    final Map<String, Set<String>> globalChatIdsByTopic = new LinkedHashMap<>();
    final Map<String, Map<String, Integer>> byWidgetCounts = new LinkedHashMap<>();
    final Map<String, Map<String, Set<String>>> byWidgetChatIds = new LinkedHashMap<>();
    final Set<String> allMatchedChatIds = new LinkedHashSet<>();
    long totalMentions = 0L;
}
