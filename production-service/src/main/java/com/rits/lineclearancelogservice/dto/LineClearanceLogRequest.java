package com.rits.lineclearancelogservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LineClearanceLogRequest {
    private String site;
    private String templeteName;
    private String description;
    private String batchNo;
//    private List<String> batchNo;
    private String phase;
    private String operation;
    private String resourceId;
    private String workCenterId;
    private String userId;
    private String taskName;
    private String taskDescription;
    private Boolean isMandatory;
    private Boolean evidenceRequired;
    private String evidence;
    private String orderNumber;
    private String item;
    private String itemVersion;
    private int quantity;
    private String status;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
    private String reason;
    private String newStatus;
    private Integer active;
    private String dateRange;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
