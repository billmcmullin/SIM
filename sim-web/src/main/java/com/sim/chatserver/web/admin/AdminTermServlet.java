package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.enterprise.inject.spi.CDI;
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

    TermsStore termsStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            List<TermDefinition> terms = termsStore().listAll();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            terms.forEach(term -> arrayBuilder.add(Json.createObjectBuilder()
                    .add("id", term.getId() == null ? 0L : term.getId())
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
                ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to list terms", e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load term definitions.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        req.setCharacterEncoding("UTF-8");

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
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
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (name.isEmpty() || description.isEmpty()) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name and description are required.");
            return;
        }

        try {
            TermDefinition term = termsStore().createTerm(name, description, pattern, type);
            if (term == null) {
                throw new SQLException("Insert failed.");
            }
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("term", Json.createObjectBuilder()
                        .add("id", term.getId() == null ? 0L : term.getId())
                            .add("name", term.getName())
                            .add("description", term.getDescription())
                            .add("matchPattern", term.getMatchPattern())
                            .add("matchType", term.getMatchType())
                            .add("isSystem", term.isSystemFlag()))
                    .build();
                        ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to create term", e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_CONFLICT, "Term already exists or could not be inserted.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        req.setCharacterEncoding("UTF-8");

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
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
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        Long idObj = payload.getJsonNumber("id") == null ? null : Long.valueOf(payload.getJsonNumber("id").toString());
        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (idObj == null || name.isEmpty() || description.isEmpty()) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "id, name, and description are required.");
            return;
        }
        long id = idObj;

        try {
            TermDefinition updated = termsStore().updateTerm(id, name, description, pattern, type);
            if (updated == null) {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "System terms cannot be modified.");
                return;
            }
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("term", Json.createObjectBuilder()
                        .add("id", updated.getId() == null ? 0L : updated.getId())
                            .add("name", updated.getName())
                            .add("description", updated.getDescription())
                            .add("matchPattern", updated.getMatchPattern())
                            .add("matchType", updated.getMatchType())
                            .add("isSystem", updated.isSystemFlag()))
                    .build();
                        ServletJsonResponseUtil.writeJson(resp, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to update term", e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update term.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }
        String idParam = ServletRequestParamUtil.firstParam(req, "id", 128, true, true);
        if (idParam == null || !SAFE_LONG_PARAM.matcher(idParam).matches()) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "id is required.");
            return;
        }

        try {
            long id = Long.parseLong(idParam);
            boolean deleted = termsStore().deleteTerm(id);
            if (!deleted) {
                ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "System terms cannot be deleted or term not found.");
                return;
            }
            ServletJsonResponseUtil.writeJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    Json.createObjectBuilder().add("status", "ok").build());
        } catch (NumberFormatException | SQLException e) {
            log.log(Level.WARNING, "Failed to delete term", e);
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to delete term.");
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            ServletJsonResponseUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    private String readRequestBody(HttpServletRequest req) throws IOException {
        if (req == null) {
            return "";
        }
        try (InputStream in = req.getInputStream(); InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return ServletRequestParamUtil.readNormalizedBodyTextOrEmptyOnLimit(reader, MAX_JSON_PAYLOAD_BYTES);
        }
    }

    private TermsStore termsStore() {
        if (termsStore != null) {
            return termsStore;
        }
        return CDI.current().select(TermsStore.class).get();
    }

}
