package com.sim.chatserver.web.dashboard.topics;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.startup.AppDataSourceHolder;
import com.sim.chatserver.term.TermChatSnapshot;
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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DashboardTopicsSelectServlet", urlPatterns = {"/dashboard/topics/select"})
public class DashboardTopicsSelectServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DashboardTopicsSelectServlet.class.getName());
    private static final int MAX_JSON_PAYLOAD_BYTES = 64 * 1024;
    private static final String JSON_UTF8 = "application/json; charset=UTF-8";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        JsonObject payload;
        if (!ServletRequestParamUtil.hasValidContentLength(req, MAX_JSON_PAYLOAD_BYTES)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }
        try (JsonReader reader = Json.createReader(new StringReader(readRequestBody(req)))) {
            payload = reader.readObject();
        } catch (JsonException ex) {
            log.log(Level.FINE, "Invalid topics selection payload", ex);
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        JsonArray arr = payload.getJsonArray("selectedChatIds");
        if (arr == null || arr.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "selectedChatIds required.");
            return;
        }

        Set<String> requestedIds = new LinkedHashSet<>();
        for (int i = 0; i < arr.size(); i++) {
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
                .add("status", "ok")
                .add("selectionId", selectionId)
                .add("requestedCount", requestedIds.size())
                .add("resolvedCount", foundIds.size())
                .build();

        writeJson(resp, HttpServletResponse.SC_OK, ok);
    }

    private String readRequestBody(HttpServletRequest req) {
        Reader sourceReader;
        try {
            sourceReader = req.getReader();
        } catch (IOException | IllegalStateException ex) {
            sourceReader = null;
        }

        if (sourceReader == null) {
            try {
                var inputStream = req.getInputStream();
                if (inputStream == null) {
                    return "";
                }
                sourceReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            } catch (IOException | IllegalStateException ex) {
                return "";
            }
        }

        try (Reader reader = sourceReader) {
            StringBuilder body = new StringBuilder();
            char[] buf = new char[1024];
            int read;
            while ((read = reader.read(buf)) != -1) {
                body.append(buf, 0, read);
                if (body.length() > MAX_JSON_PAYLOAD_BYTES) {
                    throw new IllegalArgumentException("Payload too large");
                }
            }
            return ServletRequestParamUtil.normalizeBodyText(body.toString(), MAX_JSON_PAYLOAD_BYTES, true);
        } catch (IOException | RuntimeException ex) {
            throw new JsonException("Unable to read request payload", ex);
        }
    }

    protected AppDataSourceHolder dataSourceHolder() {
        return CDI.current().select(AppDataSourceHolder.class).get();
    }

    private void writeError(HttpServletResponse resp, int status, String message) {
        try {
            ServletJsonResponseUtil.writeError(resp, status, message);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write topics-select error response", e);
            if (!resp.isCommitted()) {
                try {
                    resp.sendError(status, message == null ? "Request failed." : message);
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
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
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to write response.");
                } catch (IOException ioe) {
                    log.log(Level.FINE, "Fallback sendError failed", ioe);
                }
            }
        }
    }
}
