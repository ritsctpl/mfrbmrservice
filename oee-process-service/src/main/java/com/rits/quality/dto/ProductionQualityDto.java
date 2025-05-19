package com.rits.quality.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductionQualityDto {
    private double goodQuantity;
    private double badQuantity;
    private double plan;
    private double qualityPercentage;
    private String item;
    private String operation;
    private String shiftId;
    private String resourceId;
    private String workcenterId;
    private String batchNumber;
}
