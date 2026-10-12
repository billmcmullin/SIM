package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.web.util.ServletPathUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardTopicsServlet", urlPatterns = {"/dashboard/topics"})
public class DashboardTopicsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsServlet.class.getCanonicalName());

    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_topics.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String contextPath = ServletPathUtil.safeContextPathNoTrailingSlash(req.getContextPath());
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
                com.sim.chatserver.web.util.ServletPathUtil.forwardToLogin(req, resp, log);
                return;
            }

            String user = String.valueOf(session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER));

                String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
            String rendered = template
                    .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_CONTEXT_PATH, escapeHtml(contextPath))
                    .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_USER, escapeHtml(user))
                    .replace("${globalTopicRows}", "")
                    .replace("${perWidgetTopicTables}", "");
                String safeRendered = validateCanonicalizedRenderedTemplate(rendered);

            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType(com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8_SPACED);
                resp.getOutputStream().write(safeRendered.getBytes(StandardCharsets.UTF_8));

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

    private String loadTemplate(ServletContext context, String path) {
        if (context == null) {
            return "";
        }
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, com.sim.chatserver.util.CommonLiteralConstants.MSG_TEMPLATE_NOT_FOUND_FORMAT, path);
                return "";
            }
            byte[] bytes = stream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load topics template: " + path, e);
            return "";
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&' -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_AMP);
                case '<' -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_LT);
                case '>' -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_GT);
                case '"' -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_QUOT);
                case '\'' -> escaped.append(com.sim.chatserver.util.CommonLiteralConstants.HTML_ESCAPE_APOS);
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private String validateCanonicalizedRenderedTemplate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String canonical = TextIoSanitizerUtil.canonicalize(value);
        return canonical.replaceAll(com.sim.chatserver.util.CommonLiteralConstants.REGEX_CTRL_EXCEPT_CRLF_TAB, "");
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
