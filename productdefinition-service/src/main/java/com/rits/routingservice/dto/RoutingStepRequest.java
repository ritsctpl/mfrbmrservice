package com.rits.routingservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoutingStepRequest {
    private String site;
    private String routing;
    private String version;
    private String operation;
    private String operationVersion;
    private String stepId;
}
