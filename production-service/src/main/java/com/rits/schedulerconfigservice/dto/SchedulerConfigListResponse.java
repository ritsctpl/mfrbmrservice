package com.rits.schedulerconfigservice.dto;

import com.rits.schedulerconfigservice.model.SchedulerConfig;
import lombok.Data;

import java.util.List;
@Data
public class SchedulerConfigListResponse {
    private List<SchedulerConfig> responses; // List of configs

    public SchedulerConfigListResponse(List<SchedulerConfig> configs) {
        this.responses = configs;
    }
}
