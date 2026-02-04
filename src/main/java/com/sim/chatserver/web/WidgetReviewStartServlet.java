package com.sim.chatserver.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetReviewStartServlet", urlPatterns = {"/admin/widgets/review/start"})
public class WidgetReviewStartServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewStartServlet.class.getName());
    private static final String SESSION_KEY = "widgetReviewSelections";

    public static final class Selection {

        final String widgetId;
        final List<String> chatIds;
        final SearchTerms searchTerms;

        Selection(String widgetId, List<String> chatIds, SearchTerms searchTerms) {
            this.widgetId = widgetId;
            this.chatIds = chatIds;
            this.searchTerms = searchTerms;
        }
    }

    public static final class SearchTerms {

        final String global;
        final String prompt;
        final String response;

        SearchTerms(String global, String prompt, String response) {
            this.global = global;
            this.prompt = prompt;
            this.response = response;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"message\":\"Authentication required.\"}");
            return;
        }

        JsonObject payload;
        try (var reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (JsonException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Invalid payload.\"}");
            return;
        }

        String widgetId = payload.getString("widgetId", "").trim();
        var chatArray = payload.getJsonArray("selectedChatIds");
        if (widgetId.isBlank() || chatArray == null || chatArray.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"widgetId and selections required.\"}");
            return;
        }

        LinkedHashSet<String> chatSet = new LinkedHashSet<>();
        chatArray.forEach(value -> {
            String chatId = value.toString().replace("\"", "").trim();
            if (!chatId.isEmpty()) {
                chatSet.add(chatId);
            }
        });

        if (chatSet.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"At least one chat must be selected.\"}");
            return;
        }

        JsonObject search = payload.getJsonObject("searchTerms");
        String global = search == null ? "" : search.getString("global", "");
        String prompt = search == null ? "" : search.getString("prompt", "");
        String responseText = search == null ? "" : search.getString("response", "");

        Selection selection = new Selection(
                widgetId,
                new ArrayList<>(chatSet),
                new SearchTerms(global, prompt, responseText)
        );

        Map<String, Selection> selections = getSelectionMap(session);
        String selectionId = UUID.randomUUID().toString();
        selections.put(selectionId, selection);

        resp.setContentType("application/json");
        resp.getWriter().write(Json.createObjectBuilder()
                .add("selectionId", selectionId)
                .build()
                .toString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Selection> getSelectionMap(HttpSession session) {
        Object existing = session.getAttribute(SESSION_KEY);
        if (existing instanceof Map<?, ?> map) {
            return (Map<String, Selection>) map;
        }
        Map<String, Selection> newMap = new LinkedHashMap<>();
        session.setAttribute(SESSION_KEY, newMap);
        return newMap;
    }

    public static Selection fetchSelection(HttpSession session, String selectionId) {
        if (session == null || selectionId == null) {
            return null;
        }
        Map<String, Selection> selections = (Map<String, Selection>) session.getAttribute(SESSION_KEY);
        if (selections == null) {
            return null;
        }
        return selections.get(selectionId);
    }
}
