package com.rits.downtimeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DowntimeLiveRecord {
    private String siteId;
    private String shiftId;
    private String workcenterId;
    private String resourceId;
    private String itemId;
    private String operationId;
    private Long totalDowntime;
}
