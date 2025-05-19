package com.rits.dccollect.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DcCollectRequest {
    private String site;
    private String pcu;
    private String resource;
    private String operation;
    private String operationVersion;
    private String dataCollection;
    private String version;
    private String item;
    private String itemVersion;
    private String parameterName;
    private String actualValue;
    private String userBO;
    private String workCenter;
}
