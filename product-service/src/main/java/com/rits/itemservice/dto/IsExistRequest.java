package com.rits.itemservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IsExistRequest {
    private String site;
    private String bom;
    private String revision;
    private String routing;
    private String version;
    private String dataType;
    private String category;
    private String itemGroup;
    private String groupName;
}
