package com.rits.pcustepstatus.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PcuStepStatusDetails {
    private Integer qtyInQueue;
    private Integer qtyInWork;
    private Integer qtyCompletePending;
    private String routingBO;
    private String routingDescription;
    private String pcu;
    private String pcuStepStatus;
    private String shopOrder;
    private String itemBO;
    private String resourceBO;
    private String user;
    private String holdId;
}
