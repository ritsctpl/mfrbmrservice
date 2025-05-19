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
public class LineClearanceLogHistoryResponse {

    private String site;
    private String templeteName;
    private String batchNo;
    private String taskName;
    private String status;
    private String phase;
    private String operation;
    private LocalDateTime modifiedDateTime;
    private String modifiedBy;
}
