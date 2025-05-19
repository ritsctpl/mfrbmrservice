package com.rits.workinstructionservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Attachment {
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
    private String bom;
    private String bomVersion;
    private String component;
    private String componentVersion;
    private String orderNo;
    private String batchNo;
    private String phase;
}
