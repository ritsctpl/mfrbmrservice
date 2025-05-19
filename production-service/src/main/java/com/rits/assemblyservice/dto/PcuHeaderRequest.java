package com.rits.assemblyservice.dto;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PcuHeaderRequest {
    private String site;
    private String pcuBO;
    private String operationBO;
}
