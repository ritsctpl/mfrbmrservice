package com.rits.service;

import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.service.MessageProcessingService;
import com.rits.service.MergeMessageProcessingService;
import com.rits.service.SplitMessageProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageRouter {

    @Autowired
    private SimpleMessageProcessingService messageProcessingService;

    @Autowired
    private SplitMessageProcessingService splitMessageProcessingService;

    @Autowired
    private MergeMessageProcessingService mergeMessageProcessingService;


    public void routeMessage(String messageBody, IntegrationEntity integrationEntity) {
        switch (integrationEntity.getType().toUpperCase()) {
            case "SIMPLE":
                messageProcessingService.processMessage(messageBody, integrationEntity);
                break;
            case "SPLIT":
                splitMessageProcessingService.processMessage(messageBody, integrationEntity);
                break;
            case "MERGE":
                mergeMessageProcessingService.processMessage(messageBody, integrationEntity);
                break;
            default:
                throw new IllegalArgumentException("Unsupported message type: " + integrationEntity.getType());
        }
    }
}
