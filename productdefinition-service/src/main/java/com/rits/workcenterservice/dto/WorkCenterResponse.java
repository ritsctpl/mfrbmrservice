package com.rits.workcenterservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorkCenterResponse {
    private String workCenter;
    private String description;
    private String workCenterCategory;
}
