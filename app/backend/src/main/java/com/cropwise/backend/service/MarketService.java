package com.cropwise.backend.service;

import com.cropwise.backend.dto.MarketDtos.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/** Live mandi prices from data.gov.in Agmarknet + synthetic history. Ports lib/market-prices.ts. */
@Service
public class MarketService {

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String resourceId;

    public MarketService(WebClient.Builder builder,
                         @Value("${cropwise.market.api-key}") String apiKey,
                         @Value("${cropwise.market.resource-id}") String resourceId) {
        this.web = builder.build();
        this.apiKey = apiKey;
        this.resourceId = resourceId;
    }

    private record CropMeta(String display, String hi, int msp, List<String> match, int priority) {}

    private static final List<CropMeta> CROP_META = List.of(
        new CropMeta("Wheat","गेहूं",2275,List.of("wheat"),1),
        new CropMeta("Rice (Paddy)","धान",2300,List.of("paddy","dhan","rice"),2),
        new CropMeta("Maize","मक्का",2090,List.of("maize"),3),
        new CropMeta("Bajra","बाजरा",2625,List.of("bajra","pearl millet"),4),
        new CropMeta("Jowar","ज्वार",3371,List.of("jowar","sorghum"),5),
        new CropMeta("Barley","जौ",1850,List.of("barley"),6),
        new CropMeta("Gram (Chana)","चना",5440,List.of("bengal gram","gram","chana"),7),
        new CropMeta("Tur (Arhar)","अरहर",7550,List.of("arhar","tur","red gram"),8),
        new CropMeta("Moong","मूंग",8682,List.of("moong","green gram"),9),
        new CropMeta("Urad","उड़द",7400,List.of("urad","black gram"),10),
        new CropMeta("Lentil (Masur)","मसूर",6700,List.of("masur","lentil"),11),
        new CropMeta("Soybean","सोयाबीन",4892,List.of("soyabean","soybean"),12),
        new CropMeta("Groundnut","मूंगफली",6783,List.of("groundnut"),13),
        new CropMeta("Mustard","सरसों",5950,List.of("mustard","rapeseed","sarson"),14),
        new CropMeta("Sunflower","सूरजमुखी",7280,List.of("sunflower"),15),
        new CropMeta("Cotton","कपास",7121,List.of("cotton","kapas"),16),
        new CropMeta("Sugarcane","गन्ना",0,List.of("sugarcane"),17)
    );

    private CropMeta matchCrop(String commodity) {
        String c = commodity.toLowerCase();
        for (CropMeta m : CROP_META) if (m.match().stream().anyMatch(c::contains)) return m;
        return null;
    }

    public List<MarketPrice> currentPrices(String state, String district) {
        try {
            StringBuilder url = new StringBuilder("https://api.data.gov.in/resource/" + resourceId
                    + "?api-key=" + apiKey + "&format=json&limit=1000");
            if (state != null) url.append("&filters[state.keyword]=").append(enc(state));
            if (district != null) url.append("&filters[district]=").append(enc(district));

            String raw = web.get().uri(url.toString()).retrieve().bodyToMono(String.class).block();
            JsonNode records = mapper.readTree(raw).path("records");

            // strict scoping like the website
            List<JsonNode> scoped = new ArrayList<>();
            for (JsonNode r : records) {
                if (state != null && !state.equalsIgnoreCase(r.path("state").asText(""))) continue;
                if (district != null && !district.equalsIgnoreCase(r.path("district").asText(""))) continue;
                scoped.add(r);
            }
            return group(scoped);
        } catch (Exception e) {
            return fallback(state, district);
        }
    }

