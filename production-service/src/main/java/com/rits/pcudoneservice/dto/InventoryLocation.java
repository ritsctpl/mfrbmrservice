package com.rits.pcudoneservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryLocation {
    private int qtyonHand;
    private String storageLocation ;
    private String  productionSupplyArea;
    private String  storageBin;
    private String handlingUnitNumber ;
    private String masterHandlingUnitNumber ;
    private String  workCenter;
    private String workCenterVersion ;
    private boolean  workCenterReserve;
    private String  operation;
    private String  operationVersion;
    private boolean  operationReserve;
    private String  resource;
    private String resourceVersion ;
    private boolean  resourceReserve;
    private String  shopOrder;
    private String  shopOrderVersion;
    private boolean shopOrderReserve ;

}
