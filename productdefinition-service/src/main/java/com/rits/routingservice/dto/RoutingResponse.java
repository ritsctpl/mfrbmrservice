package com.rits.routingservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoutingResponse {
    private String routing;
    private String version;
    private String description;
    private String status;
    private boolean currentVersion;
    private String routingType;
//    private String handle;
}
