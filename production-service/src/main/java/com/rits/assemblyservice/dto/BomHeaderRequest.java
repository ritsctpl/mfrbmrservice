package com.rits.assemblyservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BomHeaderRequest {
    private List<Pcu> pcuBos;
    private String site;
    private Bom bom;
    private String pcuBomBO;
}
