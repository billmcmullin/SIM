package com.sim.chatserver.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    private static final String TEMPLATE_PATH = "/WEB-INF/views/profile.html";

    @Inject
    UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String username = String.valueOf(session.getAttribute("user"));
        String rendered = template
                .replace("${user}", escapeHtml(username))
                .replace("${contextPath}", req.getContextPath());

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(rendered);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writer.write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return;
        }

        String currentUsername = String.valueOf(session.getAttribute("user"));
        String newUsername = req.getParameter("username");
        String newPassword = req.getParameter("password");

        if (newUsername == null || newUsername.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.write("{\"status\":\"error\",\"message\":\"Username cannot be empty.\"}");
            return;
        }

        try {
            UserAccount updated = userService.updateCredentials(currentUsername, newUsername, newPassword);
            session.setAttribute("user", updated.getUsername());
            writer.write("{\"status\":\"ok\",\"username\":\"" + escapeJson(updated.getUsername()) + "\"}");
        } catch (jakarta.persistence.PersistenceException pe) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            writer.write("{\"status\":\"error\",\"message\":\"Could not update profile: " + escapeJson(pe.getMessage()) + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("{\"status\":\"error\",\"message\":\"Failed to update profile: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String loadTemplate(jakarta.servlet.ServletContext context, String path) throws IOException {
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

    private static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
