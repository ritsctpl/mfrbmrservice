package com.rits.pcudoneservice.dto;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Item {
    @Id
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
    private boolean inUse;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;


}
