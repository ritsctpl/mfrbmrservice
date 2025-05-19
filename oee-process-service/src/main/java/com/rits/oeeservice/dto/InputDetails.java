package com.rits.oeeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputDetails {
    private String site;
    private String shiftId;
    private String workcenterId;
    private String resourceId;
    private String operationBo;
    private String routingBo;
    private String itemBo;
    private String shoporderId;
}
