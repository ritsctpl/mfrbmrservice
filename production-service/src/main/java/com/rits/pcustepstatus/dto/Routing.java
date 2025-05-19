package com.rits.pcustepstatus.dto;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Routing {
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
    private String replicationToErp;
    private boolean isParentRoute;
    private String parentRouterBO;
    private List<RoutingStep> routingStepList;
    private List<CustomData> customDataList;
    private boolean inUse;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
}
