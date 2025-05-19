package com.rits.activityservice.dto;

import com.rits.activityservice.model.ActivityRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ActivityListResponse {
    private String activityId;
    private String description;
//    private List<ActivityRule> activityRules;
}
