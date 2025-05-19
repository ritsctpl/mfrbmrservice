package com.rits.startservice.dto;

import lombok.*;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWorkDetails {
    private String site;
    private String handle;
    private String pcu;
    private String item;
    private String router;
    private String operation;
    private String resource;
    private String stepID;
    private String user;
    private String workCenter;
    private String qtyInWork;
    private String qtyToComplete;
    private String shopOrder;
    private String childRouter;
    private String parentStepID;
    private String quantity;
    private String status;

    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}

