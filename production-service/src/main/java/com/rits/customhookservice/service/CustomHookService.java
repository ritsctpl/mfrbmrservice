package com.rits.customhookservice.service;

import com.rits.processorderrelease.dto.ProcessOrderReleaseRequest;
import com.rits.processorderstateservice.dto.ProcessOrderCompleteRequest;
import com.rits.processorderstateservice.dto.ProcessOrderStartRequest;

public interface CustomHookService {
    void checkLineclearance(ProcessOrderStartRequest request);

    void checkBuyoff(ProcessOrderStartRequest request);

    void checkBatchInWork(ProcessOrderStartRequest request);

    void orderRelease(ProcessOrderReleaseRequest request);

    void checkTolerance(ProcessOrderCompleteRequest request) throws Exception;
}
