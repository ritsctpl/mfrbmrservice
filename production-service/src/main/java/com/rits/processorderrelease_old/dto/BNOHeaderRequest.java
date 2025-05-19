package com.rits.processorderrelease_old.dto;


import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BNOHeaderRequest {
    private ProcessOrder processOrder;
    private List<Bno> bnoBos;
//    private int qtyInQueue;  //commented it for the lot size data type change
    private double qtyInQueue;
    private String parentOrderBO;
    private String parentBnoBO;
    private String userBO;
    private Map<String,Integer> bnoNumberList;
    private String topic;
}
