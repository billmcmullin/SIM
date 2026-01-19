package com.sim.chatserver.resource;

import java.util.List;

import com.sim.chatserver.model.WidgetMapping;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

@Path("/widgets")
@Produces(MediaType.APPLICATION_JSON)
public class WidgetResource {

    @Inject
    AppDataSourceHolder holder;

    private EntityManager createEm() {
        EntityManagerFactory emf = holder.getEmf();
        if (emf == null) {
            // DB not configured yet -> return 503
            throw new WebApplicationException("Database not configured", 503);
        }
        return emf.createEntityManager();
    }

    @GET
    public List<WidgetMapping> listWidgets() {
        EntityManager em = createEm();
        try {
            return em.createQuery("SELECT w FROM WidgetMapping w", WidgetMapping.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }
}
