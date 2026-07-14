package com.sim.chatserver.web.profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ProfileServlet.class.getName());
    private static final String TEMPLATE_PATH = "/WEB-INF/views/profile.html";
    private static final String LOGIN_PATH = "/login";
    private static final int MAX_PARAM_LEN = 128;

    @Inject
    UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(LOGIN_PATH);
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
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        Object sessionUser = session.getAttribute("user");
        if (!(sessionUser instanceof String currentUsername) || currentUsername.isBlank()) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        RequestContext context = RequestContext.from(req);
        String newUsername = sanitizeInput(context.first("username"));
        String newPassword = sanitizeInput(context.first("password"));

        if (newUsername == null || newUsername.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username cannot be empty.");
            return;
        }

        try {
            UserAccount updated = userService.updateCredentials(currentUsername, newUsername, newPassword);
            String updatedUsername = updated == null ? null : sanitizeInput(updated.getUsername());
            if (updatedUsername == null || updatedUsername.isBlank()) {
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Profile update failed.");
                return;
            }

            session.setAttribute("user", updatedUsername);
            JsonObject ok = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("username", updatedUsername)
                    .build();
            writeJson(resp, HttpServletResponse.SC_OK, ok);
        } catch (jakarta.persistence.PersistenceException pe) {
            log.log(Level.FINE, "Profile update conflict", pe);
            writeError(resp, HttpServletResponse.SC_CONFLICT, "Could not update profile.");
        } catch (RuntimeException e) {
            log.log(Level.WARNING, "Profile update failed", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update profile.");
        }
    }

    private String sanitizeInput(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\0", "").trim();
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

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject error = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, error);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (JsonWriter jsonWriter = Json.createWriter(resp.getWriter())) {
            jsonWriter.writeObject(payload);
        }
    }

    private static final class RequestContext {

        private final Map<String, String[]> params;

        private RequestContext(Map<String, String[]> params) {
            this.params = params;
        }

        private static RequestContext from(HttpServletRequest req) {
            return new RequestContext(req.getParameterMap());
        }

        private String first(String name) {
            if (name == null || name.isBlank() || params == null) {
                return null;
            }
            String[] values = params.get(name);
            if (values == null || values.length == 0 || values[0] == null) {
                return null;
            }
            String trimmed = values[0].trim();
            return trimmed.length() > MAX_PARAM_LEN ? trimmed.substring(0, MAX_PARAM_LEN) : trimmed;
        }
    }
}
