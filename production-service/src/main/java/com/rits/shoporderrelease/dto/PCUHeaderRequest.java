package com.rits.shoporderrelease.dto;


import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PCUHeaderRequest  {
    private ShopOrder shopOrder;
    private List<Pcu> pcuBos;
//    private int qtyInQueue;  //commented it for the lot size data type change
    private double qtyInQueue;
    private String parentOrderBO;
    private String parentPcuBO;
    private String userBO;
    private Map<String,Integer> pcuNumberList;
    private String topic;
}
