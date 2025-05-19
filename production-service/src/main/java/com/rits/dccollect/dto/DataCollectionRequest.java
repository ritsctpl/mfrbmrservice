package com.rits.dccollect.dto;

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
    private boolean erpGroup;
    private boolean qmInspectionGroup;
    private boolean passOrFailGroup;
    private String failOrRejectNumber;
    private boolean userAuthenticationRequired;
    private String certification;
    private String frequency;
    private List<Parameter> parameterList;
    private List<Attachment> attachmentList;
    private List<CustomData> customDataList;
}
