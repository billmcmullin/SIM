package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.util.JsonRequestParserUtil;
import com.sim.chatserver.service.UserService;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
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
    UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        if (!isAdmin(req, resp)) {
            return;
        }

        List<UserAccount> users = resolveUserService().listAllUsers();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        users.forEach(user -> {
            if (user == null) {
                return;
            }
            long userId = safeUserId(user);
            String username = user.getUsername() == null ? "" : user.getUsername();
            String role = user.getRole() == null ? "" : user.getRole();
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("id", userId)
                    .add("username", username)
                    .add("role", role)
                    .build());
        });

        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("users", arrayBuilder)
                .build();
        writeJson(resp, HttpServletResponse.SC_OK, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        if (!isAdmin(req, resp)) {
            return;
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (payload == null || payload.isEmpty()) {
            log.fine("Invalid admin user payload");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String username = JsonRequestParserUtil.getString(payload, "username", 128).trim();
        String password = JsonRequestParserUtil.getString(payload, "password", 512);
        String role = JsonRequestParserUtil.getString(payload, "role", "USER").trim().toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "username and password are required.");
            return;
        }

        try {
            resolveUserService().createUser(username, password, role);
            writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add("status", "ok").build());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.log(Level.SEVERE, "Failed to create user", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create user.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        if (!isAdmin(req, resp)) {
            return;
        }
        String userId = ServletRequestParamUtil.firstParam(req, "userId", Math.max(1, 64), true, true);
        if (userId == null || userId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing userId.");
            return;
        }

        boolean deleted = resolveUserService().deleteUser(userId);
        if (!deleted) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found.");
            return;
        }
        writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add("status", "ok").build());
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    private UserService resolveUserService() {
        if (userService != null) {
            return userService;
        }
        return CDI.current().select(UserService.class).get();
    }

    private long safeUserId(UserAccount user) {
        if (user == null) {
            return -1L;
        }
        Long id = user.getId();
        if (id == null) {
            return -1L;
        }
        try {
            return id;
        } catch (NumberFormatException e) {
            log.log(Level.FINE, "Invalid user id value", e);
            return -1L;
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        JsonObject payload = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", message == null ? "" : message)
                .build();
        writeJson(resp, status, payload);
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to write admin user response", ex);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(Level.FINE, "Unable to send fallback error response", sendErrorFailure);
            }
        }
    }
}
