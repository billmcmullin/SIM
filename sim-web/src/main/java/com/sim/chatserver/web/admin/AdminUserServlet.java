package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.util.CommonLiteralConstants;
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


import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(AdminUserServlet.class.getCanonicalName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final Pattern SAFE_LONG_VALUE = Pattern.compile("^\\d{1,18}$");

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
                String role = user.getRole();
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("id", userId)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_USERNAME, username)
                    .add(com.sim.chatserver.util.CommonLiteralConstants.ROLE, role)
                    .build());
        });

        JsonObject response = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
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
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return;
        }

        JsonObject payload = JsonRequestParserUtil.parseObject(req, MAX_JSON_PAYLOAD_BYTES);
        if (payload == null || payload.isEmpty()) {
            log.fine("Invalid admin user payload");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return;
        }

        String username = JsonRequestParserUtil.getString(payload, com.sim.chatserver.util.CommonLiteralConstants.KEY_USERNAME, 128).trim();
        String password = JsonRequestParserUtil.getString(payload, com.sim.chatserver.util.CommonLiteralConstants.KEY_PASSWORD, 512);
        String role = JsonRequestParserUtil
            .getString(payload, com.sim.chatserver.util.CommonLiteralConstants.ROLE, CommonLiteralConstants.VALUE_USER.toUpperCase(Locale.ROOT))
            .trim()
            .toUpperCase(Locale.ROOT);

        if (username.isEmpty() || password.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "username and password are required.");
            return;
        }

        try {
            resolveUserService().createUser(username, password, role);
            writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok").build());
        } catch (IllegalStateException | IllegalArgumentException | SecurityException | UnsupportedOperationException | NullPointerException e) {
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
        writeJson(resp, HttpServletResponse.SC_OK, Json.createObjectBuilder().add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok").build());
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
            return false;
        }
        String role = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE) == null ? "" : session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.ROLE).toString();
        if (!com.sim.chatserver.util.CommonLiteralConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, com.sim.chatserver.util.CommonLiteralConstants.MSG_ADMIN_ROLE_REQUIRED);
            return false;
        }
        return true;
    }

    private UserService resolveUserService() {
        return CDI.current().select(UserService.class).get();
    }

    private long safeUserId(UserAccount user) {
        if (user == null) {
            return -1L;
        }
        Object idValue = user.getId();
        if (idValue == null) {
            return -1L;
        }
        String text = idValue.toString().trim();
        if (!SAFE_LONG_VALUE.matcher(text).matches()) {
            return -1L;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid user id value", ex);
            return -1L;
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        JsonObject payload = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, message == null ? "" : message)
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

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
