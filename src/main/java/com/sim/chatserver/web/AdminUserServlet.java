package com.sim.chatserver.web;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.service.UserService;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AdminUserServlet.class.getName());

    @Inject
    UserService userService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"application/json".equalsIgnoreCase(req.getContentType())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Content-Type must be application/json\"}");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Authentication required\"}");
            return;
        }

        String roleAttr = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(roleAttr)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Admin role required\"}");
            return;
        }

        JsonObject payload;
        try (var reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid JSON\"}");
            return;
        }

        String username = payload.getString("username", "").trim();
        String password = payload.getString("password", "");
        String role = payload.getString("role", "USER").toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"username and password are required\"}");
            return;
        }

        try {
            UserAccount created = userService.createUser(username, password, role);
            JsonObject responseBody = Json.createObjectBuilder()
                    .add("id", created.getId().toString())
                    .add("username", created.getUsername())
                    .add("role", created.getRole())
                    .add("fullName", created.getFullName())
                    .add("email", created.getEmail() == null ? "" : created.getEmail())
                    .build();
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("application/json");
            resp.getWriter().write(responseBody.toString());
        } catch (jakarta.persistence.PersistenceException pe) {
            log.log(Level.WARNING, "User creation conflict", pe);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("{\"error\":\"Could not create user: " + escape(pe.getMessage()) + "\"}");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to create user", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Failed to create user\"}");
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
