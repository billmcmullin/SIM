package com.sim.chatserver.service.translation;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sim.chatserver.util.CommonLiteralConstants;
import com.sim.chatserver.util.ServerDiagnosticsLog;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

/**
 * Real translation implementation using LibreTranslate-compatible API.
 *
 * Free/self-hosted workflow: 1) Run LibreTranslate locally: docker run -d -p
 * 5000:5000 libretranslate/libretranslate
 *
 * 2) Optional env config: SIM_TRANSLATE_URL=http://localhost:5000/translate
 * SIM_TRANSLATE_API_KEY=your_key (if your server requires key)
 */
public class DefaultTranslationService implements TranslationService {

    private static final Logger log = Logger.getLogger(DefaultTranslationService.class.getCanonicalName());
    private static final String DIAG_COMPONENT = "translation-service";
    private static final String EVENT_TRANSLATION_START = "translation-start";
    private static final String EVENT_TRANSLATION_SUCCESS = "translation-success";
    private static final String EVENT_TRANSLATION_ERROR = "translation-error";
    private static final String EVENT_DETECT_REQUEST = "detect-request";
    private static final String EVENT_DETECT_RESPONSE = "detect-response";
    private static final String EVENT_DETECT_ERROR = "detect-error";
    private static final String EVENT_TRANSLATE_REQUEST = "translate-request";
    private static final String EVENT_TRANSLATE_RESPONSE = "translate-response";
    private static final String DIAG_METHOD_POST = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_METHOD_POST;
    private static final String DIAG_KV_URL = CommonLiteralConstants.LOG_KV_URL;
    private static final String DIAG_KV_STATUS = CommonLiteralConstants.LOG_KV_STATUS;
    private static final String DIAG_KV_BODY = com.sim.chatserver.util.CommonLiteralConstants.LOG_KV_BODY;
    private static final String HEADER_CONTENT_TYPE = CommonLiteralConstants.HEADER_CONTENT_TYPE;
    private static final String HEADER_ACCEPT = CommonLiteralConstants.HEADER_ACCEPT;
    private static final String CONTENT_TYPE_FORM_UTF8 = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String ACCEPT_JSON = com.sim.chatserver.util.CommonLiteralConstants.CONTENT_TYPE_APPLICATION_JSON;
    private static final String KEY_API_KEY = "api_key";
    private static final String PARAM_API_KEY = "&api_key=";
    private static final String MESSAGE_PREFIX = CommonLiteralConstants.LOG_KV_MESSAGE;
        private static final String EMPTY = "";
    private static final String LANG_EN = "en";
        private static final String LANG_ES = "es";
        private static final String LANG_FR = "fr";
        private static final String LANG_DE = "de";
        private static final String LANG_PT = "pt";
        private static final String LANG_IT = "it";
        private static final String LANG_NL = "nl";
        private static final String LANG_JA = "ja";
        private static final String LANG_KO = "ko";
        private static final String LANG_ZH = "zh";
        private static final String LANG_ZH_CN = "zh-cn";
        private static final String LANG_ZH_TW = "zh-tw";
        private static final String LANG_RU = "ru";
        private static final String LANG_AR = "ar";
    private static final String LANG_AUTO = "auto";
        private static final Set<String> SUPPORTED_LANGS = Set.of(
            LANG_EN, LANG_ES, LANG_FR, LANG_DE, LANG_PT, LANG_IT, LANG_NL,
            LANG_JA, LANG_KO, LANG_ZH, LANG_ZH_CN, LANG_ZH_TW, LANG_RU, LANG_AR
        );
        private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
        private static final String ENV_SIM_TRANSLATE_URL = "SIM_TRANSLATE_URL";
        private static final String ENV_SIM_TRANSLATE_API_KEY = "SIM_TRANSLATE_API_KEY";
        private static final String DIAG_KV_TARGET_LANG = "targetLang=";
        private static final String DIAG_KV_TEXT_CHARS = "textChars=";
        private static final String DIAG_KV_TEXT = "text=";
        private static final String DIAG_KV_SOURCE_LANG = "sourceLang=";
        private static final String DIAG_KV_TRANSLATED_CHARS = "translatedChars=";
        private static final String DIAG_KV_TRANSLATED = "translated=";
        private static final String REGEX_SPANISH_PUNCT = ".*[Â¿Â¡].*";
        private static final String REGEX_FRENCH_ACCENTS = ".*[Ã Ã¢Ã§Ã©Ã¨ÃªÃ«Ã®Ã¯Ã´Ã»Ã¹Ã¼Ã¿Å“].*";
        private static final String REGEX_GERMAN_ACCENTS = ".*[Ã¤Ã¶Ã¼ÃŸ].*";
        private static final String REGEX_PORTUGUESE_ACCENTS = ".*[Ã£ÃµÃ¡Ã¢Ã Ã§Ã©ÃªÃ­Ã³Ã´Ãº].*";
        private static final String REGEX_DUTCH_BIGRAMS = ".*\\b(ij|oo|aa|ee)\\b.*";
        private static final String PARAM_Q = "q=";
        private static final String PARAM_SOURCE = "&source=";
        private static final String PARAM_TARGET = "&target=";
        private static final String PARAM_FORMAT_TEXT = "&format=text";
        private static final String DIAG_KV_SOURCE = "source=";
        private static final String DIAG_KV_TARGET = "target=";
        private static final String MSG_PROVIDER_DETECT_HTTP_PREFIX = "Provider detect returned HTTP ";
        private static final String KEY_LANGUAGE = "language";
        private static final String KEY_TRANSLATED_TEXT = "translatedText";
        private static final String KEY_TRANSLATION = "translation";
        private static final String KEY_DATA = "data";
        private static final String KEY_TRANSLATIONS = "translations";
        private static final String MSG_TRANSLATION_HTTP_PREFIX = "Translation HTTP ";
        private static final String STATUS_BODY_SEPARATOR = ": ";
        private static final String MSG_UNABLE_PARSE_TRANSLATION_RESPONSE_PREFIX = "Unable to parse translation response: ";
        private static final String MOJIBAKE_CHAR_A_TILDE = "\u00C3";
        private static final String MOJIBAKE_CHAR_A_CIRCUMFLEX = "\u00C2";
        private static final String REGEX_REDACT_API = "(?i)(" + KEY_API_KEY + "=)[^&\\s]+";
        private static final String REDACTED_API_REPLACEMENT = "$1[REDACTED]";
        private static final String REGEX_CONTROL_CHARS = com.sim.chatserver.util.CommonLiteralConstants.REGEX_CTRL_EXCEPT_CRLF_TAB;
        private static final String PATH_TRANSLATE = "/translate";
        private static final String PATH_DETECT = "detect";
        private static final String PATH_SLASH = "/";
        private static final String PATH_DETECT_SUFFIX = "/detect";
    private static final String MSG_TEXT_REQUIRED = "Text is required.";
    private static final String MSG_TRANSLATION_EMPTY = "Translation service returned empty text.";
    private static final String MSG_TRANSLATION_UNAVAILABLE = "Unable to translate at this time.";
    private static final String MSG_TRANSLATION_FAILED = "Translation failed";
    private static final String MSG_PROVIDER_DETECT_FAILED = "Provider language detect failed, using heuristic fallback";
    private static final String DEFAULT_DETECT_URL = "http://localhost:5000/detect";

