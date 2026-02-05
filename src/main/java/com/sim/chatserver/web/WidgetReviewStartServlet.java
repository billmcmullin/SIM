package com.sim.chatserver.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sim.chatserver.term.TermChatSnapshot;

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
        final String displayName;
        final String backUrl;
        final List<String> chatIds;
        final SearchTerms searchTerms;
        final List<TermChatSnapshot> snapshots;

        private Selection(String widgetId,
                String displayName,
                String backUrl,
                List<String> chatIds,
                List<TermChatSnapshot> snapshots,
                SearchTerms searchTerms) {
            this.widgetId = widgetId;
            this.displayName = displayName;
            this.backUrl = backUrl;
            this.chatIds = chatIds;
            this.searchTerms = searchTerms;
            this.snapshots = snapshots;
        }

        static Selection fromWidget(String widgetId, List<String> chatIds, SearchTerms searchTerms) {
            return new Selection(widgetId, widgetId, null, new ArrayList<>(chatIds), null, searchTerms);
        }

        static Selection fromTermSnapshots(String displayName, String backUrl, List<TermChatSnapshot> snapshots) {
            List<String> chatIds = snapshots.stream()
                    .map(TermChatSnapshot::getChatId)
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .toList();
            return new Selection(displayName, displayName, backUrl, chatIds, new ArrayList<>(snapshots),
                    new SearchTerms("", "", ""));
        }

        boolean hasSnapshots() {
            return snapshots != null && !snapshots.isEmpty();
        }

        String getBackUrl() {
            return backUrl;
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

        Selection selection = Selection.fromWidget(
                widgetId,
                new ArrayList<>(chatSet),
                new SearchTerms(global, prompt, responseText)
        );

        String selectionId = storeSelection(session, selection);

        resp.setContentType("application/json");
        resp.getWriter().write(Json.createObjectBuilder()
                .add("selectionId", selectionId)
                .build()
                .toString());
    }

    public static String createSnapshotSelection(HttpSession session,
            String label,
            List<TermChatSnapshot> snapshots,
            String backUrl) {
        if (session == null || label == null || label.isBlank() || snapshots == null || snapshots.isEmpty()) {
            return null;
        }
        Selection selection = Selection.fromTermSnapshots(label, backUrl, snapshots);
        return storeSelection(session, selection);
    }

    private static String storeSelection(HttpSession session, Selection selection) {
        Map<String, Selection> selections = getSelectionMap(session);
        String selectionId = UUID.randomUUID().toString();
        selections.put(selectionId, selection);
        return selectionId;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Selection> getSelectionMap(HttpSession session) {
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
