package com.sim.chatserver.service.translation;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.translation.TranslationService.TranslationResult;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for TranslationResult
 *
 * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult
 * @author bmcmullin
 */
public class TranslationService_TranslationResultTest
{

    /**
     * Parasoft Jtest UTA: Test for fail(String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#fail(String)
     * @author bmcmullin
     */
    @Test
    public void testFail() throws Throwable
    {
        // When
        String message = "message"; // UTA: default value
        TranslationResult result = TranslationResult.fail(message);

        // Then - assertions for result of method fail(String)
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
            assertEquals("message", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for fail(String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#fail(String)
     * @author bmcmullin
     */
    @Test
    public void testFail2() throws Throwable
    {
        // When
        String message = null; // UTA: configured value
        TranslationResult result = TranslationResult.fail(message);

        // Then - assertions for result of method fail(String)
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
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getMessage()
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#getMessage()
     * @author bmcmullin
     */
    @Test
    public void testGetMessage() throws Throwable
    {
        // Given
        String message = "message"; // UTA: default value
        TranslationResult underTest = TranslationResult.fail(message);

        // When
        String result = underTest.getMessage();

        // Then - assertions for result of method getMessage()
        assertEquals("message", result);

        // Then - assertions for this instance of TranslationService.TranslationResult
        assertAll(() -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals("", underTest.getSourceLang());
        }, () -> {
            assertEquals("", underTest.getTargetLang());
        }, () -> {
            assertEquals("", underTest.getTranslatedText());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getSourceLang()
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#getSourceLang()
     * @author bmcmullin
     */
    @Test
    public void testGetSourceLang() throws Throwable
    {
        // Given
        String message = "message"; // UTA: default value
        TranslationResult underTest = TranslationResult.fail(message);

        // When
        String result = underTest.getSourceLang();

        // Then - assertions for result of method getSourceLang()
        assertEquals("", result);

        // Then - assertions for this instance of TranslationService.TranslationResult
        assertAll(() -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals("", underTest.getTargetLang());
        }, () -> {
            assertEquals("", underTest.getTranslatedText());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTargetLang()
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#getTargetLang()
     * @author bmcmullin
     */
    @Test
    public void testGetTargetLang() throws Throwable
    {
        // Given
        String message = "message"; // UTA: default value
        TranslationResult underTest = TranslationResult.fail(message);

        // When
        String result = underTest.getTargetLang();

        // Then - assertions for result of method getTargetLang()
        assertEquals("", result);

        // Then - assertions for this instance of TranslationService.TranslationResult
        assertAll(() -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals("", underTest.getSourceLang());
        }, () -> {
            assertEquals("", underTest.getTranslatedText());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for getTranslatedText()
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#getTranslatedText()
     * @author bmcmullin
     */
    @Test
    public void testGetTranslatedText() throws Throwable
    {
        // Given
        String message = "message"; // UTA: default value
        TranslationResult underTest = TranslationResult.fail(message);

        // When
        String result = underTest.getTranslatedText();

        // Then - assertions for result of method getTranslatedText()
        assertEquals("", result);

        // Then - assertions for this instance of TranslationService.TranslationResult
        assertAll(() -> {
            assertFalse(underTest.isSuccess());
        }, () -> {
            assertEquals("", underTest.getSourceLang());
        }, () -> {
            assertEquals("", underTest.getTargetLang());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for isSuccess()
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#isSuccess()
     * @author bmcmullin
     */
    @Test
    public void testIsSuccess() throws Throwable
    {
        // Given
        String message = "message"; // UTA: default value
        TranslationResult underTest = TranslationResult.fail(message);

        // When
        boolean result = underTest.isSuccess();

        // Then - assertions for result of method isSuccess()
        assertFalse(result);

        // Then - assertions for this instance of TranslationService.TranslationResult
        assertAll(() -> {
            assertEquals("", underTest.getSourceLang());
        }, () -> {
            assertEquals("", underTest.getTargetLang());
        }, () -> {
            assertEquals("", underTest.getTranslatedText());
        }, () -> {
            assertEquals("message", underTest.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk() throws Throwable
    {
        // When
        String sourceLang = "sourceLang"; // UTA: default value
        String targetLang = "targetLang"; // UTA: default value
        String translatedText = "translatedText"; // UTA: default value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("sourceLang", result.getSourceLang());
        }, () -> {
            assertEquals("targetLang", result.getTargetLang());
        }, () -> {
            assertEquals("translatedText", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk2() throws Throwable
    {
        // When
        String sourceLang = "sourceLang"; // UTA: default value
        String targetLang = "targetLang"; // UTA: default value
        String translatedText = null; // UTA: configured value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("sourceLang", result.getSourceLang());
        }, () -> {
            assertEquals("targetLang", result.getTargetLang());
        }, () -> {
            assertEquals("", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk3() throws Throwable
    {
        // When
        String sourceLang = "sourceLang"; // UTA: default value
        String targetLang = null; // UTA: configured value
        String translatedText = "translatedText"; // UTA: default value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("sourceLang", result.getSourceLang());
        }, () -> {
            assertEquals("", result.getTargetLang());
        }, () -> {
            assertEquals("translatedText", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk4() throws Throwable
    {
        // When
        String sourceLang = "sourceLang"; // UTA: default value
        String targetLang = null; // UTA: configured value
        String translatedText = null; // UTA: configured value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("sourceLang", result.getSourceLang());
        }, () -> {
            assertEquals("", result.getTargetLang());
        }, () -> {
            assertEquals("", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk5() throws Throwable
    {
        // When
        String sourceLang = null; // UTA: configured value
        String targetLang = "targetLang"; // UTA: default value
        String translatedText = "translatedText"; // UTA: default value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("", result.getSourceLang());
        }, () -> {
            assertEquals("targetLang", result.getTargetLang());
        }, () -> {
            assertEquals("translatedText", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk6() throws Throwable
    {
        // When
        String sourceLang = null; // UTA: configured value
        String targetLang = "targetLang"; // UTA: default value
        String translatedText = null; // UTA: configured value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("", result.getSourceLang());
        }, () -> {
            assertEquals("targetLang", result.getTargetLang());
        }, () -> {
            assertEquals("", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk7() throws Throwable
    {
        // When
        String sourceLang = null; // UTA: configured value
        String targetLang = null; // UTA: configured value
        String translatedText = "translatedText"; // UTA: default value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("", result.getSourceLang());
        }, () -> {
            assertEquals("", result.getTargetLang());
        }, () -> {
            assertEquals("translatedText", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }

    /**
     * Parasoft Jtest UTA: Test for ok(String, String, String)
     *
     * @see com.sim.chatserver.service.translation.TranslationService.TranslationResult#ok(String, String, String)
     * @author bmcmullin
     */
    @Test
    public void testOk8() throws Throwable
    {
        // When
        String sourceLang = null; // UTA: configured value
        String targetLang = null; // UTA: configured value
        String translatedText = null; // UTA: configured value
        TranslationResult result = TranslationResult.ok(sourceLang, targetLang, translatedText);

        // Then - assertions for result of method ok(String, String, String)
        assertAll(() -> {
            assertNotNull(result);
        }, () -> {
            assertTrue(result.isSuccess());
        }, () -> {
            assertEquals("", result.getSourceLang());
        }, () -> {
            assertEquals("", result.getTargetLang());
        }, () -> {
            assertEquals("", result.getTranslatedText());
        }, () -> {
            assertEquals("", result.getMessage());
        });

    }
}
