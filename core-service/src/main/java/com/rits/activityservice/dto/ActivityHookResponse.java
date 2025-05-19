package com.rits.activityservice.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityHookResponse {
    private String sequence;
    private String hookPoint;
    private String activity;
    private String hookableMethod;

}






