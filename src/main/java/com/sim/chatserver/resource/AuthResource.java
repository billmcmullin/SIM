package com.sim.chatserver.resource;

import com.sim.chatserver.dto.AuthRequest;
import com.sim.chatserver.dto.AuthResponse;
import com.sim.chatserver.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(AuthRequest req) {
        String token = authService.authenticate(req.username, req.password);
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(new AuthResponse(token, req.username, "USER")).build();
    }
}
