package com.sim.chatserver.resource;

import java.util.Collections;
import java.util.List;

import com.sim.chatserver.model.Chat;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/chats")
@Produces(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    AppDataSourceHolder holder;

    private EntityManager createEmOrThrow() {
        EntityManagerFactory emf = holder.getEmf();
        if (emf == null) {
            throw new WebApplicationException("Database not configured", Response.Status.SERVICE_UNAVAILABLE);
        }
        return emf.createEntityManager();
    }

    @GET
    public Response listChats(@QueryParam("widget") String widget,
                              @QueryParam("term") String term,
                              @QueryParam("page") @DefaultValue("1") int page,
                              @QueryParam("perPage") @DefaultValue("50") int perPage) {
        // If DB not configured, return 503
        EntityManager em;
        try {
            em = createEmOrThrow();
        } catch (WebApplicationException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Collections.singletonMap("error", "Database not configured")).build();
        }

        try {
            StringBuilder q = new StringBuilder("SELECT c FROM Chat c");
            boolean where = false;
            if (widget != null && !widget.isBlank()) {
                q.append(" WHERE c.embedUuid = :w");
                where = true;
            }
            // Note: term matching is simplified: you should use match_entity table or full-text search for production
            if (term != null && !term.isBlank()) {
                if (!where) {
                    q.append(" WHERE");
                } else {
                    q.append(" AND");
                }
                q.append(" (lower(c.prompt) LIKE :t OR lower(c.text) LIKE :t)");
            }
            q.append(" ORDER BY c.createdAt DESC");

            var query = em.createQuery(q.toString(), Chat.class);
            if (widget != null && !widget.isBlank()) query.setParameter("w", widget);
            if (term != null && !term.isBlank()) query.setParameter("t", "%" + term.toLowerCase() + "%");

            int offset = (page - 1) * perPage;
            List<Chat> items = query.setFirstResult(offset).setMaxResults(perPage).getResultList();

            long total = 0;
            try {
                String countQ = q.toString().replaceFirst("SELECT c FROM", "SELECT COUNT(c) FROM");
                var countQuery = em.createQuery(countQ, Long.class);
                if (widget != null && !widget.isBlank()) countQuery.setParameter("w", widget);
                if (term != null && !term.isBlank()) countQuery.setParameter("t", "%" + term.toLowerCase() + "%");
                total = countQuery.getSingleResult();
            } catch (Exception ex) {
                // fallback: derive from list size when count query fails
                total = items.size();
            }

            return Response.ok(Collections.unmodifiableMap(
                    java.util.Map.of("total", total, "page", page, "perPage", perPage, "items", items)))
                    .build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response getChat(@PathParam("id") String id) {
        EntityManager em;
        try {
            em = createEmOrThrow();
        } catch (WebApplicationException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Collections.singletonMap("error", "Database not configured")).build();
        }
        try {
            Chat c = em.find(Chat.class, id);
            if (c == null) return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(c).build();
        } finally {
            em.close();
        }
    }
}
