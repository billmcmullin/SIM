package com.sim.chatserver.web.dashboard.widgets;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.WidgetTableSelectIdsQueryService;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableSelectIdsServlet", urlPatterns = {"/dashboard/widgets/view/select-ids"})
public class WidgetTableSelectIdsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetTableSelectIdsServlet.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final WidgetTableSelectIdsQueryService QUERY_SERVICE = new WidgetTableSelectIdsQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String widgetId = ServletRequestParamUtil.firstParamFromValues(req, "widgetId", 128, true, true);
        if (widgetId == null || widgetId.isBlank()) {
            sendErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
            return;
        }

        String search = normalize(ServletRequestParamUtil.firstParamFromValues(req, "search", 128, true, true));
        String filterPrompt = normalize(ServletRequestParamUtil.firstParamFromValues(req, "filterPrompt", 128, true, true));
        String filterResponse = normalize(ServletRequestParamUtil.firstParamFromValues(req, "filterResponse", 128, true, true));

        // NEW: optional date filter (YYYY-MM-DD)
        LocalDate selectedDate = null;
        String dateRaw = ServletRequestParamUtil.firstParamFromValues(req, "date", 128, true, true);
        if (dateRaw != null && !dateRaw.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateRaw.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Invalid date format for widget id selection", ex);
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Json.createObjectBuilder()
                        .add("status", "error")
                        .add("message", "Invalid date. Expected YYYY-MM-DD.")
                        .build());
                return;
            }
        }

        try {
            List<String> chatIds = QUERY_SERVICE.selectIds(widgetId, search, filterPrompt, filterResponse, selectedDate);

                writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("total", chatIds.size())
                    .add("chatIds", Json.createArrayBuilder(chatIds))
                    .build());
        } catch (java.util.NoSuchElementException e) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Widget data not found")
                    .build());
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Unable to fetch select-all ids", e);
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to fetch chat IDs")
                    .build());
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void sendErrorSafe(HttpServletResponse resp, int status) {
        try {
            resp.sendError(status);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send servlet error", ex);
            sendFallbackError(resp, status);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        try {
            resp.sendError(status, message);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send servlet error with message", ex);
            sendFallbackError(resp, status);
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write widget id selection response", ex);
            sendFallbackError(resp, status);
        }
    }

    private void sendFallbackError(HttpServletResponse resp, int status) {
        try {
            if (!resp.isCommitted()) {
                resp.sendError(status);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send fallback widget id selection response", ex);
        }
    }

}
