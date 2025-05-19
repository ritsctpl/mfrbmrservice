package com.rits.documentservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DocumentOption {
    private boolean bomAssemblyMetrics;
    private boolean bomComponentData;
    private boolean bomHeaderData;
    private boolean containerAssemblyMetrics;
    private boolean containerCustomData;
    private boolean containerHeaderData;
    private boolean documentCustomData;
    private boolean floorStockHeaderData;
    private boolean floorStockReceiptData;
    private boolean materialCustomData;
    private boolean ncCodeCustomData;
    private boolean ncData;
    private boolean operationCustomData;
    private boolean parametricData;
    private boolean routingData;
    private boolean pcuData;
    private boolean pcuHeader;
    private boolean pcuPackData;
    private boolean shopOrderCustomData;
    private boolean shopOrderHeaderData;
    private boolean workInstructionData;


}
