package com.rits.startservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInQueueRequest {
    private String site;
    private String handle;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String stepID;
    private String userBO;
    private String qtyInQueue;
    private String qtyToComplete;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private String workCenter;

}
