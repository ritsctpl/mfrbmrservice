package com.rits.availability.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphOeeAvailabilityDTO {
    private String resourceId;
    private Double avgActualAvailableTime;
    private Double avgAvailabilityPercentage;

}
