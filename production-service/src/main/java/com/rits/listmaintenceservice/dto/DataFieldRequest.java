package com.rits.listmaintenceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataFieldRequest {
    private String site;
    private String dataField;
}
