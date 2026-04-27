package com.sim.chatserver.service.translation;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.translation.TranslationService.TranslationResult;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
/**
 * Unit tests for {@link DefaultTranslationService} using JUnit 5 + Mockito.
 *
 * Notes: - This class uses reflection to replace private final fields
 * (httpClient, URLs, apiKey) so the production class can be tested without
 * modification. - Tests cover the main success/failure and fallback behavior
 * paths.
 */
class DefaultTranslationServiceTest
{

    private DefaultTranslationService underTest;
    private HttpClient httpClient;
    @SuppressWarnings("unchecked")
    private HttpResponse<String> response1 = mock(HttpResponse.class);
    @SuppressWarnings("unchecked")
    private HttpResponse<String> response2 = mock(HttpResponse.class);

    @BeforeEach
    void setUp() throws Exception
    {
        underTest = new DefaultTranslationService();
        httpClient = mock(HttpClient.class);

        setField(underTest, "httpClient", httpClient);
        setField(underTest, "translateUrl", "http://mock.local/translate");
        setField(underTest, "detectUrl", "http://mock.local/detect");
        setField(underTest, "apiKey", "TEST_API_KEY");
    }

    @Test
    @DisplayName("detectAndTranslate: blank input should fail with message")
    void detectAndTranslate_blankInput_shouldFail()
    {
        TranslationService.TranslationResult result = underTest.detectAndTranslate("   ", "en");

        assertAll(
                () -> assertFalse(result.isSuccess()), () -> assertEquals("Text is required.", result.getMessage()), () -> assertEquals("", result.getSourceLang()), () -> assertEquals("", result.getTargetLang()), () -> assertEquals("", result.getTranslatedText())
        );
        verifyNoInteractions(httpClient);
    }

