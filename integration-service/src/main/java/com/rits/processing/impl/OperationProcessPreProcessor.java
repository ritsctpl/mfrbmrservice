package com.rits.processing.impl;

import com.rits.processing.PreProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("operationProcessPreProcessor")
public class OperationProcessPreProcessor implements PreProcessor {

    @Override
    public Map<String, Object> process(Map<String, Object> message) {
        if (message.containsKey("operationType") && (message.get("operationType") == null || message.get("operationType").toString().isEmpty())) {
            message.put("operationType", "Normal");
        }
        if (message.containsKey("status") && (message.get("status") == null || message.get("status").toString().isEmpty())) {
            message.put("status", "Releasable");
        }
        return message;
    }
}
