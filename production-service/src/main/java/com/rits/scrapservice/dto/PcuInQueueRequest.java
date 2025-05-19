package com.rits.scrapservice.dto;

import com.rits.pcuinqueueservice.model.PcuInQueue;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PcuInQueueRequest {
    private String handle;
    private String site;
    private LocalDateTime dateTime;
    private String pcu;
    private String itemBO;
    private String routerBO;
    private String resourceBO;
    private String operationBO;
    private String stepID;
    private String userBO;
    private String qtyToComplete;
    private String qtyInQueue;
    private String shopOrderBO;
    private String childRouterBO;
    private String parentStepID;
    private String workCenter;
    private Boolean disable;
    private int active;
    private List<PcuInQueue> pcuList;
}