    @Test
    @DisplayName("detectAndTranslate: provider detect + translate success")
    void detectAndTranslate_providerDetectAndTranslateSuccess() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1) // detect
                .thenReturn(response2); // translate

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"es\",\"confidence\":0.99}]");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"translatedText\":\"hello\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("hola", "en");

        assertAll(
                () -> assertTrue(result.isSuccess()), () -> assertEquals("es", result.getSourceLang()), () -> assertEquals("en", result.getTargetLang()), () -> assertEquals("hello", result.getTranslatedText()), () -> assertEquals("", result.getMessage())
        );

        verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @DisplayName("detectAndTranslate: detect failure should fallback to heuristic and still translate")
    void detectAndTranslate_detectFailure_shouldFallbackToHeuristic() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1) // detect fails
                .thenReturn(response2); // translate ok

        when(response1.statusCode()).thenReturn(500);
        when(response1.body()).thenReturn("server error");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"translatedText\":\"hello from heuristic\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("hola gracias", "en");

        assertAll(
                () -> assertTrue(result.isSuccess()), () -> assertEquals("es", result.getSourceLang()), // heuristic for Spanish hints
                () -> assertEquals("en", result.getTargetLang()), () -> assertEquals("hello from heuristic", result.getTranslatedText())
        );
    }

    @Test
    @DisplayName("detectAndTranslate: translate empty should fail")
    void detectAndTranslate_translateEmpty_shouldFail() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1).thenReturn(response2);

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"en\"}]");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"translatedText\":\"\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("hello", "es");

        assertAll(
                () -> assertFalse(result.isSuccess()), () -> assertEquals("Translation service returned empty text.", result.getMessage())
        );
    }

    @Test
    @DisplayName("detectAndTranslate: translate HTTP error should return generic failure")
    void detectAndTranslate_translateHttpError_shouldFailGeneric() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1).thenReturn(response2);

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"en\"}]");

        when(response2.statusCode()).thenReturn(500);
        when(response2.body()).thenReturn("{\"error\":\"boom\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("hello", "fr");

        assertAll(
                () -> assertFalse(result.isSuccess()), () -> assertEquals("Unable to translate at this time.", result.getMessage())
        );
    }

    @Test
    @DisplayName("detectAndTranslate: null target language should normalize to en")
    void detectAndTranslate_nullTarget_shouldNormalizeToEn() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1).thenReturn(response2);

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"de\"}]");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"translatedText\":\"hello\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("hallo", null);

        assertAll(
                () -> assertTrue(result.isSuccess()), () -> assertEquals("de", result.getSourceLang()), () -> assertEquals("en", result.getTargetLang()), () -> assertEquals("hello", result.getTranslatedText())
        );
    }

    @Test
    @DisplayName("detectAndTranslate: unsupported target language should normalize to en")
    void detectAndTranslate_unsupportedTarget_shouldNormalizeToEn() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1).thenReturn(response2);

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"fr\"}]");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"translatedText\":\"hello\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("bonjour", "xx");

        assertAll(
                () -> assertTrue(result.isSuccess()), () -> assertEquals("fr", result.getSourceLang()), () -> assertEquals("en", result.getTargetLang()), () -> assertEquals("hello", result.getTranslatedText())
        );
    }

    @Test
    @DisplayName("detectAndTranslate: should accept alternate translation JSON field 'translation'")
    void detectAndTranslate_alternateTranslationField_shouldSucceed() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1).thenReturn(response2);

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"pt\"}]");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"translation\":\"hello alt\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("olá", "en");

        assertAll(
                () -> assertTrue(result.isSuccess()), () -> assertEquals("pt", result.getSourceLang()), () -> assertEquals("en", result.getTargetLang()), () -> assertEquals("hello alt", result.getTranslatedText())
        );
    }

    @Test
    @DisplayName("detectAndTranslate: should accept nested data.translations[0].translatedText")
    void detectAndTranslate_nestedTranslationField_shouldSucceed() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1).thenReturn(response2);

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"nl\"}]");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"data\":{\"translations\":[{\"translatedText\":\"hello nested\"}]}}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("maak voorbeeld", "en");

        assertAll(
                () -> assertTrue(result.isSuccess()), () -> assertEquals("nl", result.getSourceLang()), () -> assertEquals("en", result.getTargetLang()), () -> assertEquals("hello nested", result.getTranslatedText())
        );
    }

    @Test
    @DisplayName("detectAndTranslate: mojibake input should be fixed before processing")
    void detectAndTranslate_mojibakeInput_shouldStillSucceed() throws Exception
    {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response1).thenReturn(response2);

        when(response1.statusCode()).thenReturn(200);
        when(response1.body()).thenReturn("[{\"language\":\"es\"}]");

        when(response2.statusCode()).thenReturn(200);
        when(response2.body()).thenReturn("{\"translatedText\":\"¿cómo estás?\"}");

        TranslationService.TranslationResult result = underTest.detectAndTranslate("Â¿CÃ³mo estÃ¡s?", "en");

        assertTrue(result.isSuccess());
        assertEquals("es", result.getSourceLang());
    }

    // ---------- Reflection helper ----------
    private static void setField(Object target, String fieldName, Object value) throws Exception
    {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);

        // Remove final modifier if necessary (works on common JVM setups for tests)
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(f, f.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        } catch (NoSuchFieldException ignored) {
            // JDKs where this is not available; direct set may still work in test runtime
        }

        f.set(target, value);
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
        DefaultTranslationService underTest2 = new DefaultTranslationService();

        // When
        String text = "text"; // UTA: configured value
        String targetLang = "targetLang"; // UTA: default value
        TranslationResult result = underTest2.detectAndTranslate(text, targetLang);

        // Then - assertions for result of method detectAndTranslate(String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertFalse(result.isSuccess());
        }, () -> {
            assertEquals("", result.getSourceLang());
        }, () -> {
            assertEquals("", result.getTargetLang());
        }, () -> {
            assertEquals("", result.getTranslatedText());
        }, () -> {
            assertEquals("Unable to translate at this time.", result.getMessage());
        });

    }

}
