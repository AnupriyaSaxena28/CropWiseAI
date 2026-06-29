package com.cropwise.backend.controller;

import com.cropwise.backend.dto.WeatherDtos.WeatherData;
import com.cropwise.backend.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weather;
    public WeatherController(WeatherService weather) { this.weather = weather; }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(required = false, defaultValue = "default") String state,
                                 @RequestParam(required = false) Double lat,
                                 @RequestParam(required = false) Double lng,
                                 @RequestParam(required = false) String label) {
        try {
            WeatherService.Coord c = weather.resolve(state, lat, lng, label);
            WeatherData data = weather.fetch(c);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Weather fetch failed"));
        }
    }
}
