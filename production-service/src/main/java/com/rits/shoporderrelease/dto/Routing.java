package com.rits.shoporderrelease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Routing {
    private String bom;
    private String bomVersion;
    private String status;
    private String site;
    private String version;
    private String subType;

    private boolean inUse;


    private String routing;
    private String handle;
    private String description;
    private String routingType;
    private boolean currentVersion;
    private boolean relaxedRoutingFlow;
    private String document;
    private String dispositionGroup;
    private String replicationToErp;
    private boolean isParentRoute;
    private String parentRouterBO;
    private List<RoutingStep> routingStepList;
    private List<RoutingLane> lanes;
    private List<CustomData> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
}
