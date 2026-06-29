package com.cropwise.backend.dto;

public record ProfileUpdateRequest(
        String name, String phone, String state, String district,
        String soilType, String preferredLanguage, Double landAreaAcres,
        String waterSource, String primaryCrop) {}
