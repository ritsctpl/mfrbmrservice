package com.rits.operationservice.dto;

import com.rits.operationservice.model.ActivityHook;
import com.rits.operationservice.model.Certification;
import com.rits.operationservice.model.OperationCustomData;
import com.rits.operationservice.model.SubStep;
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

public class OperationRequest {

    private String site;
    private String handle;
    private String operation;
    private String revision;
    private String description;
    private String status;
    private String operationType;
    private String resourceType;
    private String defaultResource;
    private String erpOperation;
    private boolean addAsErpOperation;
    private String workCenter;
    private boolean currentVersion;
    private int maxLoopCount;
    private List<Certification> certificationList;
    private List<SubStep> subStepList;
    private List<ActivityHook> activityHookList;
    private List<OperationCustomData> operationCustomDataList;
private String userId;

}


