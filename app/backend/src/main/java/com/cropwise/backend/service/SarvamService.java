package com.cropwise.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/** Sarvam AI Indic audio: Text-to-Speech and Speech-to-Text. Ports app/api/sarvam/*. */
@Service
public class SarvamService {

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public SarvamService(WebClient.Builder builder,
                         @Value("${cropwise.sarvam.api-key}") String apiKey) {
        this.web = builder.build();
        this.apiKey = apiKey;
    }

    public boolean configured() { return apiKey != null && !apiKey.isBlank(); }

    /** Returns base64 audio, or null when not configured. */
    public String textToSpeech(String text, String targetLanguageCode, String speaker) throws Exception {
        if (!configured()) return null;
        List<String> inputs = chunk(text, 480);
        Map<String, Object> body = new HashMap<>();
        body.put("inputs", inputs);
        body.put("target_language_code", targetLanguageCode == null ? "hi-IN" : targetLanguageCode);
        body.put("speaker", speaker == null ? "priya" : speaker);
        body.put("pace", 1.0);
        body.put("speech_sample_rate", 8000);
        body.put("enable_preprocessing", true);
        body.put("model", "bulbul:v3");

        String raw = web.post().uri("https://api.sarvam.ai/text-to-speech")
                .header("api-subscription-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body).retrieve().bodyToMono(String.class).block();
        JsonNode node = mapper.readTree(raw);
        JsonNode audios = node.path("audios");
        if (audios.isArray() && audios.size() > 0) return audios.get(0).asText();
        return node.path("base64_audio").asText(null);
    }

    /** Returns transcript. Mock string when not configured (mirrors website). */
    public Map<String, String> speechToText(MultipartFile file, String languageCode) throws Exception {
        String lang = (languageCode == null || languageCode.isBlank()) ? "unknown" : languageCode;
        if (!configured()) {
            return Map.of("transcript",
                    "Hello, this is a mock transcription because the Sarvam API key is not set.",
                    "languageCode", lang);
        }
        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        mb.part("file", new ByteArrayResource(file.getBytes()) {
            @Override public String getFilename() { return "recording.wav"; }
        }).contentType(MediaType.APPLICATION_OCTET_STREAM);
        mb.part("model", "saarika:v2.5");
        mb.part("language_code", lang);

        String raw = web.post().uri("https://api.sarvam.ai/speech-to-text")
                .header("api-subscription-key", apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(mb.build()))
                .retrieve().bodyToMono(String.class).block();
        JsonNode node = mapper.readTree(raw);
        String transcript = node.path("transcript").asText(node.path("text").asText(""));
        return Map.of("transcript", transcript, "languageCode", node.path("language_code").asText(lang));
    }

    private List<String> chunk(String text, int maxLen) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLen, text.length());
            String c = text.substring(start, end);
            if (end < text.length()) {
                int br = Math.max(c.lastIndexOf(' '), c.lastIndexOf('\n'));
                if (br > maxLen - 100 && br > 0) { c = c.substring(0, br); start += br + 1; }
                else start += maxLen;
            } else start += maxLen;
            if (!c.trim().isEmpty()) chunks.add(c.trim());
        }
        return chunks;
    }
}
