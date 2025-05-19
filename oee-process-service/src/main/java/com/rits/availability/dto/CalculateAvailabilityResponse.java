package com.rits.availability.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalculateAvailabilityResponse {

    private String message;
    private boolean success;
    private double availabilityPercentage;
}
