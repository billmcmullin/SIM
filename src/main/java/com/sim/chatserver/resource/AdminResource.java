package com.sim.chatserver.resource;

import com.sim.chatserver.dto.SyncRequest;
import com.sim.chatserver.service.SyncService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/admin")
public class AdminResource {

    @Inject
    SyncService syncService;

    @POST
    @Path("/sync")
    @RolesAllowed("ADMIN")
    public Response triggerSync(SyncRequest req) {
        // Kick off background sync (TODO: use EJB timer or executor)
        syncService.runSync(req.scope);
        return Response.accepted().entity("Sync started").build();
    }
}
