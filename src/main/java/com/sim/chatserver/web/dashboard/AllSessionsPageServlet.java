package com.sim.chatserver.web.dashboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Serve the All Sessions UI at /dashboard/sessions using a template at
 * /WEB-INF/views/all_sessions.html. The servlet injects a small JS config
 * snippet using safe JSON string escaping.
 */
@WebServlet(name = "AllSessionsPageServlet", urlPatterns = {"/dashboard/sessions"})
public class AllSessionsPageServlet extends HttpServlet {

    private static final String TEMPLATE_PATH = "/WEB-INF/views/all_sessions.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // require login
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String contextPath = req.getContextPath();
        Object userObj = session.getAttribute("user");
        String user = userObj == null ? "unknown" : String.valueOf(userObj);

        // Build a small, safe JS snippet. Use toJsonString(...) to produce a safe JS string literal
        String initialScript = "window.APP_CONTEXT_PATH = " + toJsonString(contextPath) + ";\n"
                + "window.ALL_SESSIONS_INITIAL = { all: true, page: 1, limit: 10 };";

        String template = loadTemplate(req, TEMPLATE_PATH);
        if (template == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Template not found: " + TEMPLATE_PATH);
            return;
        }

        // Replace placeholders. For HTML insertion (contextPath in hrefs, user in markup) escape for HTML.
        String rendered = template
                .replace("${contextPath}", escapeHtml(contextPath))
                .replace("${user}", escapeHtml(user))
                .replace("${initialScript}", initialScript);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");

        try (PrintWriter w = resp.getWriter()) {
            w.print(rendered);
        }
    }

    private String loadTemplate(HttpServletRequest req, String path) throws IOException {
        try (InputStream stream = req.getServletContext().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        }
    }

    // Minimal HTML escaping for template insertion
    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Produce a JSON-style JS string literal (double-quoted), escaping backslashes, quotes, CR/LF
    private static String toJsonString(String s) {
        if (s == null) {
            return "\"\"";
        }
        String v = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return "\"" + v + "\"";
    }
}
