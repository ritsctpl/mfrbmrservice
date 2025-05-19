package com.rits.resourceservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ActivityHookRequest {
    private String site;
    private String activityId;
}
