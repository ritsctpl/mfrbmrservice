package com.rits.containermaintenanceservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PackLevelList {
    private String packLevel;
    private String packLevelValue;
    private String version;
    private String shopOrder;
    private int minimumQuantity;
    private int maximumQuantity;
}
