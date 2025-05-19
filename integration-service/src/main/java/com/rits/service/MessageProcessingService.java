package com.rits.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.processing.ProcessorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageProcessingService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    @Autowired
    private ProcessorRegistry processorRegistry;

    @Autowired
    private ResponseRepository responseRepository;

    @Value("${integration.service.url}/identifier")
    private String integrationServiceUrl;

    @Autowired
    public MessageProcessingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        Map<String, Object> message;
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        customResponseEntity.setSite(integrationEntity.getSite());
        WebClient webClient = webClientBuilder.build();

        try {
            // Convert the JSON string into a Map
            message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
            customResponseEntity.setInput(messageBody);
            customResponseEntity.setCreatedDateTime(LocalDateTime.now());
            customResponseEntity.setIdentifier(integrationEntity.getIdentifier());
            // Fetch dynamic API URL by calling the integration service
            String apiResponse = webClient.get()
                    .uri(integrationServiceUrl + "/" + integrationEntity.getSite() + "/" + integrationEntity.getIdentifier())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (apiResponse == null || apiResponse.isEmpty()) {
                throw new RuntimeException("No API URL found for the identifier");
            }

            // Parse the API response into a Map
            Map<String, Object> apiDetails = objectMapper.readValue(apiResponse, new TypeReference<Map<String, Object>>() {});
            String identifier = integrationEntity.getIdentifier();
            String uniqueId = UUID.randomUUID().toString();
            customResponseEntity.setMessageId(uniqueId);

            // Pre-process the message if necessary
            if (apiDetails.containsKey("preprocessJolt") || apiDetails.containsKey("preprocessApi")) {
                message = processorRegistry.executePreProcessing(identifier, message,
                        (String) apiDetails.get("preprocessJolt"),
                        (String) apiDetails.get("preprocessApi"),integrationEntity.getTransformationType());
                customResponseEntity.setPreprocessJoltResponse(objectMapper.writeValueAsString(message));
            }

            // Save intermediate response after pre-processing
            responseRepository.save(customResponseEntity);

            // Call the target API (apiToProcess)
            String apiToProcessUrl = (String) apiDetails.get("apiToProcess");
            String processedResponse = webClient.post()
                    .uri(apiToProcessUrl)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            customResponseEntity.setApiToProcessResponse(processedResponse);

            // Save intermediate response after calling the target API
            responseRepository.save(customResponseEntity);

            // Post-process the response if necessary
            if (apiDetails.containsKey("postProcessJolt") || apiDetails.containsKey("postProcessApi")) {
                Map<String, Object> postProcessedMessage = processorRegistry.executePostProcessing(
                        Map.of("rawResponse", processedResponse),
                        (String) apiDetails.get("postProcessJolt"),
                        (String) apiDetails.get("postProcessApi"),
                        integrationEntity.getTransformationType()
                );
                customResponseEntity.setPostProcessJoltResponse(objectMapper.writeValueAsString(postProcessedMessage));
            }

            // Save the intermediate response after post-processing
            responseRepository.save(customResponseEntity);

            // Everything went fine, now call passHandler
            String passHandlerUrl = (String) apiDetails.get("passHandler");
            if (passHandlerUrl != null && !passHandlerUrl.isEmpty()) {
                String passHandlerResponse = webClient.post()
                        .uri(passHandlerUrl)
                        .bodyValue(messageBody) // Send the original message to passHandler
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                customResponseEntity.setPassHandlerResponse(passHandlerResponse);
            }

            // Save the response entity
            responseRepository.save(customResponseEntity);

        } catch (Exception e) {
            e.printStackTrace();

            // Call the failHandler
            try {
                String failHandlerUrl = integrationEntity.getFailHandler();
                if (failHandlerUrl != null && !failHandlerUrl.isEmpty()) {
                    String failHandlerResponse = webClient.post()
                            .uri(failHandlerUrl)
                            .bodyValue(messageBody) // Send the original message to failHandler
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    customResponseEntity.setFailHandlerResponse(failHandlerResponse);
                }

                // Update the response entity in case of failure
                responseRepository.save(customResponseEntity);

            } catch (Exception failHandlerException) {
                // Log or handle any issues calling the fail handler
                failHandlerException.printStackTrace();
            }
        }
    }
}

