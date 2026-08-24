package com.sim.chatserver.web.admin;

import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker.WidgetAvailabilityResult;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin endpoint to report widget availability status.
 *
 * URL: /admin/widget-availability.json
 *
 * Response example: { "available": true, "status": "UP", "checkedAt":
 * "2026-06-23T18:02:11.123Z", "latencyMs": 245, "details": "Synthetic check
 * succeeded" }
 */
public class WidgetAvailabilityServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetAvailabilityServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        String user = sessionUser(req);
        String role = sessionRole(req);
        log.info(() -> "Widget availability endpoint invoked: user=" + sanitizeForLog(user)
            + " role=" + sanitizeForLog(role));

        if (!isLoggedIn(req)) {
            log.warning("Widget availability request denied: unauthenticated user");
            writeUnauthorized(resp);
            return;
        }

        try {
            boolean forceRefresh = isTruthy(ServletRequestParamUtil.firstParam(req, "force", 12, true, true));
            boolean runWhenDisabled = isTruthy(ServletRequestParamUtil.firstParam(req, "runWhenDisabled", 12, true, true));
            WidgetAvailabilityResult result = availabilityChecker().checkNow(forceRefresh, runWhenDisabled);
            if (result == null) {
                log.warning(() -> "Widget availability checker returned null result for user=" + sanitizeForLog(user));
                result = new WidgetAvailabilityResult(false, "DOWN", "", 0L, "Availability check returned no result");
            }
            final WidgetAvailabilityResult finalResult = result;

            if ("DISABLED".equalsIgnoreCase(finalResult.status())) {
                log.info(() -> "Widget availability checks disabled by config for user=" + sanitizeForLog(user));
            } else if (!finalResult.available()) {
                log.warning(() -> "Widget availability check result DOWN for user=" + sanitizeForLog(user)
                        + " latencyMs=" + Math.max(0L, finalResult.latencyMs()));
            } else {
                log.info(() -> "Widget availability check result UP for user=" + sanitizeForLog(user)
                        + " latencyMs=" + Math.max(0L, finalResult.latencyMs()));
            }

            JsonObjectBuilder json = Json.createObjectBuilder()
                    .add("available", finalResult.available())
                    .add("status", safe(finalResult.status(), finalResult.available() ? "UP" : "DOWN"))
                    .add("checkedAt", safe(finalResult.checkedAtIso(), ""))
                    .add("latencyMs", Math.max(0L, finalResult.latencyMs()))
                    .add("details", safe(finalResult.details(), ""));

                writeJson(resp, HttpServletResponse.SC_OK, json.build());
        } catch (IllegalStateException | SecurityException e) {
            log.log(Level.WARNING, "Widget availability check failed for user=" + user
                    + " role=" + role, e);

            JsonObjectBuilder json = Json.createObjectBuilder()
                    .add("available", false)
                    .add("status", "DOWN")
                    .add("checkedAt", "")
                    .add("latencyMs", 0)
                    .add("details", "Availability check failed");

            writeJson(resp, HttpServletResponse.SC_OK, json.build());
        }
    
        } catch (Throwable e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    private String sessionUser(HttpServletRequest req) {
        HttpSession session = req == null ? null : req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "anonymous";
        }
        return String.valueOf(session.getAttribute("user"));
    }

    private String sessionRole(HttpServletRequest req) {
        HttpSession session = req == null ? null : req.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            return "unknown";
        }
        return String.valueOf(session.getAttribute("role"));
    }

    private void writeUnauthorized(HttpServletResponse resp) {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("available", false)
                .add("status", "UNAUTHORIZED")
                .add("checkedAt", "")
                .add("latencyMs", 0)
                .add("details", "Authentication required.");

        writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, json.build());
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write widget availability response", e);
            throw new IllegalStateException("Unable to write response", e);
        }
    }

    private String safe(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized)
                || "1".equals(normalized)
                || "yes".equals(normalized)
                || "on".equals(normalized)
                || "y".equals(normalized);
    }

    protected WidgetAvailabilityChecker availabilityChecker() {
        return CDI.current().select(WidgetAvailabilityChecker.class).get();
    }
}
