package com.rits.pcuheaderservice.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcuROuterHeaderCreateRequest {
    private String site;
    private List<Pcu> pcuBos;
    private String pcuRouterBo;
    private String shopOrderBo;
    private String qtyInQueue;
    private String userBO;
    private Map<String,Integer> pcuNumberList;
}
