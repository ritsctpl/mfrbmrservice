package com.rits.recipemaintenanceservice.model;

import com.rits.routingservice.model.CustomData;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "R_RECEIPE")
public class Recipe {
    private String site;
    private String recipeName;
    private String version;

    @Id
    private String handle;
    private String recipeType;
    private String description;
    private String status;
//    private boolean relaxedFlow;
//    private LocalDateTime createdOn;
//    private LocalDateTime changedOn;
    private String createdBy;
    private String modifiedBy;
    private String userId;
    private boolean currentVersion;
    private boolean autoGR;
    private boolean qualityValidation;
    private List<CustomData> customDataList;
    private List<Phase> phases;
}

