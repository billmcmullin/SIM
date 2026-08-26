package com.sim.chatserver.web.dashboard.sessions;

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

@WebServlet(name = "DashboardSessionNamesJsonServlet", urlPatterns = {"/dashboard/session-names.json"})
public class DashboardSessionNamesJsonServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardSessionNamesJsonServlet.class.getName());
    private static final int DEFAULT_LIMIT = 10;
    private static final DateTimeFormatter ISO_INSTANT_FMT = DateTimeFormatter.ISO_INSTANT;
    private static final DashboardSessionAggregationQueryService QUERY_SERVICE =
            new DashboardSessionAggregationQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        String contextPath = ServletPathUtil.safeContextPathStrict(req.getServletContext().getContextPath());
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    Json.createObjectBuilder().add("status", "unauthorized").build());
            return;
        }

        String query = ServletRequestParamUtil.firstParam(req, "q", 256, true, true);
        if (query == null || query.isBlank()) {
            query = ServletRequestParamUtil.firstParam(req, "search", 256, true, true);
        }

        boolean labeledOnly = "true".equalsIgnoreCase(ServletRequestParamUtil.firstParam(req, "labeledOnly", 16, true, true));

        int limit = parsePositiveInteger(ServletRequestParamUtil.firstParam(req, "limit", 32, true, true), DEFAULT_LIMIT);
        int page = parsePositiveInteger(ServletRequestParamUtil.firstParam(req, "page", 32, true, true), 1);
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
            Map<String, DashboardSessionAggregationQueryService.SessionAccumulatorData> accumulators =
                QUERY_SERVICE.collectAccumulators(widgets, query);

            Map<String, SessionLabelStore.SessionLabel> labels = SessionLabelStore.mapDisplayNames(accumulators.keySet());

            List<Map.Entry<String, DashboardSessionAggregationQueryService.SessionAccumulatorData>> sorted = new ArrayList<>(accumulators.entrySet());
            sorted.sort(
                Comparator.<Map.Entry<String, DashboardSessionAggregationQueryService.SessionAccumulatorData>>comparingInt(e -> e.getValue().count).reversed()
                            .thenComparing(Map.Entry::getKey));

            if (labeledOnly) {
                sorted.removeIf(entry -> {
                    if (entry == null || entry.getKey() == null) {
                        return true;
                    }
                    SessionLabelStore.SessionLabel label = labels.get(entry.getKey());
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

            List<Map.Entry<String, DashboardSessionAggregationQueryService.SessionAccumulatorData>> pageSlice = sorted.subList(start, end);

            JsonArrayBuilder sessions = Json.createArrayBuilder();
            for (Map.Entry<String, DashboardSessionAggregationQueryService.SessionAccumulatorData> entry : pageSlice) {
                DashboardSessionAggregationQueryService.SessionAccumulatorData acc = entry.getValue();
                SessionLabelStore.SessionLabel label = labels.get(entry.getKey());
                String displayLabel = SessionLabelStore.resolveDisplayLabel(entry.getKey(), label);

                JsonObjectBuilder builder = Json.createObjectBuilder()
                        .add("sessionId", entry.getKey())
                        .add("displayLabel", displayLabel)
                        .add("count", acc.count)
                    .add("lastEntry", formatTimestamp(acc.lastEntry))
                    .add("reviewUrl", buildReviewUrl(contextPath, entry.getKey()))
                        .add("displayName", label == null ? "" : nullSafe(label.getDisplayName()))
                        .add("email", label == null ? "" : nullSafe(label.getEmail()));
                sessions.add(builder);
            }

            JsonObject payload = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("labeledOnly", labeledOnly)
                    .add("total", totalSessions)
                    .add("totalSessions", totalSessions)
                    .add("page", page)
                    .add("limit", limit)
                    .add("totalPages", totalPages)
                    .add("sessions", sessions)
                    .build();

            writeJson(resp, HttpServletResponse.SC_OK, payload);

        } catch (SQLException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to collect session catalog", e);
            JsonObject error = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to load session catalog")
                    .build();
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
        }
    
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
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

    private String nullSafe(String v) {
        return v == null ? "" : v;
    }

    private String buildReviewUrl(String contextPath, String sessionId) {
        return contextPath + "/dashboard/sessions/drilldown/session-review?sessionId="
                + URLEncoder.encode(sessionId, StandardCharsets.UTF_8);
    }

    private String formatTimestamp(Timestamp value) {
        return value == null ? "ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â" : ISO_INSTANT_FMT.format(value.toInstant());
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
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
    }

}
