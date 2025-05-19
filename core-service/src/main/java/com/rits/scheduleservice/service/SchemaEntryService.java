package com.rits.scheduleservice.service;

import com.rits.scheduleservice.dto.EventScheduleConfigDTO;
import com.rits.scheduleservice.model.EventScheduleConfig;
import com.rits.scheduleservice.model.EventScheduleOutput;

import java.util.List;
import java.util.Optional;

public interface SchemaEntryService {

    /**
     * Creates a new schema entry and schedules the task.
     *
     * @param dto The DTO containing event schedule details.
     * @return The created EventScheduleConfig object.
     */
    EventScheduleConfig createSchemaEntry(EventScheduleConfigDTO dto);

    /**
     * Retrieves all schema entries.
     *
     * @return List of EventScheduleConfig objects.
     */
    List<EventScheduleConfig> getAllSchemaEntries();

    /**
     * Updates an existing schema entry and reschedules the task.
     *
     * @param id The ID of the schedule to update.
     * @param dto The DTO containing updated event schedule details.
     * @return The updated EventScheduleConfig object.
     */
    EventScheduleConfig updateSchemaEntry(String id, EventScheduleConfigDTO dto);

    /**
     * Deletes a schema entry and cancels the associated schedule.
     *
     * @param id The ID of the schedule to delete.
     */
    void deleteSchemaEntry(String id);

    Optional<EventScheduleConfig> getSchemaEntryById(String id);

    List<EventScheduleOutput> getSchemaEntryByScheduleId(String id);

    boolean deleteScheduleOutput(Integer hours, Integer minutes, Integer seconds);
}
