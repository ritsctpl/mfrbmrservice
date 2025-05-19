package com.rits.pcuheaderservice.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Router {
    private boolean isParentRoute;
    private String parentRouteBO;
    private String reenterStepID;
    private List<Routing> r_route;
}
