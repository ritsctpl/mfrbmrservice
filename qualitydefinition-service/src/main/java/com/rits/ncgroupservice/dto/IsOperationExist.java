package com.rits.ncgroupservice.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IsOperationExist {
    private String site;
    private String operation;
}
