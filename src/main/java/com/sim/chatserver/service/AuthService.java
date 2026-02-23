package com.sim.chatserver.service;

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

    @Inject
    UserService userService;

    public UserAccount authenticate(String username, String password) {
        // avoid logging raw username/password; log at FINE level about the lookup
        log.fine("AuthService.authenticate: lookup initiated");

        if (username == null || username.trim().isEmpty()) {
            log.warning("AuthService.authenticate: empty username");
            return null;
        }

        UserAccount u = userService.findByUsername(username);
        if (u == null) {
            // do not emit the username or any secrets in logs
            log.info("AuthService.authenticate: user not found");
            return null;
        }

        // Log non-sensitive attributes only; do NOT log password hashes or other secrets.
        String maskedUsername = maskIdentifier(u.getUsername());
        log.info("AuthService.authenticate: found user=" + maskedUsername + " role=" + safeToString(u.getRole()));

        String storedHash = u.getPasswordHash();
        boolean ok = false;
        try {
            // Do not log the provided password or stored hash
            ok = storedHash != null && BCrypt.checkpw(password, storedHash);
        } catch (Exception e) {
            // Log the exception message but avoid including sensitive data
            log.severe("AuthService.authenticate: BCrypt check threw: " + e.toString());
        }

        log.info("AuthService.authenticate: bcrypt result=" + ok);
        return ok ? u : null;
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
        // preserve first and last char, mask the middle
        return new StringBuilder()
                .append(id.charAt(0))
                .append("***")
                .append(id.charAt(len - 1))
                .toString();
    }

    private static String safeToString(Object obj) {
        return obj == null ? "UNKNOWN" : obj.toString();
    }
}
