package com.rits.lineclearancelogservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

@Document(collection = "R_LINE_CLEARANCE_LOG")
public class LineClearanceLog {

    @Id
    private String handle;
    private String batchNo;
    private String site;
    private String templeteName;
    private String description;
    private String phase;
    private String operation;
    private String startedBy;
    private String completedBy;
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
    private LocalDateTime startedDateTime;
    private LocalDateTime completedDateTime;
    private LocalDateTime modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime approvedDateTime;
    private String approvedBy;
    private LocalDateTime rejectedDateTime;
    private String rejectedBy;
    private LocalDateTime updatedDateTime;
    private String updatedBy;
    private String reason;
    private Integer active;
    private boolean approved;

}
