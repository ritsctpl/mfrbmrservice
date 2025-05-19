package com.rits.dccollect.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RetrieveRequest {
    private String site;
    private String sequence;
    private String itemGroup;
    private String item;
    private String itemVersion;
    private String routing;
    private String routingVersion;
    private String operation;
    private String workCenter;
    private String resource;
    private String shopOrder;
    private String pcu;
    private String dataCollection;
    private String version;
}
