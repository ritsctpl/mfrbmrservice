package com.rits.nonconformanceservice.dto;
import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DispositionRequest {
    private String dispositionRoutingBo;
    private String site;
    private String pcuBO;
    private String userBo;
    private String dateTime;
    private String qty;
    private String resourceBo;
    private String operationBO;
    private String shoporderBO;
    private String stepID;
    private String routerBo;
    private String workCenterBo;
    private String itemBo;
    private String toOperationBo;
    private String active;
}
