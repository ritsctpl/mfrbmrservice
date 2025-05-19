package com.rits.shiftservice.dto;

import java.time.LocalDateTime;

public class IntervalDateTime {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public IntervalDateTime(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
