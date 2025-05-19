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
@Document(collection = "event_schedule_output")
public class EventScheduleOutput {

    @Id
    private String id;

    private String scheduleId;  // Reference to the schedule config
    private LocalDateTime executionTime;
    private String apiInput;
    private String apiOutput;  // Stores the API response output
    private boolean isError;
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public String getApiOutput() {
        return apiOutput;
    }

    public void setApiOutput(String apiOutput) {
        this.apiOutput = apiOutput;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public String getApiInput() {
        return apiInput;
    }

    public void setApiInput(String apiInput) {
        this.apiInput = apiInput;
    }
}
