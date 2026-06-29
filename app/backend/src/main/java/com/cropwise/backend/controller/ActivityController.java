package com.cropwise.backend.controller;

import com.cropwise.backend.dto.ActivityDtos.*;
import com.cropwise.backend.security.CurrentUser;
import com.cropwise.backend.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activities;
    public ActivityController(ActivityService activities) { this.activities = activities; }

    @GetMapping
    public List<ActivityDto> list() { return activities.list(CurrentUser.id()); }

    @PostMapping
    public ActivityDto create(@RequestBody CreateActivityRequest req) { return activities.create(CurrentUser.id(), req); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { activities.delete(CurrentUser.id(), id); }

    @GetMapping("/eco-summary")
    public EcoSummary ecoSummary() { return activities.ecoSummary(CurrentUser.id()); }
}
