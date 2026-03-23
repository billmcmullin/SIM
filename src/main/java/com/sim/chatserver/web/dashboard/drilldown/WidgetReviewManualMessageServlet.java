package com.sim.chatserver.web.dashboard.drilldown;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sim.chatserver.config.EncryptedDbConfigStore;
import com.sim.chatserver.config.ServerConfig;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "WidgetReviewManualMessageServlet", urlPatterns = {"/dashboard/widgets/drilldown/review/manual-message"})
public class WidgetReviewManualMessageServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WidgetReviewManualMessageServlet.class.getName());
    private static final String CHAT_API_PATH_TEMPLATE = "/api/v1/workspace/%s/chat";

    // Safer budgeting for ~8k token model
    private static final int MAX_TOTAL_MESSAGE_CHARS = 5200;
    private static final int MAX_CONTEXT_CHARS = 3400;
    private static final int MAX_CONTEXT_ENTRIES_HARD_CAP = 20000;
    private static final int MAX_PROMPT_INLINE_CHARS = 80;
    private static final int MAX_INDEX_IDS = 1200;

    // NEW: Map-reduce batching to maximize context coverage
    private static final int BATCH_SIZE = 35;
    private static final int MAX_BATCH_SUMMARY_CHARS = 420;

    @Inject
    AppDataSourceHolder dsHolder;

    private transient HttpClient httpClient;

    @Override
    public void init() throws ServletException {
        super.init();
        httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isLoggedIn(req, resp)) {
            return;
        }

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());

        JsonObject payload;
        try (var reader = Json.createReader(req.getInputStream())) {
            payload = reader.readObject();
        } catch (Exception ex) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
            return;
        }

        String userMessage = payload.getString("message", "").trim();
        if (userMessage.isEmpty()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "message is required.");
            return;
        }
        userMessage = stripClientInjectedContext(userMessage);

        List<SelectedEntry> selectedEntries = parseSelectedEntries(payload);
        boolean requestReset = payload.getBoolean("requestReset", true);

        EncryptedDbConfigStore.setAppDataSourceHolder(dsHolder);
        ServerConfig config;
        try {
            config = EncryptedDbConfigStore.load();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Unable to load server configuration", ex);
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration not available.");
            return;
        }

        if (config == null) {
            respondWithError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration missing.");
            return;
        }

        String slug = buildSlug(config.getWorkspaceName());
        if (slug == null || slug.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Workspace slug not configured.");
            return;
        }

        String baseUrl = buildBaseUrl(config);
        if (baseUrl == null || baseUrl.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "Server connection information is incomplete.");
            return;
        }

        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            respondWithError(resp, HttpServletResponse.SC_BAD_REQUEST, "API key not configured.");
            return;
        }

        String encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8);
        String targetUrl = baseUrl + String.format(CHAT_API_PATH_TEMPLATE, encodedSlug);
        String sessionId = req.getSession().getId();

        String compressedContext = buildAllPromptsCompressedContext(userMessage, selectedEntries, MAX_CONTEXT_CHARS);
        String outboundMessage = buildOutboundMessage(userMessage, compressedContext, MAX_TOTAL_MESSAGE_CHARS);

        HttpResponse<String> remoteResponse = sendToWorkspace(targetUrl, apiKey, outboundMessage, sessionId, requestReset);

        if (isLikelyContextTooLarge(remoteResponse)) {
            // Retry with tighter budget and forced reset
            String retryContext = buildAllPromptsCompressedContext(userMessage, selectedEntries, 2200);
            String retryMessage = buildOutboundMessage(userMessage, retryContext, 3200);
            remoteResponse = sendToWorkspace(targetUrl, apiKey, retryMessage, sessionId, true);
        }

        mirrorUpstreamResponse(resp, remoteResponse);
    }

    private HttpResponse<String> sendToWorkspace(String targetUrl, String apiKey, String outboundMessage, String sessionId, boolean reset)
            throws IOException, ServletException {
        JsonObject requestBody = Json.createObjectBuilder()
                .add("message", outboundMessage)
                .add("mode", "chat")
                .add("sessionId", sessionId == null ? "" : sessionId)
                .add("reset", reset)
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        try {
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.log(Level.SEVERE, "Manual message request interrupted", ex);
            throw new ServletException("Request interrupted.", ex);
        }
    }

    private void mirrorUpstreamResponse(HttpServletResponse resp, HttpResponse<String> remoteResponse) throws IOException {
        resp.setStatus(remoteResponse.statusCode());

        String upstreamContentType = remoteResponse.headers().firstValue("Content-Type").orElse(null);
        if (upstreamContentType != null && !upstreamContentType.isBlank()) {
            if (!upstreamContentType.toLowerCase(Locale.ROOT).contains("charset")) {
                upstreamContentType = upstreamContentType + "; charset=UTF-8";
            }
            resp.setContentType(upstreamContentType);
        } else {
            resp.setContentType("application/json; charset=UTF-8");
        }
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        byte[] bytes = remoteResponse.body() == null ? new byte[0] : remoteResponse.body().getBytes(StandardCharsets.UTF_8);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().flush();
    }

    private boolean isLikelyContextTooLarge(HttpResponse<String> remoteResponse) {
        if (remoteResponse == null) {
            return false;
        }
        String body = remoteResponse.body() == null ? "" : remoteResponse.body().toLowerCase(Locale.ROOT);
        int status = remoteResponse.statusCode();
        return status >= 400 && (body.contains("maximum context length")
                || body.contains("too many tokens")
                || body.contains("failed_to_embed"));
    }

    /**
     * Best-effort max context: 1) ALL prompts represented via hash index
     * (coverage) 2) Map-reduce batch summaries (coverage + thematic
     * compression) 3) Relevance-ordered inline previews (high signal)
     */
    private String buildAllPromptsCompressedContext(String userMessage, List<SelectedEntry> entries, int maxChars) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }

        List<String> terms = keywordTerms(userMessage);
        List<SelectedEntry> ranked = new ArrayList<>(entries);
        ranked.sort((a, b) -> Integer.compare(scoreEntry(b, terms), scoreEntry(a, terms)));

        StringBuilder sb = new StringBuilder();
        sb.append("Selected chats summary\n");
        sb.append("- total_selected: ").append(entries.size()).append('\n');
        sb.append("- coverage: all prompts represented via hash index + batch summaries\n");

        // Section A: prompt hash index (all coverage)
        appendWithinLimit(sb, "\nPrompt hash index (all selected):\n", maxChars);
        appendWithinLimit(sb, buildPromptHashIndex(entries), maxChars);

        // Section B: batch summaries (map-reduce style)
        if (sb.length() < maxChars) {
            appendWithinLimit(sb, "\nBatch summaries (compressed):\n", maxChars);
            String batchSummary = buildBatchSummaries(ranked, terms, maxChars - sb.length());
            appendWithinLimit(sb, batchSummary, maxChars);
        }

        // Section C: inline previews ordered by relevance
        if (sb.length() < maxChars) {
            appendWithinLimit(sb, "\nPrompt inline previews:\n", maxChars);
            int omittedInline = 0;
            for (SelectedEntry e : ranked) {
                String line = formatPromptInline(e) + '\n';
                if (!appendWithinLimit(sb, line, maxChars)) {
                    omittedInline++;
                }
            }
            if (omittedInline > 0) {
                appendWithinLimit(sb, "... (" + omittedInline + " prompt previews omitted due to size)\n", maxChars);
            }
        }

        return trimTo(sb.toString(), maxChars);
    }

    private String buildBatchSummaries(List<SelectedEntry> ranked, List<String> terms, int budget) {
        if (budget <= 0 || ranked.isEmpty()) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        int total = ranked.size();
        int batchCount = (int) Math.ceil(total / (double) BATCH_SIZE);

        for (int b = 0; b < batchCount; b++) {
            int from = b * BATCH_SIZE;
            int to = Math.min(total, from + BATCH_SIZE);
            List<SelectedEntry> slice = ranked.subList(from, to);

            String line = summarizeBatch(slice, b + 1, batchCount, terms);
            line = trimTo(line, MAX_BATCH_SUMMARY_CHARS) + '\n';

            if (out.length() + line.length() > budget) {
                out.append("... (remaining batch summaries omitted due to size)\n");
                break;
            }
            out.append(line);
        }

        return out.toString();
    }

    private String summarizeBatch(List<SelectedEntry> batch, int idx, int totalBatches, List<String> terms) {
        int promptChars = 0;
        Set<String> matched = new HashSet<>();
        List<String> sampleIds = new ArrayList<>();

        for (int i = 0; i < batch.size(); i++) {
            SelectedEntry e = batch.get(i);
            String p = e.prompt == null ? "" : e.prompt;
            promptChars += p.length();

            String hay = (p + " " + (e.response == null ? "" : e.response)).toLowerCase(Locale.ROOT);
            for (String t : terms) {
                if (hay.contains(t)) {
                    matched.add(t);
                }
            }

            if (i < 4) {
                sampleIds.add(defaultIfBlank(e.chatId, "(unknown)"));
            }
        }

        String avgPrompt = batch.isEmpty() ? "0" : String.valueOf(promptChars / batch.size());
        return "Batch " + idx + "/" + totalBatches
                + " size=" + batch.size()
                + " avgPromptChars=" + avgPrompt
                + " matchedTerms=" + matched
                + " sampleIds=" + sampleIds;
    }

    private boolean appendWithinLimit(StringBuilder sb, String text, int maxChars) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        if (sb.length() >= maxChars) {
            return false;
        }
        int room = maxChars - sb.length();
        if (text.length() <= room) {
            sb.append(text);
            return true;
        }
        sb.append(text, 0, room);
        return false;
    }

    private String buildPromptHashIndex(List<SelectedEntry> entries) {
        StringBuilder out = new StringBuilder();
        int count = 0;
        for (SelectedEntry e : entries) {
            if (count >= MAX_INDEX_IDS) {
                out.append("... (index truncated at ").append(MAX_INDEX_IDS).append(")\n");
                break;
            }
            String id = defaultIfBlank(e.chatId, "(unknown)");
            String prompt = e.prompt == null ? "" : e.prompt;
            String hash = sha1Hex(prompt);
            out.append(id).append("|").append(hash).append('\n');
            count++;
        }
        return out.toString();
    }

    private String formatPromptInline(SelectedEntry e) {
        String id = defaultIfBlank(e.chatId, "(unknown)");
        String p = compressText(e.prompt, MAX_PROMPT_INLINE_CHARS);
        String t = defaultIfBlank(e.createdAt, "?");
        return "- " + id + " | " + t + " | P: " + p;
    }

    private int scoreEntry(SelectedEntry e, List<String> terms) {
        if (terms.isEmpty()) {
            return 0;
        }
        String hay = ((e.prompt == null ? "" : e.prompt) + " " + (e.response == null ? "" : e.response)).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String t : terms) {
            if (hay.contains(t)) {
                score++;
            }
        }
        return score;
    }

    private List<String> keywordTerms(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Set<String> stop = new HashSet<>(Arrays.asList(
                "the", "and", "for", "with", "that", "this", "from", "into", "about",
                "what", "when", "where", "which", "have", "has", "had", "you", "your",
                "are", "was", "were", "how", "why"
        ));
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(s -> s.length() >= 3 && !stop.contains(s))
                .distinct()
                .limit(12)
                .collect(Collectors.toList());
    }

    private String sha1Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "sha1_error";
        }
    }

    private String stripClientInjectedContext(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "";
        }
        String marker = "\n\nSelected chats context:\n";
        int idx = userMessage.indexOf(marker);
        return idx >= 0 ? userMessage.substring(0, idx).trim() : userMessage.trim();
    }

    private List<SelectedEntry> parseSelectedEntries(JsonObject payload) {
        List<SelectedEntry> entries = new ArrayList<>();
        if (payload == null || !payload.containsKey("selectedEntries")) {
            return entries;
        }

        JsonValue raw = payload.get("selectedEntries");
        if (raw == null || raw.getValueType() != JsonValue.ValueType.ARRAY) {
            return entries;
        }

        JsonArray arr = payload.getJsonArray("selectedEntries");
        if (arr == null) {
            return entries;
        }

        int count = 0;
        for (JsonValue value : arr) {
            if (count >= MAX_CONTEXT_ENTRIES_HARD_CAP) {
                break;
            }
            if (value == null || value.getValueType() != JsonValue.ValueType.OBJECT) {
                continue;
            }

            JsonObject obj = value.asJsonObject();
            String chatId = str(obj, "chatId");
            String prompt = str(obj, "prompt");
            String response = str(obj, "response");
            String createdAt = str(obj, "createdAt");
            String sessionId = str(obj, "sessionId");

            if (chatId.isBlank() && prompt.isBlank() && response.isBlank()) {
                continue;
            }

            entries.add(new SelectedEntry(chatId, prompt, response, createdAt, sessionId));
            count++;
        }

        entries.sort(Comparator.comparing((SelectedEntry e) -> e.createdAt == null ? "" : e.createdAt).reversed());
        return entries;
    }

    private String compressText(String text, int maxChars) {
        if (text == null || text.isBlank()) {
            return "(empty)";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    private String buildOutboundMessage(String userMessage, String compressedContext, int maxTotalChars) {
        String base = userMessage == null ? "" : userMessage.trim();
        if (compressedContext == null || compressedContext.isBlank()) {
            return trimTo(base, maxTotalChars);
        }

        String suffix = "\n\nSelected chats context:\n" + compressedContext;
        String combined = base + suffix;
        if (combined.length() <= maxTotalChars) {
            return combined;
        }

        int roomForSuffix = Math.max(0, maxTotalChars - base.length());
        if (roomForSuffix <= 0) {
            return trimTo(base, maxTotalChars);
        }
        return base + trimTo(suffix, roomForSuffix);
    }

    private String str(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.containsKey(key)) {
            return "";
        }
        JsonValue v = obj.get(key);
        if (v == null) {
            return "";
        }
        if (v.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString) v).getString();
        }
        return v.toString();
    }

    private String trimTo(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        if (maxChars <= 0) {
            return "";
        }
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars);
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String buildBaseUrl(ServerConfig config) {
        String connectionInfo = config.getConnectionInfo();
        if (connectionInfo != null && !connectionInfo.isBlank()) {
            return stripTrailingSlash(connectionInfo.trim());
        }

        String host = config.getServerHost();
        if (host == null || host.isBlank()) {
            return null;
        }

        String normalized = host.trim();
        StringBuilder builder = new StringBuilder();
        if (normalized.contains("://")) {
            builder.append(normalized);
        } else {
            builder.append("https://").append(normalized);
        }

        boolean hasPort = normalized.matches(".*:\\d+$");
        if (!hasPort && config.getServerPort() > 0) {
            builder.append(':').append(config.getServerPort());
        }

        return stripTrailingSlash(builder.toString());
    }

    private String buildSlug(String workspaceName) {
        if (workspaceName == null) {
            return "";
        }
        String normalized = workspaceName.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceFirst("^-+", "");
        normalized = normalized.replaceFirst("-+$", "");
        return normalized.isBlank() ? "" : normalized;
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private boolean isLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Authentication required.\"}");
            return false;
        }
        return true;
    }

    private void respondWithError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write("{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", " ");
    }

    private static final class SelectedEntry {

        private final String chatId;
        private final String prompt;
        private final String response;
        private final String createdAt;
        private final String sessionId;

        private SelectedEntry(String chatId, String prompt, String response, String createdAt, String sessionId) {
            this.chatId = chatId == null ? "" : chatId;
            this.prompt = prompt == null ? "" : prompt;
            this.response = response == null ? "" : response;
            this.createdAt = createdAt == null ? "" : createdAt;
            this.sessionId = sessionId == null ? "" : sessionId;
        }
    }
}
