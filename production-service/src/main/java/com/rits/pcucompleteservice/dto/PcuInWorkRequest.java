package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWorkRequest {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcuBO;
    private String itemBO;
    private String routerBO;
    private String resourceBO;
    private String operationBO;
    private String stepID;
    private String userBO;
    private String qtyToComplete;
    private String  qtyInWork;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private String workCenter;
}
