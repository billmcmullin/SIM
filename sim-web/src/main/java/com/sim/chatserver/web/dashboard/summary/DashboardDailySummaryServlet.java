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

import jakarta.enterprise.inject.spi.CDI;
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
    private static final Logger log = Logger.getLogger(DashboardDailySummaryServlet.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Object SUMMARY_STORE_LOCK = new Object();

    private static volatile DashboardDailySummaryStore summaryStore;

    @Override
    public void init() throws ServletException {
        super.init();
        ensureSummaryStoreInitialized();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        ZoneId zone = ZoneId.systemDefault();
        RequestParamContext requestContext = RequestParamContext.from(req);
        LocalDate day = parseDay(requestContext.first("day", 256), zone);
        int slot = parseSlotOrCurrent(requestContext.first("slot", 32), zone);

        try {
            DashboardDailySummaryStore store = ensureSummaryStoreInitialized();

            JsonObject payload = store.fetchExactOrLatest(day, slot);
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

    private DashboardDailySummaryStore ensureSummaryStoreInitialized() throws ServletException {
        DashboardDailySummaryStore local = summaryStore;
        if (local != null) {
            return local;
        }

        synchronized (SUMMARY_STORE_LOCK) {
            local = summaryStore;
            if (local != null) {
                return local;
            }

            try {
                DashboardDailySummaryStore created = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
                created.ensureTable();
                summaryStore = created;
                return created;
            } catch (IllegalStateException | IllegalArgumentException e) {
                log.log(Level.SEVERE, "Unable to initialize DashboardDailySummaryStore", e);
                throw new ServletException("Failed to initialize daily summary store", e);
            }
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private static final class RequestParamContext {
        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        private String first(String name, int maxLen) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String value = request.getParameter(name);
            String normalized = normalize(value, maxLen);
            if (normalized != null) {
                return normalized;
            }
            return null;
        }

        private String normalize(String value, int maxLen) {
            if (value == null) {
                return null;
            }
            String normalized = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (normalized.isEmpty()) {
                return null;
            }
            int effectiveMax = maxLen <= 0 ? 256 : maxLen;
            return normalized.length() > effectiveMax ? normalized.substring(0, effectiveMax) : normalized;
        }
    }
}
