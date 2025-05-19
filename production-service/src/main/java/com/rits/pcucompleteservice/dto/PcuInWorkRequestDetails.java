package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInWorkRequestDetails {
    private String site;
    private String handle;
    private LocalDateTime dateTime;
    private String pcu;
    private String item;
    private String router;
    private String resource;
    private String operation;
    private String stepID;
    private String user;
    private String qtyToComplete;
    private String qtyInWork;
    private String shopOrder;
    private String childRouter;
    private String parentStepID;
    private String workCenter;
}
