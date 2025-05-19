package com.rits.productionlogservice.dto;

import java.time.LocalDateTime;

public class IntervalTimesResponse {
    private LocalDateTime firstCreatedDateTime;
    private LocalDateTime lastCreatedDateTime;

    public IntervalTimesResponse(LocalDateTime firstCreatedDateTime, LocalDateTime lastCreatedDateTime) {
        this.firstCreatedDateTime = firstCreatedDateTime;
        this.lastCreatedDateTime = lastCreatedDateTime;
    }

    public LocalDateTime getFirstCreatedDateTime() {
        return firstCreatedDateTime;
    }

    public void setFirstCreatedDateTime(LocalDateTime firstCreatedDateTime) {
        this.firstCreatedDateTime = firstCreatedDateTime;
    }

    public LocalDateTime getLastCreatedDateTime() {
        return lastCreatedDateTime;
    }

    public void setLastCreatedDateTime(LocalDateTime lastCreatedDateTime) {
        this.lastCreatedDateTime = lastCreatedDateTime;
    }
}