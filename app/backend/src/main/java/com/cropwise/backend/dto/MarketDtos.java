package com.cropwise.backend.dto;

/** Mirrors MarketPrice + MarketHistoricalPoint from the website. */
public class MarketDtos {

    public record MarketPrice(
            String cropName, String cropNameHi, int msp, int currentPrice,
            int priceChange, double priceChangePercent, String unit,
            String market, String lastUpdated) {}

    public record HistoricalPoint(String date, int price, int msp) {}
}
