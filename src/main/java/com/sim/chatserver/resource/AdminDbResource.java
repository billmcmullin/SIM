package com.sim.chatserver.resource;

import com.sim.chatserver.dto.DbConfig;
import com.sim.chatserver.service.ConfigStore;
import com.sim.chatserver.service.DbBootstrapService;
import com.sim.chatserver.service.UserService;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/db")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminDbResource {

    @Inject
    DbBootstrapService bootstrapSvc;

    @Inject
    AppDataSourceHolder holder;

    @Inject
    ConfigStore configStore;

    @Inject
    UserService userService;

    @POST
    @Path("/test")
    public Response test(DbConfig cfg) {
        try {
            var ds = bootstrapSvc.createAndTestDataSource(cfg.jdbcUrl, cfg.username, cfg.password, cfg.driverClass);
            if (ds instanceof AutoCloseable) {
                ((AutoCloseable) ds).close();
            }
            return Response.ok().entity(java.util.Map.of("ok", true)).build();
        } catch (Exception e) {
            return Response.status(400).entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/connect")
    public Response connect(DbConfig cfg) {
        try {
            // Attempt to auto-create DB if it doesn't exist (may throw if user lacks rights)
            try {
                bootstrapSvc.createDatabaseIfNotExists(cfg.jdbcUrl, cfg.username, cfg.password);
            } catch (Exception e) {
                // If creation fails, surface the message but still try to continue (the later pool creation will fail if DB missing)
                // You may prefer to return error here instead - keep as info so admins see the reason.
                // log.warn("Auto-create DB failed: " + e.getMessage(), e);
            }

            var ds = bootstrapSvc.createAndTestDataSource(cfg.jdbcUrl, cfg.username, cfg.password, cfg.driverClass);
            bootstrapSvc.runFlywayMigrations(ds);
            var emf = bootstrapSvc.buildEntityManagerFactory(ds);

            holder.setDataSource(ds);
            holder.setEmf(emf);

            try {
                configStore.saveEncrypted(cfg);
            } catch (Exception ignored) {
            }

            // Seed default admin if needed
            try {
                var existing = userService.findByUsername("admin");
                if (existing == null) {
                    userService.createUser("admin", "admin", "ADMIN");
                }
            } catch (Exception ignored) {
            }

            return Response.ok().entity(java.util.Map.of("ok", true)).build();
        } catch (Exception e) {
            return Response.serverError().entity(java.util.Map.of("error", e.getMessage())).build();
        }
    }

}
