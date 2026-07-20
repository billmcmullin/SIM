package com.sim.chatserver.web.admin;

import com.sim.chatserver.service.widget.WidgetAvailabilityChecker;
import com.sim.chatserver.service.widget.WidgetAvailabilityChecker.WidgetAvailabilityResult;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Inject
    WidgetAvailabilityChecker checker;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = sessionUser(req);
        String role = sessionRole(req);
        log.info(() -> "Widget availability endpoint invoked: user=" + user
                + " role=" + role
                + " remoteAddr=" + safe(req == null ? null : req.getRemoteAddr(), "unknown"));

        if (!isLoggedIn(req)) {
            log.warning(() -> "Widget availability request denied: unauthenticated remoteAddr="
                    + safe(req == null ? null : req.getRemoteAddr(), "unknown"));
            writeUnauthorized(resp);
            return;
        }

        try {
            WidgetAvailabilityResult result = checker.checkNow();
            if (result == null) {
                log.warning(() -> "Widget availability checker returned null result for user=" + user);
                result = new WidgetAvailabilityResult(false, "DOWN", "", 0L, "Availability check returned no result");
            }
            final WidgetAvailabilityResult finalResult = result;

            if (!finalResult.available()) {
                log.warning(() -> "Widget availability check result DOWN for user=" + user
                        + " details=" + safe(finalResult.details(), "")
                        + " latencyMs=" + Math.max(0L, finalResult.latencyMs()));
            } else {
                log.info(() -> "Widget availability check result UP for user=" + user
                        + " latencyMs=" + Math.max(0L, finalResult.latencyMs()));
            }

            JsonObjectBuilder json = Json.createObjectBuilder()
                    .add("available", finalResult.available())
                    .add("status", safe(finalResult.status(), finalResult.available() ? "UP" : "DOWN"))
                    .add("checkedAt", safe(finalResult.checkedAtIso(), ""))
                    .add("latencyMs", Math.max(0L, finalResult.latencyMs()))
                    .add("details", safe(finalResult.details(), ""));

            writeJson(resp, HttpServletResponse.SC_OK, json.build().toString());
        } catch (RuntimeException e) {
            log.log(Level.WARNING, "Widget availability check failed for user=" + user
                    + " role=" + role, e);

            JsonObjectBuilder json = Json.createObjectBuilder()
                    .add("available", false)
                    .add("status", "DOWN")
                    .add("checkedAt", "")
                    .add("latencyMs", 0)
                    .add("details", "Availability check failed");

            writeJson(resp, HttpServletResponse.SC_OK, json.build().toString());
        }
    }

    private boolean isLoggedIn(HttpServletRequest req) {
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

    private void writeUnauthorized(HttpServletResponse resp) throws IOException {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("available", false)
                .add("status", "UNAUTHORIZED")
                .add("checkedAt", "")
                .add("latencyMs", 0)
                .add("details", "Authentication required.");

        writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, json.build().toString());
    }

    private void writeJson(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body == null ? "{}" : body);
    }

    private String safe(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
