package com.sim.chatserver.resource;

import com.sim.chatserver.dto.CreateUserRequest;
import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Admin endpoints for user management.
 */
@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminUserResource {

    @Inject
    UserService userService;

    @POST
    @RolesAllowed("ADMIN")
    public Response createUser(CreateUserRequest req) {
        if (req == null || req.username == null || req.username.isBlank() || req.password == null || req.password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"username and password are required\"}")
                    .build();
        }
        String role = (req.role == null || req.role.isBlank()) ? "USER" : req.role.toUpperCase();

        try {
            // createUser persists and returns the entity
            UserAccount created = userService.createUser(req.username.trim(), req.password, role);
            // build a minimal response without exposing password hash
            var resp = java.util.Map.of(
                    "id", created.getId().toString(),
                    "username", created.getUsername(),
                    "role", created.getRole(),
                    "fullName", created.getFullName(),
                    "email", created.getEmail()
            );
            return Response.status(Response.Status.CREATED).entity(resp).build();
        } catch (jakarta.persistence.PersistenceException pe) {
            // likely unique constraint violation or other DB error
            return Response.status(Response.Status.CONFLICT)
                    .entity(java.util.Map.of("error", "Could not create user: " + pe.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("error", "Failed to create user: " + e.getMessage()))
                    .build();
        }
    }
}
