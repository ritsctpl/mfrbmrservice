package com.rits.barcodeservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Builder
@Getter
@Setter
public class BarcodeAllCodeList {
    private List<BarcodeAllCodeResponse> barcodeAllCodeResponses;
}
