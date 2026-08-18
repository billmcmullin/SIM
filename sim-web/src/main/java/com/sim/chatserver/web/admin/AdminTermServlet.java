package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import com.sim.chatserver.util.TextIoSanitizerUtil;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            List<TermDefinition> terms = termsStore().listAll();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            terms.forEach(term -> {
                Long rawId = term.getId();
                long termId = rawId == null ? 0L : rawId.longValue();
                arrayBuilder.add(Json.createObjectBuilder()
                    .add("id", termId)
                    .add("name", term.getName())
                    .add("description", term.getDescription())
                    .add("matchPattern", term.getMatchPattern())
                    .add("matchType", term.getMatchType())
                    .add("isSystem", term.isSystemFlag())
                    .build());
            });
            JsonObject response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("terms", arrayBuilder)
                    .build();
            writeJsonSafe(resp, HttpServletResponse.SC_OK, response);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Unable to list terms", e);
            writeErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load term definitions.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.log(Level.FINE, "Unable to set request character encoding", e);
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
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
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (name.isEmpty() || description.isEmpty()) {
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Name and description are required.");
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
                        .add("id", term.getId() == null ? 0L : term.getId().longValue())
                            .add("name", term.getName())
                            .add("description", term.getDescription())
                            .add("matchPattern", term.getMatchPattern())
                            .add("matchType", term.getMatchType())
                            .add("isSystem", term.isSystemFlag()))
                    .build();
            writeJsonSafe(resp, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to create term", e);
            writeErrorSafe(resp, HttpServletResponse.SC_CONFLICT, "Term already exists or could not be inserted.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req, resp)) {
            return;
        }

        try {
            req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.log(Level.FINE, "Unable to set request character encoding", e);
        }

        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
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
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        Long idObj = null;
        try {
            idObj = payload.getJsonNumber("id") == null ? null : Long.valueOf(payload.getJsonNumber("id").toString());
        } catch (NumberFormatException ex) {
            log.log(Level.FINE, "Invalid term id payload", ex);
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id.");
            return;
        }
        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (idObj == null || name.isEmpty() || description.isEmpty()) {
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "id, name, and description are required.");
            return;
        }

        try {
            TermDefinition updated = termsStore().updateTerm(idObj, name, description, pattern, type);
            if (updated == null) {
                writeErrorSafe(resp, HttpServletResponse.SC_FORBIDDEN, "System terms cannot be modified.");
                return;
            }
            JsonObject body = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("term", Json.createObjectBuilder()
                        .add("id", updated.getId() == null ? 0L : updated.getId().longValue())
                            .add("name", updated.getName())
                            .add("description", updated.getDescription())
                            .add("matchPattern", updated.getMatchPattern())
                            .add("matchType", updated.getMatchType())
                            .add("isSystem", updated.isSystemFlag()))
                    .build();
            writeJsonSafe(resp, HttpServletResponse.SC_OK, body);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to update term", e);
            writeErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update term.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        if (!isAdmin(req, resp)) {
            return;
        }
        String idParam = ServletRequestParamUtil.firstParam(req, "id", 128, true, true);
        if (idParam == null || !SAFE_LONG_PARAM.matcher(idParam).matches()) {
            writeErrorSafe(resp, HttpServletResponse.SC_BAD_REQUEST, "id is required.");
            return;
        }

        try {
            Long id = Long.valueOf(idParam);
            boolean deleted = termsStore().deleteTerm(id);
            if (!deleted) {
                writeErrorSafe(resp, HttpServletResponse.SC_FORBIDDEN, "System terms cannot be deleted or term not found.");
                return;
            }
            writeJsonSafe(
                    resp,
                    HttpServletResponse.SC_OK,
                    Json.createObjectBuilder().add("status", "ok").build());
        } catch (NumberFormatException | SQLException e) {
            log.log(Level.WARNING, "Failed to delete term", e);
            writeErrorSafe(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to delete term.");
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeErrorSafe(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return false;
        }
        String role = session.getAttribute("role") == null ? "" : session.getAttribute("role").toString();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            writeErrorSafe(resp, HttpServletResponse.SC_FORBIDDEN, "Admin role required.");
            return false;
        }
        return true;
    }

    private String readRequestBody(HttpServletRequest req) {
        if (req == null) {
            return "";
        }
        try {
            java.io.Reader reader = req.getReader();
            if (reader == null) {
                return "";
            }
            try {
                String body = ServletRequestParamUtil.readNormalizedBodyTextOrEmptyOnLimit(reader, MAX_JSON_PAYLOAD_BYTES);
                return validateCanonicalizedBodyText(body);
            } finally {
                reader.close();
            }
        } catch (IOException | IllegalStateException e) {
            log.log(Level.WARNING, "Failed reading request body", e);
            return "";
        }
    }

    private String validateCanonicalizedBodyText(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String canonical = TextIoSanitizerUtil.canonicalize(value);
        String normalizedInput = ServletRequestParamUtil.normalizeBodyText(canonical, MAX_JSON_PAYLOAD_BYTES, false);
        if (normalizedInput == null || normalizedInput.isEmpty()) {
            return "";
        }
        StringBuilder safe = new StringBuilder(Math.min(normalizedInput.length(), MAX_JSON_PAYLOAD_BYTES));
        int limit = Math.min(normalizedInput.length(), MAX_JSON_PAYLOAD_BYTES);
        for (int i = 0; i < limit; i++) {
            char ch = normalizedInput.charAt(i);
            if (Character.isISOControl(ch) && ch != '\n' && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }
        return safe.toString();
    }

    private void writeErrorSafe(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed writing error response", e);
            sendFallbackServerError(resp);
        }
    }

    private void writeJsonSafe(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed writing JSON response", e);
            sendFallbackServerError(resp);
        }
    }

    private void sendFallbackServerError(HttpServletResponse resp) {
        if (resp == null || resp.isCommitted()) {
            return;
        }
        try {
            resp.reset();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
        } catch (IOException ioe) {
            log.log(Level.FINE, "Failed sending fallback server error", ioe);
        }
    }

    private TermsStore termsStore() {
        return CDI.current().select(TermsStore.class).get();
    }

}
