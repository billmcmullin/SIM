package com.sim.chatserver.web.dashboard;

final class DashboardProgressMetrics {

    private static final DashboardProgressMetrics ZERO = new DashboardProgressMetrics(0, 0, 0, 0);

    private final int chatsToday;
    private final int chatsYesterday;
    private final ProgressStat chatsProgression;

    private final int termsToday;
    private final int termsYesterday;
    private final ProgressStat termsProgression;

    private DashboardProgressMetrics(int chatsToday, int chatsYesterday, Object termsTodayCount, Object termsYesterdayCount) {
        this.chatsToday = chatsToday;
        this.chatsYesterday = chatsYesterday;
        this.chatsProgression = new ProgressStat(chatsToday, chatsYesterday);

        int normalizedTermsToday = termsTodayCount == null ? 0 : Integer.parseInt(termsTodayCount.toString());
        int normalizedTermsYesterday = termsYesterdayCount == null ? 0 : Integer.parseInt(termsYesterdayCount.toString());
        this.termsToday = normalizedTermsToday;
        this.termsYesterday = normalizedTermsYesterday;
        this.termsProgression = new ProgressStat(normalizedTermsToday, normalizedTermsYesterday);
    }

    static DashboardProgressMetrics of(int chatsToday, int chatsYesterday, Object termsTodayCount, Object termsYesterdayCount) {
        return new DashboardProgressMetrics(chatsToday, chatsYesterday, termsTodayCount, termsYesterdayCount);
    }

    static DashboardProgressMetrics fromCore(com.sim.chatserver.service.dashboard.DashboardProgressMetrics core) {
        if (core == null) {
            return ZERO;
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

    ProgressStat getChatsProgression() {
        return chatsProgression;
    }

    int getTermsToday() {
        return termsToday;
    }

    int getTermsYesterday() {
        return termsYesterday;
    }

    ProgressStat getTermsProgression() {
        return termsProgression;
    }
}
