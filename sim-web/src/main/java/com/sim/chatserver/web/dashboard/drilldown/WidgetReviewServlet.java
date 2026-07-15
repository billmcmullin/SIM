package com.sim.chatserver.web.dashboard.drilldown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetReviewServlet", urlPatterns = {"/dashboard/widgets/drilldown/review"})
public class WidgetReviewServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/widget_review.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        String selectionId = trimToNull(firstQueryParam(req, "selectionId"));
        if (selectionId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "selectionId required.");
            return;
        }

        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            log.log(Level.INFO, "Selection not found for selectionId={0}", sanitizeForLog(selectionId));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Selection not found.");
            return;
        }

        String widgetId = trimToNull(selection.widgetId);
        String displayName = trimToNull(selection.displayName);
        String subjectLabel = displayName != null ? displayName : (widgetId != null ? widgetId : "Selected Chats");
        String subjectType = selection.hasSnapshots() ? "Term" : "Widget";

        String backLink = trimToNull(selection.getBackUrl());
        if (backLink == null) {
            if (widgetId != null) {
                backLink = req.getContextPath() + "/dashboard/widgets/view?widgetId=" + urlEncode(widgetId);
            } else {
                backLink = req.getContextPath() + "/dashboard";
            }
        }

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);

        String userName = String.valueOf(session.getAttribute("user"));
        String role = session.getAttribute("role") == null ? "USER" : String.valueOf(session.getAttribute("role"));
        String contextPath = req.getContextPath();

        // HTML placeholder replacements
        String rendered = template
                .replace("${user}", escapeHtml(userName))
                .replace("${role}", escapeHtml(role))
                .replace("${contextPath}", escapeHtml(contextPath))
                .replace("${widgetName}", escapeHtml(subjectLabel))
                .replace("${widgetId}", escapeHtml(widgetId == null ? "" : widgetId))
                .replace("${selectionId}", escapeHtml(selectionId))
                .replace("${subjectType}", escapeHtml(subjectType))
                .replace("${subjectLabel}", escapeHtml(subjectLabel))
                .replace("${backLink}", escapeHtml(backLink));

        // JS string literal safety (for placeholders used in inline script config)
        rendered = rendered
                .replace("'${contextPath}'", "'" + escapeJs(contextPath) + "'")
                .replace("'${selectionId}'", "'" + escapeJs(selectionId) + "'")
                .replace("'${subjectLabel}'", "'" + escapeJs(subjectLabel) + "'")
                .replace("'${subjectType}'", "'" + escapeJs(subjectType) + "'");

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    private String loadTemplate(ServletContext context, String path) throws IOException {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Template not found: " + path);
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
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeJs(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\\' ->
                    sb.append("\\\\");
                case '\'' ->
                    sb.append("\\'");
                case '"' ->
                    sb.append("\\\"");
                case '\n' ->
                    sb.append("\\n");
                case '\r' ->
                    sb.append("\\r");
                case '\t' ->
                    sb.append("\\t");
                case '\b' ->
                    sb.append("\\b");
                case '\f' ->
                    sb.append("\\f");
                case '<' ->
                    sb.append("\\x3C");
                case '>' ->
                    sb.append("\\x3E");
                case '&' ->
                    sb.append("\\x26");
                default ->
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private String firstQueryParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String query = req.getQueryString();
        if (query == null || query.isBlank()) {
            return null;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair == null || pair.isBlank()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String rawKey = eq >= 0 ? pair.substring(0, eq) : pair;
            String rawVal = eq >= 0 && eq < pair.length() - 1 ? pair.substring(eq + 1) : "";
            String key = urlDecode(rawKey);
            if (!name.equals(key)) {
                continue;
            }
            String val = urlDecode(rawVal);
            String normalized = val.replace("\r", "").replace("\n", "").trim();
            return normalized.length() > 256 ? normalized.substring(0, 256) : normalized;
        }
        return null;
    }

    private String urlDecode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid URL encoding in query parameter", ex);
            return value;
        }
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.log(Level.FINE, "Failed to URL-encode value", e);
            return value;
        }
    }
}
