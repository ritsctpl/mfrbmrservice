package com.rits.pcucompleteservice.dto;

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
    private String qtyInQueue;
    private String qtyToComplete;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private String workCenter;
}
