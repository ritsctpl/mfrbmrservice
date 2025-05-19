package com.rits.datacollectionservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "R_DATA_COLLECTION")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollection {
    private String site;
    private String dataCollection;
    private String version;
    @Id
    private String handle;
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
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
