package com.cropwise.backend.dto;

import java.util.List;

/** Mirrors WeatherData + ExtendedWeatherData from the website. */
public class WeatherDtos {

    public record ForecastDay(
            String date, String dayName, int high, int low,
            String condition, String conditionCode, int precipitationChance) {}

    public record WeatherData(
            String location, int temperature, int feelsLike, int humidity,
            int windSpeed, String condition, String conditionCode,
            double precipitation, double uvIndex, int visibility,
            List<ForecastDay> forecast, String updatedAt,
            // extended soil/agri fields
            int soilMoisture, double soilMoistureRaw, double evapotranspiration,
            double solarRadiation, int soilTemp,
            List<Double> hourlyTemp, List<Double> hourlyRain, List<String> hourlyTime) {}
}
