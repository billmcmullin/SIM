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
import com.sim.chatserver.web.dashboard.widgets.Selection;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "WidgetReviewServlet", urlPatterns = {com.sim.chatserver.util.CommonLiteralConstants.PATH_WIDGET_REVIEW})
public class WidgetReviewServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewServlet.class.getCanonicalName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/widget_review.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            com.sim.chatserver.web.util.ServletPathUtil.forwardToLogin(req, resp, log);
            return;
        }

        String selectionId = ServletRequestParamUtil.normalizeValue(
            ServletRequestParamUtil.firstParam(req, com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID, 256, true, true),
            256,
            true,
            true);
        if (selectionId == null) {
            Object forwardedSelectionId = req.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID);
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

        Selection selection = WidgetReviewStartServlet.fetchSelection(session, selectionId);
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

        String userName = String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER));
        String role = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE) == null ? "USER" : String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE));
        String contextPath = req.getContextPath();

        // HTML placeholder replacements
        String rendered = template
                .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_USER, escapeHtml(userName))
                .replace("${role}", escapeHtml(role))
                .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_CONTEXT_PATH, escapeHtml(contextPath))
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
        resp.setContentType(com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8);
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
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
                case '&' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_AMP);
                case '<' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_LT);
                case '>' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_GT);
                case '"' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_QUOT);
                case '\'' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_APOS);
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
                    sb.append(com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO);
                case '\'' ->
                    sb.append(com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_SINGLE_QUOTE);
                case '"' ->
                    sb.append(com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_QUOTE_TO);
                case com.sim.chatserver.util.LineSeparatorUtil.LINE_FEED ->
                    sb.append(com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO);
                case com.sim.chatserver.util.LineSeparatorUtil.CARRIAGE_RETURN ->
                    sb.append(com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_CARRIAGE_RETURN);
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
        String normalized = value.replace(System.lineSeparator().charAt(0), '_').replace(System.lineSeparator().charAt(System.lineSeparator().length() - 1), '_');
        return normalized.length() > 120 ? new String(normalized.toCharArray(), 0, 120) : normalized;
    }

    private boolean isValidSelectionId(String value) {
        try {
            UUID parsed = UUID.fromString(value);
            return parsed != null;
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP).log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.LOG_HANDLED_EXCEPTION, ex);
            return false;
        }
    }

    private String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
