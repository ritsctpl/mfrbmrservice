package com.rits.activityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityHookListRequest {
    private String sequence;
    private String activityId;
    private String hookPoint;
    private String activity;
    private String hookableMethod;
    private boolean enable;
    private String userArgument;
    //private String site;
    private String currentSite;
}