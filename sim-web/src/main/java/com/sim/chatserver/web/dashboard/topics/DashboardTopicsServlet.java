package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.web.util.ServletPathUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsServlet", urlPatterns = {"/dashboard/topics"})
public class DashboardTopicsServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsServlet.class.getName());

    private static final String TEMPLATE_PATH = "/WEB-INF/views/dashboard_topics.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String contextPath = ServletPathUtil.safeContextPathNoTrailingSlash(req.getContextPath());
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                req.getRequestDispatcher("/login").forward(req, resp);
                return;
            }

            String user = String.valueOf(session.getAttribute("user"));

            String template = loadTemplate(req, TEMPLATE_PATH);
            String rendered = template
                    .replace("${contextPath}", escapeHtml(contextPath))
                    .replace("${user}", escapeHtml(user))
                    .replace("${globalTopicRows}", "")
                    .replace("${perWidgetTopicTables}", "");
                String safeRendered = sanitizeRenderedTemplate(rendered);

            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.setContentType("text/html; charset=UTF-8");
                resp.getOutputStream().write(safeRendered.getBytes(StandardCharsets.UTF_8));

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

    private String loadTemplate(HttpServletRequest req, String path) {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                log.log(Level.WARNING, "Template not found: {0}", path);
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
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private String sanitizeRenderedTemplate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        return normalized.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
    }

}
