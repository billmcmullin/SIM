package com.sim.chatserver.resource;

import java.util.Map;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.security.JwtUtil;
import com.sim.chatserver.service.AuthService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Debugging login endpoint wrapper: logs attempts and returns helpful JSON on
 * failure.
 */
@Path("/api/auth")
public class AuthResource {

    private static final Logger log = Logger.getLogger(AuthResource.class.getName());

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Map<String, String> creds) {
        String username = creds == null ? null : creds.get("username");
        String password = creds == null ? null : creds.get("password");
        log.info("AuthResource.login attempt for username=" + username);
        try {
            UserAccount user = authService.authenticate(username, password);
            if (user == null) {
                log.info("AuthResource.login failed for username=" + username);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "invalid credentials")).build();
            }
            String token = JwtUtil.generateToken(user.getUsername(), user.getRole());
            Map<String, Object> out = Map.of("token", token, "username", user.getUsername(), "role", user.getRole());
            return Response.ok(out).build();
        } catch (Exception e) {
            log.severe("AuthResource.login error: " + e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "server error", "message", e.getMessage())).build();
        }
    }
}
