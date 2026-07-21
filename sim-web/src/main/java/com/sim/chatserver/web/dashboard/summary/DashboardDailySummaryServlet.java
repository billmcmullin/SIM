package com.sim.chatserver.web.dashboard.summary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardDailySummaryServlet", urlPatterns = {"/dashboard/daily-summary.json"})
public class DashboardDailySummaryServlet extends HttpServlet {
    // parasoft-suppress SERVLET.CETS "Checked servlet exceptions are handled at endpoint boundaries with safe fallback responses."
    // parasoft-suppress SERVLET.IF "CDI-managed datasource dependency and cached store handle are required and do not retain mutable request state."
    // parasoft-suppress SECURITY.ESD.SIF "Injected datasource holder is framework-managed and not a serialized secret payload."

    private static final Logger log = Logger.getLogger(DashboardDailySummaryServlet.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    AppDataSourceHolder dsHolder;

    private transient DashboardDailySummaryStore summaryStore;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            summaryStore = new DashboardDailySummaryStore(dsHolder.getDataSource());
            summaryStore.ensureTable();
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Unable to initialize DashboardDailySummaryStore", e);
            throw new ServletException("Failed to initialize daily summary store", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDate day = parseDay(firstParam(req, "day"), zone);
        int slot = parseSlotOrCurrent(firstParam(req, "slot"), zone);

        try {
            if (summaryStore == null) {
                summaryStore = new DashboardDailySummaryStore(dsHolder.getDataSource());
                summaryStore.ensureTable();
            }

            JsonObject payload = summaryStore.fetchExactOrLatest(day, slot);
            writeJson(resp, payload == null ? errorJson("Unable to load summary.") : payload);

        } catch (IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, "Unable to load dashboard daily summary", e);
            writeJson(resp, errorJson("Unable to load summary."));
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, errorJson("Authentication required."));
            return false;
        }
        return true;
    }

    private LocalDate parseDay(String raw, ZoneId zone) {
        if (raw == null || raw.isBlank()) {
            return LocalDate.now(zone);
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            log.log(Level.FINE, "Invalid day parameter for dashboard summary");
            return LocalDate.now(zone);
        }
    }

    private int parseSlotOrCurrent(String raw, ZoneId zone) {
        if (raw != null && !raw.isBlank()) {
            try {
                int s = Integer.parseInt(raw.trim());
                if (s >= 0 && s <= 3) {
                    return s;
                }
            } catch (NumberFormatException e) {
                log.log(Level.FINE, "Invalid slot parameter for dashboard summary");
            }
        }

        int hour = java.time.LocalTime.now(zone).getHour();
        if (hour < 6) {
            return 0;
        }
        if (hour < 12) {
            return 1;
        }
        if (hour < 18) {
            return 2;
        }
        return 3;
    }

    private JsonObject errorJson(String message) {
        return Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "Unable to load summary." : message)
                .build();
    }

    private void writeJson(HttpServletResponse resp, JsonObject payload) {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        JsonObject safePayload = payload == null ? Json.createObjectBuilder().build() : payload;
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(safePayload);
        } catch (IOException e) {
            log.log(Level.FINE, "Unable to write dashboard summary response", e);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback dashboard summary error", sendErrorFailure);
            }
        }
    }

    private String firstParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String value = req.getParameter(name);
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
    }
}
