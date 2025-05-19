package com.rits.processorderservice.dto;

import com.rits.processorderservice.model.BatchNumber;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessOrderResponse {
    private String orderNumber;
    private String material;
    private String materialVersion;
    private String materialDescription;
    private String availableQtyToRelease;
    private String orderType;
  //  private LocalDateTime plannedCompletion;
    private String status;
    private String uom;
    private String batchNumber;
    private String productionVersion;
    private LocalDateTime createdDateTime;
    private LocalDateTime orderReleasedTime;

}
