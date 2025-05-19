package com.rits.extensionservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ActivityRequest {
    private String activityId;
    private String site;
}
