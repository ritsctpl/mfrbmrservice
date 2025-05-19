package com.rits.mfrrecipesservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextNumberRequest {

    private String numberType;
    private String site;
    private String object;
    private String objectVersion;
    private String shopOrder;
    private String pcu;
    private String ncBo;
    private String userBo;
    private double batchQty;
    private double size;
}
