package com.sim.chatserver.web.dashboard.trends;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardTrendsQueryService;
import com.sim.chatserver.service.dashboard.TrendResult;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardTrendsServlet", urlPatterns = {"/dashboard/trends"})
public class DashboardTrendsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTrendsServlet.class.getCanonicalName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_trends.html";
    private static final DashboardTrendsQueryService QUERY_SERVICE = new DashboardTrendsQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            com.sim.chatserver.web.util.ServletPathUtil.forwardToLogin(req, resp, log);
            return;
        }

        int days = parseDays(ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.FIELD_DAYS, 32, true, true));
        LocalDate end = LocalDate.now(java.time.ZoneId.systemDefault());
        LocalDate start = end.minusDays(days - 1);

            TrendResult trend = QUERY_SERVICE.loadTrendData(start, end);
        Map<LocalDate, Integer> totalDaily = trend.totalDaily;
        Map<String, Map<LocalDate, Integer>> widgetDaily = trend.widgetDaily;
        Map<String, String> widgetNameToId = trend.widgetNameToId;

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (Map.Entry<LocalDate, Integer> entry : totalDaily.entrySet()) {
            labels.add(entry.getKey().toString());
            Integer dayCount = entry.getValue();
            int safeCount = safeIntegerValue(dayCount);
            values.add(safeCount);
        }

        JsonArrayBuilder widgetSeries = Json.createArrayBuilder();
        for (Map.Entry<String, Map<LocalDate, Integer>> entry : widgetDaily.entrySet()) {
            String widgetName = entry.getKey();
            String widgetId = widgetNameToId.getOrDefault(widgetName, widgetName);

            JsonArrayBuilder widgetValues = Json.createArrayBuilder();
            int widgetTotal = 0;
            for (LocalDate d : totalDaily.keySet()) {
                Integer rawCount = entry.getValue().get(d);
                int count = safeIntegerValue(rawCount);
                widgetValues.add(count);
                widgetTotal += count;
            }

                double widgetAverage = days > 0
                    ? BigDecimal.valueOf(widgetTotal)
                        .divide(BigDecimal.valueOf(days), 6, RoundingMode.HALF_UP)
                        .doubleValue()
                    : 0.0d;

            widgetSeries.add(Json.createObjectBuilder()
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_NAME, widgetName)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, widgetId)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_VALUES, widgetValues)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_TOTAL, widgetTotal)
                    .add("avgPerDay", widgetAverage));
        }

        int grandTotal = 0;
        for (Integer value : totalDaily.values()) {
            grandTotal += safeIntegerValue(value);
        }
        double averagePostsPerDay = days > 0
            ? BigDecimal.valueOf(grandTotal)
                .divide(BigDecimal.valueOf(days), 6, RoundingMode.HALF_UP)
                .doubleValue()
            : 0.0d;

        JsonObject trendData = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_LABELS, labels)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_VALUES, values)
                .add("widgetSeries", widgetSeries)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_DAYS, days)
                .add("totalPosts", grandTotal)
                .add("averagePostsPerDay", averagePostsPerDay)
                .build();

        String trendDataJson = toJsonText(trendData);
        String currentUser = sessionUserText(session);

        String template = loadTemplate(req, TEMPLATE_PATH);
        String rendered = template
            .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_CONTEXT_PATH, DashboardTemplateRenderer.escapeHtml(req.getContextPath() == null ? "" : req.getContextPath()))
            .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_USER, DashboardTemplateRenderer.escapeHtml(currentUser))
                .replace("${selectedDays}", String.valueOf(days))
                .replace("${trendData}", escapeForJs(trendDataJson));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8);
        byte[] renderedBytes = rendered.getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(renderedBytes.length);
        try (var out = resp.getOutputStream()) {
            out.write(renderedBytes);
        }
    
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.LOG_UNHANDLED_EXCEPTION_IN_DO_GET, e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_HANDLING_FAILED);
        }
    }

    private void sendErrorSafe(HttpServletResponse resp, int status, String message) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.sendError(status, message);
        } catch (IOException ioe) {
            log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_WITH_PERIOD, ioe);
        }
    }

    private String sessionUserText(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object user = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER);
        return user instanceof String ? (String) user : "";
    }

    private String toJsonText(JsonObject payload) {
        if (payload == null) {
            return "{}";
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); JsonWriter writer = Json.createWriter(out)) {
            writer.writeObject(payload);
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.FINE, "Unable to serialize trend payload", ex);
            return "{}";
        }
    }

    private int parseDays(String raw) {
        if (raw == null || raw.isBlank()) {
            return 30;
        }
        String trimmed = raw.trim();
        if (!trimmed.matches("^\\d{1,4}$")) {
            return 30;
        }
        try {
            int d = Integer.parseInt(trimmed);
            return (d == 10 || d == 30 || d == 90 || d == 120 || d == 180) ? d : 30;
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid days parameter", e);
            return 30;
        }
    }

    private int safeIntegerValue(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid integer value", ex);
            return 0;
        }
    }

    private String loadTemplate(HttpServletRequest req, String path) {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, "Template missing: {0}", path);
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
                return builder.toString();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load trends template: " + path, e);
            return "";
        }
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO).replace("'", com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_SINGLE_QUOTE)
                .replace(System.lineSeparator(), com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO).replace(System.lineSeparator(), com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_CARRIAGE_RETURN);
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
