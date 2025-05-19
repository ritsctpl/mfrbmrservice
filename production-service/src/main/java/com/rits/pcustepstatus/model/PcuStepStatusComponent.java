package com.rits.pcustepstatus.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuStepStatusComponent {
    private String bomType;
    private String bom;
    private String bomVersion;
    private String assemblyOperation;
    private String componentPcuInventoryId;
    private String vendor;
    private String vendorLot;
    private String vendorDateCode;
    private String externalSerialnumber;
}
