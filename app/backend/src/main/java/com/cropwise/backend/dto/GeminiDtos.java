package com.cropwise.backend.dto;

import java.util.List;
import java.util.Map;

/** Mirrors the website's GeminiRequestBody / GeminiResponseBody. */
public class GeminiDtos {

    public record HistoryItem(String role, String content) {}   // role: "user" | "model"

    public record GeminiRequest(
            String prompt,
            String mode,            // "chat" | "pest_diagnosis" | "crop_advisor"
            String language,
            String imageBase64,
            String imageMimeType,
            String context,
            List<HistoryItem> history) {}

    public record GeminiResponse(
            boolean success,
            String text,
            Map<String, Object> structured,
            String error) {

        public static GeminiResponse ok(String text) {
            return new GeminiResponse(true, text, null, null);
        }
        public static GeminiResponse ok(Map<String, Object> structured) {
            return new GeminiResponse(true, null, structured, null);
        }
        public static GeminiResponse fail(String error) {
            return new GeminiResponse(false, null, null, error);
        }
    }
}
