package com.rits.revisionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AlternateComponent {
    private int sequence;
    private String alternateComponent;
    private String alternateComponentVersion;
    private String parentMaterial;
    private String parentMaterialVersion;
    private LocalDateTime validFromDateTime;
    private LocalDateTime validToDateTime;

}




