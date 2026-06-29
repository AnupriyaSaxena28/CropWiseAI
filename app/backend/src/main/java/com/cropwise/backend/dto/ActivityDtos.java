package com.cropwise.backend.dto;

import com.cropwise.backend.model.ActivityLog;

public class ActivityDtos {
    public record CreateActivityRequest(String type, String title, String notes,
                                        Double cost, String date) {}

    public record ActivityDto(String id, String type, String title, String notes,
                              Double cost, String date, String createdAt) {
        public static ActivityDto from(ActivityLog a) {
            return new ActivityDto(a.getId(), a.getType(), a.getTitle(), a.getNotes(),
                    a.getCost(), a.getDate().toString(), a.getCreatedAt().toString());
        }
    }

    /** Eco-score / carbon / water dashboard summary. */
    public record EcoSummary(int ecoScore, int carbonSaved, int waterSaved, double monthlySpend) {}
}
