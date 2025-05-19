package com.rits.oeeservice.dto;

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
@Document(collection = "R_RESOURCE")
public class Resource {
    private String site;

    @Id
    private String handle;
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
    private String setUpState;
    private boolean trackOee;
    private List<ResourceTypeList> resourceTypeList;
    private List<CertificationList> certificationList;
    private List<OpcTagList> opcTagList;
    private List<ResourceCustomDataList> resourceCustomDataList;
    private List<ActivityHook> activityHookList;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
    private int active;
}
