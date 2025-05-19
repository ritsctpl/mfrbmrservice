package com.rits.hookservice.service;

import com.rits.processorderstateservice.dto.ProcessOrderStartRequest;
import org.springframework.stereotype.Component;

@Component("customServiceExtension")
public class CustomServiceExtension {

    /**
     * BEFORE extension hook for testHookableProcessStart.
     * It accepts the input request, may modify it, and returns the (possibly modified) request.
     */
    public ProcessOrderStartRequest beforeStartProcess(ProcessOrderStartRequest request) {
        System.out.println("CustomServiceExtension.beforeStartProcess original request: " + request);
        // Example: Modify the request if needed.
        // For instance, if you want to ensure that the first batch's batchNumber is upper-case:
        if (request != null && request.getStartBatches() != null && !request.getStartBatches().isEmpty()) {
            request.getStartBatches().get(0).setBatchNumber(
                    request.getStartBatches().get(0).getBatchNumber().toUpperCase()
            );
        }
        System.out.println("CustomServiceExtension.beforeStartProcess modified request: " + request);
        return request;
    }

    /**
     * AFTER extension hook for testHookableProcessStart.
     * It accepts the method’s result and returns a modified result.
     */
    public String afterStartProcess(String result) {
        System.out.println("CustomServiceExtension.afterStartProcess original result: " + result);
        String modifiedResult = result + " [Modified by CustomServiceExtension]";
        System.out.println("CustomServiceExtension.afterStartProcess modified result: " + modifiedResult);
        return modifiedResult;
    }
}
