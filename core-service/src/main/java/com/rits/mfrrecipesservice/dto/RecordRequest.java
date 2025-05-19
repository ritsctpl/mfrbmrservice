package com.rits.mfrrecipesservice.dto;

import com.rits.bmrservice.dto.RoutingSteps;
import lombok.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class RecordRequest {
    private String site;
    private String handle;
    private String createdBy;
    private String mfrNo;
    private String version;
    private Map<String, List<Map<String, Object>>> sections;
    private Map<String, String> headerDetails;
    private List<Map<String, String>> footerDetails;
    private String productName;
    private List<RoutingSteps> routingSteps;
    private String newMfr;
    private String newVersion;
    private String newBatchSize;
    private List<Object> result;
    private String configuration;

}
