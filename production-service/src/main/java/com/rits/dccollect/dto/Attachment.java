package com.rits.dccollect.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Attachment {
    private String sequence;
    private String itemGroup;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String operation;
    private String operationVersion;
    private String workCenter;
    private String workCenterVersion;
    private String resource;
    private String resourceVersion;
    private String shopOrder;
    private String pcu;
    private String site;

}
