package com.cropwise.backend.service;

import com.cropwise.backend.dto.GeminiDtos.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * Calls the Google Gemini REST API (generativelanguage.googleapis.com).
 * Three modes mirror the website: chat, pest_diagnosis (vision), crop_advisor.
 * When GEMINI_API_KEY is unset it returns a graceful fallback so the app still runs.
 */
@Service
public class GeminiService {

    private final WebClient web;
    private final Neo4jService neo4j;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiService(WebClient.Builder builder, Neo4jService neo4j,
                         @Value("${cropwise.gemini.api-key}") String apiKey,
                         @Value("${cropwise.gemini.model}") String model,
                         @Value("${cropwise.gemini.base-url}") String baseUrl) {
        this.web = builder.build();
        this.neo4j = neo4j;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    public boolean configured() { return apiKey != null && !apiKey.isBlank(); }

    public GeminiResponse handle(GeminiRequest req) {
        String mode = req.mode() == null ? "chat" : req.mode();
        String language = req.language() == null ? "en" : req.language();
        if (req.prompt() == null || req.prompt().isBlank()) {
            if (!"pest_diagnosis".equals(mode)) return GeminiResponse.fail("Prompt required.");
        }
        return switch (mode) {
            case "chat" -> chat(req.prompt(), language, req.context(), req.history());
            case "pest_diagnosis" -> pest(req, language);
            case "crop_advisor" -> crop(req.prompt(), req.context(), language);
            default -> GeminiResponse.fail("Invalid mode: " + mode);
        };
    }

    // ── chat ──
    private GeminiResponse chat(String prompt, String language, String context, List<HistoryItem> history) {
        if (!configured()) return GeminiResponse.ok(fallbackChat(language));
        var contents = new ArrayList<Map<String, Object>>();
        if (history != null) {
            for (HistoryItem h : history) {
                contents.add(Map.of("role", h.role(), "parts", List.of(Map.of("text", h.content()))));
            }
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", prompt))));
        try {
            JsonNode resp = call(GeminiPrompts.chatSystem(language, context), contents, false);
            return GeminiResponse.ok(extractText(resp));
        } catch (Exception e) {
            return GeminiResponse.ok(fallbackChat(language));
        }
    }

    // ── pest diagnosis (vision) + Neo4j enrichment ──
    @SuppressWarnings("unchecked")
    private GeminiResponse pest(GeminiRequest req, String language) {
        if (req.imageBase64() == null || req.imageMimeType() == null) {
            return GeminiResponse.fail("Image required for pest diagnosis.");
        }
        if (!configured()) return GeminiResponse.ok(fallbackPest());

        String userText = (req.prompt() != null && !req.prompt().isBlank()) ? req.prompt()
            : "Diagnose any disease or pest visible in this crop image using the sustainable pest management guidelines.";
        var parts = new ArrayList<Map<String, Object>>();
        parts.add(Map.of("text", userText));
        parts.add(Map.of("inlineData", Map.of("mimeType", req.imageMimeType(), "data", req.imageBase64())));
        var contents = List.<Map<String, Object>>of(Map.of("role", "user", "parts", parts));

        try {
            String system = GeminiPrompts.PEST_SYSTEM + GeminiPrompts.pestLangInstruction(language);
            JsonNode resp = call(system, contents, true);
            Map<String, Object> structured = mapper.convertValue(
                    mapper.readTree(extractText(resp)), Map.class);

            // Neo4j sustainability enrichment (graceful if unavailable)
            Object disease = structured.get("diseaseName");
            if (disease != null) {
                var alts = neo4j.sustainableTreatments(disease.toString());
                if (!alts.isEmpty()) {
                    Object t = structured.get("treatment");
                    Map<String, Object> treatment = (t instanceof Map) ? (Map<String, Object>) t : new HashMap<>();
                    treatment.put("sustainableAlternativesFromGraph", alts);
                    structured.put("treatment", treatment);
                }
            }
            return GeminiResponse.ok(structured);
        } catch (Exception e) {
            return GeminiResponse.ok(fallbackPest());
        }
    }

    // ── crop advisor ──
    private GeminiResponse crop(String prompt, String context, String language) {
        if (!configured()) return GeminiResponse.ok(fallbackCrop());
        String full = (context != null && !context.isBlank())
                ? "Farmer Profile:\n" + context + "\n\nRequest:\n" + prompt : prompt;
        var contents = List.<Map<String, Object>>of(
                Map.of("role", "user", "parts", List.of(Map.of("text", full))));
        try {
            String system = GeminiPrompts.CROP_ADVISOR_SYSTEM + GeminiPrompts.cropLangInstruction(language);
            JsonNode resp = call(system, contents, true);
            @SuppressWarnings("unchecked")
            Map<String, Object> structured = mapper.convertValue(mapper.readTree(extractText(resp)), Map.class);
            return GeminiResponse.ok(structured);
        } catch (Exception e) {
            return GeminiResponse.ok(fallbackCrop());
        }
    }

    // ── low-level REST call ──
    private JsonNode call(String system, List<Map<String, Object>> contents, boolean jsonOut) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", system))));
        body.put("contents", contents);
        body.put("safetySettings", List.of(
                Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_ONLY_HIGH"),
                Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_ONLY_HIGH"),
                Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_ONLY_HIGH"),
                Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_ONLY_HIGH")));
        if (jsonOut) body.put("generationConfig", Map.of("responseMimeType", "application/json"));

        String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;
        String raw = web.post().uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return mapper.readTree(raw);
    }

    private String extractText(JsonNode resp) {
        JsonNode parts = resp.path("candidates").path(0).path("content").path("parts");
        StringBuilder sb = new StringBuilder();
        if (parts.isArray()) for (JsonNode p : parts) sb.append(p.path("text").asText(""));
        return sb.toString().trim();
    }

    // ── fallbacks (no API key) ──
    private String fallbackChat(String language) {
        return "AI is not configured yet (set GEMINI_API_KEY on the backend). " +
               "Tip: for healthy wheat, irrigate at crown-root, tillering and grain-filling stages, " +
               "and consult a local agronomist for crop-specific advice.";
    }

    private Map<String, Object> fallbackPest() {
        return Map.of(
            "diseaseName", "Unable to Diagnose",
            "scientificName", "",
            "confidencePercent", 0,
            "severity", "Low",
            "affectedArea", "Set GEMINI_API_KEY to enable image diagnosis.",
            "symptoms", List.of("Diagnosis service not configured."),
            "treatment", Map.of(
                "immediate", List.of("ORGANIC: Neem oil spray is a safe general-purpose first step."),
                "preventive", List.of("CULTURAL: Rotate crops and remove infected debris."),
                "recommendedPesticides", List.of()),
            "disclaimer", "Consult a local agronomist before applying treatments.",
            "detailedReportSections", List.of());
    }

    private Map<String, Object> fallbackCrop() {
        // Built explicitly because Map.of() supports at most 10 key-value pairs.
        Map<String, Object> rec = new LinkedHashMap<>();
        rec.put("cropName", "Wheat");
        rec.put("localName", "गेहूं");
        rec.put("suitabilityScore", 80);
        rec.put("climateRiskScore", 30);
        rec.put("climateRiskLevel", "Low");
        rec.put("carbonFootprint", "Low (0.5 kg CO2/kg)");
        rec.put("sustainabilityTags", List.of("Staple", "Rabi"));
        rec.put("expectedYield", "18-22 quintal/acre");
        rec.put("estimatedROI", Map.of("investmentPerAcre", 18000, "expectedRevenuePerAcre", 45000,
                "profitPerAcre", 27000, "paybackMonths", 5));
        rec.put("growingPeriodDays", 140);
        rec.put("waterRequirement", "Medium");
        rec.put("soilCompatibility", "Loam / Clay-loam");
        rec.put("reasonsForRecommendation", List.of("Set GEMINI_API_KEY for personalised AI recommendations."));
        rec.put("risks", List.of("Yellow rust in humid spells"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("recommendations", List.of(rec));
        out.put("generalAdvice", "Demo recommendation — configure GEMINI_API_KEY for live AI advice.");
        out.put("bestCrop", "Wheat");
        return out;
    }
}