    private static final Set<String> SPANISH_HINTS = Set.of(
            "hola", "gracias", "por favor", "necesito", "ayuda", CommonLiteralConstants.KEY_ERROR, "respuesta", "pregunta", "no funciona"
    );
    private static final Set<String> FRENCH_HINTS = Set.of(
            "bonjour", "merci", "s'il", "aide", "erreur", "rÃ©ponse", "question", "ne fonctionne"
    );
    private static final Set<String> GERMAN_HINTS = Set.of(
            "gibt", "nicht", "mÃ¶glichkeit", "kommandozeile", "Ã¼ber", "dann", "und", "hÃ¤ndisch",
            "hallo", "danke", "bitte", "hilfe", "fehler", "antwort", "frage", "funktioniert nicht"
    );
    private static final Set<String> PORTUGUESE_HINTS = Set.of(
            "olÃ¡", "obrigado", "por favor", "ajuda", "erro", "resposta", "pergunta", "nÃ£o funciona"
    );
    private static final Set<String> DUTCH_HINTS = Set.of(
            "maak", "voorbeeld", "voor", "die", "je", "kunt", "gebruiken", "startpunt", "opstellen", "een", "kunnen", "niet"
    );

    private static final String DEFAULT_TRANSLATE_URL = "http://localhost:5000/translate";
    private static final Map<String, String> ENV = new ProcessBuilder().environment();
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final String translateUrl;
    private final String detectUrl;
    private final String apiKey;

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    public DefaultTranslationService() {
        this.translateUrl = envOrDefault(ENV_SIM_TRANSLATE_URL, DEFAULT_TRANSLATE_URL);
        this.detectUrl = toDetectUrl(this.translateUrl);
        this.apiKey = envOrDefault(ENV_SIM_TRANSLATE_API_KEY, EMPTY);
    }

