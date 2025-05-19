package com.rits.assemblyservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    private String handle;
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
    private List<AlternateComponent> alternateComponentList;
    private boolean inUse;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;

}
