package com.rits.datatypeservice.dto;

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
}
