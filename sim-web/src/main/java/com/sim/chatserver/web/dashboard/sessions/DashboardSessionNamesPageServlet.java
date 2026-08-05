package com.sim.chatserver.web.dashboard.sessions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardSessionNamesPageServlet", urlPatterns = {"/dashboard/session-names"})
public class DashboardSessionNamesPageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(DashboardSessionNamesPageServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/session-names.html";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            RequestDispatcher dispatcher = req.getRequestDispatcher("/login");
            if (dispatcher != null) {
                dispatcher.forward(req, resp);
            } else {
                sendFallbackError(resp, HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        String userName = session.getAttribute("user") instanceof String value ? value : "";
        String role = session.getAttribute("role") instanceof String value ? value : "USER";
        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String contextPath = req.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }
        String rendered = template
                .replace("${contextPath}", contextPath)
                .replace("${user}", escapeHtml(userName))
                .replace("${role}", escapeHtml(role));

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        if (out == null) {
            throw new IOException("Response writer unavailable");
        }
        out.write(rendered);
        out.flush();
    }

    private String loadTemplate(ServletContext context, String path) throws IOException {
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
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
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

    private void sendFallbackError(HttpServletResponse resp, int status) {
        try {
            if (!resp.isCommitted()) {
                resp.sendError(status);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send fallback session names error", ex);
        }
    }
}
