package com.cropwise.backend.dto;

import com.cropwise.backend.model.User;

/** Safe public view of a user (no password hash). */
public record UserDto(
        String id, String email, String name, String phone,
        String state, String district, String soilType,
        String preferredLanguage, Double landAreaAcres,
        String waterSource, String primaryCrop) {

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getName(), u.getPhone(),
                u.getState(), u.getDistrict(), u.getSoilType(),
                u.getPreferredLanguage(), u.getLandAreaAcres(),
                u.getWaterSource(), u.getPrimaryCrop());
    }
}
