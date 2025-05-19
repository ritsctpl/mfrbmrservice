package com.rits.shoporderservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IsExist {
    private String site;
    private String bom;
    private String revision;
    private String item;
    private String routing;
    private String version;
    private String workCenter;
}