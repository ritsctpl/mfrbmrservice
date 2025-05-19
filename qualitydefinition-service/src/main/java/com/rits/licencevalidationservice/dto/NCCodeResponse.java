package com.rits.licencevalidationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NCCodeResponse {
    private String ncCode;
    private String description;
    private String status;
}
