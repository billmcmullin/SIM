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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(DefaultTranslationService.class.getName());

    private static final Set<String> SPANISH_HINTS = Set.of(
            "hola", "gracias", "por favor", "necesito", "ayuda", "error", "respuesta", "pregunta", "no funciona"
    );
    private static final Set<String> FRENCH_HINTS = Set.of(
            "bonjour", "merci", "s'il", "aide", "erreur", "réponse", "question", "ne fonctionne"
    );
    private static final Set<String> GERMAN_HINTS = Set.of(
            "gibt", "nicht", "möglichkeit", "kommandozeile", "über", "dann", "und", "händisch",
            "hallo", "danke", "bitte", "hilfe", "fehler", "antwort", "frage", "funktioniert nicht"
    );
    private static final Set<String> PORTUGUESE_HINTS = Set.of(
            "olá", "obrigado", "por favor", "ajuda", "erro", "resposta", "pergunta", "não funciona"
    );
    private static final Set<String> DUTCH_HINTS = Set.of(
            "maak", "voorbeeld", "voor", "die", "je", "kunt", "gebruiken", "startpunt", "opstellen", "een", "kunnen", "niet"
    );

    private static final String DEFAULT_TRANSLATE_URL = "http://localhost:5000/translate";
    private static final Map<String, String> ENV = new ProcessBuilder().environment();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final String translateUrl;
    private final String detectUrl;
    private final String apiKey;

    public DefaultTranslationService() {
        this.translateUrl = envOrDefault("SIM_TRANSLATE_URL", DEFAULT_TRANSLATE_URL);
        this.detectUrl = toDetectUrl(this.translateUrl);
        this.apiKey = envOrDefault("SIM_TRANSLATE_API_KEY", "");
    }

    @Override
    public TranslationResult detectAndTranslate(String text, String targetLang) {
        try {
            String safeText = text == null ? "" : text.trim();
            if (safeText.isBlank()) {
                return TranslationResult.fail("Text is required.");
            }

            safeText = fixCommonMojibake(safeText);

            String target = normalizeLang(targetLang);

            // Heuristic fallback
            String heuristic = detectLanguageHeuristic(safeText);

            // Provider detection preferred
            String providerDetected = detectLanguageWithProvider(safeText);
            String source = (providerDetected == null || providerDetected.isBlank()) ? heuristic : providerDetected;

            // IMPORTANT:
            // Always attempt translation call (even if source==target), because
            // detection can be wrong and provider may still produce corrected translation.
            String translated = translateWithLibreTranslate(safeText, source, target);
            if (translated == null || translated.isBlank()) {
                return TranslationResult.fail("Translation service returned empty text.");
            }

            translated = fixCommonMojibake(translated);
            return TranslationResult.ok(source, target, translated);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.WARNING, "Translation failed", ex);
            return TranslationResult.fail("Unable to translate at this time.");
        }
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return "en";
        }
        String normalized = lang.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "en", "es", "fr", "de", "pt", "it", "nl", "ja", "ko", "zh", "zh-cn", "zh-tw", "ru", "ar" ->
                normalized;
            default ->
                "en";
        };
    }

    private String detectLanguageHeuristic(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);

        int es = scoreHints(normalized, SPANISH_HINTS);
        int fr = scoreHints(normalized, FRENCH_HINTS);
        int de = scoreHints(normalized, GERMAN_HINTS);
        int pt = scoreHints(normalized, PORTUGUESE_HINTS);
        int nl = scoreHints(normalized, DUTCH_HINTS);

        if (normalized.matches(".*[¿¡].*")) {
            es += 2;
        }
        if (normalized.matches(".*[àâçéèêëîïôûùüÿœ].*")) {
            fr += 2;
        }
        if (normalized.matches(".*[äöüß].*")) {
            de += 3;
        }
        if (normalized.matches(".*[ãõáâàçéêíóôú].*")) {
            pt += 2;
        }
        if (normalized.matches(".*\\b(ij|oo|aa|ee)\\b.*")) {
            nl += 1;
        }

        int max = Math.max(Math.max(es, fr), Math.max(Math.max(de, pt), nl));
        if (max <= 0) {
            return "en";
        }
        if (max == es) {
            return "es";
        }
        if (max == fr) {
            return "fr";
        }
        if (max == de) {
            return "de";
        }
        if (max == pt) {
            return "pt";
        }
        return "nl";
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

    private String detectLanguageWithProvider(String text) {
        try {
            StringBuilder form = new StringBuilder();
            form.append("q=").append(urlEncode(text));
            if (apiKey != null && !apiKey.isBlank()) {
                form.append("&api_key=").append(urlEncode(apiKey));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(detectUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.fine(() -> "Provider detect returned HTTP " + response.statusCode());
                return "";
            }

            String body = response.body() == null ? "" : response.body().trim();
            if (body.isEmpty()) {
                return "";
            }

            try (JsonReader jr = Json.createReader(new StringReader(body))) {
                JsonArray arr = jr.readArray();
                if (arr == null || arr.isEmpty()) {
                    return "";
                }
                JsonObject top = arr.getJsonObject(0);
                if (top == null) {
                    return "";
                }
                return top.getString("language", "").toLowerCase(Locale.ROOT);
            }
        } catch (IOException | InterruptedException | JsonException | ClassCastException | IllegalStateException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.log(Level.FINE, "Provider language detect failed, using heuristic fallback", e);
            return "";
        }
    }

    private String translateWithLibreTranslate(String text, String sourceLang, String targetLang)
            throws IOException, InterruptedException {

        String source = (sourceLang == null || sourceLang.isBlank()) ? "auto" : sourceLang;

        StringBuilder form = new StringBuilder();
        form.append("q=").append(urlEncode(text));
        form.append("&source=").append(urlEncode(source));
        form.append("&target=").append(urlEncode(targetLang));
        form.append("&format=text");
        if (apiKey != null && !apiKey.isBlank()) {
            form.append("&api_key=").append(urlEncode(apiKey));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(translateUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        String body = response.body() == null ? "" : response.body();

        if (status < 200 || status >= 300) {
            throw new IOException("Translation HTTP " + status + ": " + body);
        }

        try (JsonReader jr = Json.createReader(new StringReader(body))) {
            JsonObject obj = jr.readObject();

            // LibreTranslate default:
            if (obj.containsKey("translatedText")) {
                return obj.getString("translatedText", "");
            }

            // Compatibility variants:
            if (obj.containsKey("translation")) {
                return obj.getString("translation", "");
            }

            if (obj.containsKey("data")) {
                JsonObject data = obj.getJsonObject("data");
                if (data != null && data.containsKey("translations")) {
                    JsonArray arr = data.getJsonArray("translations");
                    if (arr != null && !arr.isEmpty()) {
                        JsonObject first = arr.getJsonObject(0);
                        if (first != null) {
                            return first.getString("translatedText", "");
                        }
                    }
                }
            }
        } catch (JsonException | ClassCastException | IllegalStateException parseEx) {
            throw new IOException("Unable to parse translation response: " + body, parseEx);
        }

        return "";
    }

    private String fixCommonMojibake(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        if (s.contains("Ã") || s.contains("Â")) {
            byte[] latin1 = s.getBytes(StandardCharsets.ISO_8859_1);
            return new String(latin1, StandardCharsets.UTF_8);
        }
        return s;
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private String envOrDefault(String key, String fallback) {
        String v = sanitizeEnvValue(ENV.get(key), 4096);
        String safeFallback = Objects.requireNonNullElse(fallback, "").trim();
        return (v == null || v.isBlank()) ? safeFallback : v;
    }

    private String sanitizeEnvValue(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .trim();
        if (maxChars > 0 && normalized.length() > maxChars) {
            return normalized.substring(0, maxChars);
        }
        return normalized;
    }

    private String toDetectUrl(String translateEndpoint) {
        if (translateEndpoint == null || translateEndpoint.isBlank()) {
            return "http://localhost:5000/detect";
        }
        String t = translateEndpoint.trim();
        // replace trailing /translate with /detect
        if (t.endsWith("/translate")) {
            return t.substring(0, t.length() - "/translate".length()) + "/detect";
        }
        // fallback append
        if (t.endsWith("/")) {
            return t + "detect";
        }
        return t + "/detect";
    }
}
