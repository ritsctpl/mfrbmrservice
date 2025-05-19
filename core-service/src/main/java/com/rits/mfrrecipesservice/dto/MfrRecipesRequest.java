package com.rits.mfrrecipesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfrRecipesRequest {
    private String site;
//    private String procedureBom;
    private String handle;
    private String mfrNo;
    private String version;
    private String productName;
    private CriticalControlPoints criticalControlPoints;
    private List<FooterDetails> footerDetails;
    private Sections sections;
    private String modifiedBy;
    private String createdBy;
    private String type;

}
