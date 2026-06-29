package com.cropwise.backend.controller;

import com.cropwise.backend.dto.ProfileUpdateRequest;
import com.cropwise.backend.dto.UserDto;
import com.cropwise.backend.security.CurrentUser;
import com.cropwise.backend.service.ProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profile;
    public ProfileController(ProfileService profile) { this.profile = profile; }

    @GetMapping
    public UserDto me() { return profile.get(CurrentUser.id()); }

    @PutMapping
    public UserDto update(@RequestBody ProfileUpdateRequest req) { return profile.update(CurrentUser.id(), req); }
}
