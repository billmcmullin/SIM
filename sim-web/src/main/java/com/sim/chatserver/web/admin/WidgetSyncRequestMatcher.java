package com.sim.chatserver.web.admin;

import jakarta.servlet.http.HttpServletRequest;

final class WidgetSyncRequestMatcher {

    private WidgetSyncRequestMatcher() {
    }

    static boolean matchesPattern(HttpServletRequest request, String pattern) {
        if (request == null || pattern == null || pattern.isBlank() || request.getHttpServletMapping() == null) {
            return false;
        }
        String mappedPattern = request.getHttpServletMapping().getPattern();
        return pattern.equals(mappedPattern);
    }
}
