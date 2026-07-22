package com.sim.chatserver.web.admin;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sim.chatserver.term.TermDefinition;
import com.sim.chatserver.term.TermsStore;

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

        Long idObj = payload.getJsonNumber("id") == null ? null : Long.valueOf(payload.getJsonNumber("id").toString());
        String name = payload.getString("name", "").trim();
        String description = payload.getString("description", "").trim();
        String pattern = payload.getString("matchPattern", "").trim();
        String type = payload.getString("matchType", "WILDCARD").trim();

        if (idObj == null || name.isEmpty() || description.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"id, name, and description are required.\"}");
            return;
        }
        long id = idObj;

        try {
            TermDefinition updated = termsStore().updateTerm(id, name, description, pattern, type);
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
                        .add("id", updated.getId() == null ? 0L : updated.getId())
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
            boolean deleted = termsStore().deleteTerm(id);
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
        return RequestParamContext.from(req).first(name);
    }

    private boolean isValidJsonRequest(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        long len = req.getContentLengthLong();
        return len >= 0 && len <= MAX_JSON_PAYLOAD_BYTES;
    }

    private String readRequestBody(HttpServletRequest req) throws IOException {
        if (req == null) {
            return "";
        }
        StringBuilder body = new StringBuilder(Math.min(MAX_JSON_PAYLOAD_BYTES, 4096));
        char[] buffer = new char[2048];
        int total = 0;
        try (var reader = req.getReader()) {
            int read;
            while ((read = reader.read(buffer)) != -1) {
                total += read;
                if (total > MAX_JSON_PAYLOAD_BYTES) {
                    return "";
                }
                body.append(buffer, 0, read);
            }
        }
        return body.toString().replace("\u0000", "").replace("\r", "").trim();
    }

    private TermsStore termsStore() {
        if (termsStore != null) {
            return termsStore;
        }
        return CDI.current().select(TermsStore.class).get();
    }

    private static final class RequestParamContext {

        private final HttpServletRequest request;

        private RequestParamContext(HttpServletRequest request) {
            this.request = request;
        }

        private static RequestParamContext from(HttpServletRequest request) {
            return new RequestParamContext(request);
        }

        private String first(String name) {
            if (request == null || name == null || name.isBlank()) {
                return null;
            }
            String[] values = request.getParameterValues(name);
            if (values == null || values.length == 0 || values[0] == null) {
                return null;
            }
            String trimmed = values[0].replace("\u0000", "").replace("\r", "").replace("\n", "").trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed.length() > 128 ? trimmed.substring(0, 128) : trimmed;
        }
    }
}
