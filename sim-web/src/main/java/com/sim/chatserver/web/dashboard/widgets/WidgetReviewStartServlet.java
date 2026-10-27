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
    private static final Logger log = Logger.getLogger(WidgetReviewStartServlet.class.getCanonicalName());
    private static final String SESSION_KEY = "widgetReviewSelections";
    private static final int MAX_SELECTIONS_PER_SESSION = 200;
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final SearchTerms EMPTY_SEARCH_TERMS = new SearchTerms("", "", "");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED, null);
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

        String widgetId = safe(payload.getString(com.sim.chatserver.util.CommonLiteralConstants.KEY_WIDGET_ID, ""));
        var chatArray = payload.getJsonArray(com.sim.chatserver.util.CommonLiteralConstants.KEY_SELECTED_CHAT_IDS);
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
        String prompt = search == null ? "" : search.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_PROMPT, "");
        String responseText = search == null ? "" : search.getString(com.sim.chatserver.util.CommonLiteralConstants.FIELD_RESPONSE, "");
        String date = payload.getString(com.sim.chatserver.util.CommonLiteralConstants.PARAM_DATE, "");

        Selection selection = Selection.fromWidget(
                widgetId,
                new ArrayList<>(chatSet),
                new SearchTerms(global, prompt, responseText),
                date
        );

        String selectionId = storeSelection(session, selection);

        JsonObject ok = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add(com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID, selectionId)
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
        if (com.sim.chatserver.service.WorkflowRuntime.isInitialized()) {
            String selectionId = UUID.randomUUID().toString();
            com.sim.chatserver.service.WorkflowRuntime.records().insert(selectionOwner(session), selectionId,
                    SelectionCodec.encode(selection), 7L * 24 * 60 * 60 * 1000);
            return selectionId;
        }
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
        if (com.sim.chatserver.service.WorkflowRuntime.isInitialized()) {
            var record = com.sim.chatserver.service.WorkflowRuntime.records().find(selectionOwner(session), selectionId.trim());
            return record == null ? null : SelectionCodec.decode(record.payload());
        }
        Map<String, Selection> selections = (Map<String, Selection>) session.getAttribute(SESSION_KEY);
        if (selections == null) {
            return null;
        }
        return selections.get(selectionId.trim());
    }

    private static String selectionOwner(HttpSession session) {
        Object user = session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER);
        if (!(user instanceof String username) || username.isBlank()) {
            throw new IllegalStateException("A signed-in user is required to store or reopen a review selection");
        }
        return "review-selection:" + username;
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
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, com.sim.chatserver.util.CommonLiteralConstants.KEY_ERROR)
                .add(com.sim.chatserver.util.CommonLiteralConstants.FIELD_MESSAGE, safe(message));
        if (selectionId != null && !selectionId.isBlank()) {
            b.add(com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID, selectionId.trim());
        }
        writeJson(resp, status, b.build());
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(java.util.logging.Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_JSON_RESPONSE, e);
            try {
                if (!resp.isCommitted()) {
                    resp.sendError(status);
                }
            } catch (IOException sendErrorFailure) {
                log.log(java.util.logging.Level.FINE, "Unable to send fallback error response", sendErrorFailure);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}

