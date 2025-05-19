package com.rits.worklistservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WorkListRequest {
    private String site;
    private String list;
    private String operationBO;
    private String resourceBO;
    private String workCenterBO;
    private String pcuBO;
    private String category;
    private  String item;
    private String itemGroup;
    private  String itemVersion;
    private  String routing;
    private  String routingVersion;
    private  String resourceType;
    private  String customerOrder;
    private  String shopOrder;
    private String bom;
    private String bomVersion;
    private String component;
    private String componentVersion;
    private String resource;
    private String phase;
    private String activityId;
    private String podName;
}
