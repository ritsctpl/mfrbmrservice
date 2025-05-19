package com.rits.schedulerconfigservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.schedulerconfigservice.dto.SchedulerConfigListResponse;
import com.rits.schedulerconfigservice.dto.SchedulerRequest;
import com.rits.schedulerconfigservice.exception.SchedulerException;
import com.rits.schedulerconfigservice.model.MessageDetails;
import com.rits.schedulerconfigservice.model.SchedulerConfig;
import com.rits.schedulerconfigservice.model.SchedulerConfigResponse;
import com.rits.schedulerconfigservice.repository.SchedulerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Autowired
    private SchedulerRepository schedulerRepository;

    private final WebClient.Builder webClientBuilder;
    private final MessageSource localMessageSource;
    public String getFormattedMessage(int code, Object... args) {
        return localMessageSource.getMessage(String.valueOf(code), args, Locale.getDefault());
    }

    @Override
    public SchedulerConfigResponse createSchedulerConfig(SchedulerRequest schedularRequest) throws Exception {

        try {
            validateSchedulerConfig(schedularRequest);

            SchedulerConfig config = schedulerBuilder(schedularRequest);
            config.setCreatedDateTime(LocalDateTime.now());
            config.setCreatedBy(schedularRequest.getUser());

            SchedulerConfig savedConfig = schedulerRepository.save(config);

            return SchedulerConfigResponse.builder()
                    .response(savedConfig)
                    .message_details(new MessageDetails(schedularRequest.getEntityId() + " Scheduler configuration created", "S"))
                    .build();

        } catch (SchedulerException e) {
            throw  e;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public SchedulerConfigResponse updateSchedulerConfig(Integer entityId, SchedulerRequest schedularRequest) throws Exception {

        try {
            schedularRequest.setEntityId(entityId);
            validateSchedulerConfig(schedularRequest);
            SchedulerConfig schedulerConfig = schedulerRepository.findBySiteAndEntityId(schedularRequest.getSite(), entityId);
            if (schedulerConfig == null) {
                return SchedulerConfigResponse.builder()
                        .message_details(new MessageDetails(schedularRequest.getEntityId() + " : not present", "S"))
                        .build();
            }

            SchedulerConfig config = schedulerBuilder(schedularRequest);
            config.setCreatedBy(schedulerConfig.getCreatedBy());
            config.setCreatedDateTime(schedulerConfig.getCreatedDateTime());
            config.setModifiedDateTime(LocalDateTime.now());
            config.setModifiedBy(schedularRequest.getUser());

            SchedulerConfig updatedConfig = schedulerRepository.save(config);

            return SchedulerConfigResponse.builder()
                    .response(updatedConfig)
                    .message_details(new MessageDetails(schedularRequest.getEntityId() + "Scheduler configuration updated", "S"))
                    .build();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    @Override
    public SchedulerConfigResponse deleteSchedulerConfig(SchedulerRequest schedularRequest) throws Exception {
        try {
            SchedulerConfig schedulerConfig = schedulerRepository.findBySiteAndEntityId(schedularRequest.getSite(), schedularRequest.getEntityId());
            if (schedulerConfig == null) {
                return SchedulerConfigResponse.builder()
                        .message_details(new MessageDetails(schedularRequest.getEntityId() + " : not present", "E"))
                        .build();
            }
            schedulerConfig.setActive(0);
            schedulerConfig.setModifiedDateTime(LocalDateTime.now());
            String deletedMessage = getFormattedMessage(53, schedularRequest.getEntityId());
            return SchedulerConfigResponse.builder().message_details(new MessageDetails(deletedMessage, "S")).response(schedulerRepository.save(schedulerConfig)).build();
        }catch(Exception e){
        throw new Exception(e.getMessage());
        }
    }


    @Override
    public SchedulerConfigListResponse getAllSchedulerConfigs() throws Exception {
        List<SchedulerConfig> configs = schedulerRepository.findAll();
        return new SchedulerConfigListResponse(configs);
    }

    @Override
    public SchedulerConfigListResponse getConfigById(String site, Integer entityId) throws Exception {
        SchedulerConfig config = schedulerRepository.findBySiteAndEntityId(site, entityId);
        if (config == null) {
            throw new SchedulerException(1201);
        }

        List<SchedulerConfig> configList = List.of(config); // Use List.of() to create an immutable list
        return new SchedulerConfigListResponse(configList);
    }



    private SchedulerConfig schedulerBuilder(SchedulerRequest schedularRequest) {

        return SchedulerConfig.builder()
                .handle("SchedulerBO:" + schedularRequest.getSite() + "," + schedularRequest.getEntityId() + "," + schedularRequest.getEntityName())
                .site(schedularRequest.getSite())
                .entityType(schedularRequest.getEntityType())
                .entityId(schedularRequest.getEntityId())
                .entityName(schedularRequest.getEntityName())
                .eventType(schedularRequest.getEventType())
                .eventIntervalSeconds(schedularRequest.getEventIntervalSeconds())
                .nextRunTime(schedularRequest.getNextRunTime())
                .apiEndpoint(schedularRequest.getApiEndpoint())
                .apiInput(schedularRequest.getApiInput())
                .enabled(schedularRequest.getEnabled())
                .cronExpression(schedularRequest.getCronExpression())
                .active(1)
                .build();

    }

    private void validateSchedulerConfig(SchedulerRequest configRequest) {

        if (configRequest == null) {
            throw new SchedulerException(101);
        }

        if (configRequest.getEntityId() == null || configRequest.getEntityId() <= 0) {
            throw new SchedulerException(102);
        }

        if (StringUtils.isEmpty(configRequest.getEntityName())) {
            throw new SchedulerException(103);
        }

        if (StringUtils.isEmpty(configRequest.getEntityType())) {
            throw new SchedulerException(104);
        }

        // logic?
//        if (StringUtils.isEmpty(configRequest.getCronExpression())) {
//            if (configRequest.getEventIntervalSeconds() == null || configRequest.getEventIntervalSeconds() <= 0) {
//                throw new IllegalArgumentException("Either cronExpression or a positive eventIntervalSeconds is required.");
//            }
//        }

        if (configRequest.getEventIntervalSeconds() == null || configRequest.getEventIntervalSeconds() < 0) {
            throw new SchedulerException(105);
        }

        if (configRequest.getNextRunTime() == null) {
            throw new SchedulerException(106);
        }

        if (!configRequest.getNextRunTime().after(new Date())) {
            throw new SchedulerException(107);
        }

        if (configRequest.getApiInput() != null && !isValidJson(configRequest.getApiInput())) {
            throw new SchedulerException(108);
        }

        if (configRequest.getEnabled() == null) {
            throw new SchedulerException(109);
        }

        if (configRequest.getCronExpression() != null
                && !configRequest.getCronExpression().matches("^([0-5]?\\d|\\*)\\s([0-5]?\\d|\\*)\\s([01]?\\d|2[0-3]|\\*)\\s([1-9]|[12]\\d|3[01]|\\*)\\s([1-9]|1[0-2]|\\*)\\s([1-7]|\\?)$")) {
            throw new SchedulerException(110);
        }

//        if (configRequest.getCronExpression() != null
//                && !configRequest.getCronExpression().matches("^([0-5]?\\d|\\*)\\s([0-5]?\\d|\\*)\\s([01]?\\d|2[0-3]|\\*)\\s([1-9]|[12]\\d|3[01]|\\*)\\s([1-9]|1[0-2]|\\*)\\s([1-7]|\\*)$")) {
//            throw new IllegalArgumentException("Invalid cron expression format.");
//        }

        if (configRequest.getApiEndpoint() == null || configRequest.getApiEndpoint().trim().isEmpty()) {
            throw new SchedulerException(111);
        }
        if (!configRequest.getApiEndpoint().matches("https?://.*")) {
            throw new SchedulerException(112);
        }
    }

    private boolean isValidJson(String json) {
        try {
            new ObjectMapper().readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
