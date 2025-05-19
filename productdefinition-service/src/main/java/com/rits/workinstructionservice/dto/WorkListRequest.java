package com.rits.workinstructionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkListRequest {
    private String site;
    private String list;
    private String operationBO;
    private String resource;
    private String workCenterBO;
    private String pcu;
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
    private String batchNo;
    private String orderNo;
    private String operationId;
    private String phaseId;
}