    private List<MarketPrice> group(List<JsonNode> records) {
        String now = Instant.now().toString();
        Map<String, double[]> sums = new LinkedHashMap<>();   // key -> [sum, n]
        Map<String, String> markets = new HashMap<>();
        Map<String, String> apiNames = new HashMap<>();

        for (JsonNode r : records) {
            double modal = num(r.path("modal_price").asText(""));
            if (modal == 0) modal = (num(r.path("min_price").asText("")) + num(r.path("max_price").asText(""))) / 2;
            if (modal == 0) continue;
            CropMeta meta = matchCrop(r.path("commodity").asText(""));
            String key = meta != null ? meta.display() : r.path("commodity").asText("");
            double[] b = sums.computeIfAbsent(key, k -> new double[2]);
            b[0] += modal; b[1] += 1;
            markets.putIfAbsent(key, r.path("market").asText("") + ", " + r.path("district").asText(""));
            apiNames.putIfAbsent(key, r.path("commodity").asText(""));
        }

        List<MarketPrice> rows = new ArrayList<>();
        List<int[]> order = new ArrayList<>();
        int idx = 0;
        for (var e : sums.entrySet()) {
            CropMeta meta = CROP_META.stream().filter(m -> m.display().equals(e.getKey())).findFirst().orElse(null);
            int modal = (int) Math.round(e.getValue()[0] / e.getValue()[1]);
            int msp = meta != null ? meta.msp() : 0;
            rows.add(new MarketPrice(e.getKey(), meta != null ? meta.hi() : null, msp, modal,
                    msp != 0 ? modal - msp : 0,
                    msp != 0 ? Math.round(((modal - msp) / (double) msp) * 10000) / 100.0 : 0,
                    "quintal", markets.get(e.getKey()), now));
            order.add(new int[]{idx++, meta != null ? meta.priority() : 999});
        }
        order.sort(Comparator.comparingInt(a -> a[1]));
        List<MarketPrice> sorted = new ArrayList<>();
        for (int[] o : order) sorted.add(rows.get(o[0]));
        return sorted.size() > 18 ? sorted.subList(0, 18) : sorted;
    }

    private List<MarketPrice> fallback(String state, String district) {
        String now = Instant.now().toString();
        String mandi = district != null ? district : (state != null ? state : "Ludhiana");
        return CROP_META.stream().filter(m -> m.msp() > 0).limit(8).map(m -> {
            int offset = (m.priority() * 17) % 250 - 60;
            int price = m.msp() + offset;
            return new MarketPrice(m.display(), m.hi(), m.msp(), price, offset,
                    Math.round((offset / (double) m.msp()) * 10000) / 100.0,
                    "quintal", mandi + " Mandi", now);
        }).toList();
    }

    public List<HistoricalPoint> historical(String cropName) {
        CropMeta meta = CROP_META.stream().filter(m -> m.display().equals(cropName)).findFirst().orElse(null);
        int msp = meta != null ? meta.msp() : 2000;
        int latest = msp;
        try {
            String match = meta != null ? meta.match().get(0) : cropName;
            String url = "https://api.data.gov.in/resource/" + resourceId + "?api-key=" + apiKey
                    + "&format=json&limit=200&filters[commodity]=" + enc(match);
            JsonNode records = mapper.readTree(web.get().uri(url).retrieve().bodyToMono(String.class).block())
                    .path("records");
            long n = 0; double sum = 0;
            for (JsonNode r : records) {
                double v = num(r.path("modal_price").asText(""));
                if (v > 0) { sum += v; n++; }
            }
            if (n > 0) latest = (int) Math.round(sum / n);
        } catch (Exception ignored) {}
        return buildTrend(latest, msp);
    }

    private List<HistoricalPoint> buildTrend(int latest, int msp) {
        List<HistoricalPoint> pts = new ArrayList<>();
        int start = (int) Math.round(latest * 0.94);
        for (int i = 17; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i * 10L);
            double t = (17 - i) / 17.0;
            double wave = Math.sin(t * Math.PI * 2.5) * latest * 0.03;
            int price = i == 0 ? latest : (int) Math.round(start + (latest - start) * t + wave);
            pts.add(new HistoricalPoint(d.toString(), price, msp));
        }
        return pts;
    }

    private static double num(String v) { try { return Double.parseDouble(v); } catch (Exception e) { return 0; } }
    private static String enc(String s) { return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8); }
}
