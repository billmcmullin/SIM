package com.sim.chatserver.model;

public final class CacheValue<T> {

    private final T value;
    private final long expiresAt;

    CacheValue(T value, long expiresAt) {
        this.value = value;
        this.expiresAt = expiresAt;
    }

    public static <T> CacheValue<T> of(T value, long expiresAt) {
        return new CacheValue<>(value, expiresAt);
    }

    T getValue() {
        return value;
    }

    long getExpiresAt() {
        return expiresAt;
    }

    boolean isExpired(long now) {
        return now >= expiresAt;
    }
}
