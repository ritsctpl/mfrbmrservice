package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DowntimeForResource {
    private String resourceId;
    private Double breakTime;
    private Double downtime;
    private Double productionTime;
}
