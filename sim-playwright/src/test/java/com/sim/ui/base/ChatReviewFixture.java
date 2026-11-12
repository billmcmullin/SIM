package com.sim.ui.base;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.google.gson.JsonParser;

public final class ChatReviewFixture {
    public static final String BASE = "http://sim.test/chat-server";
    public static final String CHAT_A = "phase1-chat-a";
    public static final String CHAT_B = "phase1-chat-b";
    public static final String PROMPT_A = "Synthetic question A";
    public static final String PROMPT_B = "Synthetic question B";
    public static final String RESPONSE_A = "Synthetic answer A, preserved **verbatim**.";
    public static final String RESPONSE_B = "Synthetic answer B, preserved **verbatim**.";
    private static final Pattern SCRIPTS = Pattern.compile("<script\\b[^>]*>.*?</script>", Pattern.DOTALL);
    private static final Map<String, String> VIEWS = Map.of(
            "/dashboard", "dashboard.html",
            "/dashboard/widgets/view", "widget_table_view.html",
            "/dashboard/widgets/drilldown/review", "widget_review.html",
            "/customer-profile", "customer_profile.html",
            "/login", "login.html",
            "/logout", "login.html",
            "/dashboard/topics", "dashboard_topics.html",
            "/dashboard/sessions", "all_sessions.html");
    private final Path webRoot;
    private final List<String> unexpectedRequests = new ArrayList<>();
    private boolean holdFirstDetail;
    private Route heldDetail;
    private Route heldTranslation;
    private boolean widgetPagination;
    private String widgetResponse;
    private final Map<String, String> chatTimestamps = new LinkedHashMap<>();
    private int selectionRequests;
    private int sessionChatCount = 2;
    private final Map<String, List<String>> selectionRows = new LinkedHashMap<>();
    private final Map<String, String> selectionContexts = new LinkedHashMap<>();
    private String topicLabel = "Synthetic topic";
    private String topicStart = "2026-11-10";
    private String topicEnd = "2026-11-10";
    private List<String> topicChatIds = List.of(CHAT_A, CHAT_B);
    private com.google.gson.JsonObject lastSelection;
    private boolean sharingAllowed;
    private boolean sharingFailure;
    private final List<com.google.gson.JsonObject> sharedMessages = new ArrayList<>();
    private final List<com.google.gson.JsonObject> translationRequests = new ArrayList<>();

    public ChatReviewFixture(BrowserContext context) {
        webRoot = Path.of(System.getProperty("basedir", "."))
                .resolve("../sim-app/src/main/webapp").normalize().toAbsolutePath();
        assertTrue(Files.isRegularFile(webRoot.resolve("assets/js/widget_review.js")),
                "Required source fixture is missing; this is not a passing browser check.");
        context.route("**/*", this::serve);
    }

    public void openReview(Page page) {
        page.navigate(BASE + "/dashboard/widgets/drilldown/review?selectionId=phase1-selection");
        page.locator("#widgetReviewBody tr[data-row-key]").first().waitFor();
    }

    public void openProfile(Page page) {
        page.navigate(BASE + "/customer-profile?sessionId=phase1-session");
        page.locator("#cpSubmittedChatsBody tr[data-chat-id]").first().waitFor();
    }

    public void openWidget(Page page) {
        page.navigate(BASE + "/dashboard/widgets/view?widgetId=phase1-widget&date=2026-11-10");
        page.locator("#widgetTableBody input.row-select").first().waitFor();
    }

    public void enableWidgetPagination() {
        widgetPagination = true;
    }

    public void useWidgetResponse(String value) {
        widgetResponse = value;
    }

    public void useChatTimestamp(String chatId, String timestamp) {
        chatTimestamps.put(chatId, timestamp);
    }

    public int selectionRequests() {
        return selectionRequests;
    }

    public void useTopicData(String label, String start, String end) {
        topicLabel = label;
        topicStart = start;
        topicEnd = end;
    }

    public void useTopicChatIds(List<String> ids) {
        topicChatIds = List.copyOf(ids);
    }

    public void useSessionChatCount(int count) {
        sessionChatCount = count;
    }

    public com.google.gson.JsonObject lastSelectionPayload() {
        assertTrue(lastSelection != null, "A selection request must be observed before inspecting its payload.");
        return lastSelection;
    }

    public void enableSharingFixture() {
        sharingAllowed = true;
    }

    public void enableSharingFailureFixture() {
        sharingAllowed = true;
        sharingFailure = true;
    }

    public List<com.google.gson.JsonObject> sharedMessages() {
        return List.copyOf(sharedMessages);
    }

    public List<com.google.gson.JsonObject> translationRequests() {
        return List.copyOf(translationRequests);
    }

    public void holdFirstDetail() {
        holdFirstDetail = true;
    }

