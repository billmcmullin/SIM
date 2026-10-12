package com.sim.chatserver.service.dashboard;

import com.sim.chatserver.model.ProgressStat;

public final class DashboardProgressMetrics {

    private final int chatsToday;
    private final int chatsYesterday;
    private final ProgressStat chatsProgression;

    private final int termsToday;
    private final int termsYesterday;
    private final ProgressStat termsProgression;

    DashboardProgressMetrics(int chatsToday, int chatsYesterday, int termsTodayCount, int termsYesterdayCount) {
        this.chatsToday = chatsToday;
        this.chatsYesterday = chatsYesterday;
        this.chatsProgression = new ProgressStat(chatsToday, chatsYesterday);

        this.termsToday = termsTodayCount;
        this.termsYesterday = termsYesterdayCount;
        this.termsProgression = new ProgressStat(termsTodayCount, termsYesterdayCount);
    }

    static DashboardProgressMetrics of(int chatsToday, int chatsYesterday, int termsTodayCount, int termsYesterdayCount) {
        return new DashboardProgressMetrics(chatsToday, chatsYesterday, termsTodayCount, termsYesterdayCount);
    }

    private int getChatsToday() {
        return chatsToday;
    }

    private int getChatsYesterday() {
        return chatsYesterday;
    }

    ProgressStat getChatsProgression() {
        return chatsProgression;
    }

    private int getTermsToday() {
        return termsToday;
    }

    private int getTermsYesterday() {
        return termsYesterday;
    }

    public int chatsToday() {
        return getChatsToday();
    }

    public int chatsYesterday() {
        return getChatsYesterday();
    }

    public int termsToday() {
        return getTermsToday();
    }

    public int termsYesterday() {
        return getTermsYesterday();
    }

    ProgressStat getTermsProgression() {
        return termsProgression;
    }
}
