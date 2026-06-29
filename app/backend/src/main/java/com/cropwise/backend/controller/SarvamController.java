package com.cropwise.backend.controller;

import com.cropwise.backend.service.SarvamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/sarvam")
public class SarvamController {

    private final SarvamService sarvam;
    public SarvamController(SarvamService sarvam) { this.sarvam = sarvam; }

    public record TtsRequest(String text, String targetLanguageCode, String speaker) {}

    @PostMapping("/tts")
    public ResponseEntity<?> tts(@RequestBody TtsRequest req) {
        try {
            if (req.text() == null || req.text().isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required for TTS"));
            String audio = sarvam.textToSpeech(req.text(), req.targetLanguageCode(), req.speaker());
            if (audio == null)
                return ResponseEntity.status(501).body(Map.of("error", "SARVAM_API_KEY is missing. Cannot generate speech."));
            return ResponseEntity.ok(Map.of("audioBase64", audio));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate speech"));
        }
    }

    @PostMapping(value = "/stt", consumes = "multipart/form-data")
    public ResponseEntity<?> stt(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "language_code", required = false) String languageCode) {
        try {
            if (file == null || file.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "No audio file provided"));
            return ResponseEntity.ok(sarvam.speechToText(file, languageCode));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error"));
        }
    }
}
