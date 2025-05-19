package com.rits.dhrservice.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Bom {
    private String site;
    private String handle;
    private String bom;
    private String revision;
    private String description;
    private String status;
    private String bomType;
    private boolean currentVersion;
    private String validFrom;
    private String validTo;
    private boolean bomTemplate;
    private boolean isUsed;
    private String designCost;
    private List<BomComponent> bomComponentList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}
