package com.sim.chatserver.service;

import java.util.Locale;

import com.sim.chatserver.util.CommonLiteralConstants;

public final class ResolvedApiAuth {

    private static final String SOURCE_NONE = "NONE";
        private static final ResolvedApiAuth EMPTY_AUTH =
            new ResolvedApiAuth(null, null, CommonLiteralConstants.HEADER_AUTHORIZATION, SOURCE_NONE);

    private final String rawValue;
    private final String token;
    private final String preferredHeaderName;
    private final String source;

    ResolvedApiAuth(String rawValue, String token, String preferredHeaderName, String source) {
        this.rawValue = rawValue;
        this.token = token;
        this.preferredHeaderName = preferredHeaderName;
        this.source = source == null
            ? CommonLiteralConstants.VALUE_UNKNOWN_UPPER
            : source.toUpperCase(Locale.ROOT);
    }

    public String rawValue() {
        return rawValue;
    }

    public String token() {
        return token;
    }

    public String preferredHeaderName() {
        return preferredHeaderName;
    }

    public String source() {
        return source;
    }

    public boolean hasToken() {
        return token != null && !token.isBlank();
    }

    static ResolvedApiAuth empty() {
        return EMPTY_AUTH;
    }
}
