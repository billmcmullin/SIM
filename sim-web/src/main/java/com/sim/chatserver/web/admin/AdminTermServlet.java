package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final Pattern SAFE_LONG_PARAM = Pattern.compile("^\\d{1,18}$");
    private static final Pattern SAFE_CONTENT_TYPE = Pattern.compile("^application/json(?:\\s*;\\s*charset=[a-z0-9._\\-]+)?$");

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

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(response.toString());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to list terms", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to load term definitions.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        req.setCharacterEncoding("UTF-8");

        if (!isValidJsonRequest(req)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        JsonObject payload;
        try {
            String body = readRequestBody(req);
            try (var reader = Json.createReader(new StringReader(body))) {
                payload = reader.readObject();
            }
        } catch (JsonException e) {
            log.log(Level.FINE, "Invalid term create payload", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (name.isEmpty() || description.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
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

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(body.toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to create term", e);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Term already exists or could not be inserted.\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        req.setCharacterEncoding("UTF-8");

        if (!isValidJsonRequest(req)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid JSON payload.\"}");
            return;
        }

        JsonObject payload;
        try {
            String body = readRequestBody(req);
            try (var reader = Json.createReader(new StringReader(body))) {
                payload = reader.readObject();
            }
        } catch (JsonException e) {
            log.log(Level.FINE, "Invalid term update payload", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
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
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"id, name, and description are required.\"}");
            return;
        }

        try {
            TermDefinition updated = termsStore.updateTerm(id, name, description, pattern, type);
            if (updated == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("application/json;charset=UTF-8");
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

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(body.toString());
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to update term", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to update term.\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }
        String idParam = firstParam(req, "id");
        if (idParam == null || !SAFE_LONG_PARAM.matcher(idParam).matches()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"id is required.\"}");
            return;
        }

        try {
            long id = Long.parseLong(idParam);
            boolean deleted = termsStore.deleteTerm(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"System terms cannot be deleted or term not found.\"}");
                return;
            }
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } catch (NumberFormatException | SQLException e) {
            log.log(Level.WARNING, "Failed to delete term", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Unable to delete term.\"}");
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Admin role required.\"}");
            return false;
        }
        return true;
    }

    private String firstParam(HttpServletRequest req, String name) {
        if (req == null || name == null || name.isBlank()) {
            return null;
        }
        Map<String, String[]> params = req.getParameterMap();
        String[] values = params == null ? null : params.get(name);
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }
        String value = values[0];
        String trimmed = value.replace("\r", "").replace("\n", "").trim();
        return trimmed.length() > 128 ? trimmed.substring(0, 128) : trimmed;
    }

    private boolean isValidJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        String contentType = safeContentType(req);
        if (contentType.isEmpty() || !contentType.contains("application/json")) {
            return false;
        }
        long len = req.getContentLengthLong();
        return len >= 0 && len <= MAX_JSON_PAYLOAD_BYTES;
    }

    private String safeContentType(HttpServletRequest req) {
        if (req == null) {
            return "";
        }
        String header = req.getContentType();
        if (header == null) {
            return "";
        }
        String normalized = header.replace("\r", "").replace("\n", "").trim().toLowerCase(Locale.ROOT);
        String bounded = normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
        return SAFE_CONTENT_TYPE.matcher(bounded).matches() ? bounded : "";
    }

    private String readRequestBody(HttpServletRequest req) throws IOException {
        if (req == null) {
            return "";
        }
        byte[] bytes = req.getInputStream().readAllBytes();
        if (bytes.length > MAX_JSON_PAYLOAD_BYTES) {
            return "";
        }
        return new String(bytes, StandardCharsets.UTF_8)
                .replace("\u0000", "")
                .replace("\r", "")
                .trim();
    }
}
