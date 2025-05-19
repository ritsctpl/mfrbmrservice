package com.rits.loggingservice.event;

import com.rits.loggingservice.enums.LogLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class LogCustomEvent extends ApplicationEvent {
    private final LogLevel level;
    private final String message;
    private final String userId;
    private final String location;
    private final String application;
    private final String date;
    private final String time;

    @Builder
    public LogCustomEvent(Object source,
                          LogLevel level,
                          String message,
                          String userId,
                          String location,
                          String application,
                          String date,
                          String time) {
        super(source);
        this.level = level;
        this.message = message;
        this.userId = userId;
        this.location = location;
        this.application = application;
        this.date = date;
        this.time = time;
    }
}
