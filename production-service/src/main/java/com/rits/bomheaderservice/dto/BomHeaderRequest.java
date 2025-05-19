package com.rits.bomheaderservice.dto;

import com.rits.bomheaderservice.model.Bom;
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
    private String site;
    private Bom bom;
//    private int qtyInQueue;    //commented it for the lot size data type change
    private double qtyInQueue;
    private String pcuBomBO;
    private String pcuBO;
    private String userId;
    private Map<String,Integer> pcuNumberList;
}
