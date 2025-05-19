package com.rits.containermaintenanceservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "CONTAINER_MAINTENANCE")
public class ContainerMaintenance {
    @Id
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
}
