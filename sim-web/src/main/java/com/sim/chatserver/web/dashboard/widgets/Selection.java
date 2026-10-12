package com.sim.chatserver.web.dashboard.widgets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.sim.chatserver.term.TermChatSnapshot;

public final class Selection {

    public final String widgetId;
    public final String displayName;
    public final String backUrl;
    public final List<String> chatIds;
    public final SearchTerms searchTerms;
    public final List<TermChatSnapshot> snapshots;
    public final String date;

    Selection(String widgetId,
            String displayName,
            String backUrl,
            List<String> chatIds,
            List<TermChatSnapshot> snapshots,
            SearchTerms searchTerms,
            String date) {
        this.widgetId = widgetId;
        this.displayName = displayName;
        this.backUrl = backUrl;
        this.chatIds = chatIds;
        this.searchTerms = searchTerms;
        this.snapshots = snapshots;
        this.date = date;
    }

    static Selection fromWidget(String widgetId, List<String> chatIds, SearchTerms searchTerms, String date) {
        return new Selection(
                WidgetReviewStartServlet.safe(widgetId),
                WidgetReviewStartServlet.safe(widgetId),
                null,
                new ArrayList<>(chatIds),
                null,
            searchTerms == null ? SearchTerms.EMPTY : searchTerms,
                WidgetReviewStartServlet.normalizeDate(date)
        );
    }

    static Selection fromWidget(String widgetId, List<String> chatIds, SearchTerms searchTerms) {
        return fromWidget(widgetId, chatIds, searchTerms, null);
    }

    static Selection fromTermSnapshots(String displayName, String backUrl, List<TermChatSnapshot> snapshots) {
        List<String> chatIds = snapshots.stream()
                .map(TermChatSnapshot::getChatId)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();

        return new Selection(
                WidgetReviewStartServlet.safe(displayName),
                WidgetReviewStartServlet.safe(displayName),
                WidgetReviewStartServlet.safe(backUrl),
                chatIds,
                new ArrayList<>(snapshots),
            SearchTerms.EMPTY,
                null
        );
    }

    public boolean hasSnapshots() {
        return snapshots != null && !snapshots.isEmpty();
    }

    public String getBackUrl() {
        return backUrl;
    }

    public String getDate() {
        return date;
    }
}
