package com.cropwise.backend.controller;

import com.cropwise.backend.service.Neo4jService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/neo4j")
public class Neo4jController {

    private final Neo4jService neo4j;
    public Neo4jController(Neo4jService neo4j) { this.neo4j = neo4j; }

    /** Seed the Sustainability Knowledge Graph (mirrors website /api/neo4j/seed). */
    @GetMapping("/seed")
    public ResponseEntity<?> seed() {
        if (!neo4j.enabled())
            return ResponseEntity.ok(Map.of("success", false, "error", "NEO4J_URI not configured — graph disabled."));
        try {
            neo4j.seed();
            return ResponseEntity.ok(Map.of("success", true, "message", "Sustainability Knowledge Graph seeded successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Failed to seed knowledge graph."));
        }
    }
}
