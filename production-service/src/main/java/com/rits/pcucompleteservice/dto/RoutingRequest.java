package com.rits.pcucompleteservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutingRequest {
    private String site;
    private String routing;
    private String version;
    private String operation;
    private String operationVersion;
    private String stepId;
    private String pcuBo;
}
