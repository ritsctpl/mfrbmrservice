package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_MFR_CRITICAL_CONTROLS")
public class CriticalControlPoints {
    @Id
    private String id;
    private String handle;
    private Object title;
    private Object criticalControlPointsData;
    private int active;
    private String site;
    private String dataField;
//    private Object DosAndDonts;
}
