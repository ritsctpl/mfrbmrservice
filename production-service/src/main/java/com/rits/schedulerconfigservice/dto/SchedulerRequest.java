package com.rits.schedulerconfigservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerRequest {
    private String site;
    private String entityType;
    private Integer entityId;
    private String entityName;
    private Integer eventIntervalSeconds;

    private Date nextRunTime;

    private String apiEndpoint;

    private Boolean enabled;

    private String cronExpression;

    private String apiInput;
    private String eventType;

    private int active;
    private String user;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
