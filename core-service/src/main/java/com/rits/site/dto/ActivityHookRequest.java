package com.rits.site.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ActivityHookRequest {
    private String site;
    private String activityId;
}
