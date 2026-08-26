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

/**
 * Serves the /dashboard/sessions page which renders the ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“All SessionsÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â SPA. The
 * initial script tag injects the context path (for AJAX), default pagination
 * parameters, and initial filters.
 */
@WebServlet(name = "AllSessionsPageServlet", urlPatterns = {"/dashboard/sessions"})
public class AllSessionsPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AllSessionsPageServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/all_sessions.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        // require authentication
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            safeForwardToLogin(req, resp);
            return;
        }

        String contextPath = req.getContextPath();
        String user = String.valueOf(session.getAttribute("user"));

        // New "identity" filter:
        // all | name | email | either | both
        String initialScript = """
                window.APP_CONTEXT_PATH = %s;
                window.ALL_SESSIONS_INITIAL = {
                    all: true,
                    page: 1,
                    limit: 10,
                    activity: "all",
                    identity: "either"
                };
                """.formatted(toJsonString(contextPath));

        String template = loadTemplate(req, TEMPLATE_PATH);
        if (template == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Template missing: " + TEMPLATE_PATH);
            return;
        }

        String rendered = template
                .replace("${contextPath}", escapeHtml(contextPath))
                .replace("${user}", escapeHtml(user))
                .replace("${initialScript}", initialScript);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.print(rendered);
        }
    
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
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
                    builder.append(line).append('\n');
                }
                return builder.toString();
            }
        } catch (IOException e) {
            java.util.logging.Logger.getLogger("OWASP")
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
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(ch);
            }
        }
        return out.toString();
    }

    private String toJsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\r' -> escaped.append("\\r");
                case '\n' -> escaped.append("\\n");
                default -> escaped.append(ch);
            }
        }
        return new StringBuilder(escaped.length() + 2).append('"').append(escaped).append('"').toString();
    }

    private void safeForwardToLogin(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.getRequestDispatcher("/login").forward(req, resp);
        } catch (IOException | ServletException ex) {
            log.log(Level.WARNING, "Unable to forward unauthenticated user to login", ex);
            try {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            } catch (IOException ioe) {
                log.log(Level.FINE, "Unable to send authentication error", ioe);
            }
        }
    }
}
