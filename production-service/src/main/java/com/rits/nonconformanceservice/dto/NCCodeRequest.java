package com.rits.nonconformanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NCCodeRequest {
    private String handle;
    private String site;
    private String ncCode;
    private String description;
    private String status;
    private String assignNCtoComponent;
    private String ncCategory;
    private String dpmoCategory;
    private String ncDatatype;
    private String collectRequiredNCDataonNC;
    private String messageType;
    private int ncPriority;
    private int maximumNCLimit;
    private String ncSeverity;
    private String secondaryCodeSpecialInstruction;
    private boolean canBePrimaryCode;
    private boolean closureRequired;
    private boolean autoClosePrimaryNC;
    private boolean autoCloseIncident;
    private boolean secondaryRequiredForClosure;
    private boolean erpQNCode;
    private String erpCatalog;
    private String erpCodeGroup;
    private String erpCode;
    private boolean oeeQualityKPIRelevant;
    private List<DispositionRoutings> dispositionRoutingsList;
    private List<OperationGroups> operationGroupsList;
    private List<NCGroups> ncGroupsList;
    private List<SecondariesGroups> secondariesGroupsList;
    private List<CustomData> customDataList;
    private String userId;
    private int active;
    private LocalDateTime modifiedDateTime;
    private LocalDateTime createdDateTime;
    private List<String> ncCodeList;
}
