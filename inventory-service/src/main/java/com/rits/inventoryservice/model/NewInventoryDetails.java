package com.rits.inventoryservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewInventoryDetails {
    private String inventoryId;
    private int qty;
    private int receiveQty;
    private int inHandQty;
    private String status;
    private String storage;
    private String workCenter;
    private String operation;
    private String resource;
    private String shopOrder;
    private boolean shopOrderChk;
    private boolean resourceChk;
    private boolean operationChk;
    private boolean workCenterChk;
    private boolean detailReasonId;

}
