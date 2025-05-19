package com.rits.logbuyoffservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AttachmentDetailsRequest {
    private String key;
    private String site;
    private String value;
    private String sequence;
    private String quantityRequired;
    private String item;
    private String itemVersion;
    private String stepId;
    private String operation;
    private String operationVersion;
    private String workCenter;
    private String resource;
    private String resourceType;
    private String shopOrder;
    private String pcu;
    private String routing;
    private String routingVersion;


    private String batchNo;
    private String orderNumber;
    private String recipe;
    private String recipeVersion;
}
