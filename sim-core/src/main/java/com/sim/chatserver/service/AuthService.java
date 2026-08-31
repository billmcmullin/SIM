package com.sim.chatserver.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * AuthService.authenticate with logging that avoids emitting sensitive data.
 */
@ApplicationScoped
public class AuthService {

    private static final Logger log = Logger.getLogger(AuthService.class.getName());

    private static final String UNKNOWN = "UNKNOWN";
    private static final String LOG_LOOKUP_INIT = "AuthService.authenticate: lookup initiated";
    private static final String LOG_EMPTY_USERNAME = "AuthService.authenticate: empty username";
    private static final String LOG_USER_NOT_FOUND = "AuthService.authenticate: user not found";
    private static final String LOG_FOUND_USER = "AuthService.authenticate: found user={0} role={1}";

    @Inject
    UserService userService;

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private UserAccount authenticate(String username, String password) {
        // Avoid work if FINE is disabled
        if (log.isLoggable(Level.FINE)) {
            log.fine(LOG_LOOKUP_INIT);
        }

        // Normalize username once
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            log.warning(LOG_EMPTY_USERNAME);
            return null;
        }

        UserAccount user = userService.authenticateAndGetUser(normalizedUsername, password);
        if (user == null) {
            log.info(LOG_USER_NOT_FOUND);
            return null;
        }

        // Log only non-sensitive metadata, lazily/parameterized
        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, LOG_FOUND_USER,
                    new Object[]{maskIdentifier(user.getUsername()), safeToString(user.getRole())});
        }

        return user;
    }

    /**
     * Return a masked representation of an identifier suitable for logs.
     * Examples: - null -> "" - "a" -> "*" - "ab" -> "a*" - "alice" -> "a***e"
     */
    private static String maskIdentifier(String id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        int len = id.length();
        if (len == 1) {
            return "*";
        }
        if (len == 2) {
            return id.charAt(0) + "*";
        }
        return new StringBuilder(5)
                .append(id.charAt(0))
                .append("***")
                .append(id.charAt(len - 1))
                .toString();
    }

    private static String safeToString(Object obj) {
        return obj == null ? UNKNOWN : obj.toString();
    }

    private static String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        String trimmed = username.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
