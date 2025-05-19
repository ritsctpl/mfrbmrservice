package com.rits.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.processing.ProcessorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MergeMessageProcessingService {

    @Autowired
    private ProcessorRegistry processorRegistry;

    @Autowired
    private ResponseRepository responseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();  // For JSON handling

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        customResponseEntity.setSite(integrationEntity.getSite());
        customResponseEntity.setInput(messageBody);

        try {
            // Convert messageBody to Map<String, Object>
            Map<String, Object> message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});

            // Perform the required merge operations...
            // Assuming merging logic would be added here

            // Call pass handler, if everything succeeds
            String passHandlerUrl = integrationEntity.getPassHandler();
            if (passHandlerUrl != null && !passHandlerUrl.isEmpty()) {
                String passHandlerResponse = processorRegistry.callApi(passHandlerUrl, messageBody);
                customResponseEntity.setPassHandlerResponse(passHandlerResponse);
            }

            responseRepository.save(customResponseEntity);

        } catch (Exception e) {
            e.printStackTrace();

            // Invoke failHandler in case of an exception
            try {
                String failHandlerUrl = integrationEntity.getFailHandler();
                if (failHandlerUrl != null && !failHandlerUrl.isEmpty()) {
                    String failHandlerResponse = processorRegistry.callApi(failHandlerUrl, messageBody);
                    customResponseEntity.setFailHandlerResponse(failHandlerResponse);
                }

                // Save the response entity
                responseRepository.save(customResponseEntity);
            } catch (Exception failHandlerException) {
                failHandlerException.printStackTrace();
            }
        }
    }
}
