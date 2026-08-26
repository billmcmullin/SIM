package com.sim.chatserver.web.dashboard.summary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonObject;
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
    private static final String SUMMARY_STORE_KEY = DashboardDailySummaryStore.class.getName();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ensureSummaryStoreInitialized();
        } catch (IllegalStateException e) {
            throw new ServletException("Failed to initialize daily summary store", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (!isLoggedIn(req, resp)) {
                return;
            }

            ZoneId zone = ZoneId.systemDefault();
            LocalDate day = parseDay(ServletRequestParamUtil.firstParamFromValues(req, "day", 256, true, true), zone);
            int slot = parseSlotOrCurrent(ServletRequestParamUtil.firstParamFromValues(req, "slot", 32, true, true), zone);

            DashboardDailySummaryStore store = ensureSummaryStoreInitialized();

            JsonObject payload;
            try {
                payload = store.fetchExactOrLatest(day, slot);
            } catch (IllegalStateException e) {
                log.log(Level.WARNING, "Unable to load dashboard daily summary", e);
                writeJson(resp, HttpServletResponse.SC_OK, errorJson("Unable to load summary."));
                return;
            }
            writeJson(resp, HttpServletResponse.SC_OK,
                    payload == null ? errorJson("Unable to load summary.") : payload);

        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, errorJson("Authentication required."));
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

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        JsonObject safePayload = payload == null ? Json.createObjectBuilder().build() : payload;
        try {
            ServletJsonResponseUtil.writeJson(resp, status, safePayload);
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

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, "Failed sending fallback server error.", ioe);
        }
    }

    private DashboardDailySummaryStore ensureSummaryStoreInitialized() {
        jakarta.servlet.ServletContext context = getServletContext();
        DashboardDailySummaryStore local = (DashboardDailySummaryStore) context.getAttribute(SUMMARY_STORE_KEY);
        if (local != null) {
            return local;
        }

        synchronized (context) {
            DashboardDailySummaryStore existing = (DashboardDailySummaryStore) context.getAttribute(SUMMARY_STORE_KEY);
            if (existing != null) {
                return existing;
            }
            return createAndStoreSummaryStore(context);
        }
    }

    private DashboardDailySummaryStore createAndStoreSummaryStore(jakarta.servlet.ServletContext context) {
        try {
            DashboardDailySummaryStore created = new DashboardDailySummaryStore(dataSourceHolder().getDataSource());
            created.ensureTable();
            context.setAttribute(SUMMARY_STORE_KEY, created);
            return created;
        } catch (IllegalStateException e) {
            log.log(Level.SEVERE, "Unable to initialize DashboardDailySummaryStore", e);
            throw new IllegalStateException("Failed to initialize daily summary store", e);
        }
    }

    private AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

}
