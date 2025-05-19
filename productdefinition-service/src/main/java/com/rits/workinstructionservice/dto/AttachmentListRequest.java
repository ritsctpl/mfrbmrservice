package com.rits.workinstructionservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AttachmentListRequest {
    private String sequence;
    private  String item;
    private String itemGroup;
    private  String itemVersion;
    private  String routing;
    private  String routingVersion;
    private  String operation;
    private  String workCenter;
    private  String resource;
    private  String resourceType;
    private  String customerOrder;
    private  String shopOrder;
    private  String pcu;
    private  String site;
    private String bom;
    private String bomVersion;
    private String component;
    private String componentVersion;
}
