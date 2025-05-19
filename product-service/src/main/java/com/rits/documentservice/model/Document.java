package com.rits.documentservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@org.springframework.data.mongodb.core.mapping.Document(collection = "R_DOCUMENT")

public class Document {
    private String site;
    @Id
    private String handle;
    private String document;
    private String version;
    private String description;
    private String printQty;
    private String documentType;
    private String printBy;
    private String printMethods;
    private String status;
    private boolean currentVersion;
    private String template;
    private List<DocumentOption> documentOptions;
    private List<PrintIntegration> printIntegration;
    private List<CustomData> customDataList;
    private boolean inUse;
    private int active;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;


}
