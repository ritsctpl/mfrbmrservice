package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_RECIPES")
public class MfrRecipes {
    @Id
    private String handle;
    private String site;
    private String mfrNo;
    private String version;
    private String productName;
    private Map<String, List<Map<String, Object>>> sections;
    private Map<String, String> headerDetails;
    private List<Map<String, String>> footerDetails;
    private int active;
    private String createdBy;
//    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String type;
    private String configuration;

    private String bomPhaseSepHandle;
    private String criticalControlHandle;
    private String dosAndDontsHandle;
    private String manufacturingProHandle;

    private List<Object> routingSteps;
//    private BomPhaseSeperation bomPhaseSeperation;
//    private ManufacturingProcedure manufacturing;
//    private DosAndDonts dosAndDonts;
//    private CriticalControlPoints criticalControlPoints;
//    private MfrRecipes mfrForSections;
}
