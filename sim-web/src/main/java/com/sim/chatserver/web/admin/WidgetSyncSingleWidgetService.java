package com.sim.chatserver.web.admin;

import jakarta.json.JsonObject;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class WidgetSyncSingleWidgetService {

    private static final String ARG_TABLE_ENSURER = "tableEnsurer";
    private static final String ARG_PROGRESS_UPDATER = "progressUpdater";
    private static final String ARG_CHAT_FETCHER = "chatFetcher";
    private static final String ARG_CHAT_ID_COLLECTOR = "chatIdCollector";
    private static final String ARG_MISSING_CHAT_ID_RESOLVER = "missingChatIdResolver";
    private static final String ARG_CHAT_FILTER = "chatFilter";
    private static final String ARG_CHAT_UPSERTER = "chatUpserter";
    private static final String ARG_CACHE_RECORDER = "cacheRecorder";

    private static final String MSG_CALLING_API = "Calling API for chat messages...";
    private static final String MSG_PAYLOAD_UNCHANGED = "Payload unchanged from recent syncs. Skipping DB upsert.";
    private static final String MSG_FILTERED_PREFIX = "Filtered ";
    private static final String MSG_FILTERED_SUFFIX = " known chat(s) before DB upsert.";
    private static final String MSG_API_RETURNED_PREFIX = "API returned ";
    private static final String MSG_API_RETURNED_SUFFIX = " chat(s). Checking for new entries...";
    private static final String MSG_UPSERTING = "Upserting chat(s) into database...";
    private static final String MSG_ADDED_PREFIX = "Added ";
    private static final String MSG_ADDED_SUFFIX = " new chat(s) to database.";
    private static final String MSG_NO_NEW_CHATS = "No new chats detected. Database unchanged.";
    private static final String MSG_NO_CHAT_ROWS = "No chat rows returned from server.";
    private static final String MSG_FETCHED_PREFIX = "Fetched ";
    private static final String MSG_FETCHED_PAYLOAD_UNCHANGED_SUFFIX = " chat(s), payload unchanged from recent sync cache. Table unchanged.";
    private static final String MSG_FETCHED_NO_NEW_SUFFIX = " chat(s), no new chat entries detected. Table unchanged.";
    private static final String MSG_FETCHED_INSERTED_MID = " chat(s), inserted ";
    private static final String MSG_FETCHED_INSERTED_SUFFIX = " new chat(s).";
    private static final String EMPTY = "";

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
        tableEnsurer = Objects.requireNonNull(tableEnsurer, ARG_TABLE_ENSURER);
        progressUpdater = Objects.requireNonNull(progressUpdater, ARG_PROGRESS_UPDATER);
        chatFetcher = Objects.requireNonNull(chatFetcher, ARG_CHAT_FETCHER);
        chatIdCollector = Objects.requireNonNull(chatIdCollector, ARG_CHAT_ID_COLLECTOR);
        missingChatIdResolver = Objects.requireNonNull(missingChatIdResolver, ARG_MISSING_CHAT_ID_RESOLVER);
        chatFilter = Objects.requireNonNull(chatFilter, ARG_CHAT_FILTER);
        chatUpserter = Objects.requireNonNull(chatUpserter, ARG_CHAT_UPSERTER);
        cacheRecorder = Objects.requireNonNull(cacheRecorder, ARG_CACHE_RECORDER);

        tableEnsurer.run();

        progressUpdater.accept(MSG_CALLING_API);

        List<JsonObject> chats = chatFetcher.get();
        List<String> candidateChatIds = chatIdCollector.apply(chats);
        if (candidateChatIds == null) {
            candidateChatIds = Collections.emptyList();
        }
        List<String> missingChatIds = candidateChatIds.isEmpty() ? null : missingChatIdResolver.apply(candidateChatIds);

        boolean skippedByRecentCache = false;
        List<JsonObject> chatsForUpsert = chats;
        if (missingChatIds != null) {
            if (missingChatIds.isEmpty()) {
                skippedByRecentCache = true;
                progressUpdater.accept(MSG_PAYLOAD_UNCHANGED);
            } else if (missingChatIds.size() < candidateChatIds.size()) {
                chatsForUpsert = chatFilter.filter(chats, new LinkedHashSet<>(missingChatIds));
                int filteredCount = Math.max(0, candidateChatIds.size() - missingChatIds.size());
                progressUpdater.accept(MSG_FILTERED_PREFIX + filteredCount + MSG_FILTERED_SUFFIX);
            }
        }

        progressUpdater.accept(MSG_API_RETURNED_PREFIX + chats.size() + MSG_API_RETURNED_SUFFIX);

        int inserted = 0;
        if (!skippedByRecentCache && !chatsForUpsert.isEmpty()) {
            progressUpdater.accept(MSG_UPSERTING);
            inserted = safeInt(chatUpserter.apply(chatsForUpsert));
            progressUpdater.accept(MSG_ADDED_PREFIX + inserted + MSG_ADDED_SUFFIX);
        } else {
            progressUpdater.accept(MSG_NO_NEW_CHATS);
        }

        if (!candidateChatIds.isEmpty()) {
            cacheRecorder.accept(candidateChatIds);
        }

        String message = chats.isEmpty()
        ? MSG_NO_CHAT_ROWS
                : (skippedByRecentCache
        ? MSG_FETCHED_PREFIX + chats.size() + MSG_FETCHED_PAYLOAD_UNCHANGED_SUFFIX
                : (inserted <= 0
        ? MSG_FETCHED_PREFIX + chats.size() + MSG_FETCHED_NO_NEW_SUFFIX
        : MSG_FETCHED_PREFIX + chats.size() + MSG_FETCHED_INSERTED_MID + inserted + MSG_FETCHED_INSERTED_SUFFIX));

        return new WidgetSyncResult(message);
    }

    @FunctionalInterface
    interface ChatFilter {
        List<JsonObject> filter(List<JsonObject> chats, Set<String> allowedIds);
    }

    private static int safeInt(Object value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }

}

final class WidgetSyncResult {
    private static final String EMPTY = "";
    private final String message;

    WidgetSyncResult(String message) {
        this.message = message == null ? EMPTY : message;
    }

    String message() {
        return message;
    }
}

