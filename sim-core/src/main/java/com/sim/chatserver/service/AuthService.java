package com.sim.chatserver.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

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
    private static final String LOG_BCRYPT_THROW = "AuthService.authenticate: BCrypt check threw: {0}";
    private static final String LOG_BCRYPT_RESULT = "AuthService.authenticate: bcrypt result={0}";
    private static final String LOG_FOUND_USER = "AuthService.authenticate: found user={0} role={1}";

    @Inject
    UserService userService;

    public UserAccount authenticate(String username, String password) {
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

        // Single lookup
        UserAccount user = userService.findByUsername(normalizedUsername);
        if (user == null) {
            log.info(LOG_USER_NOT_FOUND);
            return null;
        }

        // Log only non-sensitive metadata, lazily/parameterized
        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, LOG_FOUND_USER,
                    new Object[]{maskIdentifier(user.getUsername()), safeToString(user.getRole())});
        }

        // Fast fail for obviously invalid inputs to avoid BCrypt cost
        if (password == null) {
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, LOG_BCRYPT_RESULT, false);
            }
            return null;
        }

        String storedHash = user.getPasswordHash();
        boolean ok = false;
        try {
            // No sensitive data in logs
            ok = storedHash != null && BCrypt.checkpw(password, storedHash);
        } catch (Exception e) {
            log.log(Level.WARNING, LOG_BCRYPT_THROW, e.toString());
        }

        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, LOG_BCRYPT_RESULT, ok);
        }

        return ok ? user : null;
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
