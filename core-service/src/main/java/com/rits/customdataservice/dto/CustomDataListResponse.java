package com.rits.customdataservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomDataListResponse {
    private String sequence;
    private String customData;
    private String fieldLabel;
    private String required;
}
