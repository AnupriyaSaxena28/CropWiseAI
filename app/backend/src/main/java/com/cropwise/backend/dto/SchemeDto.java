package com.cropwise.backend.dto;

import java.util.List;

/** Mirrors GovernmentScheme from the website. */
public record SchemeDto(
        String id, String name, String ministry, String description,
        List<String> benefits, List<String> eligibility,
        String applicationUrl, String deadline, boolean isActive) {}
