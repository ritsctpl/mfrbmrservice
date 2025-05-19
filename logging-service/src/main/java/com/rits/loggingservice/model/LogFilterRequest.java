package com.rits.loggingservice.model;

import com.rits.loggingservice.enums.LogLevel;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LogFilterRequest {
    private LogLevel level;
    private String message;
    private String userId;
    private String location;
    private String application;
    private String date;
    private String time;   // e.g. "2025-02-18T23:59:59"

    // Constructors

}

