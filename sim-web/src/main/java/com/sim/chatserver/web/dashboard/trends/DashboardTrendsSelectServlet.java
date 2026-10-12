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
import com.sim.chatserver.util.CommonLiteralConstants;
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


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardTrendsSelectServlet", urlPatterns = {"/dashboard/trends/select"})
public class DashboardTrendsSelectServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardTrendsSelectServlet.class.getCanonicalName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final String JSON_UTF8 = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8;
    private static final DashboardTrendsQueryService QUERY_SERVICE = new DashboardTrendsQueryService(log);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
            return;
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return;
        }
        JsonObject body = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (body == null || body.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return;
        }

        String day = body.getString(com.sim.chatserver.util.CommonLiteralConstants.KEY_DAY, "").trim();
        String widgetIdFilter = body.getString(com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, "").trim();
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
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add(com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID, selectionId)
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT, snapshots.size())
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, ok);
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_POST, e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                            .log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
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
        String normalized = value.replace(System.lineSeparator().charAt(0), '_').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), '_');
        return normalized.length() > 120 ? new String(normalized.toCharArray(), 0, 120) : normalized;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
