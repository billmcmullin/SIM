package com.sim.chatserver.service.dashboard;

final class DashboardCacheEntry<T> {

    volatile T value;
    volatile long expiresAt;
    volatile long staleUntil;
    volatile boolean refreshing;

    DashboardCacheEntry(T value, long expiresAt, long staleUntil) {
        this.value = value;
        this.expiresAt = expiresAt;
        this.staleUntil = staleUntil;
        this.refreshing = false;
    }

    static <T> DashboardCacheEntry<T> of(T value, long expiresAt, long staleUntil) {
        return new DashboardCacheEntry<>(value, expiresAt, staleUntil);
    }
}
