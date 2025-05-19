package com.rits.startservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RoutingRequest {
    private String site;
    private String routing;
    private String version;
}
