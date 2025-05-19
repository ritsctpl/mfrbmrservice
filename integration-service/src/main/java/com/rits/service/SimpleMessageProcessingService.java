package com.rits.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.model.SplitMessageEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.integration.repository.SplitMessageRepository;
import com.rits.processing.ProcessorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class SimpleMessageProcessingService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    @Autowired
    private ProcessorRegistry processorRegistry;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private SplitMessageRepository splitMessageRepository;

    @Autowired
    private DependencyManager dependencyManager;

    @Value("${integration.service.url}")
    private String integrationServiceUrl;

    private static final Logger log = LoggerFactory.getLogger(SimpleMessageProcessingService.class);
    @Autowired
    public SimpleMessageProcessingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        Map<String, Object> message;
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        customResponseEntity.setSite(integrationEntity.getSite());
        customResponseEntity.setCreatedDateTime(LocalDateTime.now());
        customResponseEntity.setIdentifier(integrationEntity.getIdentifier());
        WebClient webClient = webClientBuilder.build();

        try {
            // Convert the JSON string into a Map
            message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
            customResponseEntity.setInput(messageBody);

            // Fetch dynamic API URL by calling the integration service
            String apiResponse = webClient.get()
                    .uri(integrationServiceUrl + "/identifier/" + integrationEntity.getSite() + "/" + integrationEntity.getIdentifier())
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
                        (String) apiDetails.get("preprocessApi"),
                        integrationEntity.getTransformationType());
                customResponseEntity.setPreprocessJoltResponse(objectMapper.writeValueAsString(message));
            }

            // Save intermediate response after pre-processing
            responseRepository.save(customResponseEntity);

            // Check if the message contains the "message" field
            Object requestBody;
            if (message.containsKey("message")) {
                requestBody = message.get("message");
            } else {
                requestBody = message;
            }

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // Call the target API (apiToProcess)
            String apiToProcessUrl = (String) apiDetails.get("apiToProcess");
            String processedResponse = webClient.post()
                    .uri(apiToProcessUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBodyJson)
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
            customResponseEntity.setStatus("Pass");
            // Save the response entity
            responseRepository.save(customResponseEntity);

            // Step 7: Extract splitIdentifier from the message and check if it's part of the dependent flow
            if (message != null && message.get("splitIdentifier") != null) {
                String splitIdentifier = (String) message.get("splitIdentifier");

                Optional<SplitMessageEntity> dependentMessageOpt = splitMessageRepository.findById(splitIdentifier);

                dependentMessageOpt.ifPresent(dependentMessage -> {
                    // Mark as processed if found
                    dependencyManager.markMessageAsProcessed(dependentMessage);

                    // Step 8: Check for pending dependent messages and place the next one in the queue
                    checkAndProcessNextDependentMessage(dependentMessage.getParentIdentifier());
                });
            }

            /*String splitIdentifier = (String) message.get("splitIdentifier");

            if (splitIdentifier != null) {
                Optional<SplitMessageEntity> dependentMessageOpt = splitMessageRepository.findById(splitIdentifier);

                dependentMessageOpt.ifPresent(dependentMessage -> {
                    // Mark as processed if found
                    dependencyManager.markMessageAsProcessed(dependentMessage);

                    // Step 8: Check for pending dependent messages and place the next one in the queue
                    checkAndProcessNextDependentMessage(dependentMessage.getParentIdentifier());
                });
            }
*/

        } catch (Exception e) {
            e.printStackTrace();
            customResponseEntity.setStatusMessage(e.getMessage());
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
                customResponseEntity.setStatus("Fail");
                // Update the response entity in case of failure
                responseRepository.save(customResponseEntity);

            } catch (Exception failHandlerException) {
                failHandlerException.printStackTrace();
            }
        }
    }

    // Helper method to check for dependent messages and process the next one
    private void checkAndProcessNextDependentMessage(String parentIdentifier) {
        List<SplitMessageEntity> unprocessedMessages = splitMessageRepository.findByParentIdentifierAndProcessedOrderBySequenceAsc(parentIdentifier, false);

        // Process the next message in the queue, which has the lowest sequence number
        if (!unprocessedMessages.isEmpty()) {
            SplitMessageEntity nextMessage = unprocessedMessages.get(0);  // Pick the first unprocessed message
            enqueueMessage(nextMessage);  // Place the next message in the queue for processing
            dependencyManager.markMessageAsProcessed(nextMessage);  // Mark the message as processed
        }
    }

    // Helper method to enqueue the message into Kafka using the web client
    /*private void enqueueMessage(SplitMessageEntity splitMessageEntity) {
        WebClient webClient = webClientBuilder.build();
        webClient.post()
                .uri(integrationServiceUrl+"/enqueue-message")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(splitMessageEntity.getMessageBody())
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Blocks the thread until the request is done
    }
*/

    private void enqueueMessage(SplitMessageEntity splitMessageEntity) {
        boolean sendMessageNode = false;
        WebClient webClient = webClientBuilder.build();
        /*try {
            // Check if the message body is a list of messages
            List<Map<String, Object>> messages = objectMapper.readValue(splitMessageEntity.getMessageBody(), new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> message : messages) {
                webClient.post()
                        .uri(integrationServiceUrl + "/enqueue-message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(objectMapper.writeValueAsString(message))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
        } catch (Exception e) {
            try {
                // If it's not a list, treat it as a single message
                Map<String, Object> singleMessage = objectMapper.readValue(splitMessageEntity.getMessageBody(), new TypeReference<Map<String, Object>>() {});
                webClient.post()
                        .uri(integrationServiceUrl + "/enqueue-message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(singleMessage)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();  // Blocks the thread until the request is done
            } catch (Exception innerException) {
                throw new RuntimeException("Error enqueuing message", innerException);
            }
        }*/

        try {
            // Parse the message body
            JsonNode messageNode = objectMapper.readTree(splitMessageEntity.getMessageBody());
            JsonNode messageContent = messageNode.get("message");
            if (messageContent != null && messageContent.fields().hasNext()) {
                Map.Entry<String, JsonNode> firstEntry = messageContent.fields().next();
                JsonNode firstNode = firstEntry.getValue();
                if (firstNode.isArray()) {
                    // If the first node is an array, enqueue each item separately
                    // Extract identifier, parentIdentifier, and splitIdentifier
                    String identifier = messageNode.get("identifier").asText();
                    String parentIdentifier = messageNode.get("parentIdentifier").asText();
                    String splitIdentifier = messageNode.get("splitIdentifier").asText();

                    for (JsonNode message : firstNode) {

                        ObjectNode newMessageNode = objectMapper.createObjectNode();
                        newMessageNode.put("identifier", identifier);
                        newMessageNode.put("parentIdentifier", parentIdentifier);
                        newMessageNode.put("splitIdentifier", splitIdentifier);
                        newMessageNode.set("message", message);
                        String requestBody = objectMapper.writeValueAsString(message);

                        log.info("Enqueueing message: {}", requestBody); // Log the request body
                        String objectValue =  objectMapper.writeValueAsString(newMessageNode);
                        log.info("Enqueueing message: {}", objectValue); // Log the request body

                        webClient.post()
                                .uri(integrationServiceUrl + "/enqueue-message")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(newMessageNode)
                                .retrieve()
                                .bodyToMono(String.class)
                                .doOnError(ex -> log.error("Error response: {}", ex.getMessage())) // Log error response
                                .block();
                    }
                } else {
                    sendMessageNode = true;
                    //  throw new RuntimeException("Unsupported message format: first node is not an array");
                }
            } else {
                sendMessageNode = true;

            }

            if(sendMessageNode){
                // Treat as a single message
                String requestBody = objectMapper.writeValueAsString(messageNode);
                log.info("Enqueueing single message: {}", requestBody); // Log the request body

                webClient.post()
                        .uri(integrationServiceUrl + "/enqueue-message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnError(ex -> log.error("Error response: {}", ex.getMessage())) // Log error response
                        .block();
            }
        } catch (Exception e) {
            log.error("Error enqueuing message", e);
            throw new RuntimeException("Error enqueuing message", e);
        }

    }
}

/*
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

import java.util.Map;
import java.util.UUID;

@Service
public class SimpleMessageProcessingService {

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
    public SimpleMessageProcessingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        Map<String, Object> message;
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        WebClient webClient = webClientBuilder.build();

        try {
            // Convert the JSON string into a Map
            message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
            customResponseEntity.setInput(messageBody);

            // Fetch dynamic API URL by calling the integration service
            String apiResponse = webClient.get()
                    .uri(integrationServiceUrl + "/" + integrationEntity.getIdentifier())
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
*/
