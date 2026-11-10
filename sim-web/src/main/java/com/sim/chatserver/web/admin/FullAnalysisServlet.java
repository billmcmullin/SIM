package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.Locale;
import com.sim.chatserver.service.FullAnalysisService;
import com.sim.chatserver.service.InferenceRequestGate;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "FullAnalysisServlet", urlPatterns = "/admin/full-analysis")
public class FullAnalysisServlet extends HttpServlet {
    private static final String SERVICE = FullAnalysisServlet.class.getCanonicalName() + ".service";

    FullAnalysisService service() {
        Object existing = getServletContext().getAttribute(SERVICE);
        if (existing instanceof FullAnalysisService analysis) {
            return analysis;
        }
        synchronized (getServletContext()) {
            existing = getServletContext().getAttribute(SERVICE);
            if (existing instanceof FullAnalysisService analysis) {
                return analysis;
            }
                FullAnalysisService analysis = FullAnalysisSourceStore.createService(this::target);
            getServletContext().setAttribute(SERVICE, analysis);
            return analysis;
        }
    }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String owner = owner(request, response);
        if (owner == null) {
            return;
        }
        try {
            String action = param(request, com.sim.chatserver.util.CommonLiteralConstants.KEY_ACTION, "list");
            FullAnalysisService analysis = service();
            if ("list".equals(action)) {
                write(response, 200, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok").add("today", LocalDate.now().toString())
                    .add("jobs", analysis.list(owner)).build());
            } else {
                String id = param(request, com.sim.chatserver.util.CommonLiteralConstants.FIELD_JOB_ID, "");
                if (!id.matches("[a-fA-F0-9-]{36}")) {
                    throw new IllegalArgumentException("A valid job ID is required.");
                }
                if ("details".equals(action)) {
                    write(response, 200, analysis.page(id, owner, Integer.parseInt(param(request, "offset", "0")), 5));
                } else if ("export".equals(action)) {
                    JsonObject first = analysis.page(id, owner, 0, 5);
                    response.setContentType("application/x-ndjson;charset=UTF-8");
                    response.setHeader("Content-Disposition", "attachment; filename=full-analysis-" + id + ".ndjson");
                    var writer = response.getWriter();
                    writer.println(first.getJsonObject(com.sim.chatserver.util.CommonLiteralConstants.KEY_JOB));
                    int total = first.getInt("total");
                    for (int offset = 0; offset < total; offset += 5) {
                        JsonObject page = offset == 0 ? first : analysis.page(id, owner, offset, 5);
                        for (var item : page.getJsonArray("items")) {
                            writer.println(item);
                        }
                    }
                    writer.println(Json.createObjectBuilder().add("exportComplete", true).add("workItems", total).build());
                } else if (com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS.equals(action)) {
                    write(response, 200, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok").add(com.sim.chatserver.util.CommonLiteralConstants.KEY_JOB, analysis.status(id, owner)).build());
                } else {
                    throw new IllegalArgumentException("Unknown analysis read action.");
                }
            }
        } catch (SecurityException error) {
            error(response, 404, "Job not found.");
        } catch (IllegalArgumentException error) {
            error(response, 400, error.getMessage());
        } catch (IllegalStateException error) {
            error(response, 503, error.getMessage());
        }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String owner = owner(request, response);
        if (owner == null) {
            return;
        }
        if (request.getContentType() == null || !request.getContentType().toLowerCase(Locale.ROOT).startsWith("application/json")
                || "cross-site".equalsIgnoreCase(request.getHeader("Sec-Fetch-Site"))) {
            error(response, 415, "Use a same-origin JSON request for analysis actions.");
            return;
        }
        try {
            String text = ServletRequestParamUtil.readNormalizedBodyTextOrEmptyOnLimit(request.getReader(), 4096);
            JsonObject payload;
            try (var reader = Json.createReader(new StringReader(text))) {
                payload = reader.readObject();
            }
            String action = payload.getString(com.sim.chatserver.util.CommonLiteralConstants.KEY_ACTION, "start");
            if ("start".equals(action)) {
                LocalDate day = LocalDate.parse(payload.getString("day", LocalDate.now().toString()));
                if (day.isAfter(LocalDate.now()) || day.isBefore(LocalDate.of(2000, 1, 1))) {
                    throw new IllegalArgumentException("Choose an analysis day between 2000-01-01 and today.");
                }
                FullAnalysisService.Limits limits = new FullAnalysisService.Limits(
                        payload.getInt("requestBytes", 4096), payload.getInt("inputByteBudget", 2048),
                        payload.getInt("paceSeconds", 10), payload.getInt("requestsPerRun", 50), payload.getInt("minutesPerRun", 15));
                String id = service().start(owner, day, limits);
                write(response, 202, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "accepted").add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_JOB_ID, id).build());
            } else if ("confirmIdle".equals(action)) {
                if (!payload.getBoolean("confirmedIdle", false)) {
                    throw new IllegalArgumentException("Explicitly confirm that upstream is idle after reviewing its logs.");
                }
                if (!payload.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_JOB_ID, "").isBlank()) {
                    service().control(payload.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_JOB_ID), owner, "confirmIdle", true);
                } else {
                    InferenceRequestGate.confirmUpstreamIdle(target().endpoint());
                }
                write(response, 200, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Upstream idle confirmation recorded. No inference request was sent.").build());
            } else {
                String id = payload.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_JOB_ID, "");
                JsonObject status = service().control(id, owner, action, payload.getBoolean("confirmedIdle", false));
                write(response, 200, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok").add(com.sim.chatserver.util.CommonLiteralConstants.KEY_JOB, status).build());
            }
        } catch (SecurityException error) {
            error(response, 404, "Job not found.");
        } catch (IllegalArgumentException | jakarta.json.JsonException | ClassCastException | java.time.DateTimeException error) {
            error(response, 400, "Invalid analysis action or limits: " + error.getMessage());
        } catch (IllegalStateException error) {
            error(response, 503, error.getMessage());
        }
    }

    private FullAnalysisService.Target target() {
        return FullAnalysisSourceStore.target();
    }

    private String owner(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var session = request.getSession(false);
        if (session == null || !(session.getAttribute("user") instanceof String username) || username.isBlank()) {
            error(response, 401, "Authentication required.");
            return null;
        }
        if (!"ADMIN".equalsIgnoreCase(String.valueOf(session.getAttribute("role")))) {
            error(response, 403, "Administrator role required.");
            return null;
        }
        return username;
    }

    private String param(HttpServletRequest request, String name, String fallback) {
        String value = ServletRequestParamUtil.firstParam(request, name, 128, true, true);
        return value == null ? fallback : value;
    }

    private void write(HttpServletResponse response, int status, JsonObject value) throws IOException {
        ServletJsonResponseUtil.writeJson(response, status, value);
    }

    private void error(HttpServletResponse response, int status, String message) throws IOException {
        if (!response.isCommitted()) {
            write(response, status, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "error").add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, message == null ? "Analysis request failed." : message).build());
        }
    }

    @Override public void destroy() {
        Object value = getServletContext().getAttribute(SERVICE);
        if (value instanceof FullAnalysisService analysis) {
            analysis.close();
            getServletContext().removeAttribute(SERVICE);
        }
        super.destroy();
    }
}