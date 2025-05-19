package com.rits.assemblyservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class BomList {
    private String pcuBomBO;
    private String status;
}
