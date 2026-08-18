package com.sim.chatserver.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sim.chatserver.term.TermChatSnapshot;

/**
 * Shared dashboard view/data models extracted from DashboardServlet.
 */
public final class DashboardViewModels {

    private DashboardViewModels() {
    }

    public static final class WidgetStat {

        private final String widgetId;
        private final String label;
        private final int count;

        // NEW: day-over-day fields for Widget Chat Overview
        private final int todayCount;
        private final int yesterdayCount;
        private final int delta;        // today - yesterday
        private final String direction; // up | down | flat

        /**
         * Backward-compatible constructor.
         */
        public WidgetStat(String widgetId, String label, int count) {
            this(widgetId, label, count, 0, 0);
        }

        /**
         * Enhanced constructor with day-over-day values.
         */
        public WidgetStat(String widgetId, String label, int count, int todayCount, int yesterdayCount) {
            this.widgetId = widgetId;
            this.label = label;
            this.count = count;
            this.todayCount = todayCount;
            this.yesterdayCount = yesterdayCount;
            this.delta = todayCount - yesterdayCount;

            if (delta > 0) {
                this.direction = "up";
            } else if (delta < 0) {
                this.direction = "down";
            } else {
                this.direction = "flat";
            }
        }

        public String getWidgetId() {
            return widgetId;
        }

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }

        public int getTodayCount() {
            return todayCount;
        }

        public int getYesterdayCount() {
            return yesterdayCount;
        }

        public int getDelta() {
            return delta;
        }

        public String getDirection() {
            return direction;
        }

        ProgressStat getProgression() {
            return new ProgressStat(todayCount, yesterdayCount);
        }
    }

    public static final class ProgressStat {

        private final int today;
        private final int yesterday;
        private final int delta;
        private final double pctDelta;
        private final String direction; // up | down | flat

        public ProgressStat(int today, int yesterday) {
            this.today = today;
            this.yesterday = yesterday;
            this.delta = today - yesterday;
            this.pctDelta = yesterday == 0
                    ? (today > 0 ? 100.0 : 0.0)
                    : ((today - yesterday) * 100.0) / yesterday;

            if (delta > 0) {
                this.direction = "up";
            } else if (delta < 0) {
                this.direction = "down";
            } else {
                this.direction = "flat";
            }
        }

        public int getToday() {
            return today;
        }

        public int getYesterday() {
            return yesterday;
        }

        public int getDelta() {
            return delta;
        }

        public double getPctDelta() {
            return pctDelta;
        }

        public String getDirection() {
            return direction;
        }
    }

    public static final class TermDayCount {

        private int today;
        private int yesterday;

        public int getToday() {
            return today;
        }

        public void incToday() {
            this.today++;
        }

        public int getYesterday() {
            return yesterday;
        }

        public void incYesterday() {
            this.yesterday++;
        }
    }

    public static final class TopTopic {

        private final String label;
        private final int today;
        private final int yesterday;
        private final int total;

        public TopTopic(String label, int today, int yesterday) {
            this.label = label;
            this.today = today;
            this.yesterday = yesterday;
            this.total = today + yesterday;
        }

        public String getLabel() {
            return label;
        }

        public int getToday() {
            return today;
        }

        public int getYesterday() {
            return yesterday;
        }

        public int getTotal() {
            return total;
        }
    }

    public static final class OtherParasoftEntry {

        private final String widgetId;
        private final String widgetName;
        private final String prompt;
        private final String sessionId;
        private final Timestamp createdAt;

        public OtherParasoftEntry(String widgetId, String widgetName, String prompt, String sessionId, Timestamp createdAt) {
            this.widgetId = widgetId;
            this.widgetName = widgetName;
            this.prompt = prompt;
            this.sessionId = sessionId;
            this.createdAt = createdAt;
        }

        String getWidgetId() {
            return widgetId;
        }

        public String getWidgetName() {
            return widgetName;
        }

        public String getPrompt() {
            return prompt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }
    }

    // DashboardViewModels.java (only SessionOverview block shown updated)
