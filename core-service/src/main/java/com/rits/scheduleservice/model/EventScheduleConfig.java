package com.rits.scheduleservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "event_schedule_config")
public class EventScheduleConfig {

    @Id
    private String id;  // MongoDB document ID

    private String entityType;
    private int entityId;
    private String entityName;
    private String eventType;
    private int eventIntervalSeconds;  // Interval in seconds
    private LocalDateTime nextRunTime;
    private LocalDateTime lastRunTime; // New field to store the last execution time
    private String apiEndpoint;
    private String apiInput;  // New field to store API input JSON
    private boolean enabled;  // New field to track if the schedule is enabled or disabled
    private String cronExpression;  // New field for cron-based scheduling
    private String status;// New field to track status (e.g., "active", "completed", "failed", "disabled")
    private boolean includeRunTime; // New field to decide if runtime details should be included
    // Getters and Setters
    // ...
}
