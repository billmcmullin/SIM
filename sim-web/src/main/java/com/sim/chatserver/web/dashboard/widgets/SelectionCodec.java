package com.sim.chatserver.web.dashboard.widgets;

import java.io.StringReader;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.CommonLiteralConstants;

final class SelectionCodec {
    private static final String EMPTY = "";
    private static final String FIELD_TERM_NAME = "termName";
    private static final String FIELD_BACK_URL = "backUrl";
    private static final String FIELD_CHAT_IDS = "chatIds";
    private static final String FIELD_HAS_SNAPSHOTS = "hasSnapshots";
    private static final String FIELD_SNAPSHOTS = "snapshots";
    private static final String FIELD_GLOBAL = "global";

    private SelectionCodec() { }

    static String encode(Selection selection) {
        var ids = Json.createArrayBuilder();
        selection.chatIds.forEach(ids::add);
        var snapshots = Json.createArrayBuilder();
        if (selection.snapshots != null) {
            for (TermChatSnapshot snapshot : selection.snapshots) {
                snapshots.add(Json.createObjectBuilder().add(FIELD_TERM_NAME, snapshot.getTermName())
                    .add(CommonLiteralConstants.KEY_WIDGET_ID, snapshot.getWidgetId()).add(CommonLiteralConstants.FIELD_CHAT_ID, snapshot.getChatId())
                    .add(CommonLiteralConstants.FIELD_PROMPT, snapshot.getPrompt()).add(CommonLiteralConstants.FIELD_RESPONSE, snapshot.getResponse())
                    .add(CommonLiteralConstants.FIELD_SESSION_ID, snapshot.getSessionId()).add(CommonLiteralConstants.FIELD_CREATED_AT,
                        snapshot.getCreatedAt() == null ? EMPTY : snapshot.getCreatedAt().toInstant().toString()));
            }
        }
        SearchTerms terms = selection.searchTerms == null ? SearchTerms.EMPTY : selection.searchTerms;
        return Json.createObjectBuilder().add(CommonLiteralConstants.KEY_WIDGET_ID, safe(selection.widgetId)).add(CommonLiteralConstants.FIELD_DISPLAY_NAME, safe(selection.displayName))
            .add(FIELD_BACK_URL, safe(selection.backUrl)).add(CommonLiteralConstants.PARAM_DATE, safe(selection.date)).add(FIELD_CHAT_IDS, ids)
            .add(FIELD_HAS_SNAPSHOTS, selection.snapshots != null).add(FIELD_SNAPSHOTS, snapshots)
            .add(FIELD_GLOBAL, terms.global).add(CommonLiteralConstants.FIELD_PROMPT, terms.prompt).add(CommonLiteralConstants.FIELD_RESPONSE, terms.response).build().toString();
    }

    static Selection decode(String payload) {
        try (var reader = Json.createReader(new StringReader(payload))) {
            JsonObject json = reader.readObject();
            List<TermChatSnapshot> snapshots = json.getBoolean(FIELD_HAS_SNAPSHOTS) ? new ArrayList<>() : null;
            if (snapshots != null) {
                for (JsonObject snapshot : json.getJsonArray(FIELD_SNAPSHOTS).getValuesAs(JsonObject.class)) {
                    String createdAt = snapshot.getString(CommonLiteralConstants.FIELD_CREATED_AT);
                    snapshots.add(new TermChatSnapshot(snapshot.getString(FIELD_TERM_NAME), snapshot.getString(CommonLiteralConstants.KEY_WIDGET_ID),
                            snapshot.getString(CommonLiteralConstants.FIELD_CHAT_ID), snapshot.getString(CommonLiteralConstants.FIELD_PROMPT), snapshot.getString(CommonLiteralConstants.FIELD_RESPONSE),
                            createdAt.isEmpty() ? null : Timestamp.from(Instant.parse(createdAt)), snapshot.getString(CommonLiteralConstants.FIELD_SESSION_ID)));
                }
            }
                return new Selection(json.getString(CommonLiteralConstants.KEY_WIDGET_ID), json.getString(CommonLiteralConstants.FIELD_DISPLAY_NAME), json.getString(FIELD_BACK_URL),
                    json.getJsonArray(FIELD_CHAT_IDS).getValuesAs(JsonString.class).stream().map(JsonString::getString).toList(),
                    snapshots, new SearchTerms(json.getString(FIELD_GLOBAL), json.getString(CommonLiteralConstants.FIELD_PROMPT), json.getString(CommonLiteralConstants.FIELD_RESPONSE)),
                    json.getString(CommonLiteralConstants.PARAM_DATE));
        }
    }

    private static String safe(String value) {
        return value == null ? EMPTY : value;
    }
}