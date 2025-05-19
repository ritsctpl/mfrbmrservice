package com.rits.stepstatusservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchStepStatusRequest {
    private String site;
    private String batchNo;
    private String orderNumber;
}
