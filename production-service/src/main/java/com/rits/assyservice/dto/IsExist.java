package com.rits.assyservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IsExist {
    private String site;
    private String item;
    private String dataType;
    private String category;
    private String bom;
    private String revision;
    private String inventoryId;
    private String datastring;
    private  String  dataField;
}
