package com.rits.signoffservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWork {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
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
    private String qtyInWork;
    private String qtyToComplete;
    private String shopOrder;
    private String childRouter;
    private String childRouterVersion;
    private String parentStepID;
    private int active;
}

