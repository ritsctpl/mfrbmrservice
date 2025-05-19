package com.rits.datatypeservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BomRequest {
    private String site;
    private String bom;
    private String revision;
}
