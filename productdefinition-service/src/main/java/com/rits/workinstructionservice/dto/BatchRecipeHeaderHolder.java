package com.rits.workinstructionservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRecipeHeaderHolder {
    private String site;
    private String batchNo;
    private String orderNo;
    private String material;
    private String materialVersion;
//    private BatchNoRecipeHeader response;
}
