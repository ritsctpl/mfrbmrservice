package com.rits.qualityacceptanceservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QualityAcceptanceRequest {
    private String site;
    private String batchNo;
    private String phaseId;
    private String operation;
    private String resource;
    private String orderNumber;
    private String recipe;
    private String recipeVersion;
    private String user;
//    private Integer active;
}
