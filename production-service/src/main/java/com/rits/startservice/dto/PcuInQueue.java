package com.rits.startservice.dto;

import lombok.*;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInQueue {
    private String site;
    private String handle;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String operation;
    private String operationVersion;
    private String resource;
    private String stepID;
    private String user;
    private String workCenter;
    private String qtyInQueue;
    private String qtyToComplete;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}