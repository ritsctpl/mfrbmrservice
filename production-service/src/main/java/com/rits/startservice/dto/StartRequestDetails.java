package com.rits.startservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StartRequestDetails {

    private String site;
    private String handle;
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
    private String status;
    private List<StartRequestDetails> inWorkList;
    private Boolean disable;
    private String qtyInQueue;
    private String type;
    private int recordLimit;

    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
