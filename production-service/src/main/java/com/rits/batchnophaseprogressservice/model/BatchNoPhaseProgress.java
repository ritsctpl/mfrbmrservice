package com.rits.batchnophaseprogressservice.model;

import com.rits.batchnophaseprogressservice.dto.PhaseProgress;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "R_BATCH_NO_PHASE_PROGRESS")
public class BatchNoPhaseProgress {

    @Id
    private String handle;
    private String site;
    private LocalDateTime dateTime;
    private String batchNo;
    private String material;
    private String materialVersion;
    private String batchNoHeaderBO;
    private String recipe;
    private String recipeVersion;
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
