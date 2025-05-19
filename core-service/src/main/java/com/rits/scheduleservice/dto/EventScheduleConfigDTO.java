package com.rits.scheduleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventScheduleConfigDTO {

    private String id;  // Unique ID for the schedule
    private String entityType;
    private int entityId;
    private String entityName;
    private String eventType;
    private int eventIntervalSeconds;  // Interval in seconds
    private String cronExpression;  // New field to support cron-based scheduling
    private LocalDateTime nextRunTime;  // Optional - used to control the next execution time
    private LocalDateTime lastRunTime;
    private String apiEndpoint;  // API endpoint to trigger
    private String apiInput;  // API input JSON to be sent when calling the API
    private boolean enabled;  // New field to indicate if the schedule is enabled or disabled
    private boolean includeRunTime; // New field to decide if runtime details should be included
    private Integer hours;
    private Integer minutes;
    private Integer seconds;
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getEventIntervalSeconds() {
        return eventIntervalSeconds;
    }

    public void setEventIntervalSeconds(int eventIntervalSeconds) {
        this.eventIntervalSeconds = eventIntervalSeconds;
    }

    public LocalDateTime getNextRunTime() {
        return nextRunTime;
    }

    public void setNextRunTime(LocalDateTime nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getApiInput() {
        return apiInput;
    }

    public void setApiInput(String apiInput) {
        this.apiInput = apiInput;
    }
}