// Replace your existing SessionOverview class with this version.
    public static final class SessionOverview {

        private final List<SessionStat> topSessions;
        private final SessionTimeline timeline;
        private final int totalUsers;
        private final int activeUsers;
        private final int inactiveUsers;
        private final int activeDays;

        // Existing enhancement fields
        private final int newSessionsToday;
        private final int newSessionsYesterday;
        private final ProgressStat newSessionsProgression;

        // NEW for Top 10 Sessions active-users delta styling
        private final int activeUsersYesterday;
        private final ProgressStat activeUsersProgression;

        /**
         * Backward-compatible constructor.
         */
        SessionOverview(List<SessionStat> topSessions, SessionTimeline timeline,
                int totalUsers, int activeUsers, int inactiveUsers, int activeDays) {
            this(
                    topSessions,
                    timeline,
                    totalUsers,
                    activeUsers,
                    inactiveUsers,
                    activeDays,
                    0,
                    0,
                    new ProgressStat(0, 0),
                    0,
                    new ProgressStat(0, 0)
            );
        }

        /**
         * Existing enhanced constructor (new sessions metrics only).
         */
        public SessionOverview(List<SessionStat> topSessions, SessionTimeline timeline,
                int totalUsers, int activeUsers, int inactiveUsers, int activeDays,
                int newSessionsToday, int newSessionsYesterday, ProgressStat newSessionsProgression) {
            this(
                    topSessions,
                    timeline,
                    totalUsers,
                    activeUsers,
                    inactiveUsers,
                    activeDays,
                    newSessionsToday,
                    newSessionsYesterday,
                    newSessionsProgression,
                    0,
                    new ProgressStat(0, 0)
            );
        }

        /**
         * Full constructor with active-users day-over-day metrics.
         */
        public SessionOverview(List<SessionStat> topSessions, SessionTimeline timeline,
                int totalUsers, int activeUsers, int inactiveUsers, int activeDays,
                int newSessionsToday, int newSessionsYesterday, ProgressStat newSessionsProgression,
                int activeUsersYesterday, ProgressStat activeUsersProgression) {
            this.topSessions = List.copyOf(topSessions);
            this.timeline = timeline;
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.activeDays = activeDays;

            this.newSessionsToday = newSessionsToday;
            this.newSessionsYesterday = newSessionsYesterday;
            this.newSessionsProgression = newSessionsProgression == null
                    ? new ProgressStat(newSessionsToday, newSessionsYesterday)
                    : newSessionsProgression;

            this.activeUsersYesterday = activeUsersYesterday;
            this.activeUsersProgression = activeUsersProgression == null
                    ? new ProgressStat(activeUsers, activeUsersYesterday)
                    : activeUsersProgression;
        }

        public List<SessionStat> getTopSessions() {
            return topSessions;
        }

        public SessionTimeline getTimeline() {
            return timeline;
        }

        public int getTotalUsers() {
            return totalUsers;
        }

        public int getActiveUsers() {
            return activeUsers;
        }

        public int getInactiveUsers() {
            return inactiveUsers;
        }

        int getActiveDays() {
            return activeDays;
        }

        public int getNewSessionsToday() {
            return newSessionsToday;
        }

        public int getNewSessionsYesterday() {
            return newSessionsYesterday;
        }

        public ProgressStat getNewSessionsProgression() {
            return newSessionsProgression;
        }

        int getActiveUsersYesterday() {
            return activeUsersYesterday;
        }

        ProgressStat getActiveUsersProgression() {
            return activeUsersProgression;
        }
    }

    public static final class SessionStat {

        private final String sessionId;
        private final int count;
        private final String lastEntry;

        public SessionStat(String sessionId, int count, String lastEntry) {
            this.sessionId = sessionId;
            this.count = count;
            this.lastEntry = lastEntry;
        }

        public String getSessionId() {
            return sessionId;
        }

        public int getCount() {
            return count;
        }

        public String getLastEntry() {
            return lastEntry;
        }
    }

    public static final class SessionTimeline {

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

    public static final class SessionAccumulator {

        private int count;
        private Timestamp lastEntry;

        public int getCount() {
            return count;
        }

        public void addCount(int delta) {
            this.count += delta;
        }

        public Timestamp getLastEntry() {
            return lastEntry;
        }

        public void setLastEntry(Timestamp lastEntry) {
            this.lastEntry = lastEntry;
        }
    }

    public static final class TermSummary {

        private final Map<String, Integer> termCounts = new LinkedHashMap<>();
        private final Map<String, List<TermChatSnapshot>> termSnapshots = new LinkedHashMap<>();

        public void ensureTerm(String termName) {
            termCounts.putIfAbsent(termName, 0);
            termSnapshots.putIfAbsent(termName, new ArrayList<>());
        }

        public void recordMatch(String termName, TermChatSnapshot snapshot) {
            termCounts.merge(termName, 1, Integer::sum);
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

    public static final class CacheValue<T> {

        private final T value;
        private final long expiresAt;

        CacheValue(T value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        public static <T> CacheValue<T> of(T value, long expiresAt) {
            return new CacheValue<>(value, expiresAt);
        }

        public T getValue() {
            return value;
        }

        long getExpiresAt() {
            return expiresAt;
        }

        public boolean isExpired(long now) {
            return now >= expiresAt;
        }
    }
}
