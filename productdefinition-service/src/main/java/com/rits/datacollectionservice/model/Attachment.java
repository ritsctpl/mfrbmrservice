package com.rits.datacollectionservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Attachment {
    private String sequence;
    private String itemGroup;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String operation;
    private String operationVersion;
    private String workCenter;
    private String resource;
    private String shopOrder;
    private String pcu;
    private String batchNo;
    private String orderNo;
    private String phaseId;
    private String operationId;

}
