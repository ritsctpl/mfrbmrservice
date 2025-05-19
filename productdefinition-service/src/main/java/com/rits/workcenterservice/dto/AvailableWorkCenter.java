package com.rits.workcenterservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AvailableWorkCenter {
    private String workCenter;
    private String description;
}
