package com.rits.shoporderrelease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ActivityRequest {
    private String site;
    private String activityId;
    private List<ActivityRule> activityRules;
    private String imageUrl;
    private String userId;

    public ActivityRequest(String site, String activityId) {
        this.site=site;
        this.activityId=activityId;
    }
}
