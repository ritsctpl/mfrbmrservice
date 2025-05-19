package com.rits.downtimeservice.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineLogRequest {
    private String siteId;
    private String shiftId;
    private LocalDateTime shiftCreatedDateTime;
    private LocalDateTime shiftBreakCreatedDateTime;
    private String workcenterId;
    private String resourceId;
    private String itemId;
    private String operationId;
    private String logMessage;
    private String logEvent;
    private String reason;
    private String rootCause;
    private String commentUsr;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
    private Integer active;
}
