package com.rits.activityhookservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityHookListResponse {
    private String activityHookId;
    private String description;
    private String hookType;
    private String hookPoint;
    private String hookClass;
    private String hookMethod;
    private String executionMode;
}
