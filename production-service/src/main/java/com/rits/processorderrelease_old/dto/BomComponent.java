package com.rits.processorderrelease_old.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomComponent {
    private String assySequence;
    private String component;
    private String componentVersion;
    private String componentType;
    private String componentDescription;
    private String assyOperation;
    private String assyQty;
    private String assemblyDataTypeBo;
    private String storageLocationBo;
    private String maxUsage;
    private String maxNc;
    private List<AlternateComponent> alternateComponentList;
    private List<ComponentCustomData> componentCustomDataList;
}