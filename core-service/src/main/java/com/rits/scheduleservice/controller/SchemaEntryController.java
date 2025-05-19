package com.rits.scheduleservice.controller;

import com.rits.scheduleservice.dto.EventScheduleConfigDTO;
import com.rits.scheduleservice.model.EventScheduleConfig;
import com.rits.scheduleservice.model.EventScheduleOutput;
import com.rits.scheduleservice.service.SchemaEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/app/v1/schedule-service")
public class SchemaEntryController {

    @Autowired
    private SchemaEntryService schemaEntryService;

    // Create a new schema entry
    @PostMapping("createSchedule")
    public EventScheduleConfig createSchemaEntry(@RequestBody EventScheduleConfigDTO dto) {
        return schemaEntryService.createSchemaEntry(dto);
    }

    // Get all schema entries
    @PostMapping("getAllSchedules")
    public List<EventScheduleConfig> getAllSchemaEntries() {
        return schemaEntryService.getAllSchemaEntries();
    }

    // Update and reschedule an existing schema entry
    @PostMapping("updateSchedule")
    public EventScheduleConfig updateSchemaEntry(@RequestBody EventScheduleConfigDTO dto) {
        return schemaEntryService.updateSchemaEntry(dto.getId(), dto);
    }

    // Delete a schema entry
    @PostMapping("deleteSchedule")
    public void deleteSchemaEntry(@RequestBody EventScheduleConfigDTO dto) {
        schemaEntryService.deleteSchemaEntry(dto.getId());
    }

    @PostMapping("getScheduleStatus")
    public String getScheduleStatus(@RequestBody EventScheduleConfigDTO dto) {
        Optional<EventScheduleConfig> config = schemaEntryService.getSchemaEntryById(dto.getId());
        return config.map(EventScheduleConfig::getStatus)
                .orElse("Schedule not found");
    }

    @PostMapping("getScheduleOutput")
    public List<EventScheduleOutput> getScheduleOutput(@RequestBody EventScheduleConfigDTO dto) {
            return schemaEntryService.getSchemaEntryByScheduleId(dto.getId());
    }

    @PostMapping("/delete")
    public boolean deleteScheduleOutput(@RequestBody EventScheduleConfigDTO dto) {
        return schemaEntryService.deleteScheduleOutput(dto.getHours(), dto.getMinutes(), dto.getSeconds());
    }

}