package com.rits.schedulerconfigservice.controller;

import com.rits.schedulerconfigservice.dto.SchedulerConfigListResponse;
import com.rits.schedulerconfigservice.dto.SchedulerRequest;
import com.rits.schedulerconfigservice.exception.SchedulerException;
import com.rits.schedulerconfigservice.model.SchedulerConfig;
import com.rits.schedulerconfigservice.model.SchedulerConfigResponse;
import com.rits.schedulerconfigservice.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/scheduler-config")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;
//    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/scheduler-conf")
    public SchedulerConfigResponse createSchedulerConfig(@RequestBody SchedulerRequest schedularRequest) {

        if(schedularRequest == null)
            throw new IllegalArgumentException("User request is missing.");

        if (StringUtils.isEmpty(schedularRequest.getSite()))
            throw new SchedulerException(5102);

        try {
            return schedulerService.createSchedulerConfig(schedularRequest);
        } catch (SchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/scheduler-conf-update")
    public SchedulerConfigResponse updateSchedulerConfig(@RequestBody SchedulerRequest schedularRequest) {

        if(schedularRequest == null)
            throw new IllegalArgumentException("User request is missing.");

        if (StringUtils.isNotEmpty(schedularRequest.getSite()) && schedularRequest.getEntityId() > 0)
            try {
                return schedulerService.updateSchedulerConfig(schedularRequest.getEntityId(), schedularRequest);
            } catch (SchedulerException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else
            throw new SchedulerException(5109);
    }

    @PostMapping("delete")
    public ResponseEntity<SchedulerConfigResponse> deleteSchedulerConfig(@RequestBody SchedulerRequest schedulerRequest) {
        SchedulerConfigResponse deleteResponse;
        if (schedulerRequest.getSite() != null && !schedulerRequest.getSite().isEmpty()) {
            try {
                deleteResponse = schedulerService.deleteSchedulerConfig(schedulerRequest);
                return ResponseEntity.ok(deleteResponse);

            } catch (SchedulerException schedulerException) {
                throw schedulerException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SchedulerException(1);
    }

    @PostMapping("/retrieve")
    public ResponseEntity<SchedulerConfigListResponse> getAllConfigs(@RequestBody SchedulerRequest schedulerRequest) {
        if (schedulerRequest.getSite() != null && !schedulerRequest.getSite().isEmpty()) {
            try {
                SchedulerConfigListResponse retrieve = schedulerService.getAllSchedulerConfigs();
                return ResponseEntity.ok(retrieve);
            } catch (SchedulerException schedulerException) {
                throw schedulerException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SchedulerException(1);
    }



    @PostMapping("/retrieveById")
    public ResponseEntity<SchedulerConfigListResponse> getConfigById(@RequestBody SchedulerRequest schedulerRequest)  {
        if (schedulerRequest.getSite() != null && !schedulerRequest.getSite().isEmpty()) {
            try {
                SchedulerConfigListResponse retrieve = schedulerService.getConfigById(schedulerRequest.getSite(), schedulerRequest.getEntityId());
                return ResponseEntity.ok(retrieve);
            } catch (SchedulerException schedulerException) {
                throw schedulerException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new SchedulerException(1);
    }

}
