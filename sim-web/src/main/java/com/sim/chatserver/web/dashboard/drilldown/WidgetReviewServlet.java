package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.web.util.ServletRequestParamUtil;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            req.getRequestDispatcher("/login").forward(req, resp);
            return;
        }

        String selectionId = ServletRequestParamUtil.normalizeValue(
            ServletRequestParamUtil.firstParam(req, "selectionId", 256, true, true),
            256,
            true,
            true);
        if (selectionId == null) {
            Object forwardedSelectionId = req.getAttribute("selectionId");
            if (forwardedSelectionId instanceof String forwarded) {
                String candidate = ServletRequestParamUtil.normalizeValue(forwarded, 256, true, true);
                if (candidate != null && isValidSelectionId(candidate)) {
                    selectionId = candidate;
                } else {
                    log.log(Level.FINE, "Ignored invalid forwarded selectionId.");
                }
            }
        }
        if (selectionId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "selectionId required.");
            return;
        }

        WidgetReviewStartServlet.Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
        if (selection == null) {
            log.log(Level.INFO, "Selection not found");
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
            .replace("'${contextPath}'", '"' + escapeJs(contextPath) + '"')
            .replace("'${selectionId}'", '"' + escapeJs(selectionId) + '"')
            .replace("'${subjectLabel}'", '"' + escapeJs(subjectLabel) + '"')
            .replace("'${subjectType}'", '"' + escapeJs(subjectType) + '"');

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    
        } catch (IOException | ServletException | IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (resp != null && !resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private String loadTemplate(ServletContext context, String path) {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Template not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load template: " + path, ex);
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (char c : input.toCharArray()) {
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
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

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\r', '_').replace('\n', '_');
        return normalized.length() > 120 ? normalized.substring(0, 120) : normalized;
    }

    private boolean isValidSelectionId(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger("OWASP").log(java.util.logging.Level.FINE, "Handled exception", ex);
            return false;
        }
    }

    private String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
