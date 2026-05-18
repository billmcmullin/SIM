package com.sim.chatserver.web.dashboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
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

    @Inject
    AppDataSourceHolder dsHolder;

    private transient DashboardDailySummaryStore summaryStore;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            summaryStore = new DashboardDailySummaryStore(dsHolder.getDataSource());
            summaryStore.ensureTable();
        } catch (Exception e) {
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
        LocalDate day = parseDay(req.getParameter("day"), zone);
        int slot = resolveCurrentSlot();

        try {
            if (summaryStore == null) {
                summaryStore = new DashboardDailySummaryStore(dsHolder.getDataSource());
                summaryStore.ensureTable();
            }

            JsonObject payload = summaryStore.fetchExactOrLatest(day, slot);
            writeJson(resp, payload.toString());
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load dashboard daily summary", e);
            writeJson(resp, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load summary.")
                    .build()
                    .toString());
        }
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, "{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private LocalDate parseDay(String day, ZoneId zone) {
        if (day == null || day.isBlank()) {
            return LocalDate.now(zone);
        }
        try {
            return LocalDate.parse(day.trim(), DATE_FMT);
        } catch (Exception e) {
            return LocalDate.now(zone);
        }
    }

    private int resolveCurrentSlot() {
        int hour = java.time.LocalTime.now(ZoneId.systemDefault()).getHour();
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

    private void writeJson(HttpServletResponse resp, String body) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(body);
    }
}
