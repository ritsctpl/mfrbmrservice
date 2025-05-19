package com.rits.pcudoneservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAssemblyDataTypeRequest {
    private String site;
    private  String shopOrder;
    private String item;
    private String revision;
    private String bom;
    private String dataType;
    private String category;
}
