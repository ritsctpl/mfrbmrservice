package com.rits.bmrservice.dto;

import com.rits.mfrscreenconfigurationservice.model.MFRRefList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BmrRequest {
    private String site;
    private String bmrNo;
    private String version;
    private String mfrNo;
    private String mfrVersion;
    private String batchSize;
    private String nameOfTheProduct;
    private String configType;
    private String configuration;
    private String type;
    private List<MFRRefList> mrfRefList;
    private String createdBy;
    private String modifiedBy;
    private Map<String, String> headerDetails;
    private Map<String, List<String>> referenceId;


}