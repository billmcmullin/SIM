package com.sim.chatserver.api;

import java.util.Map;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

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

@Path("/auth")
public class AuthResource {

    private static final Logger log = Logger.getLogger(AuthResource.class.getName());

    @Inject
    UserService userService;

    @Context
    HttpServletRequest servletRequest;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Map<String, String> payload) {
        String username = payload == null ? null : payload.get("username");
        String password = payload == null ? null : payload.get("password");

        if (username == null || password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "username and password required"))
                    .build();
        }

        final String name = username.trim();
        UserAccount user = userService.findByUsername(username);
        if (user == null || !userService.authenticate(username, password)) {
            log.warning(() -> "Authentication failed for username: " + name);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("authenticated", false))
                    .build();
        }

        HttpSession session = servletRequest.getSession(true);
        final String resolvedUsername = user.getUsername();
        final String role = user.getRole().toUpperCase();
        session.setAttribute("user", resolvedUsername);
        session.setAttribute("role", role);
        log.info(() -> String.format("User '%s' logged in with role '%s'", resolvedUsername, role));

        return Response.ok(Map.of("authenticated", true, "username", resolvedUsername)).build();
    }
}
