package com.rits.revisionservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ItemRequest {
    private String site;
    private String item;
    private String revision;
    private String description;
    private String itemGroup;
    private String status;
    private String procurementType;
    private boolean currentVersion;
    private String itemType;
    private String lotSize;
    private String routing;
    private String routingVersion;
    private String bom;
    private String bomVersion;
    private String assemblyDataType;
    private String removalDataType;
    private String receiptDataType;
    private String printDocument;
    private List<CustomData> customDataList;
    private List<AlternateComponent> alternateComponentList;
}

