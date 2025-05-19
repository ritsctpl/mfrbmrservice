package com.rits.batchnohold.service;


import com.rits.batchnohold.dto.BatchNoHoldRequest;
import com.rits.batchnohold.model.BatchNoHoldMessageModel;

public interface BatchNoHoldService {
    BatchNoHoldMessageModel hold(BatchNoHoldRequest batchNoHoldRequest) throws Exception;

    BatchNoHoldMessageModel unhold(BatchNoHoldRequest batchNoHoldRequest) throws Exception;

    boolean isBatchOnHold(String site, String batchNo) throws Exception;
}
