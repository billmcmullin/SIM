package com.sim.chatserver.web.admin;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WidgetSyncSingleWidgetServiceTest {

    @Test
    void execute_requiresNonNullInputs() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> WidgetSyncSingleWidgetService.execute(
                        null,
                        ignored -> {},
                        List::of,
                        ignored -> List.of(),
                        ignored -> List.of(),
                        (chats, allowedIds) -> chats,
                        ignored -> 0,
                        ignored -> {}
                )
        );

        assertEquals("tableEnsurer", ex.getMessage());
    }

    @Test
    void execute_whenNoChats_returnsNoRowsMessage_andSkipsUpsertAndCache() {
        AtomicInteger tableEnsured = new AtomicInteger();
        List<String> progress = new ArrayList<>();
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        AtomicInteger upserts = new AtomicInteger();
        AtomicInteger cacheWrites = new AtomicInteger();

        WidgetSyncSingleWidgetService.WidgetSyncResult result = WidgetSyncSingleWidgetService.execute(
                tableEnsured::incrementAndGet,
                progress::add,
                List::of,
                chats -> List.of(),
                ids -> {
                    resolverCalled.set(true);
                    return List.of();
                },
                (chats, allowedIds) -> chats,
                chats -> {
                    upserts.incrementAndGet();
                    return 1;
                },
                ids -> cacheWrites.incrementAndGet()
        );

        assertNotNull(result);
        assertEquals("No chat rows returned from server.", result.message());
        assertEquals(1, tableEnsured.get());
        assertFalse(resolverCalled.get());
        assertEquals(0, upserts.get());
        assertEquals(0, cacheWrites.get());
        assertTrue(progress.stream().anyMatch(m -> m.contains("Calling API")));
        assertTrue(progress.stream().anyMatch(m -> m.contains("No new chats detected")));
    }

    @Test
    void execute_whenResolverReturnsEmpty_skipsUpsert_andReportsRecentCacheMessage() {
        List<String> progress = new ArrayList<>();
        AtomicInteger upserts = new AtomicInteger();
        AtomicReference<List<String>> cachedIds = new AtomicReference<>();
        List<JsonObject> chats = List.of(chat("a"), chat("b"));

        WidgetSyncSingleWidgetService.WidgetSyncResult result = WidgetSyncSingleWidgetService.execute(
                () -> {},
                progress::add,
                () -> chats,
                ignored -> List.of("a", "b"),
                ignored -> List.of(),
                (input, allowedIds) -> input,
                ignored -> {
                    upserts.incrementAndGet();
                    return 99;
                },
                cachedIds::set
        );

        assertEquals(0, upserts.get());
        assertEquals(List.of("a", "b"), cachedIds.get());
        assertEquals(
                "Fetched 2 chat(s), payload unchanged from recent sync cache. Table unchanged.",
                result.message()
        );
        assertTrue(progress.stream().anyMatch(m -> m.contains("Payload unchanged")));
    }

    @Test
    void execute_whenSubsetMissing_filtersBeforeUpsert_andReportsInsertedCount() {
        List<String> progress = new ArrayList<>();
        AtomicReference<Set<String>> allowedSeenByFilter = new AtomicReference<>();
        AtomicReference<List<JsonObject>> upsertedChats = new AtomicReference<>();

        JsonObject chatA = chat("a");
        JsonObject chatB = chat("b");
        JsonObject chatC = chat("c");

        WidgetSyncSingleWidgetService.WidgetSyncResult result = WidgetSyncSingleWidgetService.execute(
                () -> {},
                progress::add,
                () -> List.of(chatA, chatB, chatC),
                ignored -> List.of("a", "b", "c"),
                ignored -> List.of("b"),
                (allChats, allowedIds) -> {
                    allowedSeenByFilter.set(allowedIds);
                    return List.of(chatB);
                },
                filtered -> {
                    upsertedChats.set(filtered);
                    return 2;
                },
                ignored -> {}
        );

        assertEquals(Set.of("b"), allowedSeenByFilter.get());
        assertEquals(List.of(chatB), upsertedChats.get());
        assertEquals("Fetched 3 chat(s), inserted 2 new chat(s).", result.message());
        assertTrue(progress.stream().anyMatch(m -> m.contains("Filtered 2 known chat(s)")));
        assertTrue(progress.stream().anyMatch(m -> m.contains("Added 2 new chat(s)")));
    }

    @Test
    void execute_whenUpserterReturnsNull_treatsAsZero() {
        AtomicInteger cacheWrites = new AtomicInteger();

        WidgetSyncSingleWidgetService.WidgetSyncResult result = WidgetSyncSingleWidgetService.execute(
                () -> {},
                ignored -> {},
                () -> List.of(chat("x")),
                ignored -> List.of("x"),
                ignored -> null,
                (allChats, allowedIds) -> allChats,
                ignored -> null,
                ignored -> cacheWrites.incrementAndGet()
        );

        assertEquals(1, cacheWrites.get());
        assertEquals("Fetched 1 chat(s), no new chat entries detected. Table unchanged.", result.message());
    }

    @Test
    void execute_whenCollectorReturnsNull_doesNotCallCacheRecorder_andStillUpsertsChats() {
        AtomicBoolean resolverCalled = new AtomicBoolean(false);
        AtomicInteger cacheWrites = new AtomicInteger();
        AtomicInteger upserts = new AtomicInteger();

        WidgetSyncSingleWidgetService.WidgetSyncResult result = WidgetSyncSingleWidgetService.execute(
                () -> {},
                ignored -> {},
                () -> List.of(chat("x")),
                ignored -> null,
                ignored -> {
                    resolverCalled.set(true);
                    return List.of();
                },
                (allChats, allowedIds) -> allChats,
                ignored -> {
                    upserts.incrementAndGet();
                    return 1;
                },
                ignored -> cacheWrites.incrementAndGet()
        );

        assertFalse(resolverCalled.get());
        assertEquals(0, cacheWrites.get());
        assertEquals(1, upserts.get());
        assertEquals("Fetched 1 chat(s), inserted 1 new chat(s).", result.message());
    }

    private static JsonObject chat(String id) {
        return Json.createObjectBuilder().add("id", id).build();
    }
}
