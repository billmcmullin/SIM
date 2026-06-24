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
        if (!isLoggedIn(req)) {
            writeUnauthorized(resp);
            return;
        }

        try {
            WidgetAvailabilityResult result = checker.checkNow();

            JsonObjectBuilder json = Json.createObjectBuilder()
                    .add("available", result.available())
                    .add("status", safe(result.status(), result.available() ? "UP" : "DOWN"))
                    .add("checkedAt", safe(result.checkedAtIso(), ""))
                    .add("latencyMs", Math.max(0L, result.latencyMs()))
                    .add("details", safe(result.details(), ""));

            writeJson(resp, HttpServletResponse.SC_OK, json.build().toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "Widget availability check failed", e);

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