    @Override
    public TranslationResult detectAndTranslate(String text, String targetLang) {
        String requestId = UUID.randomUUID().toString();
        try {
            String safeText = text == null ? EMPTY : text.trim();
            if (safeText.isBlank()) {
                return TranslationResult.fail(MSG_TEXT_REQUIRED);
            }

            safeText = fixCommonMojibake(safeText);

            String target = normalizeLang(targetLang);
                ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_TRANSLATION_START,
                    DIAG_KV_TARGET_LANG + target
                        + LINE_SEPARATOR + DIAG_KV_TEXT_CHARS + safeText.length()
                        + LINE_SEPARATOR + DIAG_KV_TEXT + safeText
                );

            // Heuristic fallback
            String heuristic = detectLanguageHeuristic(safeText);

            // Provider detection preferred
                String providerDetected = detectLanguageWithProvider(safeText, requestId);
            String source = (providerDetected == null || providerDetected.isBlank()) ? heuristic : providerDetected;

            // IMPORTANT:
            // Always attempt translation call (even if source==target), because
            // detection can be wrong and provider may still produce corrected translation.
            String translated = translateWithLibreTranslate(safeText, source, target, requestId);
            if (translated == null || translated.isBlank()) {
                return TranslationResult.fail(MSG_TRANSLATION_EMPTY);
            }

            translated = fixCommonMojibake(translated);
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_TRANSLATION_SUCCESS,
                    DIAG_KV_SOURCE_LANG + source
                        + LINE_SEPARATOR + DIAG_KV_TARGET_LANG + target
                        + LINE_SEPARATOR + DIAG_KV_TRANSLATED_CHARS + translated.length()
                        + LINE_SEPARATOR + DIAG_KV_TRANSLATED + translated
            );
            return TranslationResult.ok(source, target, translated);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, MSG_TRANSLATION_FAILED, ex);
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_TRANSLATION_ERROR,
                    MESSAGE_PREFIX + safe(ex.getMessage()),
                    ex
            );
                return TranslationResult.fail(MSG_TRANSLATION_UNAVAILABLE);
        } catch (IOException ex) {
            log.log(Level.WARNING, MSG_TRANSLATION_FAILED, ex);
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_TRANSLATION_ERROR,
                    MESSAGE_PREFIX + safe(ex.getMessage()),
                    ex
            );
                return TranslationResult.fail(MSG_TRANSLATION_UNAVAILABLE);
        }
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return LANG_EN;
        }
        String normalized = lang.trim().toLowerCase(Locale.ROOT);
        return SUPPORTED_LANGS.contains(normalized) ? normalized : LANG_EN;
    }

    private String detectLanguageHeuristic(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);

        int es = scoreHints(normalized, SPANISH_HINTS);
        int fr = scoreHints(normalized, FRENCH_HINTS);
        int de = scoreHints(normalized, GERMAN_HINTS);
        int pt = scoreHints(normalized, PORTUGUESE_HINTS);
        int nl = scoreHints(normalized, DUTCH_HINTS);

        if (normalized.matches(REGEX_SPANISH_PUNCT)) {
            es += 2;
        }
        if (normalized.matches(REGEX_FRENCH_ACCENTS)) {
            fr += 2;
        }
        if (normalized.matches(REGEX_GERMAN_ACCENTS)) {
            de += 3;
        }
        if (normalized.matches(REGEX_PORTUGUESE_ACCENTS)) {
            pt += 2;
        }
        if (normalized.matches(REGEX_DUTCH_BIGRAMS)) {
            nl += 1;
        }

        int max = Math.max(Math.max(es, fr), Math.max(Math.max(de, pt), nl));
        if (max <= 0) {
            return LANG_EN;
        }
        if (max == es) {
            return LANG_ES;
        }
        if (max == fr) {
            return LANG_FR;
        }
        if (max == de) {
            return LANG_DE;
        }
        if (max == pt) {
            return LANG_PT;
        }
        return LANG_NL;
    }

    private int scoreHints(String text, Set<String> hints) {
        int score = 0;
        for (String h : hints) {
            if (text.contains(h)) {
                score++;
            }
        }
        return score;
    }

    private String detectLanguageWithProvider(String text, String requestId) {
        try {
            StringBuilder form = new StringBuilder();
            form.append(PARAM_Q).append(urlEncode(text));
            if (apiKey != null && !apiKey.isBlank()) {
                form.append(PARAM_API_KEY).append(urlEncode(apiKey));
            }

            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_DETECT_REQUEST,
                    DIAG_METHOD_POST + LINE_SEPARATOR + DIAG_KV_URL + detectUrl
                        + LINE_SEPARATOR + DIAG_KV_BODY + redactApiKey(form.toString())
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(detectUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_UTF8)
                    .header(HEADER_ACCEPT, ACCEPT_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responseBody = response.body() == null ? EMPTY : response.body();
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_DETECT_RESPONSE,
                    DIAG_KV_STATUS + response.statusCode()
                        + LINE_SEPARATOR + DIAG_KV_BODY + responseBody
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.fine(() -> MSG_PROVIDER_DETECT_HTTP_PREFIX + response.statusCode());
                return EMPTY;
            }

            String body = responseBody.trim();
            if (body.isEmpty()) {
                return EMPTY;
            }

            try (JsonReader jr = Json.createReader(new StringReader(body))) {
                JsonArray arr = jr.readArray();
                if (arr == null || arr.isEmpty()) {
                    return EMPTY;
                }
                JsonObject top = arr.getJsonObject(0);
                if (top == null) {
                    return EMPTY;
                }
                return top.getString(KEY_LANGUAGE, EMPTY).toLowerCase(Locale.ROOT);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.FINE, MSG_PROVIDER_DETECT_FAILED, e);
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_DETECT_ERROR,
                    MESSAGE_PREFIX + safe(e.getMessage()),
                    e
            );
            return EMPTY;
        } catch (IOException | JsonException | ClassCastException | IllegalStateException e) {
            log.log(Level.FINE, MSG_PROVIDER_DETECT_FAILED, e);
            ServerDiagnosticsLog.write(
                    DIAG_COMPONENT,
                    requestId,
                    EVENT_DETECT_ERROR,
                    MESSAGE_PREFIX + safe(e.getMessage()),
                    e
            );
            return EMPTY;
        }
    }

    private String translateWithLibreTranslate(String text, String sourceLang, String targetLang, String requestId)
            throws IOException, InterruptedException {

        String source = (sourceLang == null || sourceLang.isBlank()) ? LANG_AUTO : sourceLang;
        String safeTargetLang = (targetLang == null || targetLang.isBlank()) ? LANG_EN : targetLang;

        StringBuilder form = new StringBuilder();
        form.append(PARAM_Q).append(urlEncode(text));
        form.append(PARAM_SOURCE).append(urlEncode(source));
        form.append(PARAM_TARGET).append(urlEncode(safeTargetLang));
        form.append(PARAM_FORMAT_TEXT);
        if (apiKey != null && !apiKey.isBlank()) {
            form.append(PARAM_API_KEY).append(urlEncode(apiKey));
        }

        ServerDiagnosticsLog.write(
            DIAG_COMPONENT,
            requestId,
            EVENT_TRANSLATE_REQUEST,
            DIAG_METHOD_POST + LINE_SEPARATOR + DIAG_KV_URL + translateUrl
                + LINE_SEPARATOR + DIAG_KV_SOURCE + source
                + LINE_SEPARATOR + DIAG_KV_TARGET + safeTargetLang
                + LINE_SEPARATOR + DIAG_KV_BODY + redactApiKey(form.toString())
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(translateUrl))
                .timeout(Duration.ofSeconds(20))
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_UTF8)
                .header(HEADER_ACCEPT, ACCEPT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(form.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        String body = response.body() == null ? EMPTY : response.body();

        ServerDiagnosticsLog.write(
            DIAG_COMPONENT,
            requestId,
            EVENT_TRANSLATE_RESPONSE,
            DIAG_KV_STATUS + status
                + LINE_SEPARATOR + DIAG_KV_BODY + body
        );

        if (status < 200 || status >= 300) {
            throw new IOException(MSG_TRANSLATION_HTTP_PREFIX + status + STATUS_BODY_SEPARATOR + body);
        }

        try (JsonReader jr = Json.createReader(new StringReader(body))) {
            JsonObject obj = jr.readObject();

            // LibreTranslate default:
            if (obj.containsKey(KEY_TRANSLATED_TEXT)) {
                return obj.getString(KEY_TRANSLATED_TEXT, EMPTY);
            }

            // Compatibility variants:
            if (obj.containsKey(KEY_TRANSLATION)) {
                return obj.getString(KEY_TRANSLATION, EMPTY);
            }

            if (obj.containsKey(KEY_DATA)) {
                JsonObject data = obj.getJsonObject(KEY_DATA);
                if (data != null && data.containsKey(KEY_TRANSLATIONS)) {
                    JsonArray arr = data.getJsonArray(KEY_TRANSLATIONS);
                    if (arr != null && !arr.isEmpty()) {
                        JsonObject first = arr.getJsonObject(0);
                        if (first != null) {
                            return first.getString(KEY_TRANSLATED_TEXT, EMPTY);
                        }
                    }
                }
            }
        } catch (JsonException | ClassCastException | IllegalStateException parseEx) {
            throw new IOException(MSG_UNABLE_PARSE_TRANSLATION_RESPONSE_PREFIX + body, parseEx);
        }

        return EMPTY;
    }

    private String fixCommonMojibake(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        if (s.contains(MOJIBAKE_CHAR_A_TILDE) || s.contains(MOJIBAKE_CHAR_A_CIRCUMFLEX)) {
            byte[] latin1 = s.getBytes(StandardCharsets.ISO_8859_1);
            return new String(latin1, StandardCharsets.UTF_8);
        }
        return s;
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s == null ? EMPTY : s, StandardCharsets.UTF_8);
    }

    private String envOrDefault(String key, String fallback) {
        String v = sanitizeEnvValue(ENV.get(key), 4096);
        String safeFallback = Objects.requireNonNullElse(fallback, EMPTY).trim();
        return (v == null || v.isBlank()) ? safeFallback : v;
    }

    private String safe(String value) {
        return value == null ? EMPTY : value;
    }

    private String redactApiKey(String value) {
        if (value == null || value.isBlank()) {
            return EMPTY;
        }
        return value.replaceAll(REGEX_REDACT_API, REDACTED_API_REPLACEMENT);
    }

    private String sanitizeEnvValue(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replaceAll(REGEX_CONTROL_CHARS, EMPTY)
                .trim();
        if (maxChars > 0 && normalized.length() > maxChars) {
            return new String(normalized.toCharArray(), 0, maxChars);
        }
        return normalized;
    }

    private String toDetectUrl(String translateEndpoint) {
        if (translateEndpoint == null || translateEndpoint.isBlank()) {
            return DEFAULT_DETECT_URL;
        }
        String t = translateEndpoint.trim();
        // replace trailing /translate with /detect
        if (t.endsWith(PATH_TRANSLATE)) {
            return new String(t.toCharArray(), 0, t.length() - PATH_TRANSLATE.length()) + PATH_DETECT_SUFFIX;
        }
        // fallback append
        if (t.endsWith(PATH_SLASH)) {
            return t + PATH_DETECT;
        }
        return t + PATH_DETECT_SUFFIX;
    }
}
