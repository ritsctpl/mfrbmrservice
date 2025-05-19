package com.rits.nextnumbergeneratorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BmrRequest {
    private String site;
    private String bmrNo;
    private String mfrNo;
    private String version;
    private String batchSize;
    private String productName;
    private String configType;
    private String configuration;
    private String type;
//    private List<MFRRefList> mrfRefList;
}