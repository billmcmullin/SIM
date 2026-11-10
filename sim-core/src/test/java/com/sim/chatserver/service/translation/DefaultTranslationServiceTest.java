package com.sim.chatserver.service.translation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;

import com.sim.chatserver.service.translation.TranslationResult;
/**
 * Parasoft Jtest UTA: Test class for DefaultTranslationService
 *
 * @see com.sim.chatserver.service.translation.DefaultTranslationService
 * @author bmcmullin
 */
public class DefaultTranslationServiceTest
{

    @Test
    public void japaneseTextIsSentUnchangedWithAutoDetection() throws Exception
    {
        assertTranslationRequest("  2024.2\u3067\u306f\u30b7\u30b9\u30c6\u30e0\u8981\u4ef6\u306f64-bit\r\n", "System requirements");
    }

    @Test
    public void originalEncodingAndProviderOutputArePreserved() throws Exception
    {
        assertTranslationRequest("\u00c3 & + = \u2460\t\n", "\u00c3 original provider output");
    }

    @Test
    public void englishTextStillGoesToTheProvider() throws Exception
    {
        assertTranslationRequest("Original text", "Provider translation");
    }

    private void assertTranslationRequest(String text, String translatedText) throws Exception
    {
        assertTranslationRequest(text, translatedText, null, "auto");
    }

    @Test
    public void manualJapaneseOverridesDetectionForSqlPrefixedText() throws Exception
    {
        assertTranslationRequest("SELECT test_id FROM Test WHERE test_id = ${TestData.test_id}\u3053\u308c\u3092\u56fa\u5b9a\u3067\u5165\u308c\u3066\u5b9f\u884c", "Execute with this fixed value", "ja", "ja");
    }

    @Test
    public void invalidSourceLanguageIsRejected()
    {
        TranslationResult result = new DefaultTranslationService().detectAndTranslate("text", "invalid", "en");
        assertEquals(false, result.isSuccess());
        assertEquals("Unsupported source language.", result.getMessage());
    }

    private void assertTranslationRequest(String text, String translatedText, String sourceLang, String expectedSource) throws Exception
    {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> translation = mock(HttpResponse.class);
        when(translation.statusCode()).thenReturn(200);
        when(translation.body()).thenReturn(jakarta.json.Json.createObjectBuilder()
                .add("translatedText", translatedText).build().toString());
        AtomicReference<HttpRequest> translationRequest = new AtomicReference<>();
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenAnswer(invocation -> {
                    translationRequest.set(invocation.getArgument(0));
                    return translation;
                });
        DefaultTranslationService service = new DefaultTranslationService();
        Field clientField = DefaultTranslationService.class.getDeclaredField("httpClient");
        clientField.setAccessible(true);
        clientField.set(service, client);

        TranslationResult result = sourceLang == null
            ? service.detectAndTranslate(text, "en")
            : service.detectAndTranslate(text, sourceLang, "en");

        assertTrue(result.isSuccess());
        assertEquals(translatedText, result.getTranslatedText());
        assertEquals(expectedSource, result.getSourceLang());
        assertEquals("/translate", translationRequest.get().uri().getPath());
        verify(client, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
        translationRequest.get().bodyPublisher().orElseThrow().subscribe(new java.util.concurrent.Flow.Subscriber<>() {
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }
            public void onNext(java.nio.ByteBuffer buffer) {
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                body.writeBytes(bytes);
            }
            public void onError(Throwable error) {
                throw new AssertionError(error);
            }
            public void onComplete() {
            }
        });
        String form = body.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(expectedSource, java.util.Arrays.stream(form.split("&"))
                .filter(parameter -> parameter.startsWith("source="))
                .findFirst().orElseThrow().substring("source=".length()));
        assertEquals(text, java.net.URLDecoder.decode(java.util.Arrays.stream(form.split("&"))
            .filter(parameter -> parameter.startsWith("q="))
            .findFirst().orElseThrow().substring("q=".length()), java.nio.charset.StandardCharsets.UTF_8));
        assertTrue(form.contains("&target=en&format=text"));
    }

    /**
     * Parasoft Jtest UTA: Test for detectAndTranslate(String, String)
     *
     * @see com.sim.chatserver.service.translation.DefaultTranslationService#detectAndTranslate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testDetectAndTranslate() throws Throwable
    {
        // Given
        DefaultTranslationService underTest = new DefaultTranslationService();

        // When
        String text = "text"; // UTA: configured value
        String targetLang = "targetLang"; // UTA: default value
        TranslationResult result = underTest.detectAndTranslate(text, targetLang);

    }

    /**
     * Parasoft Jtest UTA: Test for detectAndTranslate(String, String)
     *
     * @see com.sim.chatserver.service.translation.DefaultTranslationService#detectAndTranslate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testDetectAndTranslate2() throws Throwable
    {
        // Given
        DefaultTranslationService underTest = new DefaultTranslationService();

        // When
        String text = null; // UTA: configured value
        String targetLang = "targetLang"; // UTA: default value
        TranslationResult result = underTest.detectAndTranslate(text, targetLang);

    }

    /**
     * Parasoft Jtest UTA: Test for detectAndTranslate(String, String)
     *
     * @see com.sim.chatserver.service.translation.DefaultTranslationService#detectAndTranslate(String, String)
     * @author bmcmullin
     */
    @Test
    public void testDetectAndTranslate3() throws Throwable
    {
        // Given
        DefaultTranslationService underTest = new DefaultTranslationService();

        // When
        String text = null; // UTA: configured value
        String targetLang = null; // UTA: configured value
        TranslationResult result = underTest.detectAndTranslate(text, targetLang);

    }
}
