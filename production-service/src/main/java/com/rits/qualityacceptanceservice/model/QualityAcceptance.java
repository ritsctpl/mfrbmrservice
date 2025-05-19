package com.rits.qualityacceptanceservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "R_QUALITY_ACCEPTANCE")
public class QualityAcceptance {
    @Id
    private String handle;
    private String site;
    private String batchNo;
    private String phaseId;
    private String operation;
    private String resource;
    private String orderNumber;
    private String recipe;
    private String recipeVersion;
    private String user;
    private Integer active;

    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
}