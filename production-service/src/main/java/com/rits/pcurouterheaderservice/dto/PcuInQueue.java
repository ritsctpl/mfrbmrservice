package com.rits.pcurouterheaderservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInQueue{
    private String site;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String operationBO;
    private String resourceBO;
    private String stepID;
    private String userBO;
    private String qtyToComplete;
    private String qtyInQueue;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
}

