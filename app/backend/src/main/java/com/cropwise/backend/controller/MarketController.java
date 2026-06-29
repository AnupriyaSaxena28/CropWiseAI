package com.cropwise.backend.controller;

import com.cropwise.backend.service.MarketService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService market;
    public MarketController(MarketService market) { this.market = market; }

    @GetMapping
    public Map<String, Object> get(@RequestParam(defaultValue = "current") String type,
                                   @RequestParam(defaultValue = "Wheat") String crop,
                                   @RequestParam(required = false) String state,
                                   @RequestParam(required = false) String district) {
        if ("historical".equals(type)) {
            return Map.of("success", true, "data", market.historical(crop));
        }
        return Map.of("success", true, "data", market.currentPrices(state, district));
    }
}
