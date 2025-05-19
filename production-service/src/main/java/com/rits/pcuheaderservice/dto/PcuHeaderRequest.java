package com.rits.pcuheaderservice.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class PcuHeaderRequest {
    private String site;
    private String pcuBO;
    private String pcuBomBO;
    private String operationBO;
    private List<Pcu> pcuBos;
    private ShopOrder shopOrder;
    private String shopOrderBO;
    private String itemBO;
    private double qtyInQueue;
    private PcuRequest pcuRequest;
    private String parentOrderBO;
    private String parentPcuBO;
    private String userBO;
    private String routerBO;
    private Map<String,Integer> pcuNumberList;
    private String topic;
}
