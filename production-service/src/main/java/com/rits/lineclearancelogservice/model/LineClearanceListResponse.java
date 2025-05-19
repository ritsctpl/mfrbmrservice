package com.rits.lineclearancelogservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineClearanceListResponse {

    private String templeteName;
    private String taskName;
    private String description;
    private Boolean isMandatory;
    private Boolean evidenceRequired;
    private String evidence; // Base64 string
    private String status;
}
