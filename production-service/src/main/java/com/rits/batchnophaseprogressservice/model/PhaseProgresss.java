package com.rits.batchnophaseprogressservice.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseProgresss {

    private String phase;
    private String operation;
    private int startQuantityBaseUom;
    private int startQuantityMeasuredUom;
    private int completeQuantityBaseUom;
    private int completeQuantityMeasuredUom;
    private int scrapQuantityBaseUom;
    private int scrapQuantityMeasuredUom;
    private String baseUom;
    private String measuredUom;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private String status;
}
