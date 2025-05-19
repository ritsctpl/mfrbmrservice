package com.rits.pcurouterheaderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingRequest {
    private String site;
    private String routing;
    private String version;
}
