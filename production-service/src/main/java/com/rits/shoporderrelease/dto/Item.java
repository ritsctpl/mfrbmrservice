package com.rits.shoporderrelease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    private String status;
    private double lotSize;
    private String site;
    private String revision;
    private String item;
    private String routing;
    private String routingVersion;
    private String bom;
    private String bomVersion;
    private String procurementType;
    private boolean inUse;
    private String handle;
    private String description;
    private String itemGroup;
    private boolean currentVersion;
    private String itemType;
    private String assemblyDataType;
    private String removalDataType;
    private String receiptDataType;
    private List<PrintDocument> printDocuments;
    private List<CustomData> customDataList;
    private List<AlternateComponent> alternateComponentList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
