package com.rits.signoffservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StartRequest {
    private String site;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String operation;
    private String operationVersion;
    private String qtyInWork;
    private String resource;
    private String workCenter;
    private String quantity;
    private String qtyToComplete;
    private String stepID;
    private String user;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private String stepId;
}
