package com.rits.stepstatusservice.controller;


import com.rits.batchnorecipeheaderservice.exception.BatchNoRecipeHeaderException;
import com.rits.stepstatusservice.dto.BatchStepStatusRequest;
import com.rits.stepstatusservice.exception.BatchStepStatusException;
import com.rits.stepstatusservice.model.StepStatus;
import com.rits.stepstatusservice.service.BatchStepStatusService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("app/v1/batchstepstatus-service")
public class BatchStepStatusController {

    private final BatchStepStatusService batchStepStatusService;

    @PostMapping("/getStepStatus")
    public ResponseEntity<List<StepStatus>> getStepStatusByBatch(@RequestBody BatchStepStatusRequest batchStepStatusRequest){

        if(StringUtils.isEmpty(batchStepStatusRequest.getSite()))
            throw new BatchStepStatusException(113);

        try {
            return ResponseEntity.ok(batchStepStatusService.getStepStatusByBatch(batchStepStatusRequest));
        } catch (BatchStepStatusException batchStepStatusException){
            throw batchStepStatusException;
        } catch (BatchNoRecipeHeaderException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
