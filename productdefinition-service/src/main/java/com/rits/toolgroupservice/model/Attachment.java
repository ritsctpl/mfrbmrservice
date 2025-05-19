package com.rits.toolgroupservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String quantityRequired;
    private String stepId;
    private String resourceType;

}
