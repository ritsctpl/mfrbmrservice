package com.rits.activityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityHookEnabledResponse {
    private String sequence;
    private String hookPoint;
    private String activity;
    private String  userArgument;
    private String hookableMethod;
//    private boolean enable;



}
