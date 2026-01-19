package com.sim.chatserver.resource;

import java.util.Map;

import com.sim.chatserver.service.ConfigStore;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/db/status")
@Produces(MediaType.APPLICATION_JSON)
public class AdminDbStatusResource {

    @Inject
    AppDataSourceHolder holder;

    @Inject
    ConfigStore configStore;

    @GET
    public Map<String,Object> status() {
        boolean configured = false;
        try {
            if (holder != null && holder.getEmf() != null) configured = true;
            else {
                // check persisted config as fallback
                if (configStore != null && configStore.loadEncrypted() != null) configured = true;
            }
        } catch (Exception e) {
            // ignore errors — treat as not configured
        }
        return Map.of("configured", configured);
    }
}
