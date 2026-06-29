package com.cropwise.backend.service;

import com.cropwise.backend.dto.ProfileUpdateRequest;
import com.cropwise.backend.dto.UserDto;
import com.cropwise.backend.model.User;
import com.cropwise.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final UserRepository users;

    public ProfileService(UserRepository users) { this.users = users; }

    public UserDto get(String userId) {
        return UserDto.from(load(userId));
    }

    public UserDto update(String userId, ProfileUpdateRequest r) {
        User u = load(userId);
        if (r.name() != null) u.setName(r.name());
        if (r.phone() != null) u.setPhone(r.phone());
        if (r.state() != null) u.setState(r.state());
        if (r.district() != null) u.setDistrict(r.district());
        if (r.soilType() != null) u.setSoilType(r.soilType());
        if (r.preferredLanguage() != null) u.setPreferredLanguage(r.preferredLanguage());
        if (r.landAreaAcres() != null) u.setLandAreaAcres(r.landAreaAcres());
        if (r.waterSource() != null) u.setWaterSource(r.waterSource());
        if (r.primaryCrop() != null) u.setPrimaryCrop(r.primaryCrop());
        return UserDto.from(users.save(u));
    }

    private User load(String userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
