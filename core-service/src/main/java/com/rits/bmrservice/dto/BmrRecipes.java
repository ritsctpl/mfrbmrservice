package com.rits.bmrservice.dto;

import com.rits.mfrrecipesservice.dto.BomPhaseSeperation;
import com.rits.mfrrecipesservice.dto.CriticalControlPoints;
import com.rits.mfrrecipesservice.dto.DosAndDonts;
import com.rits.mfrrecipesservice.dto.ManufacturingProcedure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "R_BMR_RECIPES")
public class BmrRecipes {
    @Id
    private String handle;
    private String site;
    private String bmrNo;
    private String version;
    private String productName;
    private String configuration;
    private Map<String, List<Map<String, Object>>> sections;
    private Map<String, String> headerDetails;
    private List<Map<String, String>> footerDetails;
    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String type;
}
