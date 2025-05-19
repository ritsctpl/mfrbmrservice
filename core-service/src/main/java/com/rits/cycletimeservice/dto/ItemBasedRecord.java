package com.rits.cycletimeservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ItemBasedRecord {
    private String site;
    private String operation;
    private String operationVersion;
    private String resource;
    private String resourceType;
    private String workCenter;
    private Double cycleTime;
    private Double manufacturedTime;
    private Double targetQuantity;
    private String time;
}
