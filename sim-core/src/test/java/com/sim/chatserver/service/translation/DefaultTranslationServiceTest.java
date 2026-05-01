package com.sim.chatserver.service.translation;

import org.junit.jupiter.api.Test;

import com.sim.chatserver.service.translation.TranslationService.TranslationResult;
/**
 * Parasoft Jtest UTA: Test class for DefaultTranslationService
 *
 * @see com.sim.chatserver.service.translation.DefaultTranslationService
 * @author bmcmullin
 */
public class DefaultTranslationServiceTest
{

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
