package com.rits.nonconformanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataField {
    private String dataField;
    private String value;
}
