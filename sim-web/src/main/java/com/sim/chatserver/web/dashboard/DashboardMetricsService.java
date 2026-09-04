package com.sim.chatserver.web.dashboard;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sim.chatserver.model.DashboardViewModels.OtherParasoftEntry;
import com.sim.chatserver.model.DashboardViewModels.TopTopic;
import com.sim.chatserver.model.DashboardViewModels.WidgetStat;
import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TextSanitizer;
import com.sim.chatserver.widget.WidgetEntry;

final class DashboardMetricsService {

    private static final Logger LOG = Logger.getLogger(DashboardMetricsService.class.getName());

    static final String OTHER_PARASOFT_LABEL = com.sim.chatserver.service.dashboard.DashboardMetricsService.OTHER_PARASOFT_LABEL;

    private final com.sim.chatserver.service.dashboard.DashboardMetricsService delegate;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private DashboardMetricsService(com.sim.chatserver.service.dashboard.DashboardMetricsService delegate) {
        this.delegate = delegate;
    }

    static DashboardMetricsService create(AppDataSourceHolder dsHolder, com.sim.chatserver.term.TermsStore termsStore, int topTopicLimit) {
        return new DashboardMetricsService(
            com.sim.chatserver.service.dashboard.DashboardMetricsService.createForDashboard(dsHolder, termsStore, topTopicLimit)
        );
    }

    static final class DashboardProgressMetrics {

        private final int chatsToday;
        private final int chatsYesterday;
        private final DashboardLocalViewModels.ProgressStat chatsProgression;

        private final int termsToday;
        private final int termsYesterday;
        private final DashboardLocalViewModels.ProgressStat termsProgression;

        private DashboardProgressMetrics(int chatsToday, int chatsYesterday, Integer termsTodayCount, Integer termsYesterdayCount) {
            this.chatsToday = chatsToday;
            this.chatsYesterday = chatsYesterday;
            this.chatsProgression = new DashboardLocalViewModels.ProgressStat(chatsToday, chatsYesterday);

            int normalizedTermsToday = termsTodayCount == null ? 0 : termsTodayCount.intValue();
            int normalizedTermsYesterday = termsYesterdayCount == null ? 0 : termsYesterdayCount.intValue();
            this.termsToday = normalizedTermsToday;
            this.termsYesterday = normalizedTermsYesterday;
            this.termsProgression = new DashboardLocalViewModels.ProgressStat(normalizedTermsToday, normalizedTermsYesterday);
        }

        static DashboardProgressMetrics of(int chatsToday, int chatsYesterday, Integer termsTodayCount, Integer termsYesterdayCount) {
            return new DashboardProgressMetrics(chatsToday, chatsYesterday, termsTodayCount, termsYesterdayCount);
        }

        private static DashboardProgressMetrics fromCore(com.sim.chatserver.service.dashboard.DashboardMetricsService.DashboardProgressMetrics core) {
            if (core == null) {
                return new DashboardProgressMetrics(0, 0, 0, 0);
            }
            return new DashboardProgressMetrics(
                    core.chatsToday(),
                    core.chatsYesterday(),
                    Integer.valueOf(core.termsToday()),
                    Integer.valueOf(core.termsYesterday())
            );
        }

        int getChatsToday() {
            return chatsToday;
        }

        int getChatsYesterday() {
            return chatsYesterday;
        }

        DashboardLocalViewModels.ProgressStat getChatsProgression() {
            return chatsProgression;
        }

        int getTermsToday() {
            return termsToday;
        }

        int getTermsYesterday() {
            return termsYesterday;
        }

        DashboardLocalViewModels.ProgressStat getTermsProgression() {
            return termsProgression;
        }
    }

    List<WidgetStat> buildWidgetStats(List<WidgetEntry> widgets) {
        return delegate.collectWidgetStats(widgets);
    }

    DashboardLocalViewModels.ProgressStat buildChatProgression(List<WidgetEntry> widgets) {
        return toLocalProgressStat(delegate.collectChatProgression(widgets));
    }

    DashboardLocalViewModels.ProgressStat buildNewUserProgression(List<WidgetEntry> widgets) {
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

    private DashboardLocalViewModels.ProgressStat toLocalProgressStat(
            com.sim.chatserver.model.DashboardViewModels.ProgressStat coreProgress
    ) {
        if (coreProgress == null) {
            return new DashboardLocalViewModels.ProgressStat(0, 0);
        }
        return new DashboardLocalViewModels.ProgressStat(coreProgress.todayValue(), coreProgress.yesterdayValue());
    }

    private boolean matchesAnyPattern(String text, List<Pattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }

        String safeText = TextSanitizer.sanitizeForMatching(text == null ? "" : text);
        for (Pattern pattern : patterns) {
            if (findPatternStart(pattern, safeText) != Integer.MAX_VALUE) {
                return true;
            }
        }

        return false;
    }

    private String resolveBestMatchingLabel(String promptRaw, List<TermDefinition> activeTerms, List<Pattern> compiledPatterns) {
        String sanitizedPrompt = TextSanitizer.sanitizeForMatching(promptRaw == null ? "" : promptRaw);
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
            Matcher matcher = pattern.matcher(text == null ? "" : text);
            return matcher.find() ? matcher.start() : Integer.MAX_VALUE;
        } catch (IllegalStateException ex) {
            LOG.log(Level.FINE, "Topic pattern evaluation failed", ex);
            return Integer.MAX_VALUE;
        }
    }
}
