package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DownTimeByResourcePerDay {
    private String resource;
    private double downTime;
    private LocalDate date;

}
