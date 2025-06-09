package ma.ensa.ebankingver1.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MyMemoryTranslateClient {
    private static final Logger logger = LoggerFactory.getLogger(MyMemoryTranslateClient.class);
    private static final String TRANSLATE_URL = "https://api.mymemory.translated.net/get";
    private static final String API_KEY = "f4d21d9d68d621cd2da6";
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String translate(String text, String sourceLang, String targetLang) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            logger.error("Text to translate is empty or null");
            throw new IllegalArgumentException("Text to translate cannot be empty or null");
        }

        logger.info("Traduction de '{}' de {} vers {}", text, sourceLang, targetLang);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(TRANSLATE_URL).newBuilder();
        urlBuilder.addQueryParameter("q", text.trim())
                .addQueryParameter("langpair", sourceLang + "|" + targetLang);
        if (!API_KEY.isEmpty()) {
            urlBuilder.addQueryParameter("key", API_KEY);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Erreur MyMemory : {} - {}", response.code(), response.body() != null ? response.body().string() : "No body");
                throw new IOException("Erreur MyMemory : " + response.code() + " - " + (response.body() != null ? response.body().string() : "No details"));
            }
            JsonNode jsonResponse = objectMapper.readTree(response.body().string());
            String translatedText = jsonResponse.path("responseData").path("translatedText").asText();
            if (translatedText == null || translatedText.trim().isEmpty()) {
                logger.warn("Translated text is empty or null for input: {}", text);
                return text; // Retourner le texte original en cas d'échec
            }
            logger.debug("Texte traduit : {}", translatedText);
            return translatedText;
        } catch (IOException e) {
            logger.error("Erreur lors de l'appel MyMemory : {}", e.getMessage(), e);
            throw e;
        }
    }
}