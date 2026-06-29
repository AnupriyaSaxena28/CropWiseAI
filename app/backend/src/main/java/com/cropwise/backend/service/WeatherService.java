package com.cropwise.backend.service;

import com.cropwise.backend.dto.WeatherDtos.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

/** Real weather + soil data from Open-Meteo (free, no key). Ports lib/weather.ts. */
@Service
public class WeatherService {

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherService(WebClient.Builder builder) { this.web = builder.build(); }

    public record Coord(double lat, double lng, String label) {}

    public static final Map<String, Coord> REGION_COORDS = new LinkedHashMap<>();
    static {
        REGION_COORDS.put("Punjab", new Coord(30.9010, 75.8573, "Ludhiana, Punjab"));
        REGION_COORDS.put("Madhya Pradesh", new Coord(23.2599, 77.4126, "Bhopal, Madhya Pradesh"));
        REGION_COORDS.put("Maharashtra", new Coord(20.0112, 75.5791, "Aurangabad, Maharashtra"));
        REGION_COORDS.put("Uttar Pradesh", new Coord(26.8467, 80.9462, "Lucknow, Uttar Pradesh"));
        REGION_COORDS.put("Gujarat", new Coord(22.2587, 71.1924, "Rajkot, Gujarat"));
        REGION_COORDS.put("Rajasthan", new Coord(27.0238, 74.2179, "Ajmer, Rajasthan"));
        REGION_COORDS.put("Haryana", new Coord(29.0588, 76.0856, "Hisar, Haryana"));
        REGION_COORDS.put("Andhra Pradesh", new Coord(15.9129, 79.7400, "Guntur, Andhra Pradesh"));
        REGION_COORDS.put("Telangana", new Coord(17.3850, 78.4867, "Hyderabad, Telangana"));
        REGION_COORDS.put("Karnataka", new Coord(15.3173, 75.7139, "Dharwad, Karnataka"));
        REGION_COORDS.put("Tamil Nadu", new Coord(11.1271, 78.6569, "Coimbatore, Tamil Nadu"));
        REGION_COORDS.put("Bihar", new Coord(25.0961, 85.3131, "Patna, Bihar"));
        REGION_COORDS.put("West Bengal", new Coord(22.9868, 87.8550, "Barddhaman, West Bengal"));
        REGION_COORDS.put("Odisha", new Coord(20.9517, 85.0985, "Cuttack, Odisha"));
        REGION_COORDS.put("Chhattisgarh", new Coord(21.2787, 81.8661, "Raipur, Chhattisgarh"));
        REGION_COORDS.put("default", new Coord(23.2599, 77.4126, "Bhopal, Madhya Pradesh"));
    }

    private static final Map<Integer, String[]> WMO = new HashMap<>();
    static {
        WMO.put(0, new String[]{"Clear Sky","sunny"});      WMO.put(1, new String[]{"Mainly Clear","sunny"});
        WMO.put(2, new String[]{"Partly Cloudy","partly-cloudy"}); WMO.put(3, new String[]{"Overcast","overcast"});
        WMO.put(45, new String[]{"Foggy","overcast"});      WMO.put(48, new String[]{"Icy Fog","overcast"});
        WMO.put(51, new String[]{"Light Drizzle","drizzle"}); WMO.put(53, new String[]{"Drizzle","drizzle"});
        WMO.put(55, new String[]{"Heavy Drizzle","drizzle"}); WMO.put(61, new String[]{"Light Rain","rain"});
        WMO.put(63, new String[]{"Rain","rain"});           WMO.put(65, new String[]{"Heavy Rain","rain"});
        WMO.put(71, new String[]{"Light Snow","overcast"}); WMO.put(80, new String[]{"Rain Showers","rain"});
        WMO.put(95, new String[]{"Thunderstorm","rain"});
    }
    private static String[] wmo(int c) { return WMO.getOrDefault(c, new String[]{"Partly Cloudy","partly-cloudy"}); }

    public Coord resolve(String state, Double lat, Double lng, String label) {
        if (lat != null && lng != null && lat != 0 && lng != 0) {
            return new Coord(lat, lng, (label != null && !label.isBlank()) ? label : state);
        }
        return REGION_COORDS.getOrDefault(state, REGION_COORDS.get("default"));
    }

    public WeatherData fetch(Coord c) throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + c.lat() + "&longitude=" + c.lng()
            + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,"
            + "wind_speed_10m,visibility,uv_index,surface_pressure,soil_temperature_0cm,soil_moisture_0_to_1cm"
            + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,"
            + "precipitation_sum,et0_fao_evapotranspiration,shortwave_radiation_sum"
            + "&hourly=temperature_2m,precipitation_probability,soil_moisture_0_to_1cm"
            + "&timezone=Asia%2FKolkata&forecast_days=7";

        String raw = web.get().uri(url).retrieve().bodyToMono(String.class).block();
        JsonNode d = mapper.readTree(raw);
        JsonNode cur = d.get("current"), daily = d.get("daily"), hourly = d.get("hourly");

        String[] now = wmo(cur.path("weather_code").asInt());
        List<ForecastDay> forecast = new ArrayList<>();
        JsonNode times = daily.get("time");
        for (int i = 0; i < Math.min(7, times.size()); i++) {
            String[] w = wmo(daily.get("weather_code").get(i).asInt());
            LocalDate ld = LocalDate.parse(times.get(i).asText());
            String dayName = i == 0 ? "Today" : ld.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            forecast.add(new ForecastDay(times.get(i).asText(), dayName,
                    daily.get("temperature_2m_max").get(i).asInt(),
                    daily.get("temperature_2m_min").get(i).asInt(),
                    w[0], w[1], daily.get("precipitation_probability_max").get(i).asInt(0)));
        }

        double soilRaw = cur.path("soil_moisture_0_to_1cm").asDouble(0.3);
        int soilMoisture = (int) Math.min(100, Math.round((soilRaw / 0.5) * 100));
        double et0 = Math.round((daily.get("et0_fao_evapotranspiration").get(0).asDouble(4.5)) * 10) / 10.0;
        double solar = Math.round((daily.get("shortwave_radiation_sum").get(0).asDouble(18) / 3.6) * 10) / 10.0;
        int soilTemp = (int) Math.round(cur.path("soil_temperature_0cm").asDouble(cur.path("temperature_2m").asDouble() - 2));

        int hourNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).getHour();
        List<Double> hTemp = slice(hourly.get("temperature_2m"), hourNow, 24);
        List<Double> hRain = slice(hourly.get("precipitation_probability"), hourNow, 24);
        List<String> hTime = new ArrayList<>();
        JsonNode ht = hourly.get("time");
        for (int i = hourNow; i < Math.min(hourNow + 24, ht.size()); i++) {
            hTime.add(LocalDateTime.parse(ht.get(i).asText()).toLocalTime().toString());
        }

        return new WeatherData(
            c.label(), cur.get("temperature_2m").asInt(), cur.get("apparent_temperature").asInt(),
            cur.get("relative_humidity_2m").asInt(), cur.get("wind_speed_10m").asInt(),
            now[0], now[1], cur.get("precipitation").asDouble(), cur.get("uv_index").asDouble(),
            (int) Math.round(cur.get("visibility").asDouble() / 1000), forecast, Instant.now().toString(),
            soilMoisture, soilRaw, et0, solar, soilTemp, hTemp, hRain, hTime);
    }

    private List<Double> slice(JsonNode arr, int from, int count) {
        List<Double> out = new ArrayList<>();
        for (int i = from; i < Math.min(from + count, arr.size()); i++) out.add(arr.get(i).asDouble());
        return out;
    }
}
