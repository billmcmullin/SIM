package com.sim.chatserver.web.dashboard.widgets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.util.DashboardTemplateRenderer;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetTableViewServlet", urlPatterns = {"/dashboard/widgets/view"})
public class WidgetTableViewServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetTableViewServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/widget_table_view.html";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Pattern WIDGET_ID_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        String widgetId = ServletRequestParamUtil.firstParam(req, "widgetId", 128, true, true);
        if (widgetId == null || widgetId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
            return;
        }
        if (!WIDGET_ID_PATTERN.matcher(widgetId).matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid widgetId format.");
            return;
        }

        // Optional date filter from dashboard links
        String dateRaw = ServletRequestParamUtil.firstParam(req, "date", 128, true, true);
        LocalDate selectedDate = null;
        if (dateRaw != null && !dateRaw.isBlank()) {
            try {
                selectedDate = LocalDate.parse(dateRaw.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                log.log(Level.FINE, "Invalid date parameter for widget table view: {0}", sanitizeForLog(dateRaw));
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date format. Expected YYYY-MM-DD.");
                return;
            }
        }

        WidgetEntry target = null;
        try {
            List<WidgetEntry> widgets = WidgetStore.list(null);
            for (WidgetEntry widget : widgets) {
                if (widget != null && widgetId.equals(widget.getWidgetId())) {
                    target = widget;
                    break;
                }
            }
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            log.log(Level.WARNING, "Unable to list widgets", e);
        }

        String widgetName = target == null || target.getDisplayName() == null || target.getDisplayName().isBlank()
                ? widgetId
                : target.getDisplayName();

        String selectedDateText = selectedDate == null ? "" : selectedDate.format(DATE_FMT);
        String selectedDateLabel;
        if (selectedDate == null) {
            selectedDateLabel = "All dates";
        } else {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            if (selectedDate.equals(today)) {
                selectedDateLabel = "Today (" + selectedDateText + ')';
            } else if (selectedDate.equals(today.minusDays(1))) {
                selectedDateLabel = "Yesterday (" + selectedDateText + ')';
            } else {
                selectedDateLabel = selectedDateText;
            }
        }

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        if (template == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Template not found: " + TEMPLATE_PATH);
            return;
        }

        String userName = safeSessionAttribute(session, "user", "");
        String role = safeSessionAttribute(session, "role", "USER");

        String rendered = template
        .replace("${user}", DashboardTemplateRenderer.escapeHtml(userName))
        .replace("${role}", DashboardTemplateRenderer.escapeHtml(role))
        .replace("${contextPath}", DashboardTemplateRenderer.escapeHtml(ServletPathUtil.safeContextPathStrict(req.getContextPath())))
        .replace("${widgetId}", DashboardTemplateRenderer.escapeHtml(widgetId))
        .replace("${widgetName}", DashboardTemplateRenderer.escapeHtml(widgetName))
        .replace("${selectedDate}", DashboardTemplateRenderer.escapeHtml(selectedDateText)) // raw YYYY-MM-DD for JS/API calls
        .replace("${selectedDateLabel}", DashboardTemplateRenderer.escapeHtml(selectedDateLabel));   // human display label

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        resp.getOutputStream().write(rendered.getBytes(StandardCharsets.UTF_8));
    
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger(getClass().getName())
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private String loadTemplate(jakarta.servlet.ServletContext context, String path) {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
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
            log.log(Level.WARNING, "Unable to load template: " + path, e);
            return null;
        }
    }

    private String safeSessionAttribute(HttpSession session, String name, String fallback) {
        if (session == null || name == null || name.isBlank()) {
            return fallback == null ? "" : fallback;
        }
        Object value = session.getAttribute(name);
        if (value instanceof String text) {
            String trimmed = text.trim();
            return trimmed.isEmpty() ? (fallback == null ? "" : fallback) : trimmed;
        }
        return fallback == null ? "" : fallback;
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

}
