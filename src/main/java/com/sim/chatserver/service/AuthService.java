package com.sim.chatserver.service;

import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import com.sim.chatserver.model.UserAccount;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Debugging version of AuthService.authenticate that logs the lookup and bcrypt
 * result.
 */
@ApplicationScoped
public class AuthService {

    private static final Logger log = Logger.getLogger(AuthService.class.getName());

    @Inject
    UserService userService;

    public UserAccount authenticate(String username, String password) {
        log.info("AuthService.authenticate: username=" + username);
        if (username == null || username.trim().isEmpty()) {
            log.warning("AuthService.authenticate: empty username");
            return null;
        }
        UserAccount u = userService.findByUsername(username);
        if (u == null) {
            log.info("AuthService.authenticate: user not found: " + username);
            return null;
        }
        String storedHash = u.getPasswordHash();
        log.info("AuthService.authenticate: found user=" + u.getUsername() + " role=" + u.getRole()
                + " passwordHash=" + (storedHash == null ? "null"
                        : storedHash.substring(0, Math.min(60, storedHash.length())) + (storedHash.length() > 60 ? "..." : "")));
        boolean ok = false;
        try {
            ok = storedHash != null && BCrypt.checkpw(password, storedHash);
        } catch (Exception e) {
            log.severe("AuthService.authenticate: BCrypt check threw: " + e.toString());
        }
        log.info("AuthService.authenticate: bcrypt OK=" + ok);
        return ok ? u : null;
    }
}
