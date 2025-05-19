package com.rits.activityservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityHook {
    private String sequence;
    private String hookPoint;
    private String activity;
    private String hookableMethod;
    private boolean enable;
    private String userArgument;

}

