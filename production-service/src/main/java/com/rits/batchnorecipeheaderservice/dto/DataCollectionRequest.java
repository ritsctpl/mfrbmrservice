package com.rits.batchnorecipeheaderservice.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DataCollectionRequest {
    private String site;
    private String batchNo;
    private String orderNo;
    private String material;
    private String materialVersion;
    private String phaseId;
    private String operationId;
    private Map<String,String> recipeDc;
    private List<DataCollection> dataCollectionList;
}
