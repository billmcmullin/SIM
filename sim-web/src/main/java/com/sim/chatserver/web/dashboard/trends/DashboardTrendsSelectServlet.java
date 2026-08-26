package com.sim.chatserver.web.dashboard.trends;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardTrendsQueryService;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTrendsSelectServlet", urlPatterns = {"/dashboard/trends/select"})
public class DashboardTrendsSelectServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardTrendsSelectServlet.class.getName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final String JSON_UTF8 = "application/json; charset=UTF-8";
    private static final DashboardTrendsQueryService QUERY_SERVICE = new DashboardTrendsQueryService(log);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }
        JsonObject body = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (body == null || body.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String day = body.getString("day", "").trim();
        String widgetIdFilter = body.getString("widgetId", "").trim();
        if (day.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "day is required (yyyy-MM-dd).");
            return;
        }

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(day);
        } catch (java.time.format.DateTimeParseException ex) {
            log.fine("Invalid day format in trends selection payload.");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid day format. Use yyyy-MM-dd.");
            return;
        }

        List<TermChatSnapshot> snapshots;
        try {
            snapshots = QUERY_SERVICE.collectSnapshotsForDay(targetDate, widgetIdFilter);
        } catch (IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to collect chats for day", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to collect chats for day.");
            return;
        }

        if (snapshots.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "No chats found for selected day.");
            return;
        }

        String label = widgetIdFilter.isBlank()
            ? ("Entry Trends " + day)
            : ("Entry Trends " + widgetIdFilter + ' ' + day);

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                label,
                snapshots,
                req.getContextPath() + "/dashboard/trends"
        );

        if (selectionId == null || selectionId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection.");
            return;
        }

        JsonObject ok = Json.createObjectBuilder()
                .add("status", "ok")
                .add("selectionId", selectionId)
                .add("count", snapshots.size())
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, ok);
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
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

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write trends selection response", ex);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback error response", sendErrorFailure);
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write trends selection response", ex);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback error response", sendErrorFailure);
            }
        }
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }
}
