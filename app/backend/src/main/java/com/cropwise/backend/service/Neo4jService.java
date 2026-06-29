package com.cropwise.backend.service;

import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sustainability Knowledge Graph (Neo4j AuraDB). Optional: if NEO4J_URI is unset
 * the driver is never created and queries return empty, exactly mirroring the
 * website's "continue without failing" behaviour.
 */
@Service
public class Neo4jService implements AutoCloseable {

    private final String uri;
    private final String user;
    private final String password;
    private Driver driver;

    public Neo4jService(@Value("${cropwise.neo4j.uri}") String uri,
                        @Value("${cropwise.neo4j.username}") String user,
                        @Value("${cropwise.neo4j.password}") String password) {
        this.uri = uri;
        this.user = user;
        this.password = password;
    }

    public boolean enabled() {
        return uri != null && !uri.isBlank();
    }

    private synchronized Driver driver() {
        if (driver == null) {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        }
        return driver;
    }

    /** Fetch sustainable treatments for a detected pest/disease, eco-friendly first. */
    public List<Map<String, Object>> sustainableTreatments(String diseaseName) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (!enabled() || diseaseName == null || diseaseName.isBlank()) return out;
        String cypher = """
            MATCH (p:Pest)-[:TREATED_BY]->(t:Treatment)
            WHERE toLower(p.name) CONTAINS toLower($d) OR toLower($d) CONTAINS toLower(p.name)
            RETURN t.name AS name, t.type AS type, t.isEcoFriendly AS isEcoFriendly,
                   t.description AS description, t.soilHealthBenefit AS soilHealthBenefit,
                   t.sustainabilityScore AS score
            ORDER BY t.sustainabilityScore DESC
            """;
        try (Session s = driver().session()) {
            Result r = s.run(cypher, Values.parameters("d", diseaseName));
            r.forEachRemaining(rec -> out.add(Map.of(
                    "name", rec.get("name").asString(""),
                    "type", rec.get("type").asString(""),
                    "description", rec.get("description").asString(""),
                    "soilHealthBenefit", rec.get("soilHealthBenefit").asString(""),
                    "score", rec.get("score").asInt(0)
            )));
        } catch (Exception e) {
            // Graph down / not seeded: degrade gracefully like the website did.
        }
        return out;
    }

    /** Seed the demo Sustainability Knowledge Graph (same data as the website seed route). */
    public void seed() {
        if (!enabled()) throw new IllegalStateException("NEO4J_URI not configured");
        String cypher = """
            MERGE (cotton:Crop {name: 'Cotton', category: 'Fiber'})
            MERGE (rice:Crop {name: 'Rice', category: 'Cereal'})
            MERGE (soybean:Crop {name: 'Soybean', category: 'Oilseed'})
            MERGE (bollworm:Pest {name: 'Pink Bollworm', type: 'Insect'})
            MERGE (blast:Pest {name: 'Rice Blast', type: 'Fungus'})
            MERGE (stemBorer:Pest {name: 'Stem Borer', type: 'Insect'})
            MERGE (neemOil:Treatment {name: 'Neem Oil Spray', type: 'Organic', isEcoFriendly: true,
                description: 'Natural pesticide that disrupts insect growth without harming beneficial insects.',
                soilHealthBenefit: 'Leaves zero toxic chemical residue in the soil, preserving earthworms and essential soil microbes.',
                sustainabilityScore: 95})
            MERGE (trichogramma:Treatment {name: 'Trichogramma Wasps', type: 'Biological Control', isEcoFriendly: true,
                description: 'Tiny wasps that parasitize the eggs of many moth and butterfly pests.',
                soilHealthBenefit: 'Completely natural biological control; prevents chemical accumulation in groundwater and soil layers.',
                sustainabilityScore: 100})
            MERGE (pseudomonas:Treatment {name: 'Pseudomonas fluorescens', type: 'Bio-Fungicide', isEcoFriendly: true,
                description: 'Beneficial bacteria that suppresses fungal pathogens.',
                soilHealthBenefit: 'Actively promotes root growth and naturally enriches the soil microbiome instead of destroying it like synthetic fungicides.',
                sustainabilityScore: 90})
            MERGE (cotton)-[:ATTACKED_BY]->(bollworm)
            MERGE (rice)-[:ATTACKED_BY]->(blast)
            MERGE (rice)-[:ATTACKED_BY]->(stemBorer)
            MERGE (soybean)-[:ATTACKED_BY]->(stemBorer)
            MERGE (bollworm)-[:TREATED_BY]->(trichogramma)
            MERGE (bollworm)-[:TREATED_BY]->(neemOil)
            MERGE (stemBorer)-[:TREATED_BY]->(neemOil)
            MERGE (blast)-[:TREATED_BY]->(pseudomonas)
            """;
        try (Session s = driver().session()) {
            s.run(cypher).consume();
        }
    }

    @Override
    public void close() {
        if (driver != null) driver.close();
    }
}
