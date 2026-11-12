package com.sim.chatserver.web.dashboard.widgets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.sim.chatserver.term.TermChatSnapshot;

public final class Selection {

    private static final String EMPTY = "";
    private static final String WIDGET_CONTEXT_PREFIX = "Widget: ";
    private static final String DAY_CONTEXT_PREFIX = " | Day: ";

    public final String widgetId;
    public final String displayName;
    public final String backUrl;
    public final List<String> chatIds;
    public final SearchTerms searchTerms;
    public final List<TermChatSnapshot> snapshots;
    public final String date;
    private final String displayContext;

    Selection(String widgetId,
            String displayName,
            String backUrl,
            List<String> chatIds,
            List<TermChatSnapshot> snapshots,
            SearchTerms searchTerms,
            String date) {
        this(widgetId, displayName, backUrl, chatIds, snapshots, searchTerms, date, EMPTY);
    }

    Selection(String widgetId, String displayName, String backUrl, List<String> chatIds,
            List<TermChatSnapshot> snapshots, SearchTerms searchTerms, String date, String displayContext) {
        this.widgetId = widgetId;
        this.displayName = displayName;
        this.backUrl = backUrl;
        this.chatIds = chatIds;
        this.searchTerms = searchTerms;
        this.snapshots = snapshots;
        this.date = date;
        this.displayContext = normalizeDisplayContext(displayContext);
    }

    public Selection withDisplayContext(String value) {
        return new Selection(widgetId, displayName, backUrl, chatIds, snapshots, searchTerms, date, value);
    }

    public String getDisplayContext() {
        return displayContext;
    }

    private static String normalizeDisplayContext(String value) {
        if (value == null || value.length() > 512 || value.chars().anyMatch(Character::isISOControl)) return EMPTY;
        return value.trim();
    }

    static Selection fromWidget(String widgetId, List<String> chatIds, SearchTerms searchTerms, String date) {
        Selection selection = new Selection(
                WidgetReviewStartServlet.safe(widgetId),
                WidgetReviewStartServlet.safe(widgetId),
                null,
                new ArrayList<>(chatIds),
                null,
            searchTerms == null ? SearchTerms.EMPTY : searchTerms,
                WidgetReviewStartServlet.normalizeDate(date)
        );
            return selection.withDisplayContext(WIDGET_CONTEXT_PREFIX + selection.displayName
                + (selection.date == null || selection.date.isEmpty() ? EMPTY : DAY_CONTEXT_PREFIX + selection.date));
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
