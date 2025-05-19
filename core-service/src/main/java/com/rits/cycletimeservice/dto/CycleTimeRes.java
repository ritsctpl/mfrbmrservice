package com.rits.cycletimeservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CycleTimeRes {
    private String operation;
    private String operationVersion;
    private String resource;
    private String itemId;
    private String item;
    private String itemVersion;
    private String material;
    private String materialVersion;
    private String workCenter;
    private double cycleTime;
    private double manufacturedTime;
}
