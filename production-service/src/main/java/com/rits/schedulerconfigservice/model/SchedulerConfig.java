package com.rits.schedulerconfigservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "R_SCHEDULER_CONFIGURATIONS")
public class SchedulerConfig {

    @Id
    private String handle;
    private Integer entityId;
    private String site;
    private String entityType;
    private String entityName;
    private String eventType;
    private Integer eventIntervalSeconds;
    private Date nextRunTime;
    private String apiEndpoint;
    private String apiInput;
    private Boolean enabled;
    private String cronExpression;

    private int active;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDateTime;
    private LocalDateTime modifiedDateTime;
}
