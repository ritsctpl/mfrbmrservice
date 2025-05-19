package com.rits.lineclearanceservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrieveLineClearanceLogResponse {
    private String templeteName;
    private String taskName;
    private String description;
    private Boolean isMandatory;
    private Boolean evidenceRequired;
    private String evidence; // Base64 string
    private String status;
    private String clearanceTimeLimit;
    private LocalDateTime completedDateTime;
    private String reason;


}
