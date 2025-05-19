package com.rits.bomheaderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BomComponentListRequest {
    private String site;
    private String bom;
    private String revision;
    private String pcuBO;
    private String operation;
}
