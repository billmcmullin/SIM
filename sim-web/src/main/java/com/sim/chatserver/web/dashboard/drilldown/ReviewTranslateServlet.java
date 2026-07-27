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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ReviewTranslateServlet", urlPatterns = {"/dashboard/widgets/drilldown/review/translate"})
public class ReviewTranslateServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ReviewTranslateServlet.class.getName());
    private static final String JSON_UTF8 = "application/json; charset=UTF-8";
    private static final int MAX_JSON_PAYLOAD_BYTES = 16 * 1024;

    private static final TranslationService TRANSLATION_SERVICE = new DefaultTranslationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);

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
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.SEVERE, "Translate request failed", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(resp, Json.createObjectBuilder()
                    .add("status", "error")
                    .add("message", "Unable to translate at this time.")
                    .build());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(JSON_UTF8);
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        writeJson(resp, Json.createObjectBuilder()
                .add("status", "error")
                .add("message", "POST required.")
                .build());
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    private void writeJson(HttpServletResponse resp, JsonObject obj) throws IOException {
        try (JsonWriter writer = Json.createWriter(resp.getOutputStream())) {
            writer.writeObject(obj == null ? Json.createObjectBuilder().build() : obj);
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
            StringBuilder body = new StringBuilder();
            char[] buffer = new char[2048];
            int total = 0;
            int read;
            try (BufferedReader reader = req.getReader()) {
                while ((read = reader.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_JSON_PAYLOAD_BYTES) {
                        throw new IllegalArgumentException("Payload exceeds allowed size");
                    }
                    body.append(buffer, 0, read);
                }
            }
            return body.toString().replace("\u0000", "").replace("\r", "").trim();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid JSON payload", ex);
        }
    }

    private String nvl(String v) {
        return v == null ? "" : v;
    }
}
