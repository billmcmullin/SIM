package com.sim.chatserver.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminTermServlet", urlPatterns = {"/admin/terms"})
public class AdminTermServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminTermServlet.class.getName());

    @Inject
    TermsStore termsStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            List<TermDefinition> terms = termsStore.listAll();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            terms.forEach(term -> arrayBuilder.add(Json.createObjectBuilder()
                    .add("id", term.getId())
                    .add("name", term.getName())
                    .add("description", term.getDescription())
                    .add("matchPattern", term.getMatchPattern())
                    .add("matchType", term.getMatchType())
                    .add("isSystem", term.isSystemFlag())
                    .build()));
            JsonObject response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("terms", arrayBuilder)
                    .build();
            resp.setContentType("application/json");
            resp.getWriter().write(response.toString());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to list terms", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load term definitions.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        JsonObject payload;
        try (var reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (JsonException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (name.isEmpty() || description.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Name and description are required.\"}");
            return;
        }

        try {
            TermDefinition term = termsStore.createTerm(name, description, pattern, type);
            if (term == null) {
                throw new SQLException("Insert failed.");
            }
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("term", Json.createObjectBuilder()
                            .add("id", term.getId())
                            .add("name", term.getName())
                            .add("description", term.getDescription())
                            .add("matchPattern", term.getMatchPattern())
                            .add("matchType", term.getMatchType())
                            .add("isSystem", term.isSystemFlag()))
                    .build();
            resp.setContentType("application/json");
            resp.getWriter().write(body.toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to create term", e);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Term already exists or could not be inserted.\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }
        JsonObject payload;
        try (var reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (JsonException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        Long id = payload.getJsonNumber("id") == null ? null : payload.getJsonNumber("id").longValue();
        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (id == null || name.isEmpty() || description.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"id, name, and description are required.\"}");
            return;
        }

        try {
            TermDefinition updated = termsStore.updateTerm(id, name, description, pattern, type);
            if (updated == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"System terms cannot be modified.\"}");
                return;
            }
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("term", Json.createObjectBuilder()
                            .add("id", updated.getId())
                            .add("name", updated.getName())
                            .add("description", updated.getDescription())
                            .add("matchPattern", updated.getMatchPattern())
                            .add("matchType", updated.getMatchType())
                            .add("isSystem", updated.isSystemFlag()))
                    .build();
            resp.setContentType("application/json");
            resp.getWriter().write(body.toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to update term", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to update term.\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"id is required.\"}");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            boolean deleted = termsStore.deleteTerm(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"System terms cannot be deleted or term not found.\"}");
                return;
            }
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } catch (NumberFormatException | SQLException e) {
            log.log(Level.WARNING, "Failed to delete term", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to delete term.\"}");
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Admin role required.\"}");
            return false;
        }
        return true;
    }
}
