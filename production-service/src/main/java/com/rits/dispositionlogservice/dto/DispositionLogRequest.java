package com.rits.dispositionlogservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DispositionLogRequest {
    private String site;
    private String dispositionRoutingBo;
    private String pcuBO;
    private String userBo;
    private String dateTime;
    private String qty;
    private String refDes;
    private String resourceBo;
    private String operationBO;
    private String stepID;
    private String routerBo;
    private String workCenterBo;
    private String itemBo;
    private String toOperationBo;
    private String shopOrderBo;
    private String toRoutingBo;
    private String active;
}
