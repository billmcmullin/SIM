package com.sim.chatserver.web.dashboard.sessions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
/**
 * Serves the /dashboard/sessions page which renders the "All Sessions" SPA. The
 * initial script tag injects the context path (for AJAX), default pagination
 * parameters, and initial filters.
 */
@WebServlet(name = "AllSessionsPageServlet", urlPatterns = {"/dashboard/sessions"})
public class AllSessionsPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AllSessionsPageServlet.class.getCanonicalName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/all_sessions.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        // require authentication
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            safeForwardToLogin(req, resp);
            return;
        }

        String contextPath = req.getContextPath();
        String user = String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER));

        // New "identity" filter:
        // all | name | email | either | both
        String initialScript = """
                window.APP_CONTEXT_PATH = %s;
                window.ALL_SESSIONS_INITIAL = {
                    all: true,
                    page: 1,
                    limit: 10,
                    activity: com.sim.chatserver.util.CommonLiteralConstants.VALUE_ALL,
                    identity: "either"
                };
                """.formatted(toJsonString(contextPath));

        String template = loadTemplate(req, TEMPLATE_PATH);
        if (template == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Template missing: " + TEMPLATE_PATH);
            return;
        }

        String rendered = template
                .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_CONTEXT_PATH, escapeHtml(contextPath))
                .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_USER, escapeHtml(user))
                .replace("${initialScript}", initialScript);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8_SPACED);
        try (PrintWriter writer = resp.getWriter()) {
            writer.print(rendered);
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

    private String loadTemplate(HttpServletRequest req, String path) {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
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
            java.util.logging.Logger.getLogger(com.sim.chatserver.util.CommonLiteralConstants.LOGGER_OWASP)
                    .log(java.util.logging.Level.WARNING, "Unable to load template: " + path, e);
            return null;
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '&' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_AMP);
                case '<' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_LT);
                case '>' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_GT);
                case '"' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_QUOT);
                case '\'' -> out.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_APOS);
                default -> out.append(ch);
            }
        }
        return out.toString();
    }

    private String toJsonString(String value) {
        if (value == null) {
            return com.sim.chatserver.util.CommonLiteralConstants.DOUBLE_QUOTE_ESCAPED;
        }
        StringBuilder escaped = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_BACKSLASH_TO);
                case '"' -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_QUOTE_TO);
                case com.sim.chatserver.util.LineSeparatorUtil.CARRIAGE_RETURN -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.ESCAPED_CARRIAGE_RETURN);
                case com.sim.chatserver.util.LineSeparatorUtil.LINE_FEED -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.JSON_ESCAPE_NEWLINE_TO);
                default -> escaped.append(ch);
            }
        }
        return new StringBuilder(escaped.length() + 2).append('"').append(escaped).append('"').toString();
    }

    private void safeForwardToLogin(HttpServletRequest req, HttpServletResponse resp) {
        com.sim.chatserver.web.util.ServletPathUtil.forwardToLogin(req, resp, log);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
