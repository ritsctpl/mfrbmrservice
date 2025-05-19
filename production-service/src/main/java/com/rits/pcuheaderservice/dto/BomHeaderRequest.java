package com.rits.pcuheaderservice.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BomHeaderRequest {

    private List<Pcu> pcuBos;
//    private int qtyInQueue;    //commented it for the lot size data type change
    private double qtyInQueue;
    private Bom bom;
    private Map<String,Integer> pcuNumberList;
    private String pcuBomBO;
}
