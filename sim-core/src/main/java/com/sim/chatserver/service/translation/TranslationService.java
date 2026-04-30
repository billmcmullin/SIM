package com.sim.chatserver.service.translation;

/**
 * Translation service abstraction used by dashboard review UI.
 */
public interface TranslationService {

    TranslationResult detectAndTranslate(String text, String targetLang);

    final class TranslationResult {

        private final boolean success;
        private final String sourceLang;
        private final String targetLang;
        private final String translatedText;
        private final String message;

        public TranslationResult(boolean success, String sourceLang, String targetLang, String translatedText, String message) {
            this.success = success;
            this.sourceLang = sourceLang == null ? "" : sourceLang;
            this.targetLang = targetLang == null ? "" : targetLang;
            this.translatedText = translatedText == null ? "" : translatedText;
            this.message = message == null ? "" : message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getSourceLang() {
            return sourceLang;
        }

        public String getTargetLang() {
            return targetLang;
        }

        public String getTranslatedText() {
            return translatedText;
        }

        public String getMessage() {
            return message;
        }

        public static TranslationResult ok(String sourceLang, String targetLang, String translatedText) {
            return new TranslationResult(true, sourceLang, targetLang, translatedText, "");
        }

        public static TranslationResult fail(String message) {
            return new TranslationResult(false, "", "", "", message);
        }
    }
}
