package com.rits.overallequipmentefficiency.dto;


import java.time.LocalDateTime;

public class IntervalTimeResponse {
    private LocalDateTime intervalStartTime;
    private LocalDateTime intervalEndTime;

    public IntervalTimeResponse(LocalDateTime intervalStartTime, LocalDateTime intervalEndTime) {
        this.intervalStartTime = intervalStartTime;
        this.intervalEndTime = intervalEndTime;
    }

    public LocalDateTime getIntervalStartTime() {
        return intervalStartTime;
    }

    public void setIntervalStartTime(LocalDateTime intervalStartTime) {
        this.intervalStartTime = intervalStartTime;
    }

    public LocalDateTime getIntervalEndTime() {
        return intervalEndTime;
    }

    public void setIntervalEndTime(LocalDateTime intervalEndTime) {
        this.intervalEndTime = intervalEndTime;
    }
}