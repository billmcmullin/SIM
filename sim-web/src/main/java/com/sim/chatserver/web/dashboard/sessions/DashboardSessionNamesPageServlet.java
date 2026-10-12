package com.sim.chatserver.web.dashboard.sessions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.DashboardTemplateRenderer;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "DashboardSessionNamesPageServlet", urlPatterns = {"/dashboard/session-names"})
public class DashboardSessionNamesPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionNamesPageServlet.class.getCanonicalName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/session-names.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            com.sim.chatserver.web.util.ServletPathUtil.forwardToLogin(req, resp, log);
            return;
        }

        String userName = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) instanceof String value ? value : "";
        String role = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE) instanceof String roleValue ? roleValue : "USER";
        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String contextPath = req.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }
        String rendered = template
                .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_CONTEXT_PATH, contextPath)
            .replace(com.sim.chatserver.util.CommonLiteralConstants.TEMPLATE_USER, DashboardTemplateRenderer.escapeHtml(userName))
            .replace("${role}", DashboardTemplateRenderer.escapeHtml(role));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_HTML_UTF8_SPACED);
        PrintWriter out = resp.getWriter();
        if (out == null) {
            throw new IOException("Response writer unavailable");
        }
        out.write(rendered);
        out.flush();
    
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
        if (context == null) {
            return "";
        }
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                log.warning("Session names template not found.");
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
            log.log(Level.WARNING, "Unable to load session names template: " + path, e);
            return "";
        }
    }

    private void sendFallbackError(HttpServletResponse resp, int status) {
        try {
            if (!resp.isCommitted()) {
                resp.sendError(status);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send fallback session names error", ex);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
