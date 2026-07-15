package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminUserServlet.class.getName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;

    @Inject
    UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        List<UserAccount> users = userService.listAllUsers();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        users.forEach(user -> arrayBuilder.add(Json.createObjectBuilder()
                .add("id", user.getId())
                .add("username", user.getUsername())
                .add("role", user.getRole())
                .build()));

        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("users", arrayBuilder)
                .build();
        resp.setContentType("application/json");
        resp.getWriter().write(response.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        if (!isValidJsonRequest(req)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        JsonObject payload;
        try (var reader = Json.createReader(req.getReader())) {
            payload = reader.readObject();
        } catch (JsonException | IOException e) {
            log.log(Level.FINE, "Invalid admin user payload", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        String username = payload.getString("username", "").trim();
        String password = payload.getString("password", "");
        String role = payload.getString("role", "USER").toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"username and password are required.\"}");
            return;
        }

        try {
            userService.createUser(username, password, role);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } catch (RuntimeException e) {
            log.log(Level.SEVERE, "Failed to create user", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to create user.\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }
        String userId = firstQueryParam(req, "userId");
        if (userId == null || userId.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Missing userId.\"}");
            return;
        }

        boolean deleted = userService.deleteUser(userId);
        if (!deleted) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"User not found.\"}");
            return;
        }
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"ok\"}");
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Admin role required.\"}");
            return false;
        }
        return true;
    }

    private boolean isValidJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
            return false;
        }
        long len = req.getContentLengthLong();
        return len >= 0 && len <= MAX_JSON_PAYLOAD_BYTES;
    }

    private String firstQueryParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        String query = req.getQueryString();
        if (query == null || query.isBlank()) {
            return null;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair == null || pair.isBlank()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String rawKey = eq >= 0 ? pair.substring(0, eq) : pair;
            String rawVal = eq >= 0 && eq < pair.length() - 1 ? pair.substring(eq + 1) : "";
            String key = urlDecode(rawKey);
            if (!name.equals(key)) {
                continue;
            }
            String val = urlDecode(rawVal).replace("\r", "").replace("\n", "").trim();
            return val.length() > 64 ? val.substring(0, 64) : val;
        }
        return null;
    }

    private String urlDecode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid query parameter encoding", ex);
            return value;
        }
    }
}
