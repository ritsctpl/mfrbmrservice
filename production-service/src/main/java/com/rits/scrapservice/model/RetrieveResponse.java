package com.rits.scrapservice.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RetrieveResponse {
    private String pcu;
    private String item;
    private String itemVersion;
    private String router;
    private String routerVersion;
    private String operation;
    private String operationVersion;
    private String resource;
    private String processLot;
    private String shopOrder;
    private String bom;
    private String bomVersion;
    private String status;

}
