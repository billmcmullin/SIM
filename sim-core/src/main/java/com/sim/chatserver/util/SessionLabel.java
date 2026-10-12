package com.sim.chatserver.util;

public final class SessionLabel {

    private static final String EMPTY = "";

    private final String displayName;
    private final String email;

    SessionLabel(String displayName, String email) {
        this.displayName = displayName == null ? EMPTY : displayName;
        this.email = email == null ? EMPTY : email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }
}
