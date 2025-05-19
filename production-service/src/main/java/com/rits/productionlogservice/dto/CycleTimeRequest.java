package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CycleTimeRequest {
    /*private String shopOrder;*//*
    private String routing;
    private String routingVersion;*/
    private String site;
    private String operation;
    private String operationVersion;
    private String material;
    private String materialVersion;
    private String itemId;
    private String itemVersion;
    private String resource;
    private String workCenter;
    private Double cycleTime;
    private Double manufacturedTime;
    private String handle;
    private String createdBy;
    private String modifiedBy;
    private String userId;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private Integer active;
    private String pcu;
    private String shiftId;
    /*
    private double wholeCycletime;
    private double wholeManufacturedTime;*/
}
