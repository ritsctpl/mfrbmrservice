package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OEEByResource {
    private String resource;
    private double oee;
}
