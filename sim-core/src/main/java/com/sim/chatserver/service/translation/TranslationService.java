package com.sim.chatserver.service.translation;

/**
 * Translation service abstraction used by dashboard review UI.
 */
public interface TranslationService {

    TranslationResult detectAndTranslate(String text, String targetLang);
}

