package com.rits.bomservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
