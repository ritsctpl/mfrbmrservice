package com.rits.resourceservice.dto;

import com.rits.resourceservice.model.*;
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
    private boolean trackOee;
    private List<ResourceTypeList> resourceTypeList;
    private List<CertificationList> certificationList;
    private List<OpcTagList> opcTagList;
    private List<ResourceCustomDataList> resourceCustomDataList;
    private List<ActivityHook> activityHookList;
    private String userId;
    private String activity;
    private String setUpState;
}
