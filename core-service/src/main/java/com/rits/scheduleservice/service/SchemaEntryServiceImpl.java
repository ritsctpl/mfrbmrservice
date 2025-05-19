package com.rits.scheduleservice.service;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.parser.CronParser;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.mongodb.client.result.DeleteResult;
import com.rits.scheduleservice.dto.EventScheduleConfigDTO;
import com.rits.scheduleservice.model.EventScheduleConfig;
import com.rits.scheduleservice.model.EventScheduleOutput;
import com.rits.scheduleservice.repository.EventScheduleConfigRepository;
import com.rits.scheduleservice.repository.EventScheduleOutputRepository;
import com.rits.scheduleservice.service.SchemaEntryService;
import com.rits.scheduleservice.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SchemaEntryServiceImpl implements SchemaEntryService {

    @Autowired
    private EventScheduleConfigRepository configRepository;


    @Autowired
    private EventScheduleOutputRepository outputRepository;

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public EventScheduleConfig createSchemaEntry(EventScheduleConfigDTO dto) {

    if (dto.getEntityType().equalsIgnoreCase("CRONO")){
            if (dto.getCronExpression() != null && !dto.getCronExpression().isEmpty()) {
                if (!validateCronExpression(dto.getCronExpression())) {
                    throw new IllegalArgumentException("Invalid cron expression: " + dto.getCronExpression());
                }
            }
    }
        // Check if a schedule with the same entityId already exists
        Optional<EventScheduleConfig> existingConfig = configRepository.findByEntityId(dto.getEntityId());

        EventScheduleConfig config;
        if (existingConfig.isPresent()) {
            // Update the existing config if found
            config = existingConfig.get();
            config.setEntityType(dto.getEntityType());
            config.setEntityName(dto.getEntityName());
            config.setEventType(dto.getEventType());
            config.setEventIntervalSeconds(dto.getEventIntervalSeconds());
            config.setApiEndpoint(dto.getApiEndpoint());
            config.setApiInput(dto.getApiInput());
            config.setNextRunTime(LocalDateTime.now().plusSeconds(dto.getEventIntervalSeconds()));
            config.setLastRunTime(existingConfig.get().getLastRunTime()); // Retain the existing lastRunTime
            config.setEnabled(dto.isEnabled());
            config.setCronExpression(dto.getCronExpression());
            config.setIncludeRunTime(dto.isIncludeRunTime());
            configRepository.save(config);

            // Update the schedule if enabled
            if (config.isEnabled()) {
                try {
                    schedulingService.updateSchedule(config);  // Reschedule the task
                } catch (Exception e) {
                    throw new RuntimeException("Error updating schedule", e);
                }
            } else {
                try {
                    schedulingService.cancelSchedule("route-" + config.getId());  // Cancel if disabled
                } catch (Exception e) {
                    throw new RuntimeException("Error canceling schedule", e);
                }
            }

        } else {
            // Create a new config if none found
            config = new EventScheduleConfig();
            config.setEntityType(dto.getEntityType());
            config.setEntityId(dto.getEntityId());
            config.setEntityName(dto.getEntityName());
            config.setEventType(dto.getEventType());
            config.setEventIntervalSeconds(dto.getEventIntervalSeconds());
            config.setApiEndpoint(dto.getApiEndpoint());
            config.setApiInput(dto.getApiInput());
            config.setNextRunTime(LocalDateTime.now().plusSeconds(dto.getEventIntervalSeconds()));
            config.setLastRunTime(null); // Initialize lastRunTime to null for new entries
            config.setEnabled(dto.isEnabled());
            config.setCronExpression(dto.getCronExpression());
            config.setIncludeRunTime(dto.isIncludeRunTime());
            configRepository.save(config);

            // Only schedule the task if it's enabled
            if (config.isEnabled()) {
                try {
                    schedulingService.schedule(config);  // Schedule the task using Camel
                } catch (Exception e) {
                    throw new RuntimeException("Error scheduling task", e);
                }
            }
        }

        return config;
    }

    public boolean validateCronExpression(String cronExpression) {
        try {
            // Use Quartz definition to validate the 6-part cron expression
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            Cron cron = parser.parse(cronExpression);
            cron.validate(); // This will throw an exception if the expression is invalid
            return true;     // If no exception, the cron is valid
        } catch (Exception e) {
            return false;    // Return false if validation fails
        }
    }
  /*  @Override
    public EventScheduleConfig createSchemaEntry(EventScheduleConfigDTO dto) {
        EventScheduleConfig config = new EventScheduleConfig();
        config.setEntityType(dto.getEntityType());
        config.setEntityId(dto.getEntityId());
        config.setEntityName(dto.getEntityName());
        config.setEventType(dto.getEventType());
        config.setEventIntervalSeconds(dto.getEventIntervalSeconds());
        config.setApiEndpoint(dto.getApiEndpoint());
        config.setApiInput(dto.getApiInput());
        config.setNextRunTime(LocalDateTime.now().plusSeconds(dto.getEventIntervalSeconds()));
        config.setEnabled(dto.isEnabled());  // Set the enabled/disabled status
        config.setCronExpression(dto.getCronExpression());
        configRepository.save(config);

        // Only schedule the task if it's enabled
        if (config.isEnabled()) {
            try {
                schedulingService.schedule(config);  // Schedule the task using Camel
            } catch (Exception e) {
                throw new RuntimeException("Error scheduling task", e);
            }
        }

        return config;
    }*/

    @Override
    public List<EventScheduleConfig> getAllSchemaEntries() {
        return configRepository.findAll();
    }

    @Override
    public EventScheduleConfig updateSchemaEntry(String id, EventScheduleConfigDTO dto) {
        if (dto.getEntityType().equalsIgnoreCase("CRONO")){
            if (dto.getCronExpression() != null && !dto.getCronExpression().isEmpty()) {
                if (!validateCronExpression(dto.getCronExpression())) {
                    throw new IllegalArgumentException("Invalid cron expression: " + dto.getCronExpression());
                }
            }
        }
        Optional<EventScheduleConfig> optionalConfig = configRepository.findById(id);
        if (optionalConfig.isPresent()) {
            EventScheduleConfig config = optionalConfig.get();
            config.setEventIntervalSeconds(dto.getEventIntervalSeconds());
            config.setApiEndpoint(dto.getApiEndpoint());
            config.setApiInput(dto.getApiInput());
            config.setNextRunTime(LocalDateTime.now().plusSeconds(dto.getEventIntervalSeconds()));
            config.setLastRunTime(config.getLastRunTime()); // Retain the existing lastRunTime
            config.setEnabled(dto.isEnabled());  // Update the enabled/disabled status
            config.setCronExpression(dto.getCronExpression());
            config.setEntityType(dto.getEntityType());
            config.setIncludeRunTime(dto.isIncludeRunTime());


            configRepository.save(config);

            // Reschedule the task only if it's enabled
            try {
                if (config.isEnabled()) {
                    schedulingService.updateSchedule(config);  // Reschedule the task
                } else {
                    schedulingService.cancelSchedule("route-" + config.getId());  // Cancel the schedule if disabled
                }
            } catch (Exception e) {
                throw new RuntimeException("Error handling task schedule", e);
            }

            return config;
        } else {
            throw new RuntimeException("Schedule not found with id: " + id);
        }
    }

    @Override
    public void deleteSchemaEntry(String id) {
        configRepository.deleteById(id);
        try {
            schedulingService.cancelSchedule("route-" + id);  // Cancel the scheduled task using Camel
        } catch (Exception e) {
            throw new RuntimeException("Error canceling schedule", e);
        }
    }

    public Optional<EventScheduleConfig> getSchemaEntryById(String id) {
        return configRepository.findById(id);
    }

    public List<EventScheduleOutput> getSchemaEntryByScheduleId(String id) {
        return outputRepository.findTop15ByScheduleIdOrderByExecutionTimeDesc(id);
    }

    public boolean deleteScheduleOutput(Integer hours, Integer minutes, Integer seconds) {
        Query latestRecordQuery = new Query()
                .with(Sort.by(Sort.Direction.DESC, "executionTime"))
                .limit(1);

        EventScheduleOutput latestRecord = mongoTemplate.findOne(latestRecordQuery, EventScheduleOutput.class);

        if (latestRecord == null || latestRecord.getExecutionTime() == null) {
            return false; // No records found, nothing to delete
        }

        LocalDateTime cutoffDate = latestRecord.getExecutionTime();
        if (hours != null) {
            cutoffDate = cutoffDate.minusHours(hours);
        }
        if (minutes != null) {
            cutoffDate = cutoffDate.minusMinutes(minutes);
        }
        if (seconds != null) {
            cutoffDate = cutoffDate.minusSeconds(seconds);
        }

        // Delete records where executionTime is older than the cutoff
        Query deleteQuery = new Query(Criteria.where("executionTime").lte(cutoffDate));

        DeleteResult result = mongoTemplate.remove(deleteQuery, EventScheduleOutput.class);

        return result.getDeletedCount() > 0; // Returns true if any records were deleted
    }
}
