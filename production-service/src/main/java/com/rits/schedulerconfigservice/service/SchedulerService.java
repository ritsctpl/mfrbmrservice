package com.rits.schedulerconfigservice.service;

import com.rits.schedulerconfigservice.dto.SchedulerConfigListResponse;
import com.rits.schedulerconfigservice.dto.SchedulerRequest;
import com.rits.schedulerconfigservice.model.SchedulerConfigResponse;

public interface SchedulerService {

    SchedulerConfigResponse createSchedulerConfig(SchedulerRequest configRequest) throws Exception;

    SchedulerConfigResponse updateSchedulerConfig(Integer entityId, SchedulerRequest configRequest) throws Exception;

    SchedulerConfigResponse deleteSchedulerConfig(SchedulerRequest schedularRequest) throws Exception;

    SchedulerConfigListResponse getAllSchedulerConfigs() throws Exception;

    SchedulerConfigListResponse getConfigById(String site, Integer entityId) throws Exception;
}
