package com.sim.chatserver.web.dashboard.sessions;

import com.sim.chatserver.util.SessionLabel;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardSessionAggregationQueryService;
import com.sim.chatserver.service.dashboard.SessionAccumulatorData;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.SessionLabelStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardSessionNamesJsonServlet", urlPatterns = {"/dashboard/session-names.json"})
public class DashboardSessionNamesJsonServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardSessionNamesJsonServlet.class.getCanonicalName());
    private static final int DEFAULT_LIMIT = 10;
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;
    private static final DashboardSessionAggregationQueryService QUERY_SERVICE =
            new DashboardSessionAggregationQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        String contextPath = ServletPathUtil.safeContextPathStrict(req.getServletContext().getContextPath());
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.TEXT_UNAUTHORIZED).build());
            return;
        }

        String query = ServletRequestParamUtil.firstParam(req, "q", 256, true, true);
        if (query == null || query.isBlank()) {
            query = ServletRequestParamUtil.firstParam(req, CommonLiteralConstants.PARAM_SEARCH, 256, true, true);
        }

        boolean labeledOnly = com.sim.chatserver.util.CommonLiteralConstants.VALUE_TRUE.equalsIgnoreCase(ServletRequestParamUtil.firstParam(req, "labeledOnly", 16, true, true));

        int limit = parsePositiveInteger(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT, 32, true, true), DEFAULT_LIMIT);
        int page = parsePositiveInteger(ServletRequestParamUtil.firstParam(req, CommonLiteralConstants.PARAM_PAGE, 32, true, true), 1);
        int offset = parseNonNegativeInteger(ServletRequestParamUtil.firstParam(req, "offset", 32, true, true), -1);

        if (offset >= 0) {
            page = (offset / limit) + 1;
        }

        List<WidgetEntry> widgets = List.of();
        try {
            widgets = WidgetStore.list(null);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Unable to list widgets for session catalog", e);
        }

        try {
            Map<String, SessionAccumulatorData> accumulators =
                QUERY_SERVICE.collectAccumulators(widgets, query);

            Map<String, SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());

            List<Map.Entry<String, SessionAccumulatorData>> sorted = new ArrayList<>(accumulators.entrySet());
            sorted.sort(
                Comparator.<Map.Entry<String, SessionAccumulatorData>>comparingInt(e -> e.getValue().count).reversed()
                            .thenComparing(Map.Entry::getKey));

            if (labeledOnly) {
                sorted.removeIf(entry -> {
                    if (entry == null || entry.getKey() == null) {
                        return true;
                    }
                    SessionLabel label = labels.get(entry.getKey());
                    if (label == null) {
                        return true;
                    }
                    boolean hasName = label.getDisplayName() != null && !label.getDisplayName().isBlank();
                    boolean hasEmail = label.getEmail() != null && !label.getEmail().isBlank();
                    return !(hasName || hasEmail);
                });
            }

            int totalSessions = sorted.size();
            int totalPages = Math.max(1, (totalSessions + limit - 1) / limit);

            if (page > totalPages) {
                page = totalPages;
            }

            int start = Math.max(0, (page - 1) * limit);
            int end = Math.min(start + limit, totalSessions);

            List<Map.Entry<String, SessionAccumulatorData>> pageSlice = sorted.subList(start, end);

            JsonArrayBuilder sessions = Json.createArrayBuilder();
            for (Map.Entry<String, SessionAccumulatorData> entry : pageSlice) {
                SessionAccumulatorData acc = entry.getValue();
                SessionLabel label = labels.get(entry.getKey());
                String displayLabel = SessionLabelStore.resolveDisplayLabel(entry.getKey(), label);

                JsonObjectBuilder builder = Json.createObjectBuilder()
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSION_ID, entry.getKey())
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_DISPLAY_LABEL, displayLabel)
                        .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_COUNT, acc.count)
                    .add("lastEntry", formatTimestamp(acc.lastEntry))
                    .add("reviewUrl", buildReviewUrl(contextPath, entry.getKey()))
                        .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_DISPLAY_NAME, label == null ? "" : nullSafe(label.getDisplayName()))
                        .add("email", label == null ? "" : nullSafe(label.getEmail()));
                sessions.add(builder);
            }

            JsonObject payload = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                    .add("labeledOnly", labeledOnly)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL, totalSessions)
                    .add("totalSessions", totalSessions)
                    .add(CommonLiteralConstants.PARAM_PAGE, page)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_LIMIT, limit)
                    .add("totalPages", totalPages)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_SESSIONS, sessions)
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, payload);

        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to collect session catalog", e);
            JsonObject error = Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, "Unable to load session catalog")
                    .build();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
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

    private String nullSafe(String v) {
        return v == null ? "" : v;
    }

    private String buildReviewUrl(String contextPath, String sessionId) {
        return contextPath + "/dashboard/sessions/drilldown/session-review?sessionId="
                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? com.sim.chatserver.util.CommonLiteralConstants.VALUE_NA : ISO_INSTANT_FMT.format(value.toInstant());
    }

    private int parsePositiveInteger(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid positive integer parameter", e);
            return fallback;
        }
    }

    private int parseNonNegativeInteger(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid non-negative integer parameter", e);
            return fallback;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, body);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write session-names JSON response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
