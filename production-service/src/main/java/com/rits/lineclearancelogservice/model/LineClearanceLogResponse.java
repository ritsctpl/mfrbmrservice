package com.rits.lineclearancelogservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LineClearanceLogResponse {
    private String resourceId;
    private String workCenterId;
    private String templeteName;
    private String taskName;
    private String description;
    private Boolean isMandatory;
    private Boolean evidenceRequired;
    private String evidence; // Base64 string
    private String status;
    private LocalDateTime completedDateTime;
}
