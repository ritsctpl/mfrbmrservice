package com.rits.ncgroupservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcGroupResponse {
    private String ncGroup;
    private String description;
}
