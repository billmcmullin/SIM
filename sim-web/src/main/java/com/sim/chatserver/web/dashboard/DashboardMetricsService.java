package com.sim.chatserver.web.dashboard;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.model.OtherParasoftEntry;
import com.sim.chatserver.model.TopTopic;
import com.sim.chatserver.model.WidgetStat;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.widget.WidgetEntry;

final class DashboardMetricsService {

    private static final String EMPTY = "";
    private static final String LOG_TOPIC_PATTERN_EVALUATION_FAILED = "Topic pattern evaluation failed";


    private static final Logger LOG = Logger.getLogger(DashboardMetricsService.class.getCanonicalName());
        private static final ProgressStat ZERO_PROGRESS =
            new ProgressStat(0, 0);

    static final String OTHER_PARASOFT_LABEL = com.sim.chatserver.service.dashboard.DashboardMetricsService.OTHER_PARASOFT_LABEL;

    private final com.sim.chatserver.service.dashboard.DashboardMetricsService delegate;

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private DashboardMetricsService(com.sim.chatserver.service.dashboard.DashboardMetricsService delegate) {
        this.delegate = delegate;
    }

    static DashboardMetricsService create(AppDataSourceHolder dsHolder, com.sim.chatserver.term.TermsStore termsStore, int topTopicLimit) {
        return new DashboardMetricsService(
            com.sim.chatserver.service.dashboard.DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit)
        );
    }

    List<WidgetStat> buildWidgetStats(List<WidgetEntry> widgets) {
        return delegate.collectWidgetStats(widgets);
    }

    ProgressStat buildChatProgression(List<WidgetEntry> widgets) {
        return toLocalProgressStat(delegate.collectChatProgression(widgets));
    }

    ProgressStat buildNewUserProgression(List<WidgetEntry> widgets) {
        return toLocalProgressStat(delegate.collectNewUserProgression(widgets));
    }

    DashboardProgressMetrics buildDashboardProgressMetrics(List<WidgetEntry> widgets) {
        return DashboardProgressMetrics.fromCore(delegate.collectDashboardProgressMetrics(widgets));
    }

    List<TopTopic> buildTopTopicsTodayVsYesterday(List<WidgetEntry> widgets) {
        return delegate.collectTopTopicsTodayVsYesterday(widgets);
    }

    List<OtherParasoftEntry> buildLatestOtherParasoftEntries(List<WidgetEntry> widgets, int limit) {
        return delegate.collectLatestOtherParasoftEntries(widgets, limit);
    }

    private ProgressStat toLocalProgressStat(
            com.sim.chatserver.model.ProgressStat coreProgress
    ) {
        if (coreProgress == null) {
            return ZERO_PROGRESS;
        }
        return new ProgressStat(coreProgress.todayValue(), coreProgress.yesterdayValue());
    }

    private boolean matchesAnyPattern(String text, List<Pattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }

        String safeText = TextSanitizer.sanitizeForMatching(text == null ? EMPTY : text);
        for (Pattern pattern : patterns) {
            if (findPatternStart(pattern, safeText) != Integer.MAX_VALUE) {
                return true;
            }
        }

        return false;
    }

    private String resolveBestMatchingLabel(String promptRaw, List<TermDefinition> activeTerms, List<Pattern> compiledPatterns) {
        String sanitizedPrompt = TextSanitizer.sanitizeForMatching(promptRaw == null ? EMPTY : promptRaw);
        int bestStart = Integer.MAX_VALUE;
        String bestTermName = null;

        List<TermDefinition> safeTerms = activeTerms == null ? List.of() : activeTerms;
        List<Pattern> safePatterns = compiledPatterns == null ? List.of() : compiledPatterns;

        int bound = Math.min(safeTerms.size(), safePatterns.size());
        for (int i = 0; i < bound; i++) {
            int candidateStart = findPatternStart(safePatterns.get(i), sanitizedPrompt);
            if (candidateStart >= bestStart) {
                continue;
            }

            bestStart = candidateStart;
            TermDefinition term = safeTerms.get(i);
            bestTermName = term == null ? null : term.getName();
            if (bestStart == 0) {
                break;
            }
        }

        return bestTermName;
    }

    private int findPatternStart(Pattern pattern, String text) {
        if (pattern == null) {
            return Integer.MAX_VALUE;
        }

        try {
            Matcher matcher = pattern.matcher(text == null ? EMPTY : text);
            return matcher.find() ? matcher.start() : Integer.MAX_VALUE;
        } catch (IllegalStateException ex) {
            LOG.log(Level.FINE, LOG_TOPIC_PATTERN_EVALUATION_FAILED, ex);
            return Integer.MAX_VALUE;
        }
    }
}


