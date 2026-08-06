package com.sim.chatserver.web.dashboard.trends;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.util.SqlTimeUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
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

    AppDataSourceHolder dsHolder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        int days = parseDays(ServletRequestParamUtil.firstParam(req, "days", 32, true, true));
        LocalDate end = LocalDate.now(ZoneId.systemDefault());
        LocalDate start = end.minusDays(days - 1);

        Map<LocalDate, Integer> totalDaily = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            totalDaily.put(start.plusDays(i), 0);
        }

        Map<String, Map<LocalDate, Integer>> widgetDaily = new LinkedHashMap<>();
        Map<String, String> widgetNameToId = new LinkedHashMap<>();

        try (Connection conn = dataSourceHolder().getDataSource().getConnection()) {
            List<WidgetEntry> widgets = WidgetStore.list(null);

            for (WidgetEntry widget : widgets) {
                if (widget == null || widget.getWidgetId() == null || widget.getWidgetId().isBlank()) {
                    continue;
                }

                String widgetId = widget.getWidgetId();
                String widgetName = widget.getDisplayName();
                if (widgetName == null || widgetName.isBlank()) {
                    widgetName = widgetId;
                }

                String tableName = sanitizeWidgetTableName(widgetId);
                if (!tableExists(conn, tableName)) {
                    continue;
                }

                Map<LocalDate, Integer> series = new LinkedHashMap<>();
                for (LocalDate d : totalDaily.keySet()) {
                    series.put(d, 0);
                }

                String sql = "SELECT created_at FROM " + quoteIdentifier(tableName)
                        + " WHERE created_at >= ? AND created_at < ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                    ps.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Timestamp ts = SqlTimeUtil.safeTimestamp(rs, "created_at");
                            if (ts == null) {
                                continue;
                            }

                            LocalDate entryDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            if (!totalDaily.containsKey(entryDate)) {
                                continue;
                            }

                            totalDaily.put(entryDate, totalDaily.get(entryDate) + 1);
                            series.put(entryDate, series.get(entryDate) + 1);
                        }
                    }
                }

                widgetDaily.put(widgetName, series);
                widgetNameToId.put(widgetName, widgetId);
            }
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            throw new ServletException("Unable to load trend data", e);
        }

        JsonArrayBuilder labels = Json.createArrayBuilder();
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (Map.Entry<LocalDate, Integer> entry : totalDaily.entrySet()) {
            labels.add(entry.getKey().toString());
            values.add(entry.getValue());
        }

        JsonArrayBuilder widgetSeries = Json.createArrayBuilder();
        for (Map.Entry<String, Map<LocalDate, Integer>> entry : widgetDaily.entrySet()) {
            String widgetName = entry.getKey();
            String widgetId = widgetNameToId.getOrDefault(widgetName, widgetName);

            JsonArrayBuilder widgetValues = Json.createArrayBuilder();
            int widgetTotal = 0;
            for (LocalDate d : totalDaily.keySet()) {
                int count = entry.getValue().getOrDefault(d, 0);
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

        int grandTotal = totalDaily.values().stream().mapToInt(Integer::intValue).sum();
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
    
        } catch (IOException | ServletException | RuntimeException e) {
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

    private AppDataSourceHolder dataSourceHolder() {
        if (dsHolder != null) {
            return dsHolder;
        }
        return CDI.current().select(AppDataSourceHolder.class).get();
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

    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            for (String c : new String[]{tableName, tableName.toUpperCase(), tableName.toLowerCase()}) {
                try (ResultSet rs = meta.getTables(null, null, c, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.FINE, "Unable to inspect table metadata for " + tableName, e);
        }
        return false;
    }

    private String sanitizeWidgetTableName(String widgetId) {
        if (widgetId == null || widgetId.isBlank()) {
            return "widget";
        }
        String normalized = widgetId.trim().replaceAll("[^A-Za-z0-9_]", "_");
        if (normalized.isEmpty()) {
            normalized = "widget";
        }
        if (!Character.isLetter(normalized.charAt(0))) {
            normalized = "w_" + normalized;
        }
        if (normalized.length() > 60) {
            normalized = normalized.substring(0, 60);
        }
        return normalized;
    }

    private String quoteIdentifier(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }

    private String escapeForJs(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

}
