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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        RequestContext context = RequestContext.from(req);
        String widgetId = context.first("widgetId");
        if (widgetId == null || widgetId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "widgetId required");
            return;
        }
        if (!WIDGET_ID_PATTERN.matcher(widgetId).matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid widgetId format.");
            return;
        }

        // Optional date filter from dashboard links
        String dateRaw = context.first("date");
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

        String userName = String.valueOf(session.getAttribute("user"));
        String role = session.getAttribute("role") == null ? "USER" : session.getAttribute("role").toString();

        String rendered = template
                .replace("${user}", escapeHtml(userName))
                .replace("${role}", escapeHtml(role))
                .replace("${contextPath}", escapeHtml(safeContextPath(req.getContextPath())))
                .replace("${widgetId}", escapeHtml(widgetId))
                .replace("${widgetName}", escapeHtml(widgetName))
                .replace("${selectedDate}", escapeHtml(selectedDateText)) // raw YYYY-MM-DD for JS/API calls
                .replace("${selectedDateLabel}", escapeHtml(selectedDateLabel));   // human display label

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        resp.getOutputStream().write(rendered.getBytes(StandardCharsets.UTF_8));
    }

    private String loadTemplate(jakarta.servlet.ServletContext context, String path) throws IOException {
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
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(ch);
            }
        }
        return escaped.toString();
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private String safeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank()) {
            return "";
        }
        String trimmed = contextPath.trim();
        if (trimmed.isEmpty() || trimmed.charAt(0) != '/' || trimmed.contains("://") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return "";
        }
        return trimmed;
    }

    private static final class RequestContext {
        private static final int MAX_PARAM_LEN = 128;

        private final HttpServletRequest request;

        private RequestContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestContext from(HttpServletRequest req) {
            return new RequestContext(req);
        }

        private String first(String name) {
            if (name == null || name.isBlank() || request == null) {
                return null;
            }
            String value = request.getParameter(name);
            if (value == null) {
                return null;
            }
            String trimmed = value.replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed.length() > MAX_PARAM_LEN ? trimmed.substring(0, MAX_PARAM_LEN) : trimmed;
        }
    }
}
