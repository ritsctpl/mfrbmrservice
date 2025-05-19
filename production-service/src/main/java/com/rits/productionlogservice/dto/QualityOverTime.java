package com.rits.productionlogservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityOverTime {
    private double qualityPercentage;
    private double goodQty;
    private double badQty;
    private LocalDateTime dateValue;
    private String shiftValue;
    private String resourceValue;
    private String itemValue;
    private String reasonValue;
    private String workcenterValue;
    private Long scrapValue;
    private Long reworkValue;
    private Long occurance;

}
