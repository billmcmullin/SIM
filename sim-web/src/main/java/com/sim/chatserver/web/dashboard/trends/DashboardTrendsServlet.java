package com.sim.chatserver.web.dashboard.trends;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.dashboard.DashboardTrendsQueryService;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTrendsServlet", urlPatterns = {"/dashboard/trends"})
public class DashboardTrendsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTrendsServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_trends.html";
    private static final DashboardTrendsQueryService QUERY_SERVICE = new DashboardTrendsQueryService(log);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        int days = parseDays(ServletRequestParamUtil.firstParam(req, "days", 32, true, true));
        LocalDate end = LocalDate.now(java.time.ZoneId.systemDefault());
        LocalDate start = end.minusDays(days - 1);

            DashboardTrendsQueryService.TrendResult trend = QUERY_SERVICE.loadTrendData(start, end);
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

            double widgetAverage = days > 0 ? (double) widgetTotal / days : 0.0;

            widgetSeries.add(Json.createObjectBuilder()
                    .add("name", widgetName)
                    .add("widgetId", widgetId)
                    .add("values", widgetValues)
                    .add("total", widgetTotal)
                    .add("avgPerDay", widgetAverage));
        }

        int grandTotal = 0;
        for (Integer value : totalDaily.values()) {
            grandTotal += safeIntegerValue(value);
        }
        double averagePostsPerDay = days > 0 ? (double) grandTotal / days : 0.0;

        JsonObject trendData = Json.createObjectBuilder()
                .add("labels", labels)
                .add("values", values)
                .add("widgetSeries", widgetSeries)
                .add("days", days)
                .add("totalPosts", grandTotal)
                .add("averagePostsPerDay", averagePostsPerDay)
                .build();

        String template = loadTemplate(req, TEMPLATE_PATH);
        String rendered = template
            .replace("${contextPath}", DashboardTemplateRenderer.escapeHtml(req.getContextPath() == null ? "" : req.getContextPath()))
            .replace("${user}", DashboardTemplateRenderer.escapeHtml(String.valueOf(session.getAttribute("user"))))
                .replace("${selectedDays}", String.valueOf(days))
                .replace("${trendData}", escapeForJs(trendData.toString()));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    
        } catch (IOException | ServletException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unhandled exception in doGet", e);
            sendErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
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

    private int safeIntegerValue(Integer value) {
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
                    builder.append(line).append('\n');
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
        return value.replace("\\", "\\\\").replace("'", "\\'")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

}