/*
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.processing.ProcessorRegistry;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Service("messageProcessingService")
public class MessageProcessingService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    @Autowired
    private ProcessorRegistry processorRegistry;

    @Autowired
    private ResponseRepository responseRepository;

    @Value("${integration.service.url}/identifier")
    private String integrationServiceUrl;

    @Autowired
    public MessageProcessingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }

    @Handler
    public void processMessage(String messageBody, String topic) {
        Map<String, Object> message;
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        WebClient webClient = webClientBuilder.build();

        try {
            // Convert the JSON string into a Map
            message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
            customResponseEntity.setInput(messageBody);

            // Fetch dynamic API URL by calling the integration service
            String apiResponse = webClient.get()
                    .uri(integrationServiceUrl + "/" + topic)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (apiResponse == null || apiResponse.isEmpty()) {
                throw new RuntimeException("No API URL found for the identifier");
            }

            // Parse the API response into a Map
            Map<String, Object> apiDetails = objectMapper.readValue(apiResponse, new TypeReference<Map<String, Object>>() {});
            String identifier = (String) message.get("identifier");
            String uniqueId = UUID.randomUUID().toString();
            customResponseEntity.setIdentifier(uniqueId);

            // Pre-process the message if necessary
            if (apiDetails.containsKey("preprocessJolt") || apiDetails.containsKey("preprocessApi")) {
                message = processorRegistry.executePreProcessing(identifier, message,
                        (String) apiDetails.get("preprocessJolt"),
                        (String) apiDetails.get("preprocessApi"));
                customResponseEntity.setPreprocessJoltResponse(objectMapper.writeValueAsString(message));
            }

            // Save intermediate response after pre-processing
            responseRepository.save(customResponseEntity);

            // Call the target API (apiToProcess)
            String apiToProcessUrl = (String) apiDetails.get("apiToProcess");
            String processedResponse = webClient.post()
                    .uri(apiToProcessUrl)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            customResponseEntity.setApiToProcessResponse(processedResponse);

            // Save intermediate response after calling the target API
            responseRepository.save(customResponseEntity);

            // Post-process the response if necessary
            if (apiDetails.containsKey("postProcessJolt") || apiDetails.containsKey("postProcessApi")) {
                Map<String, Object> postProcessedMessage = processorRegistry.executePostProcessing(
                        identifier,
                        Map.of("rawResponse", processedResponse),
                        (String) apiDetails.get("postProcessJolt"),
                        (String) apiDetails.get("postProcessApi")
                );
                customResponseEntity.setPostProcessJoltResponse(objectMapper.writeValueAsString(postProcessedMessage));
            }

            // Save the intermediate response after post-processing
            responseRepository.save(customResponseEntity);

            // Everything went fine, now call passHandler
            String passHandlerUrl = (String) apiDetails.get("passHandler");
            if (passHandlerUrl != null && !passHandlerUrl.isEmpty()) {
                String passHandlerResponse = webClient.post()
                        .uri(passHandlerUrl)
                        .bodyValue(messageBody) // Send the original message to passHandler
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                customResponseEntity.setPassHandlerResponse(passHandlerResponse);
            }

            // Save the response entity
            responseRepository.save(customResponseEntity);

        } catch (Exception e) {
            e.printStackTrace();

            // Call the failHandler
            try {
                String apiResponse = webClient.get()
                        .uri(integrationServiceUrl + "/" + topic)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (apiResponse != null && !apiResponse.isEmpty()) {
                    Map<String, Object> apiDetails = objectMapper.readValue(apiResponse, new TypeReference<Map<String, Object>>() {});

                    String failHandlerUrl = (String) apiDetails.get("failHandler");
                    if (failHandlerUrl != null && !failHandlerUrl.isEmpty()) {
                        String failHandlerResponse = webClient.post()
                                .uri(failHandlerUrl)
                                .bodyValue(messageBody) // Send the original message to failHandler
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        customResponseEntity.setFailHandlerResponse(failHandlerResponse);
                    }
                }

                // Update the response entity in case of failure
                responseRepository.save(customResponseEntity);

            } catch (Exception failHandlerException) {
                // Log or handle any issues calling the fail handler
                failHandlerException.printStackTrace();
            }
        }
    }

}
*/
