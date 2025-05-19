package com.rits.downtimeservice.dto;

import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class DowntimeResponseList {
    private Long id;
    private String resourceId;
    private String workcenterId;
    private String shiftId;
    private LocalDateTime downtimeStart;
    private LocalDateTime downtimeEnd;
    private Long downtimeDuration;
    private String downtimeType;
    private String reason;
    private String rootCause;
    private Integer isActive;
}
