package com.sim.chatserver.web.admin;

import jakarta.json.JsonObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class WidgetSyncSingleWidgetService {

    static final class WidgetSyncResult {
        private final String message;

        private WidgetSyncResult(String message) {
            this.message = defaultString(message);
        }

        String message() {
            return message;
        }
    }

    private WidgetSyncSingleWidgetService() {
    }

    static WidgetSyncResult execute(
            Runnable tableEnsurer,
            Consumer<String> progressUpdater,
            Supplier<List<JsonObject>> chatFetcher,
            Function<List<JsonObject>, List<String>> chatIdCollector,
            Function<List<String>, List<String>> missingChatIdResolver,
            ChatFilter chatFilter,
            Function<List<JsonObject>, Integer> chatUpserter,
            Consumer<List<String>> cacheRecorder
    ) {
        Objects.requireNonNull(tableEnsurer, "tableEnsurer");
        Objects.requireNonNull(progressUpdater, "progressUpdater");
        Objects.requireNonNull(chatFetcher, "chatFetcher");
        Objects.requireNonNull(chatIdCollector, "chatIdCollector");
        Objects.requireNonNull(missingChatIdResolver, "missingChatIdResolver");
        Objects.requireNonNull(chatFilter, "chatFilter");
        Objects.requireNonNull(chatUpserter, "chatUpserter");
        Objects.requireNonNull(cacheRecorder, "cacheRecorder");

        tableEnsurer.run();

        progressUpdater.accept("Calling API for chat messages...");

        List<JsonObject> chats = chatFetcher.get();
        List<String> candidateChatIds = chatIdCollector.apply(chats);
        List<String> missingChatIds = candidateChatIds.isEmpty() ? null : missingChatIdResolver.apply(candidateChatIds);

        boolean skippedByRecentCache = false;
        List<JsonObject> chatsForUpsert = chats;
        if (missingChatIds != null) {
            if (missingChatIds.isEmpty()) {
                skippedByRecentCache = true;
                progressUpdater.accept("Payload unchanged from recent syncs. Skipping DB upsert.");
            } else if (missingChatIds.size() < candidateChatIds.size()) {
                chatsForUpsert = chatFilter.filter(chats, new LinkedHashSet<>(missingChatIds));
                int filteredCount = Math.max(0, candidateChatIds.size() - missingChatIds.size());
                progressUpdater.accept("Filtered " + filteredCount + " known chat(s) before DB upsert.");
            }
        }

        progressUpdater.accept("API returned " + chats.size() + " chat(s). Checking for new entries...");

        int inserted = 0;
        if (!skippedByRecentCache && !chatsForUpsert.isEmpty()) {
            progressUpdater.accept("Upserting chat(s) into database...");
            inserted = safeInt(chatUpserter.apply(chatsForUpsert));
            progressUpdater.accept("Added " + inserted + " new chat(s) to database.");
        } else {
            progressUpdater.accept("No new chats detected. Database unchanged.");
        }

        if (!candidateChatIds.isEmpty()) {
            cacheRecorder.accept(candidateChatIds);
        }

        String message = chats.isEmpty()
                ? "No chat rows returned from server."
                : (skippedByRecentCache
                ? "Fetched " + chats.size() + " chat(s), payload unchanged from recent sync cache. Table unchanged."
                : (inserted <= 0
                ? "Fetched " + chats.size() + " chat(s), no new chat entries detected. Table unchanged."
                : "Fetched " + chats.size() + " chat(s), inserted " + inserted + " new chat(s)."));

        return new WidgetSyncResult(message);
    }

    @FunctionalInterface
    interface ChatFilter {
        List<JsonObject> filter(List<JsonObject> chats, Set<String> allowedIds);
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }
}
