package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import com.sim.chatserver.util.TextIoSanitizerUtil;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.web.util.ServletJsonResponseUtil;
import com.sim.chatserver.web.util.ServletRequestParamUtil;
import com.sim.chatserver.web.util.ServletPathUtil;
import com.sim.chatserver.web.dashboard.widgets.WidgetReviewStartServlet;
import com.sim.chatserver.widget.WidgetEntry;
import com.sim.chatserver.widget.WidgetStore;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsSelectServlet", urlPatterns = {"/dashboard/topics/select"})
public class DashboardTopicsSelectServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsSelectServlet.class.getCanonicalName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final String JSON_UTF8 = CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(com.sim.chatserver.util.CommonLiteralConstants.VALUE_USER) == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, com.sim.chatserver.util.CommonLiteralConstants.MSG_AUTHENTICATION_REQUIRED);
            return;
        }

        JsonObject payload;
        if (!isValidJsonPayloadRequest(req)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return;
        }
        try (JsonReader reader = Json.createReader(new StringReader(readRequestBody(req)))) {
            payload = reader.readObject();
        } catch (JsonException ex) {
            log.log(Level.FINE, "Invalid topics selection payload", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, com.sim.chatserver.util.CommonLiteralConstants.MSG_INVALID_JSON_PAYLOAD);
            return;
        }

        JsonArray arr = payload.getJsonArray(com.sim.chatserver.util.CommonLiteralConstants.KEY_SELECTED_CHAT_IDS);
        if (arr == null || arr.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "selectedChatIds required.");
            return;
        }

        Set<String> requestedIds = new LinkedHashSet<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonValue entry = arr.get(i);
            if (entry == null || entry.getValueType() != JsonValue.ValueType.STRING) {
                continue;
            }
            String id = arr.getString(i, "").trim();
            if (!id.isBlank()) {
                requestedIds.add(id);
            }
        }

        if (requestedIds.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "No valid chat IDs provided.");
            return;
        }

        Map<String, WidgetEntry> widgetById = new LinkedHashMap<>();

        try {
            for (WidgetEntry w : WidgetStore.list(null)) {
                if (w != null && w.getWidgetId() != null && !w.getWidgetId().isBlank()) {
                    widgetById.put(w.getWidgetId(), w);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.FINE, "Unable to load widget list for topics selection", ex);
        }

        DashboardTopicsSelectionService.SelectionResolution resolved;
        try {
            resolved = new DashboardTopicsSelectionService(dataSourceHolder(), log)
                    .resolveSelectedChats(requestedIds, widgetById);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.log(Level.WARNING, "Unable to resolve selected chats", ex);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to resolve selected chats.");
            return;
        }

        List<TermChatSnapshot> snapshots = resolved.snapshots();
        Set<String> foundIds = resolved.foundIds();

        if (snapshots.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "No matching chats found in widget tables.");
            return;
        }

        String selectionId = WidgetReviewStartServlet.createSnapshotSelection(
                session,
                "Popular Topics",
                snapshots,
            ServletPathUtil.safeContextPathEnsureLeadingSlash(req.getContextPath()) + "/dashboard/topics"
        );

        if (selectionId == null || selectionId.isBlank()) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create selection.");
            return;
        }

        JsonObject ok = Json.createObjectBuilder()
                .add(com.sim.chatserver.util.CommonLiteralConstants.KEY_STATUS, "ok")
                .add(com.sim.chatserver.util.CommonLiteralConstants.PARAM_SELECTION_ID, selectionId)
                .add("requestedCount", requestedIds.size())
                .add("resolvedCount", foundIds.size())
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, ok);
    }

    private String readRequestBody(HttpServletRequest req) {
        if (req == null || !isValidJsonPayloadRequest(req)) {
            return "";
        }

        Reader sourceReader;
        try {
            sourceReader = req.getReader();
        } catch (IOException | IllegalStateException ex) {
            log.log(Level.FINE, "Unable to read topics-select request body from request reader", ex);
            sourceReader = null;
        }

        if (sourceReader == null) {
            return "";
        }

        try (Reader reader = sourceReader) {
            String body = ServletRequestParamUtil.readNormalizedBodyTextOrEmptyOnLimit(reader, MAX_JSON_PAYLOAD_BYTES);
            String normalized = ServletRequestParamUtil.normalizeBodyText(body, MAX_JSON_PAYLOAD_BYTES, true);
            return validateCanonicalizedBodyText(normalized);
        } catch (IOException | IllegalArgumentException ex) {
            log.log(Level.FINE, "Unable to parse topics-select request body", ex);
            throw new JsonException("Unable to read request payload", ex);
        }
    }

    private final AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private boolean isValidJsonPayloadRequest(HttpServletRequest req) {
        return ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)
                && hasJsonContentType(req);
    }

    private boolean hasJsonContentType(HttpServletRequest req) {
        if (req == null) {
            return false;
        }
        String raw = ServletRequestParamUtil.normalizeValue(req.getContentType(), 128, true, false);
        if (raw == null || raw.isBlank()) {
            return false;
        }
        int semicolon = raw.indexOf((char) 59);
        String mediaType = (semicolon >= 0 ? raw.substring(0, semicolon) : raw).trim().toLowerCase(java.util.Locale.ROOT);
        if (mediaType.isBlank()) {
            return false;
        }
        return com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON.equals(mediaType) || mediaType.endsWith("+json");
    }

    private String validateCanonicalizedBodyText(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String canonical = TextIoSanitizerUtil.canonicalize(value);
        StringBuilder safe = new StringBuilder(canonical.length());
        for (int i = 0; i < canonical.length(); i++) {
            char ch = canonical.charAt(i);
            if (Character.isISOControl(ch) && ch != System.lineSeparator().charAt(System.lineSeparator().length() - 1) && ch != '\t') {
                continue;
            }
            safe.append(ch);
        }
        String normalized = safe.toString();
        return normalized.length() > MAX_JSON_PAYLOAD_BYTES
                ? new String(normalized.toCharArray(), 0, MAX_JSON_PAYLOAD_BYTES)
                : normalized;
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write topics-select error response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? com.sim.chatserver.util.CommonLiteralConstants.MSG_REQUEST_FAILED : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject payload) {
        try {
            ServletJsonResponseUtil.writeJson(resp, status, payload);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write topics-select JSON response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, com.sim.chatserver.util.CommonLiteralConstants.MSG_UNABLE_TO_WRITE_RESPONSE);
                } catch (IOException ioe) {
                    log.log(Level.FINE, com.sim.chatserver.util.CommonLiteralConstants.MSG_FALLBACK_SEND_ERROR_FAILED, ioe);
                }
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.InvalidObjectException(com.sim.chatserver.util.CommonLiteralConstants.MSG_DESERIALIZATION_NOT_ALLOWED);
    }

}
