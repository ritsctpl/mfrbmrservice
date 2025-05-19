package com.rits.checkhook.dto;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResourceRequest {
    private String site;
    private String resource;
    private String description;
    private String status;
    private String defaultOperation;
    private boolean processResource;
    private String erpEquipmentNumber;
    private String erpPlantMaintenanceOrder;
    private String validFrom;
    private String validTo;
    private String utilizationPricePerHr;
    private List<ActivityHook>  activityHookList;
    private String userId;
    private String activity;
    private String setUpState;
}
