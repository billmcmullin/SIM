package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.service.translation.DefaultTranslationService;
import com.sim.chatserver.service.translation.TranslationService;
import com.sim.chatserver.service.translation.TranslationService.TranslationResult;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ReviewTranslateServlet", urlPatterns = {"/dashboard/widgets/drilldown/review/translate"})
public class ReviewTranslateServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ReviewTranslateServlet.class.getName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 16 * 1024;

    private static final TranslationService TRANSLATION_SERVICE = new DefaultTranslationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());

        if (!isLoggedIn(req)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Authentication required.")
                    .build());
            return;
        }

        JsonObject payload;
        try (JsonReader jsonReader = Json.createReader(new StringReader(readRequestBody(req)))) {
            payload = jsonReader.readObject();
        } catch (jakarta.json.JsonException | IllegalArgumentException ex) {
            log.log(Level.FINE, "Invalid translate payload", ex);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Invalid JSON body.")
                    .build());
            return;
        }

        String text = payload.getString("text", "");
        String targetLang = payload.getString("targetLang", "en");

        if (text == null || text.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "text is required.")
                    .build());
            return;
        }

        try {
            TranslationResult result = TRANSLATION_SERVICE.detectAndTranslate(text, targetLang);

            JsonObjectBuilder out = Json.createObjectBuilder()
                    .add("status", result.isSuccess() ? "ok" : "error")
                    .add("sourceLang", nvl(result.getSourceLang()))
                    .add("targetLang", nvl(result.getTargetLang()))
                    .add("translatedText", nvl(result.getTranslatedText()))
                    .add("message", nvl(result.getMessage()));

            if (!result.isSuccess()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            writeJson(resp, out.build());
        } catch (Throwable ex) {
            log.log(Level.SEVERE, "Translate request failed", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to translate at this time.")
                    .build());
        }
    
        } catch (Throwable e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doPost", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        writeJson(resp, Json.createObjectBuilder()
                .add("status", "error")
                .add("message", "POST required.")
                .build());
    
        } catch (Throwable e) {
            java.util.logging.Logger.getLogger("OWASP")
                    .log(java.util.logging.Level.WARNING, "Unhandled exception in doGet", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Request handling failed.");
                } catch (java.io.IOException ioe) {
                    java.util.logging.Logger.getLogger("OWASP")
                            .log(java.util.logging.Level.FINE, "Failed sending fallback server error.", ioe);
                }
            }
        }
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    private void writeJson(HttpServletResponse resp, JsonObject obj) {
        int status = resp.getStatus() <= 0 ? HttpServletResponse.SC_OK : resp.getStatus();
        try {
            ServletJsonResponseUtil.writeJson(resp, status, obj == null ? Json.createObjectBuilder().build() : obj);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write translate response", e);
            throw new IllegalStateException("Unable to write response", e);
        }
    }

    private String readRequestBody(HttpServletRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Missing request");
        }
        long len = req.getContentLengthLong();
        if (len < 0 || len > MAX_JSON_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("Invalid content length");
        }

        try {
            try (BufferedReader reader = req.getReader()) {
                return ServletRequestParamUtil.readNormalizedBodyText(reader, MAX_JSON_PAYLOAD_BYTES);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid JSON payload", ex);
        }
    }

    private String nvl(String v) {
        return v == null ? "" : v;
    }
}
