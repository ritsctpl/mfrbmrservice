package com.rits.dataFieldService.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class DataFieldResponse {
    private String dataField;
    private String description;
    private String type;
    private String fieldLabel;
}
