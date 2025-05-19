package com.rits.stepstatusservice.service;


import com.rits.stepstatusservice.dto.BatchStepStatusRequest;
import com.rits.stepstatusservice.model.StepStatus;

import java.util.List;

public interface BatchStepStatusService {
    List<StepStatus> getStepStatusByBatch(BatchStepStatusRequest batchStepStatusRequest) throws Exception;
}
