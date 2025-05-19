package com.rits.processing.impl;

import com.rits.processing.PostProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("operationProcessPostProcessor")
public class OperationProcessPostProcessor implements PostProcessor {

    @Override
    public Map<String, Object> process(Map<String, Object> response) {
        response.put("status", "Processed");
        // Additional post-processing logic
        return response;
    }
}
