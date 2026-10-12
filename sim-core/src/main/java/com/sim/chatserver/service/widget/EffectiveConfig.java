package com.sim.chatserver.service.widget;

final class EffectiveConfig {

    String source;
    boolean enabled;
    int checkIntervalSeconds;
    String url;
    String method;
    int timeoutMs;
    String expectField;
    String expectValue;
    String widgetId;

    // Optional synthetic probe request headers
    String requestOrigin;
    String requestReferer;
    String requestUserAgent;
    String requestCookie;
    String apiKeyHeaderName;
    String apiKeyValue;
}
