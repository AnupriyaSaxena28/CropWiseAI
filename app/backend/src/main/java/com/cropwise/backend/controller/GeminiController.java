package com.cropwise.backend.controller;

import com.cropwise.backend.dto.GeminiDtos.*;
import com.cropwise.backend.service.GeminiService;
import org.springframework.web.bind.annotation.*;

/** Central AI endpoint — chat, pest_diagnosis, crop_advisor (mirrors website /api/gemini). */
@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService gemini;
    public GeminiController(GeminiService gemini) { this.gemini = gemini; }

    @PostMapping
    public GeminiResponse generate(@RequestBody GeminiRequest req) { return gemini.handle(req); }
}
