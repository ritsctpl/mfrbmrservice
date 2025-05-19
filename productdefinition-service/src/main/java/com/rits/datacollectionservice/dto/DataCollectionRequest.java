package com.rits.datacollectionservice.dto;

import com.rits.datacollectionservice.model.Attachment;
import com.rits.datacollectionservice.model.CustomData;
import com.rits.datacollectionservice.model.Parameter;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollectionRequest {
    private String site;
    private String dataCollection;
    private String version;
    private String description;
    private String status;
    private boolean currentVersion;
    private String collectionType;
    private String collectDataAt;
    private String collectionMethod;
    private boolean erpGroup;
    private boolean qmInspectionGroup;
    private boolean passOrFailGroup;
    private String failOrRejectNumber;
    private boolean userAuthenticationRequired;
    private String certification;
    private boolean dataCollected;
    private boolean showReport;
    private String frequency;
    private List<String> tags;
    private List<Parameter> parameterList;
    private List<Attachment> attachmentList;
    private List<CustomData> customDataList;
    private String userId;
}
