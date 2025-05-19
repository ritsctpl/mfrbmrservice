package com.rits.ncgroupservice.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcCodeDPMOCategory {
    private String ncCode;
    private String description;

    public NcCodeDPMOCategory(String ncCode) {
        this.ncCode = ncCode;
    }
}
