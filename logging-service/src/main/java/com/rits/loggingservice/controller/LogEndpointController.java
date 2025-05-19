package com.rits.loggingservice.controller;

import com.rits.loggingservice.entity.LogEntryEntity;
import com.rits.loggingservice.model.LogEntry;
import com.rits.loggingservice.model.LogFilterRequest;
import com.rits.loggingservice.repository.LogEntryRepository;
import com.rits.loggingservice.event.LogCustomEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/app/v1/logging-service")
public class LogEndpointController {

    private static final Logger logger = LoggerFactory.getLogger(LogEndpointController.class);
    private final LogEntryRepository logEntryRepository;

    // Inject the repository via constructor
    public LogEndpointController(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    /**
     * Endpoint to accept and store log entries.
     * POST: /app/v1/logging-service/logs
     *
     * Expected Request Body Example:
     * {
     *   "level": "INFO",
     *   "message": "Some log message",
     *   "userId": "user123",
     *   "location": "MyClass",
     *   "application": "MyApp",
     *   "date": "2025-02-19",
     *   "time": "12:34:56"
     * }
     */
    @PostMapping("/logs")
    public String receiveLog(@RequestBody LogEntry logEntry) {

        LogEntry entryToSave = LogEntry.builder()
                .level(logEntry.getLevel())
                .message(logEntry.getMessage())
                .userId(logEntry.getUserId())
                .location(logEntry.getLocation())
                .application(logEntry.getApplication())
                .date(logEntry.getDate())
                .time(logEntry.getTime())
                .build();

        logEntryRepository.save(entryToSave);
        return "Log received and stored successfully";
    }

    /**
     * POST endpoint to filter and view logs.
     * Accepts a JSON body with optional fields: level, message, userId, location, application, date, time.
     *
     * Expected Request Body Example:
     * {
     *   "level": "ERROR",
     *   "message": "Exception",
     *   "userId": "user123",
     *   "location": "MyClass",
     *   "application": "MyApp",
     *   "date": "2025-02-18",
     *   "time": "14:30:00"
     * }
     */
    @PostMapping("/logs/filter")
    public List<LogEntry> filterLogs(@RequestBody LogFilterRequest filter) {
        List<LogEntry> allLogs = logEntryRepository.findAll();

        return allLogs.stream().filter(log -> {
            boolean matches = true;
            // Filter by log level if provided
            if (filter.getLevel() != null && !filter.getLevel().toString().isEmpty()) {
                matches &= log.getLevel().equalsIgnoreCase(filter.getLevel().toString());
            }
            // Filter by message keyword if provided
            if (filter.getMessage() != null && !filter.getMessage().isEmpty()) {
                matches &= log.getMessage() != null && log.getMessage().contains(filter.getMessage());
            }
            // Filter by userId if provided
            if (filter.getUserId() != null && !filter.getUserId().isEmpty()) {
                matches &= log.getUserId() != null && log.getUserId().equals(filter.getUserId());
            }
            // Filter by location if provided
            if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
                matches &= log.getLocation() != null && log.getLocation().equalsIgnoreCase(filter.getLocation());
            }
            // Filter by application if provided
            if (filter.getApplication() != null && !filter.getApplication().isEmpty()) {
                matches &= log.getApplication() != null && log.getApplication().equalsIgnoreCase(filter.getApplication());
            }
            // Filter by date if provided
            if (filter.getDate() != null && !filter.getDate().isEmpty()) {
                matches &= log.getDate() != null && log.getDate().equals(filter.getDate());
            }
            // Filter by time if provided
            if (filter.getTime() != null && !filter.getTime().isEmpty()) {
                matches &= log.getTime() != null && log.getTime().equals(filter.getTime());
            }
            return matches;
        }).collect(Collectors.toList());
    }

    @GetMapping("/test-log")
    public String testLog() {
        logger.info("Received request to /test-log endpoint.");
        logger.debug("Debug message from /test-log.");
        logger.warn("Warning message from /test-log.");
        logger.error("Error message from /test-log.");
        return "Log messages sent. Check your real-time log viewer.";
    }
    @EventListener
    public void handleLogCustomEvent(LogCustomEvent event) {
        LogEntry entryToSave = LogEntry.builder()
                .level(event.getLevel().toString())
                .message(event.getMessage())
                .userId(event.getUserId())
                .location(event.getLocation())
                .application(event.getApplication())
                .date(event.getDate())
                .time(event.getTime())
                .build();

        logEntryRepository.save(entryToSave);
        logger.info("Processed LogCustomEvent: {}", event);
    }
}
