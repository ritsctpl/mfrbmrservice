package com.rits.assemblyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailsList {
    private String sequence;
    private String fieldValue;
    private String labelValue;
    private String type;
}
