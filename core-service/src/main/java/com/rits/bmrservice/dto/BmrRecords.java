package com.rits.bmrservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BmrRecords {
    private BmrHeaders bmrHeaders;
    private BmrProductDetails bmrProductDetails;
}
