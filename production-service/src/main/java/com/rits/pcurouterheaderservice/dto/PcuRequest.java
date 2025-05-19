package com.rits.pcurouterheaderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcuRequest {
    private String site;
    private String pcuBo;
    private String stepId;
    private String router;
    private String version;
    private String operation;
    private String operationVersion;
}
