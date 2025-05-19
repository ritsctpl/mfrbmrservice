package com.rits.cycletimeservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IsExist {
    private String site;
    private String shopOrder;
    private String resource;
    private String operation;
    private String revision;
    private String item;
    private String itemVersion;
    private String routing;
    private String version;
}