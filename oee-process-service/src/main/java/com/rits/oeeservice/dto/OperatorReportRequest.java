package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperatorReportRequest {

    private String site;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> shiftId;
    private List<String> batchNumber;
    private List<String> operation;
    private List<String> workCenter;
    private List<String> resource;
    private List<String> item;
    private List<String> shoporderId;
    private List<String> category;

    private List<String> queryTypes; // Add this field
}
