package com.rits.documentservice.dto;

import com.rits.documentservice.model.CustomData;
import com.rits.documentservice.model.DocumentOption;
import com.rits.documentservice.model.PrintIntegration;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DocumentRequest {
    private String site;
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
    private String userId;
}
