package com.sim.chatserver.api;

import java.util.Map;

import com.sim.chatserver.service.UserService;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {

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

        boolean authenticated = userService.authenticate(username, password);
        if (!authenticated) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("authenticated", false))
                    .build();
        }

        servletRequest.getSession(true).setAttribute("user", username);
        return Response.ok(Map.of("authenticated", true, "username", username)).build();
    }
}
