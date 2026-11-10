package com.sim.chatserver.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class InferenceRequestGateTest {
    @Test
    @SuppressWarnings("unchecked")
    void uncertainRequestsBlockUntilExplicitIdleConfirmation() throws Exception {
        WorkflowRecordStore records = mock(WorkflowRecordStore.class);
        AtomicReference<WorkflowRecordStore.StoredRecord> active = new AtomicReference<>();
        when(records.find(anyString(), anyString())).thenAnswer(invocation -> active.get());
        when(records.tryTaskLock(anyString())).thenAnswer(invocation -> DatabaseTaskLock.localOnly());
        doAnswer(invocation -> {
            active.set(new WorkflowRecordStore.StoredRecord(invocation.getArgument(2), 0));
            return null;
        }).when(records).insert(anyString(), anyString(), anyString(), anyLong());
        doAnswer(invocation -> { active.set(null); return null; }).when(records).delete(anyString(), anyString());
        URI target = URI.create("https://example.test/api/v1/workspace/one/chat");
        try (var runtime = mockStatic(WorkflowRuntime.class)) {
            runtime.when(WorkflowRuntime::isInitialized).thenReturn(true);
            runtime.when(WorkflowRuntime::records).thenReturn(records);
            try (var lease = InferenceRequestGate.acquire(target)) {
                assertNotNull(active.get());
                assertThrows(InferenceRequestGate.BusyException.class,
                        () -> InferenceRequestGate.confirmUpstreamIdle(target.toString()));
            }
            assertThrows(IllegalStateException.class, () -> InferenceRequestGate.acquire(target));
            InferenceRequestGate.confirmUpstreamIdle(target.toString());
            assertNull(active.get());
            try (var lease = InferenceRequestGate.acquire(target)) {
                lease.complete();
            }
            assertNull(active.get());
            assertEquals(InferenceRequestGate.serverKey(target.toString()),
                    InferenceRequestGate.serverKey("https://EXAMPLE.test:443/api/v1/workspace/two/chat"));
                java.net.http.HttpClient http = mock(java.net.http.HttpClient.class);
                java.net.http.HttpResponse<String> response = mock(java.net.http.HttpResponse.class);
                when(response.statusCode()).thenReturn(504);
                when(response.body()).thenReturn("gateway timeout");
                when(response.headers()).thenReturn(java.net.http.HttpHeaders.of(java.util.Map.of(), (name, value) -> true));
                when(http.send(any(java.net.http.HttpRequest.class), any(java.net.http.HttpResponse.BodyHandler.class))).thenReturn(response);
                WorkspaceClient client = new WorkspaceClient(http, 10, java.time.Duration.ofSeconds(5));
                client.sendChatBearerCompatOnce(target.toString(), "test-token", "small request", "chat",
                    "test-session", true, null, "gateway-test", 4096);
                assertNotNull(active.get());
                assertThrows(IllegalStateException.class, () -> client.sendChatBearerCompatOnce(target.toString(),
                    "test-token", "small request", "chat", "test-session", true, null, "blocked-test", 4096));
                verify(http, times(1)).send(any(java.net.http.HttpRequest.class), any(java.net.http.HttpResponse.BodyHandler.class));
                InferenceRequestGate.confirmUpstreamIdle(target.toString());
        }
    }
}