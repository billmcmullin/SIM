package com.sim.chatserver.resource;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * Export endpoint skeleton.
 */
@Path("/export")
public class ExportResource {

    @POST
    @Path("/csv")
    public Response exportCsv() {
        // TODO: generate CSV from DB and stream response
        return Response.ok("CSV export not implemented yet").build();
    }
}
