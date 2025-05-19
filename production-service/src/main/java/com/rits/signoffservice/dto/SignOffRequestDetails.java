package com.rits.signoffservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SignOffRequestDetails {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String resource;
    private String operation;
    private String operationVersion;
    private String stepID;
    private String user;
    private String quantity;
    private String qtyToComplete;
    private String workCenter;
    private String qtyInQueue;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private String status;
    private LocalDateTime createdDateTime;
}
