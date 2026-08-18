package com.sim.chatserver.web.dashboard.widgets;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetReviewStartServlet", urlPatterns = {"/dashboard/widgets/review/start"})
public class WidgetReviewStartServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(WidgetReviewStartServlet.class.getName());
    private static final String SESSION_KEY = "widgetReviewSelections";
    private static final int MAX_SELECTIONS_PER_SESSION = 200;
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;

    public static final class Selection {

        public final String widgetId;
        public final String displayName;
        public final String backUrl;
        public final List<String> chatIds;
        public final SearchTerms searchTerms;
        public final List<TermChatSnapshot> snapshots;
        public final String date; // optional YYYY-MM-DD scope

        private Selection(String widgetId,
                String displayName,
                String backUrl,
                List<String> chatIds,
                List<TermChatSnapshot> snapshots,
                SearchTerms searchTerms,
                String date) {
            this.widgetId = widgetId;
            this.displayName = displayName;
            this.backUrl = backUrl;
            this.chatIds = chatIds;
            this.searchTerms = searchTerms;
            this.snapshots = snapshots;
            this.date = date;
        }

        static Selection fromWidget(String widgetId, List<String> chatIds, SearchTerms searchTerms, String date) {
            return new Selection(
                    safe(widgetId),
                    safe(widgetId),
                    null,
                    new ArrayList<>(chatIds),
                    null,
                    searchTerms == null ? new SearchTerms("", "", "") : searchTerms,
                    normalizeDate(date)
            );
        }

        static Selection fromWidget(String widgetId, List<String> chatIds, SearchTerms searchTerms) {
            return fromWidget(widgetId, chatIds, searchTerms, null);
        }

        static Selection fromTermSnapshots(String displayName, String backUrl, List<TermChatSnapshot> snapshots) {
            List<String> chatIds = snapshots.stream()
                    .map(TermChatSnapshot::getChatId)
                    .filter(v -> v != null && !v.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .toList();

            return new Selection(
                    safe(displayName),
                    safe(displayName),
                    safe(backUrl),
                    chatIds,
                    new ArrayList<>(snapshots),
                    new SearchTerms("", "", ""),
                    null
            );
        }

        public boolean hasSnapshots() {
            return snapshots != null && !snapshots.isEmpty();
        }

        public String getBackUrl() {
            return backUrl;
        }

        public String getDate() {
            return date;
        }
    }

    public static final class SearchTerms {

        public final String global;
        public final String prompt;
        public final String response;

        private SearchTerms(String global, String prompt, String response) {
            this.global = safe(global);
            this.prompt = safe(prompt);
            this.response = safe(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.", null);
            return;
        }

        JsonObject payload;
        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid payload.", null);
            return;
        }
        String payloadText = readValidatedJsonPayload(req);
        if (payloadText == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid payload.", null);
            return;
        }
        try (var reader = Json.createReader(new StringReader(payloadText))) {
            payload = reader.readObject();
        } catch (JsonException e) {
            log.log(java.util.logging.Level.FINE, "Unable to parse widget review start payload", e);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid payload.", null);
            return;
        }

        String widgetId = safe(payload.getString("widgetId", ""));
        var chatArray = payload.getJsonArray("selectedChatIds");
        if (widgetId.isBlank() || chatArray == null || chatArray.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "widgetId and selections required.", null);
            return;
        }

        LinkedHashSet<String> chatSet = new LinkedHashSet<>();
        chatArray.forEach(value -> {
            String chatId = value.getValueType() == JsonValue.ValueType.STRING
                    ? ((JsonString) value).getString().trim()
                    : "";
            if (!chatId.isEmpty()) {
                chatSet.add(chatId);
            }
        });

        if (chatSet.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "At least one chat must be selected.", null);
            return;
        }

        JsonObject search = payload.getJsonObject("searchTerms");
        String global = search == null ? "" : search.getString("global", "");
        String prompt = search == null ? "" : search.getString("prompt", "");
        String responseText = search == null ? "" : search.getString("response", "");
        String date = payload.getString("date", "");

        Selection selection = Selection.fromWidget(
                widgetId,
                new ArrayList<>(chatSet),
                new SearchTerms(global, prompt, responseText),
                date
        );

        String selectionId = storeSelection(session, selection);

        JsonObject ok = Json.createObjectBuilder()
                .add("status", "ok")
                .add("selectionId", selectionId)
                .build()
            ;
        writeJson(resp, HttpServletResponse.SC_OK, ok);
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

        // prevent unbounded session growth
        while (selections.size() >= MAX_SELECTIONS_PER_SESSION) {
            Iterator<String> it = selections.keySet().iterator();
            if (!it.hasNext()) {
                break;
            }
            it.next();
            it.remove();
        }

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

    @SuppressWarnings("unchecked")
    public static Selection fetchSelection(HttpSession session, String selectionId) {
        if (session == null || selectionId == null || selectionId.isBlank()) {
            return null;
        }
        Map<String, Selection> selections = (Map<String, Selection>) session.getAttribute(SESSION_KEY);
        if (selections == null) {
            return null;
        }
        return selections.get(selectionId.trim());
    }

    static String createSelectionFromGlobalChatIds(HttpSession session,
            List<String> chatIds,
            String label,
            String backUrl) {
        if (session == null || chatIds == null || chatIds.isEmpty()) {
            return null;
        }

        LinkedHashSet<String> dedup = new LinkedHashSet<>();
        for (String id : chatIds) {
            if (id == null) {
                continue;
            }
            String t = id.trim();
            if (!t.isEmpty()) {
                dedup.add(t);
            }
        }
        if (dedup.isEmpty()) {
            return null;
        }

        String safeLabel = (label == null || label.isBlank()) ? "Selected Chats" : label;

        Selection selection = new Selection(
                safeLabel, // widgetId placeholder for global selections
                safeLabel, // displayName
                safe(backUrl), // backUrl
                new ArrayList<>(dedup), // chatIds
                null, // snapshots (DB-backed selection)
                new SearchTerms("", "", ""), // search terms
                null // date scope
        );

        return storeSelection(session, selection);
    }

    static String normalizeDate(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    static String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String readValidatedJsonPayload(HttpServletRequest req) {
        if (req == null || !ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            return null;
        }

        String json;
        try {
            var reader = req.getReader();
            try {
                json = ServletRequestParamUtil.readNormalizedBodyText(reader, MAX_JSON_PAYLOAD_BYTES, 4096);
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            log.log(java.util.logging.Level.FINE, "Unable to read widget review start payload", e);
            return null;
        }
        if (json == null) {
            return null;
        }

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isISOControl(c) && !Character.isWhitespace(c)) {
                return null;
            }
        }

        json = json.trim();
        if (json.isEmpty() || json.charAt(0) != '{') {
            return null;
        }
        return json;
    }

    private void writeError(HttpServletResponse resp, int status, String message, String selectionId) {
        var b = Json.createObjectBuilder()
                .add("status", "error")
                .add("message", safe(message));
        if (selectionId != null && !selectionId.isBlank()) {
            b.add("selectionId", selectionId.trim());
        }
        writeJson(resp, status, b.build());
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(java.util.logging.Level.FINE, "Unable to write JSON response", e);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(java.util.logging.Level.FINE, "Unable to send fallback error response", sendErrorFailure);
            }
        }
    }
}
