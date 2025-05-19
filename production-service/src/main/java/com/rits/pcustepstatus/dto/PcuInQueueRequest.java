package com.rits.pcustepstatus.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInQueueRequest {
    private String site;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String resourceBO;
    private String operationBO;
    private String stepID;
    private String userBO;
    private String qtyToComplete;
    private String qtyInQueue;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
}
