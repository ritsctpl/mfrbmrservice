package com.rits.containermaintenanceservice.dto;

import com.rits.containermaintenanceservice.model.CustomData;
import com.rits.containermaintenanceservice.model.Dimensions;
import com.rits.containermaintenanceservice.model.Documents;
import com.rits.containermaintenanceservice.model.PackLevelList;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ContainerMaintenanceRequest {
    private String handle;
    private String site;
    private String container;
    private String description;
    private String containerCategory;
    private String status;
    private String containerDataType;
    private String sfcDataType;
    private String sfcPackOrder;
    private Boolean handlingUnitManaged;
    private Boolean generateHandlingUnitNumber;
    private int totalMinQuantity;
    private int totalMaxQuantity;
    private List<PackLevelList> packLevelList;
    private List<Documents> documentsList;
    private List<Dimensions> dimensionsList;
    private List<CustomData> customDataList;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String userId;
}
