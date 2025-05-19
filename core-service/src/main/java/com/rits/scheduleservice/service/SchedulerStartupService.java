package com.rits.scheduleservice.service;

import com.rits.scheduleservice.model.EventScheduleConfig;
import com.rits.scheduleservice.repository.EventScheduleConfigRepository;
import com.rits.scheduleservice.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class SchedulerStartupService {

    @Autowired
    private EventScheduleConfigRepository configRepository;

    @Autowired
    private SchedulingService schedulingService;

    // This method will run after the application starts
    @PostConstruct
    public void initializeSchedulersOnStartup() {
        // Fetch all event schedules from the database
        List<EventScheduleConfig> allSchedules = configRepository.findAll();

        // Loop through each schedule and start only the enabled ones
        for (EventScheduleConfig config : allSchedules) {
            if (config.isEnabled()) {
                try {
                    schedulingService.schedule(config);  // Start the enabled schedule
                    System.out.println("Started scheduler for: " + config.getEntityName());
                } catch (Exception e) {
                    System.err.println("Failed to start scheduler for: " + config.getEntityName());
                }
            } else {
                System.out.println("Skipping disabled scheduler for: " + config.getEntityName());
            }
        }
    }
}
