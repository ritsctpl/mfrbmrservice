package com.rits.pcucompleteservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcuRequest {
    private String site;
    private String pcuBo;
    private String router;
    private String version;
    private String operation;
    private String operationVersion;
    private String stepId;
}
