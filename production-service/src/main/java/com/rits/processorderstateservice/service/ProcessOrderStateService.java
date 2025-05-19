package com.rits.processorderstateservice.service;

import com.rits.processorderstateservice.dto.*;

public interface ProcessOrderStateService {
    ProcessOrderStartResponse startProcess(ProcessOrderStartRequest request) throws Exception;

    ProcessOrderSignoffResponse signoffProcess(ProcessOrderSignoffRequest request) throws Exception;

    ProcessOrderCompleteResponse processOrderComplete(ProcessOrderCompleteRequest request) throws Exception;

    String testHookableProcessStart(ProcessOrderStartRequest request) throws Exception;
}
