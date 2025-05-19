package com.rits.processorderrelease_old.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AlternateComponent {
    private String sequence;
    private String alternateComponent;
    private String alternateComponentVersion;
    private String parentMaterial;
    private String parentMaterialVersion;
    private String validFromDateTime;
    private String validToDateTime;

}




