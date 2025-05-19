package com.rits.assemblyservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RetrieveRequest {
    private String  site;
    private String pcuBO;
    private String component;
    private String operationBO;
    private String dataField;
    private String dataAttribute;
    private String uniqueId;
    private boolean removed;
    private String pcuRouterBO;
    private String stepId;
}