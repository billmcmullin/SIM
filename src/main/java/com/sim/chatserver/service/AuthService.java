package com.sim.chatserver.service;

import org.mindrot.jbcrypt.BCrypt;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.security.JwtUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {

    @Inject
    UserService userService;

    /**
     * Authenticate user. If DB is not yet configured, allow the default bootstrap admin credentials:
     * username == "admin" && password == "admin".
     */
    public String authenticate(String username, String password) {
        try {
            UserAccount u = userService.findByUsername(username);
            if (u == null) return null;
            if (BCrypt.checkpw(password, u.getPasswordHash())) {
                return JwtUtil.generateToken(u.getUsername(), u.getRole());
            }
            return null;
        } catch (IllegalStateException ise) {
            // DB not configured — allow one-time default admin login so initial setup can be performed.
            if ("admin".equals(username) && "admin".equals(password)) {
                return JwtUtil.generateToken("admin", "ADMIN");
            }
            return null;
        } catch (Exception e) {
            // other errors: do not expose internal details; authentication fails
            return null;
        }
    }
}
