package com.rits.checkhook.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class IsExist {
    private String site;
    private String operation;
    private String revision;
    private String resource;
    private String shopOrder;
    private String item;
    private String routing;
    private String version;
    private String workCenter;
    private String activityId;
}
