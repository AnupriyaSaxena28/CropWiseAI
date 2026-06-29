package com.cropwise.backend.service;

import com.cropwise.backend.dto.ActivityDtos.*;
import com.cropwise.backend.model.ActivityLog;
import com.cropwise.backend.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Farm ledger + Eco-Score engine. The scoring rules mirror the website's
 * activity-log calculation (organic inputs and manual labour reward the score,
 * synthetics penalise it).
 */
@Service
public class ActivityService {

    private final ActivityLogRepository repo;

    public ActivityService(ActivityLogRepository repo) { this.repo = repo; }

    public List<ActivityDto> list(String userId) {
        return repo.findByUserIdOrderByDateDesc(userId).stream().map(ActivityDto::from).toList();
    }

    public ActivityDto create(String userId, CreateActivityRequest r) {
        ActivityLog a = new ActivityLog();
        a.setUserId(userId);
        a.setType(r.type() == null ? "other" : r.type().toLowerCase());
        a.setTitle(r.title());
        a.setNotes(r.notes());
        a.setCost(r.cost());
        a.setDate(r.date() != null ? LocalDate.parse(r.date()) : LocalDate.now());
        return ActivityDto.from(repo.save(a));
    }

    public void delete(String userId, String id) {
        repo.findById(id).filter(a -> a.getUserId().equals(userId)).ifPresent(repo::delete);
    }

    public EcoSummary ecoSummary(String userId) {
        List<ActivityLog> rows = repo.findByUserIdOrderByDateDesc(userId);
        int eco = 85, carbon = 120;
        double spend = 0;
        for (ActivityLog a : rows) {
            if (a.getCost() != null) spend += a.getCost();
            String type = a.getType() == null ? "" : a.getType().toLowerCase();
            String notes = a.getNotes() == null ? "" : a.getNotes().toLowerCase();
            switch (type) {
                case "pesticide" -> {
                    if (notes.contains("organic") || notes.contains("neem") || notes.contains("bio")) { eco += 3; carbon += 10; }
                    else { eco -= 5; carbon -= 20; }
                }
                case "fertilizer" -> {
                    if (notes.contains("organic") || notes.contains("compost") || notes.contains("manure")) { eco += 3; carbon += 15; }
                    else { eco -= 3; carbon -= 15; }
                }
                case "sowing" -> { eco += 2; carbon += 30; }
                case "weeding" -> {
                    if (notes.contains("manual") || notes.contains("hand")) { eco += 2; carbon += 5; }
                    else { eco -= 1; carbon -= 10; }
                }
                case "irrigation" -> {
                    if (notes.contains("drip") || notes.contains("delay") || notes.contains("rain") || notes.contains("ai")) { eco += 2; carbon += 5; }
                }
                default -> { }
            }
        }
        long irrigationCount = rows.stream().filter(a -> "irrigation".equalsIgnoreCase(a.getType())).count();
        int water = (int) (4000 + irrigationCount * 500);
        return new EcoSummary(Math.min(100, Math.max(0, eco)), Math.max(0, carbon), water, spend);
    }
}
