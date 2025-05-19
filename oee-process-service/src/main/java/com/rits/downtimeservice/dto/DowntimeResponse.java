package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DowntimeResponse {
//    private String downtimeID;
//    private String handle;
//    private String siteID;
//    private String shiftID;
//    private String workcenterID;
//    private String resourceID;
//    private String itemID;
//    private String operationID;
//    private LocalDateTime downtimeStart;
//    private LocalDateTime downtimeEnd;
//    private double downtime;
//    private String reason;
//    private LocalDateTime createdDateTime;
//    private LocalDateTime modifiedDateTime;
//    private int active;

    private String message;
    private boolean success;

}
