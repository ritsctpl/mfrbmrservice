package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AvailabilityByResource {
    private double availability;
    private String resource;
    private LocalDate createdDate;
}
