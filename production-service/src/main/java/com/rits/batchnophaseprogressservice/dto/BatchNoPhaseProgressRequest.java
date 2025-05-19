package com.rits.batchnophaseprogressservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BatchNoPhaseProgressRequest {

        private String handle;
        private String site;
        private LocalDateTime dateTime;
        private String batchNo;
        private String material;
        private String materialVersion;
        private String recipe;
        private String recipeVersion;
        private String batchNoHeaderBO;
        private String batchNoRecipeHeaderBO;
        private String orderNumber;
        private List<PhaseProgress> phaseProgress;

        private String user;
        private Integer active;
        private LocalDateTime createdDateTime;
        private LocalDateTime modifiedDateTime;
        private String createdBy;
        private String modifiedBy;
}
