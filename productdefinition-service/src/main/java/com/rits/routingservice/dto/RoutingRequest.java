package com.rits.routingservice.dto;

import com.rits.routingservice.model.CustomData;
import com.rits.routingservice.model.RoutingLane;
import com.rits.routingservice.model.RoutingStep;
import lombok.*;

import java.time.LocalDateTime;
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
    private String handle;
    private String description;
    private String routingType;
    private String status;
    private String subType;
    private boolean currentVersion;
    private boolean relaxedRoutingFlow;
    private String document;
    private String dispositionGroup;
    private String bom;
    private String bomVersion;
    private boolean replicationToErp;
    private boolean isParentRoute;
    private String parentRouterBO;
    private List<RoutingStep> routingStepList;
    private List<RoutingLane> lanes;
    private List<CustomData> customDataList;
    private boolean inUse;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
    private String createdBy;
    private String modifiedBy;
    private String userId;
}
