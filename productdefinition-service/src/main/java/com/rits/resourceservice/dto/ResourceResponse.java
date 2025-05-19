package com.rits.resourceservice.dto;

import com.rits.resourceservice.model.CertificationList;
import com.rits.resourceservice.model.OpcTagList;
import com.rits.resourceservice.model.ResourceCustomDataList;
import com.rits.resourceservice.model.ResourceTypeList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResourceResponse {
    private String site;
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
    private List<ResourceTypeList> resourceTypeList;
    private List<CertificationList> certificationList;
    private List<OpcTagList> opcTagList;
    private List<ResourceCustomDataList> resourceCustomDataList;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private int active;

}
