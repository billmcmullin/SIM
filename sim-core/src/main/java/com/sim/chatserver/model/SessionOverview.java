package com.sim.chatserver.model;

import java.util.List;

public final class SessionOverview {

    private static final ProgressStat ZERO_PROGRESS = new ProgressStat(0, 0);

    private final List<SessionStat> topSessions;
    private final SessionTimeline timeline;
    private final int totalUsers;
    private final int activeUsers;
    private final int inactiveUsers;
    private final int activeDays;
    private final int newSessionsToday;
    private final int newSessionsYesterday;
    private final ProgressStat newSessionsProgression;
    private final int activeUsersYesterday;
    private final ProgressStat activeUsersProgression;

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
                ZERO_PROGRESS,
                0,
                ZERO_PROGRESS
        );
    }

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
                ZERO_PROGRESS
        );
    }

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

    int getTotalUsers() {
        return totalUsers;
    }

    int getActiveUsers() {
        return activeUsers;
    }

    int getInactiveUsers() {
        return inactiveUsers;
    }

    int getActiveDays() {
        return activeDays;
    }

    int getNewSessionsToday() {
        return newSessionsToday;
    }

    int getNewSessionsYesterday() {
        return newSessionsYesterday;
    }

    ProgressStat getNewSessionsProgression() {
        return newSessionsProgression;
    }

    int getActiveUsersYesterday() {
        return activeUsersYesterday;
    }

    ProgressStat getActiveUsersProgression() {
        return activeUsersProgression;
    }
}
