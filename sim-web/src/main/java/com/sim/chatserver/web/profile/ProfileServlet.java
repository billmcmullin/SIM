package com.sim.chatserver.web.profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.enterprise.inject.spi.CDI;
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

    @Inject
    UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            try {
                resp.sendRedirect(LOGIN_PATH);
            } catch (IOException ex) {
                log.log(Level.FINE, "Unable to redirect to login", ex);
                sendFallbackError(resp, HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        String template = loadTemplate(req.getServletContext(), TEMPLATE_PATH);
        String username = String.valueOf(session.getAttribute("user"));
        String rendered = template
                .replace("${user}", escapeHtml(username))
                .replace("${contextPath}", req.getContextPath());

        resp.setContentType("text/html;charset=UTF-8");
        try {
            try (PrintWriter out = resp.getWriter()) {
                out.print(rendered);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write profile page", ex);
            sendFallbackError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
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
            UserAccount updated = resolveUserService().updateCredentials(currentUsername, newUsername, newPassword);
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
        } catch (IllegalStateException e) {
            log.log(Level.WARNING, "Profile update failed", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update profile.");
        }
    }

    private UserService resolveUserService() {
        if (userService != null) {
            return userService;
        }
        return CDI.current().select(UserService.class).get();
    }

    private String sanitizeInput(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\0", "").trim();
    }

    private String loadTemplate(jakarta.servlet.ServletContext context, String path) {
        try (InputStream stream = context.getResourceAsStream(path)) {
            if (stream == null) {
                log.warning("Template not found for profile page.");
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
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to load profile template", ex);
            return "";
        }
    }

    private static String escapeHtml(String input) {
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

    private void writeError(HttpServletResponse resp, int status, String message) {
        JsonObject error = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, error);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            try (JsonWriter jsonWriter = Json.createWriter(resp.getWriter())) {
                jsonWriter.writeObject(payload);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write profile servlet JSON response", ex);
            sendFallbackError(resp, status);
        }
    }

    private void sendFallbackError(HttpServletResponse resp, int status) {
        try {
            if (!resp.isCommitted()) {
                resp.sendError(status);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to send fallback profile response", ex);
        }
    }

    private static final class RequestContext {

        private static final int MAX_PARAM_LEN = 128;

        private final HttpServletRequest req;

        RequestContext(HttpServletRequest req) {
            this.req = req;
        }

        static RequestContext from(HttpServletRequest req) {
            return new RequestContext(req);
        }

        String first(String name) {
            if (name == null || name.isBlank() || req == null) {
                return null;
            }
            String[] values = req.getParameterValues(name);
            if (values == null || values.length == 0 || values[0] == null) {
                return null;
            }
            String trimmed = values[0].trim();
            return trimmed.length() > MAX_PARAM_LEN ? trimmed.substring(0, MAX_PARAM_LEN) : trimmed;
        }
    }
}
