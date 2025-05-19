package com.rits.buyoffservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttachmentList {
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
