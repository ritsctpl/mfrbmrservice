package com.rits.pcucompleteservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuDoneDetails {
    private String site;
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String user;
    private String qtyDone;
    private String shopOrder;
    private String workCenter;
    private LocalDateTime dateTime;
    private String operation;
    private String operationVersion;
    private String resource;
}
