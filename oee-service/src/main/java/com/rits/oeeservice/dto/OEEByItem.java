package com.rits.oeeservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OEEByItem {
    private String item;
    private double oee;

}
