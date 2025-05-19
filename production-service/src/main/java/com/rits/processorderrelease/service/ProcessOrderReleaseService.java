package com.rits.processorderrelease.service;
import com.rits.processorderrelease.dto.ProcessOrderReleaseRequest;
import com.rits.processorderrelease.dto.ProcessOrderReleaseResponse;

public interface ProcessOrderReleaseService {
    ProcessOrderReleaseResponse releaseOrders(ProcessOrderReleaseRequest request);

}
