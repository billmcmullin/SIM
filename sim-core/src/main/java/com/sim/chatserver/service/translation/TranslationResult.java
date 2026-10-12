package com.sim.chatserver.service.translation;

public final class TranslationResult {

    private static final String EMPTY = "";

    private final boolean success;
    private final String sourceLang;
    private final String targetLang;
    private final String translatedText;
    private final String message;

    private TranslationResult(boolean success, String sourceLang, String targetLang, String translatedText, String message) {
        this.success = success;
        this.sourceLang = sourceLang == null ? EMPTY : sourceLang;
        this.targetLang = targetLang == null ? EMPTY : targetLang;
        this.translatedText = translatedText == null ? EMPTY : translatedText;
        this.message = message == null ? EMPTY : message;
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

    static TranslationResult ok(String sourceLang, String targetLang, String translatedText) {
        return new TranslationResult(true, sourceLang, targetLang, translatedText, EMPTY);
    }

    static TranslationResult fail(String message) {
        return new TranslationResult(false, EMPTY, EMPTY, EMPTY, message);
    }
}
