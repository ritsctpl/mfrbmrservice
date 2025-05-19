package com.rits.dataFieldService.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class DataFieldResponseList {
    private List<DataFieldResponse> dataFieldList;
}
