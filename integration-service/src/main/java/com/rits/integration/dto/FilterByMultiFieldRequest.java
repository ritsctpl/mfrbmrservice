package com.rits.integration.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter

@Setter

@Data

@AllArgsConstructor

@NoArgsConstructor

public class FilterByMultiFieldRequest {

    private String site;

    private String identifier;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer hours;
    private Integer minutes;
    private Integer seconds;

}
