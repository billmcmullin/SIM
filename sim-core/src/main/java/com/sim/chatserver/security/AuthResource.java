package com.sim.chatserver.security;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;
import com.sim.chatserver.util.CommonLiteralConstants;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(AuthResource.PATH_AUTH)
public class AuthResource implements AuthApiEndpoint {


    private static final Logger log = Logger.getLogger(AuthResource.class.getCanonicalName());

    private static final String KEY_USERNAME = CommonLiteralConstants.KEY_USERNAME;
    private static final String KEY_PASSWORD = CommonLiteralConstants.KEY_PASSWORD;
    private static final String KEY_AUTHENTICATED = "authenticated";
    private static final String KEY_ERROR = CommonLiteralConstants.KEY_ERROR;
    public static final String PATH_AUTH = "/auth";
    private static final String PATH_LOGIN = CommonLiteralConstants.PATH_LOGIN;
    private static final String KEY_SESSION_USER = CommonLiteralConstants.VALUE_USER;
    private static final String KEY_SESSION_ROLE = CommonLiteralConstants.ROLE;
    private static final String LOG_AUTH_FAILED = "Authentication failed for username: {0}";
    private static final String LOG_USER_LOGGED_IN = "User ''{0}'' logged in with role ''{1}''";
    private static final String MSG_USERNAME_PASSWORD_REQUIRED = "username and password required";

    @Inject
    UserService userService;

    @Context
    HttpServletRequest servletRequest;

    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @POST
    @Path(PATH_LOGIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginApi(Map<String, String> payload) {
        return login(payload);
    }

    private final Response login(Map<String, String> payload) {
        Credentials credentials = extractCredentials(payload);
        if (credentials == null) {
            return badRequest();
        }

        // Single lookup/authentication call (no double lookup)
        UserAccount user = userService.authenticateAndGetUser(credentials.username(), credentials.password());

        if (user == null) {
            if (log.isLoggable(Level.WARNING)) {
                log.log(Level.WARNING, LOG_AUTH_FAILED, credentials.username());
            }
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(KEY_AUTHENTICATED, Boolean.FALSE))
                    .build();
        }

        HttpSession existing = servletRequest.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }

        HttpSession session = servletRequest.getSession(true);
        String resolvedUsername = user.getUsername();
        String role = normalizeRole(user.getRole());

        session.setAttribute(KEY_SESSION_USER, resolvedUsername);
        session.setAttribute(KEY_SESSION_ROLE, role);
        session.setMaxInactiveInterval(30 * 60);

        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, LOG_USER_LOGGED_IN,
                    new Object[]{resolvedUsername, role});
        }

        return Response.ok(Map.of(KEY_AUTHENTICATED, Boolean.TRUE, KEY_USERNAME, resolvedUsername)).build();
    }

    private static Credentials extractCredentials(Map<String, String> payload) {
        if (payload == null) {
            return null;
        }

        String rawUsername = payload.get(KEY_USERNAME);
        String password = payload.get(KEY_PASSWORD);

        if (rawUsername == null || password == null) {
            return null;
        }

        String username = rawUsername.trim();
        if (username.isEmpty()) {
            return null;
        }

        return new Credentials(username, password);
    }

    private static String normalizeRole(String role) {
        return role == null ? null : role.toUpperCase();
    }

    private static Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(KEY_ERROR, MSG_USERNAME_PASSWORD_REQUIRED))
                .build();
    }

    private record Credentials(String username, String password) {
    }
}