package com.rits.dhrservice.dto;

import com.rits.nonconformanceservice.dto.DataField;
import com.rits.nonconformanceservice.dto.SecondaryNCData;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NcData {
    private String handle;
    private String changeStamp;
    private String ncContextGbo;
    private String userBo;
    private String dateTime;
    private String sequence;
    private String site;
    private String parentNcDataBo;
    private String ncState;
    private String ncCodeBo;
    private String ncDataTypeBo;
    private double qty;
    private double defectCount;
    private String componentBo;
    private String compContextGbo;
    private String refDes;
    private String comments;
    private String routerBo;
    private String dispositionRouterBo;
    private String stepId;
    private String operationBo;
    private int timesProcessed;
    private String resourceBo;
    private String workCenterBo;
    private String itemBo;
    private boolean closureRequired;
    private String closedUserBo;
    private String closedDateTime;
    private String cancelledUserBo;
    private String cancelledDateTime;
    private String incidentDateTime;
    private String identifier;
    private String failureId;
    private String verifiedState;
    private String createdDateTime;
    private String modifiedDateTime;
    private String ncCategory;
    private String verifiedDateTime;
    private String location;
    private String reportingCenterBo;
    private String incidentNumberBo;
    private String dispositionDone;
    private String rootCauseOperBo;
    private boolean transferredToDpmo;
    private String componentSfcBo;
    private String componentSfcItemBo;
    private String dispositionFunctionBo;
    private String assemblyIncidentNum;
    private String batchIncidentNum;
    private String originalTransferKey;
    private String actionCode;
    private String partitionDate;
    private String copiedFromNcDataBo;
    private List<DataField> dataFieldsList;
    private List<SecondaryNCData> secondaryNCDataList;
    private int count;

}
