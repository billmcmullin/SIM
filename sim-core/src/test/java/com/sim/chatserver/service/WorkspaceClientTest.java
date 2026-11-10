package com.sim.chatserver.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.WorkspaceResponse;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

/**
 * Unit tests for WorkspaceClient and its lightweight response DTO.
 *
 * @see com.sim.chatserver.service.WorkspaceClient
 */
public class WorkspaceClientTest
{

    @Test
    public void isLikelyContextTooLarge_returnsFalse_whenResponseIsNull()
    {
        WorkspaceClient underTest = createUnderTest();

        boolean result = underTest.isLikelyContextTooLarge(null);

        assertFalse(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsTrue_whenStatusCodeIs413()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(413);
        when(response.body()).thenReturn(null);

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertTrue(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsTrue_whenBodyContainsContextSignal()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(response.body()).thenReturn("Maximum context length exceeded for model");

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertTrue(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsTrue_whenJsonErrorContainsTokenLimitPhrase()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(response.body()).thenReturn("{\"error\":\"token limit reached\"}");

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertTrue(result);
    }

    @Test
    public void isLikelyContextTooLarge_returnsFalse_whenNoKnownSignalExists()
    {
        WorkspaceClient underTest = createUnderTest();
        WorkspaceResponse response = mock(WorkspaceResponse.class);
        when(response.statusCode()).thenReturn(414);
        when(response.body()).thenReturn("short request URI");

        boolean result = underTest.isLikelyContextTooLarge(response);

        assertFalse(result);
    }

    @Test
    public void workspaceResponse_bodyGetter_returnsConstructorBody()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, "body", "application/json");

        String result = underTest.body();

        assertEquals("body", result);
    }

    @Test
    public void workspaceResponse_contentTypeGetter_returnsConstructorContentType()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, "body", "application/json");

        String result = underTest.contentType();

        assertEquals("application/json", result);
    }

    @Test
    public void workspaceResponse_statusCodeGetter_returnsConstructorStatusCode()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(201, "body", "application/json");

        int result = underTest.statusCode();

        assertEquals(201, result);
    }

    @Test
    public void workspaceResponse_isError_returnsFalse_forNonErrorStatus()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, "body", "application/json");

        boolean result = underTest.isError();

        assertFalse(result);
    }

    @Test
    public void workspaceResponse_isError_returnsTrue_forErrorStatus()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(500, "body", "application/json");

        boolean result = underTest.isError();

        assertTrue(result);
    }

    @Test
    public void workspaceResponse_constructor_normalizesNullValues()
    {
        WorkspaceResponse underTest = new WorkspaceResponse(200, null, null);

        assertAll(
                () -> assertEquals(200, underTest.statusCode()),
                () -> assertEquals("", underTest.body()),
                () -> assertEquals("", underTest.contentType())
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void singleAttemptSummary_neverRetriesOrChangesAuthentication() throws Exception {
        for (int status : new int[] {200, 401, 403, 413, 429, 500, 503, 504}) {
            HttpClient httpClient = mock(HttpClient.class);
            HttpResponse<String> response = mock(HttpResponse.class);
            when(response.statusCode()).thenReturn(status);
            when(response.body()).thenReturn("{\"error\":\"upstream response\"}");
            when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (name, value) -> true));
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
            WorkspaceClient client = new WorkspaceClient(httpClient, 10, Duration.ofSeconds(5));

            WorkspaceResponse result = client.sendChatBearerCompatOnce("https://example.test/chat", "test-token",
                    "Summarize chats", "chat", "summary-test", true, null, "test-summary", 1024);

            assertEquals(status, result.statusCode());
            ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient, times(1)).send(request.capture(), any(HttpResponse.BodyHandler.class));
            assertEquals(HttpClient.Version.HTTP_1_1, request.getValue().version().orElseThrow());
            assertEquals("Bearer test-token", request.getValue().headers().firstValue("Authorization").orElseThrow());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void singleAttemptSummary_doesNotRetryNetworkErrorsOrInterruptions() throws Exception {
        for (Exception failure : new Exception[] {new IOException("connection failed"),
                new java.net.http.HttpTimeoutException("timed out"), new InterruptedException("cancelled")}) {
            HttpClient httpClient = mock(HttpClient.class);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(failure);
            WorkspaceClient client = new WorkspaceClient(httpClient, 10, Duration.ofSeconds(5));

            assertThrows(failure.getClass(), () -> client.sendChatBearerCompatOnce("https://example.test/chat",
                    "test-token", "Summarize chats", "chat", "summary-test", true, null, "test-summary", 1024));
            verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void singleAttemptSummary_boundsEntireJsonBodyIncludingEscapingAndUnicode() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{}");
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (name, value) -> true));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        WorkspaceClient client = new WorkspaceClient(httpClient, 10, Duration.ofSeconds(5));
        String message = "\"\\\n\u00e9\ud83d\ude00".repeat(1000);

        client.sendChatBearerCompatOnce("https://example.test/chat", "test-token", message,
                "chat", "summary-test", true, null, "test-summary", 1024);

        ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(1)).send(request.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest.BodyPublisher publisher = request.getValue().bodyPublisher().orElseThrow();
        assertTrue(publisher.contentLength() <= 1024);
        HttpResponse.BodySubscriber<String> subscriber = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        publisher.subscribe(new Flow.Subscriber<ByteBuffer>() {
            public void onSubscribe(Flow.Subscription subscription) {
                subscriber.onSubscribe(subscription);
            }

            public void onNext(ByteBuffer buffer) {
                subscriber.onNext(List.of(buffer));
            }

            public void onError(Throwable error) {
                subscriber.onError(error);
            }

            public void onComplete() {
                subscriber.onComplete();
            }
        });
        String body = subscriber.getBody().toCompletableFuture().get(5, TimeUnit.SECONDS);
        try (var reader = Json.createReader(new StringReader(body))) {
            JsonObject payload = reader.readObject();
            assertTrue(message.startsWith(payload.getString("message")));
            assertFalse(payload.getString("message").contains("\ufffd"));
            assertTrue(payload.getBoolean("reset"));
        }
    }

    private WorkspaceClient createUnderTest()
    {
        HttpClient httpClient = mock(HttpClient.class);
        Duration requestTimeout = mock(Duration.class);
        return new WorkspaceClient(httpClient, 1, requestTimeout);

    }
}



