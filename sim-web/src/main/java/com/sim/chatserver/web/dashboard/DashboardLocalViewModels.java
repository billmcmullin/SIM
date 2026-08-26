package com.sim.chatserver.web.dashboard;

import java.util.List;

import com.sim.chatserver.model.DashboardViewModels.SessionStat;
import com.sim.chatserver.model.DashboardViewModels.SessionTimeline;

final class DashboardLocalViewModels {

    private DashboardLocalViewModels() {
    }

    static final class ProgressStat {

        private final int today;
        private final int yesterday;
        private final int delta;
        private final double pctDelta;
        private final String direction;

        ProgressStat(int today, int yesterday) {
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

        int getToday() {
            return today;
        }

        int getYesterday() {
            return yesterday;
        }

        int getDelta() {
            return delta;
        }

        double getPctDelta() {
            return pctDelta;
        }

        String getDirection() {
            return direction;
        }
    }

    static final class SessionOverview {

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

        SessionOverview(
                List<SessionStat> topSessions,
                SessionTimeline timeline,
                int totalUsers,
                int activeUsers,
                int inactiveUsers,
                int activeDays,
                int newSessionsToday,
                int newSessionsYesterday,
                ProgressStat newSessionsProgression,
                int activeUsersYesterday,
                ProgressStat activeUsersProgression
        ) {
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

        List<SessionStat> getTopSessions() {
            return topSessions;
        }

        SessionTimeline getTimeline() {
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
}
