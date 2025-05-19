package com.rits.toolgroupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachmentListResponse {
    private String sequence;
    private String quantityRequired;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String stepId;
    private String operation;
    private String workCenter;
    private String resource;
    private String resourceType;
    private String shopOrder;
}