    public void awaitHeldDetail(Page page) {
        page.waitForCondition(() -> heldDetail != null);
    }

    public void releaseFirstDetail() {
        assertTrue(heldDetail != null, "The first detail request must be observed before release.");
        json(heldDetail, data(CHAT_A));
        heldDetail = null;
    }

    public void releaseFirstDetailFailure() {
        assertTrue(heldDetail != null, "The first detail request must be held before failure release.");
        heldDetail.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/json")
                .setBody("{\"status\":\"error\",\"message\":\"Synthetic old request failure\"}"));
        heldDetail = null;
    }

    public void awaitHeldTranslation(Page page) {
        page.waitForCondition(() -> heldTranslation != null);
    }

    public com.google.gson.JsonObject heldTranslationPayload() {
        assertTrue(heldTranslation != null, "A translation must be held before inspecting its synthetic payload.");
        return JsonParser.parseString(heldTranslation.request().postData()).getAsJsonObject();
    }

    public void releaseFirstTranslation() {
        assertTrue(heldTranslation != null, "The first translation must be held before release.");
        json(heldTranslation, "{\"status\":\"ok\",\"translatedText\":\"Synthetic translation A\",\"sourceLang\":\"en\",\"targetLang\":\"en\"}");
        heldTranslation = null;
    }

    public void capture(Page page, String name) throws IOException {
        Path output = Path.of(System.getProperty("chatReviewEvidenceDir",
            Path.of(System.getProperty("basedir", ".")).resolve("../target/chat-review-validation/screenshots").toString())).normalize();
        Files.createDirectories(output);
        page.screenshot(new Page.ScreenshotOptions().setPath(output.resolve(name + ".png")));
    }

    public void assertNoUnexpectedRequests() {
        assertTrue(unexpectedRequests.isEmpty(), () -> "Unexpected fixture requests: " + unexpectedRequests);
    }

    private void serve(Route route) {
        URI uri = URI.create(route.request().url());
        if ("cdn.jsdelivr.net".equals(uri.getHost()) && "GET".equals(route.request().method())) {
            route.fulfill(new Route.FulfillOptions().setContentType("application/javascript").setBody(""));
            return;
        }
        if (!"sim.test".equals(uri.getHost())) {
            unexpectedRequests.add("external:" + uri.getHost());
            route.abort();
            return;
        }
        String path = uri.getPath().replaceFirst("^/chat-server", "");
        try {
            if (path.startsWith("/assets/")) {
                Path asset = webRoot.resolve(path.substring(1)).normalize();
                if (!asset.startsWith(webRoot) || !Files.isRegularFile(asset)) {
                    unexpectedRequests.add("missing-asset:" + path);
                    route.fulfill(new Route.FulfillOptions().setStatus(404).setBody("Missing fixture asset"));
                    return;
                }
                String type = path.endsWith(".css") ? "text/css" : "application/javascript";
                route.fulfill(new Route.FulfillOptions().setContentType(type).setBody(Files.readString(asset)));
            } else if (VIEWS.containsKey(path)) {
                String html = Files.readString(webRoot.resolve("WEB-INF/views/" + VIEWS.get(path)));
                if ("/dashboard".equals(path)) {
                    html = SCRIPTS.matcher(html).replaceAll(match ->
                            match.group().contains("/assets/js/navigation.js")
                            || match.group().contains("/assets/js/dashboard_util/inlineView.js")
                                || match.group().contains("/assets/js/chat_preview.js")
                                || match.group().contains("/assets/js/dashboard_util/termReview.js")
                            ? java.util.regex.Matcher.quoteReplacement(match.group()) : "");
                    html = html.replace("</head>", "<script>window.dashboardConfig={contextPath:'/chat-server'};</script></head>");
                            html = html.replace("${otherParasoftLatestRows}",
                                "<tr data-chat-id='phase1-chat-a' data-widget-id='phase1-widget' data-created-at='2026-11-10T12:00:00Z'><td>1</td><td>Phase 1 widget</td><td>Synthetic question A</td><td><a class='customer-profile-link' href='/chat-server/customer-profile?sessionId=phase1-session'>phase1-session</a></td><td>2026-11-10</td></tr>"
                                + "<tr data-chat-id='phase1-chat-b' data-widget-id='phase1-widget' data-created-at='2026-11-10T12:00:00Z'><td>2</td><td>Phase 1 widget</td><td>Synthetic question B</td><td>phase1-session</td><td>2026-11-10</td></tr>"
                                + "<tr><td>3</td><td>Legacy widget</td><td>Legacy prompt</td><td>Legacy session</td><td>2026-11-10</td></tr>");
                }
                String origin = selectionContexts.getOrDefault(queryValue(uri, "selectionId"), "");
                html = html.replace("${contextPath}", "/chat-server")
                        .replace("'${selectionId}'", new com.google.gson.JsonPrimitive(queryValue(uri, "selectionId").isEmpty()
                            ? "phase1-selection" : queryValue(uri, "selectionId")).toString())
                        .replace("${rawSessionId}", "phase1-session")
                        .replace("${sessionId}", "phase1-session")
                        .replace("${widgetId}", "phase1-widget")
                        .replace("${widgetName}", "Phase 1 widget")
                        .replace("${selectedDate}", "2026-11-10")
                        .replace("${subjectLabel}", "Phase 1 widget")
                        .replace("${subjectType}", "Widget")
                        .replace("${displaySubjectType}", origin.startsWith("Sessions") ? "Session" : origin.startsWith("Popular Topics") ? "Topic" : "Widget")
                        .replace("${displayContext}", escapeHtml(origin))
                        .replace("${user}", "Phase 1 test user")
                        .replaceAll("\\$\\{[^}]+}", "")
                        .replaceAll("<%@.*?%>", "");
                route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(html));
            } else if (path.endsWith("/review/email") && sharingAllowed) {
                sharedMessages.add(JsonParser.parseString(route.request().postData()).getAsJsonObject());
                if (sharingFailure) {
                    route.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/json")
                            .setBody("{\"status\":\"error\",\"message\":\"Synthetic delivery unavailable\"}"));
                } else {
                    json(route, "{\"status\":\"ok\",\"translatedPrompt\":false,\"translatedResponse\":false}");
                }
            } else if (path.endsWith("/review/translate")) {
                var payload = JsonParser.parseString(route.request().postData()).getAsJsonObject();
                translationRequests.add(payload.deepCopy());
                if (PROMPT_A.equals(payload.get("text").getAsString())) {
                    heldTranslation = route;
                } else {
                    json(route, "{\"status\":\"ok\",\"translatedText\":\"Synthetic translation B\",\"sourceLang\":\"en\",\"targetLang\":\"en\"}");
                }
            } else if (path.endsWith("/review-data")) {
                String selection = queryValue(uri, "selectionId");
                if (holdFirstDetail && "selection-a".equals(selection)) {
                    heldDetail = route;
                } else {
                    json(route, selectionData(selection));
                }
            } else if (path.endsWith("/topics/data")) {
                var payload = JsonParser.parseString("{\"status\":\"ok\",\"termsTotal\":2,\"uniqueChatsTotal\":2,\"globalTopics\":[{\"mentions\":2,\"selectedChatIds\":[\"phase1-chat-a\",\"phase1-chat-b\"]}],\"widgets\":[]}").getAsJsonObject();
                payload.addProperty("day", topicEnd);
                payload.addProperty("rangeStart", topicStart);
                payload.addProperty("rangeEnd", topicEnd);
                payload.addProperty("termsTotal", topicChatIds.size());
                payload.addProperty("uniqueChatsTotal", topicChatIds.size());
                var topic = payload.getAsJsonArray("globalTopics").get(0).getAsJsonObject();
                topic.addProperty("topic", topicLabel);
                topic.addProperty("mentions", topicChatIds.size());
                var ids = new com.google.gson.JsonArray();
                topicChatIds.forEach(ids::add);
                topic.add("selectedChatIds", ids);
                json(route, payload.toString());
            } else if (path.endsWith("/sessions/data")) {
                var payload = JsonParser.parseString("{\"status\":\"ok\",\"totalSessions\":1,\"totalPages\":1,\"widgetNames\":{\"phase1-widget\":\"Phase 1 widget\"},\"sessions\":[{\"sessionId\":\"phase1-session\",\"totalCount\":2,\"widgets\":[\"phase1-widget\"],\"lastSeen\":\"2026-11-10T12:00:00Z\"}]}").getAsJsonObject();
                payload.getAsJsonArray("sessions").get(0).getAsJsonObject().addProperty("totalCount", sessionChatCount);
                json(route, payload.toString());
            } else if (path.endsWith("/view/data") && widgetPagination) {
                int requestedPage = Integer.parseInt(queryValue(uri, "page"));
                int limit = Integer.parseInt(queryValue(uri, "limit"));
                var payload = JsonParser.parseString(data(null)).getAsJsonObject();
                var rows = new com.google.gson.JsonArray();
                for (int index = 0; index < Math.min(limit, 30 - (requestedPage - 1) * limit); index++) {
                    rows.add(JsonParser.parseString(row("phase2-page-" + requestedPage + "-chat-" + index)));
                }
                payload.add("rows", rows);
                payload.addProperty("total", 30);
                payload.addProperty("totalCount", 30);
                payload.addProperty("totalPages", (int) Math.ceil(30.0 / limit));
                payload.addProperty("page", requestedPage);
                json(route, payload.toString());
            } else if (path.endsWith("/view/data") || path.endsWith("/sessions/chats")) {
                var payload = JsonParser.parseString(data(null)).getAsJsonObject();
                if (path.endsWith("/view/data") && widgetResponse != null) {
                    payload.getAsJsonArray("rows").get(0).getAsJsonObject().addProperty("response", widgetResponse);
                }
                if (path.endsWith("/sessions/chats")) {
                    var rows = new com.google.gson.JsonArray();
                    for (int index = 0; index < sessionChatCount; index++) {
                        String chatId = index == 0 ? CHAT_A : index == 1 ? CHAT_B : "phase4-chat-" + index;
                        var chat = JsonParser.parseString(row(chatId)).getAsJsonObject();
                        for (String key : List.of("response", "widgetId", "widgetName", "sessionId")) chat.remove(key);
                        rows.add(chat);
                    }
                    payload.add("rows", rows);
                    payload.addProperty("total", sessionChatCount);
                    payload.addProperty("totalCount", sessionChatCount);
                }
                json(route, payload.toString());
            } else if (path.endsWith("/sessions/select") || path.endsWith("/widgets/review/start") || path.endsWith("/topics/select")) {
                selectionRequests++;
                var payload = JsonParser.parseString(route.request().postData()).getAsJsonObject();
                lastSelection = payload.deepCopy();
                var ids = payload.getAsJsonArray("selectedChatIds");
                List<String> selectedIds = new ArrayList<>();
                ids.forEach(value -> selectedIds.add(value.getAsString()));
                String selection = selectedIds.size() == 1 && CHAT_A.equals(selectedIds.get(0)) ? "selection-a"
                    : selectedIds.size() == 1 && CHAT_B.equals(selectedIds.get(0)) ? "selection-b"
                    : "phase4-selection-" + selectionRequests;
                selectionRows.put(selection, List.copyOf(selectedIds));
                String displayContext = payload.has("displayContext") ? payload.get("displayContext").getAsString()
                    : path.endsWith("/sessions/select") ? "Sessions | Session: phase1-session"
                    : "Widget: phase1-widget | Day: 2026-11-10";
                selectionContexts.put(selection, displayContext);
                json(route, "{\"status\":\"ok\",\"selectionId\":\"" + selection + "\"}");
            } else if (path.endsWith("/session-names.json")) {
                json(route, "{\"status\":\"ok\",\"rows\":[]}");
            } else if (path.endsWith("/favicon.ico")) {
                route.fulfill(new Route.FulfillOptions().setStatus(204));
            } else {
                unexpectedRequests.add(route.request().method() + ":" + path);
                route.fulfill(new Route.FulfillOptions().setStatus(409).setBody("Blocked by Phase 1 fixture"));
            }
        } catch (IOException error) {
            throw new IllegalStateException("Unable to read source-backed chat fixture", error);
        }
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String row(String chatId) {
        boolean first = CHAT_A.equals(chatId);
        var row = new com.google.gson.JsonObject();
        row.addProperty("chatId", chatId);
        row.addProperty("widgetId", "phase1-widget");
        row.addProperty("widgetName", "Phase 1 widget");
        row.addProperty("sessionId", "phase1-session");
        row.addProperty("createdAt", chatTimestamps.containsKey(chatId) ? chatTimestamps.get(chatId) : "2026-11-10T12:00:00Z");
        row.addProperty("prompt", first ? PROMPT_A : PROMPT_B);
        row.addProperty("response", first ? RESPONSE_A : RESPONSE_B);
        return row.toString();
    }

    private String selectionData(String selection) {
        List<String> selected = selectionRows.get(selection);
        if (selected == null) return data("selection-a".equals(selection) ? CHAT_A : "selection-b".equals(selection) ? CHAT_B : null);
        var payload = JsonParser.parseString(data(null)).getAsJsonObject();
        var rows = new com.google.gson.JsonArray();
        selected.forEach(chatId -> rows.add(JsonParser.parseString(row(chatId))));
        payload.add("rows", rows);
        payload.addProperty("total", selected.size());
        payload.addProperty("totalCount", selected.size());
        return payload.toString();
    }

    private String data(String chatId) {
        String rows = chatId == null ? row(CHAT_A) + "," + row(CHAT_B) : row(chatId);
        int count = chatId == null ? 2 : 1;
        return "{\"status\":\"ok\",\"total\":" + count + ",\"totalCount\":" + count + ",\"rows\":[" + rows + "]}";
    }

    private static String queryValue(URI uri, String name) {
        if (uri.getRawQuery() != null) {
            for (String parameter : uri.getRawQuery().split("&")) {
                String[] pair = parameter.split("=", 2);
                if (URLDecoder.decode(pair[0], StandardCharsets.UTF_8).equals(name)) {
                    return pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                }
            }
        }
        return "";
    }

    private static void json(Route route, String body) {
        route.fulfill(new Route.FulfillOptions().setContentType("application/json").setBody(body));
    }
}