package com.rits.dhrservice.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class BomRequest {
    private String site;
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
    private String operation;
    private List<BomComponent> bomComponentList;
    private String userId;
}
