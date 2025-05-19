package com.rits.pcudoneservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BomComponent {
    private int assySequence;
    private String component;
    private String componentVersion;
    private String componentType;
    private String componentDescription;
    private String assyOperation;
    private String assembledQty;
    private String assyQty;
    private String assemblyDataTypeBo;
    private String storageLocationBo;
    private int maxUsage;
    private int maxNc;


}
