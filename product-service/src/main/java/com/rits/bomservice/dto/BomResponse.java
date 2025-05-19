package com.rits.bomservice.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BomResponse {

    private String bom;
    private String revision;
    private String description;
    private String status;
    private boolean currentVersion;
    private boolean bomTemplate;
    private String bomType;
    private String validFrom;
    private String validTo;

}
