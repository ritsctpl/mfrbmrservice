package com.rits.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.model.SplitMessageEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.integration.repository.SplitMessageRepository;
import com.rits.processing.ProcessorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SplitMessageProcessingService {

    @Autowired
    private ProcessorRegistry processorRegistry;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private SplitMessageRepository splitMessageRepository;

    @Autowired
    private DependencyManager dependencyManager;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${integration.service.url}")
    private String integrationServiceUrl;
    private final ObjectMapper objectMapper;  // For JSON handling

    private final WebClient.Builder webClientBuilder;  // For web client requests

    private static final Logger log = LoggerFactory.getLogger(SplitMessageProcessingService.class);

    @Autowired
    public SplitMessageProcessingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper();
    }

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        customResponseEntity.setSite(integrationEntity.getSite());
        customResponseEntity.setInput(messageBody);
        customResponseEntity.setCreatedDateTime(LocalDateTime.now());
        customResponseEntity.setIdentifier(integrationEntity.getIdentifier());
        try {
            // Step 1: Pre-process using XSLT or JOLT
            boolean processedAsXml = false;
            List<Map<String, Object>> splitMessages = new ArrayList<>();
            Map<String, Object> message =null;
            if (integrationEntity.isProcessSplitXslt()) {
                // Replace special characters like '&' with '-'
                messageBody = messageBody.replaceAll("&", "-");

                // Convert JSON to XML
                String xmlMessage = processorRegistry.convertJsonToXml(messageBody.trim());

                // Apply preprocess XSLT if specified
                String preprocessXslt = integrationEntity.getPreprocessXslt();
                if (preprocessXslt != null && !preprocessXslt.isEmpty()) {
                    xmlMessage = processorRegistry.applyXsltTransformation(preprocessXslt, xmlMessage);
                    customResponseEntity.setPreprocessXsltResponse(xmlMessage);
                    responseRepository.save(customResponseEntity);
                }

                processedAsXml = true;
            } else if ("JSONATA".equalsIgnoreCase(integrationEntity.getTransformationType())) {
                // JSONata Processing
                String preprocessJsonata = integrationEntity.getPreprocessJolt();  // Using the pre-process Jolt field for JSONata
                message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
                if (preprocessJsonata != null && !preprocessJsonata.isEmpty()) {
                  //  message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
                    splitMessages = processorRegistry.applyJsonataTransformation(message, preprocessJsonata);
                    messageBody = objectMapper.writeValueAsString(splitMessages);
                }

            }else {
                // Existing JOLT transformation logic
                String preprocessJolt = integrationEntity.getPreprocessJolt();
                message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
                if (preprocessJolt != null && !preprocessJolt.isEmpty()) {
                    splitMessages = processorRegistry.executeSplitPreProcessing(message, preprocessJolt, integrationEntity.getPreprocessApi(),integrationEntity.getTransformationType());
                    messageBody = objectMapper.writeValueAsString(splitMessages);
                }


            }

            // Step 2: Create SplitMessageEntities based on expectedSplits and XSLT
            if (integrationEntity.isProcessSplitXslt()|| "JSONATA".equalsIgnoreCase(integrationEntity.getTransformationType())) {
                for (IntegrationEntity.SplitDefinition splitDef : integrationEntity.getExpectedSplits()) {
                    Map<String, Object> splitMessage;

                    if (integrationEntity.isProcessSplitXslt()) {
                        // Split using XSLT
                        String processJoltXslt = splitDef.getProcessJoltXslt();
                        if (processJoltXslt != null && !processJoltXslt.isEmpty()) {
                            String splitXmlMessage = processorRegistry.applyXsltTransformation(processJoltXslt, processorRegistry.convertJsonToXml(messageBody));
                            String splitJsonMessage = processorRegistry.convertXmlToJson(splitXmlMessage);
                            splitMessage = objectMapper.readValue(splitJsonMessage, new TypeReference<Map<String, Object>>() {});
                        } else {
                            continue;  // Skip if there's no transformation defined
                        }
                        // Use a mutable HashMap to avoid issues when adding fields
                        Map<String, Object> splitMessageWithIdentifiers = new HashMap<>();
                        splitMessageWithIdentifiers.put("identifier", splitDef.getSplitIdentifier());
                        splitMessageWithIdentifiers.put("message", splitMessage);
                        splitMessages.add(splitMessageWithIdentifiers);

                    } else {  // JSONATA transformation
                        String jsonataSpec = splitDef.getProcessJoltXslt();  // Assume this field holds the JSONata spec in case of JSONata
                        if (jsonataSpec == null || jsonataSpec.isEmpty()) {
                            continue;  // Skip if there's no JSONata transformation defined
                        }
                        List<Map<String, Object>> transformedMessages = processorRegistry.applyJsonataTransformation(message, jsonataSpec);

                        for (Map<String, Object> jsonataSplitMessage : transformedMessages) {
                            Map<String, Object> jsonataSplitMessageWithIdentifiers = new HashMap<>();
                            jsonataSplitMessageWithIdentifiers.put("identifier", splitDef.getSplitIdentifier());
                            jsonataSplitMessageWithIdentifiers.put("message", jsonataSplitMessage);
                            splitMessages.add(jsonataSplitMessageWithIdentifiers);
                        }
                //        splitMessage = processorRegistry.applyJsonataTransformation(message, jsonataSpec).get(0);
                    }

                  /*  // Use a mutable HashMap to avoid issues when adding fields
                    Map<String, Object> splitMessageWithIdentifiers = new HashMap<>();
                    splitMessageWithIdentifiers.put("identifier", splitDef.getSplitIdentifier());
                    splitMessageWithIdentifiers.put("message", splitMessage);
                    splitMessages.add(splitMessageWithIdentifiers);*/
                }
                /*for (IntegrationEntity.SplitDefinition splitDef : integrationEntity.getExpectedSplits()) {
                    String processJoltXslt = splitDef.getProcessJoltXslt();
                    if (processJoltXslt != null && !processJoltXslt.isEmpty()) {
                        String splitXmlMessage = processorRegistry.applyXsltTransformation(processJoltXslt, processorRegistry.convertJsonToXml(messageBody));
                        String splitJsonMessage = processorRegistry.convertXmlToJson(splitXmlMessage);
                        Map<String, Object> splitMessage = objectMapper.readValue(splitJsonMessage, new TypeReference<Map<String, Object>>() {});
                     //   splitMessages.add(Map.of("identifier", splitDef.getSplitIdentifier(), "message", splitMessage));
                        // Use a mutable HashMap to avoid issues when adding fields
                        Map<String, Object> splitMessageWithIdentifiers = new HashMap<>(splitMessage);
                        splitMessageWithIdentifiers.put("identifier", splitDef.getSplitIdentifier());
                        splitMessageWithIdentifiers.put("message", splitMessage);
                        splitMessages.add(splitMessageWithIdentifiers);

                        //SplitMessageEntity splitMessageEntity = createSplitMessageEntityFromXml(splitXmlMessage, splitDef.getSequence(), integrationEntity.getIdentifier());
                       // splitMessageEntity.setProcessedAsXml(true);
                      //  splitMessageRepository.save(splitMessageEntity);
                    }
                }*/
            } //else {
                // Existing logic for splitting messages based on JOLT
            //    List<SplitMessageEntity> splitMessageEntities = processorRegistry.createSplitMessageEntitiesFromJson(messageBody, integrationEntity);
            //    splitMessageEntities.forEach(splitMessageRepository::save);
                String preprocessApi = integrationEntity.getPreprocessApi();
                if (preprocessApi != null && !preprocessApi.isEmpty() && splitMessages != null) {
                    splitMessages = splitMessages.stream()
                            .map(splitMessage -> processorRegistry.callApi(preprocessApi, splitMessage))
                            .collect(Collectors.toList());
                }

                customResponseEntity.setPreprocessApiResponse(objectMapper.writeValueAsString(splitMessages));
                responseRepository.save(customResponseEntity);

          //  }

            // Step 3: Save each message in SplitMessageEntity and determine the lowest sequence
            SplitMessageEntity lowestSequenceMessage = null;
            String parentId = UUID.randomUUID().toString();
            //store the parent in splitmessages with processed true.
            SplitMessageEntity parentMessageEntity = new SplitMessageEntity();
            parentMessageEntity.setId(parentId); // Set unique UUID for the message
            parentMessageEntity.setParentIdentifier(parentId);
            parentMessageEntity.setSplitIdentifier(integrationEntity.getIdentifier());
            parentMessageEntity.setMessageBody(objectMapper.writeValueAsString(messageBody));
            parentMessageEntity.setSequence("00");  // Set sequence from IntegrationEntity's expectedSplits
            parentMessageEntity.setProcessed(true);
            parentMessageEntity.setCreatedDateTime(LocalDateTime.now());

            splitMessageRepository.save(parentMessageEntity);

            for (Map<String, Object> splitMessage : splitMessages) {
                String splitIdentifier = (String) splitMessage.get("identifier");
                String splitUid = UUID.randomUUID().toString();
                // Retrieve the sequence from expectedSplits using the splitIdentifier
                String sequence = getSequenceForSplitIdentifier(integrationEntity, splitIdentifier);

                // Create and save SplitMessageEntity with a unique ID (UUID)
                SplitMessageEntity splitMessageEntity = new SplitMessageEntity();
                splitMessageEntity.setId(splitUid); // Set unique UUID for the message
                splitMessageEntity.setParentIdentifier(parentId);
                splitMessageEntity.setSplitIdentifier(splitIdentifier);
                // Append parentIdentifier and splitIdentifier to the splitMessage before saving it
                splitMessage.put("parentIdentifier", parentId);
                splitMessage.put("splitIdentifier", splitUid);
                splitMessageEntity.setMessageBody(objectMapper.writeValueAsString(splitMessage));
                splitMessageEntity.setSequence(sequence);  // Set sequence from IntegrationEntity's expectedSplits
                splitMessageEntity.setProcessed(false);
                splitMessageEntity.setCreatedDateTime(LocalDateTime.now());

                splitMessageRepository.save(splitMessageEntity);

                // Identify the lowest sequence message
                if (lowestSequenceMessage == null || compareSequence(splitMessageEntity.getSequence(), lowestSequenceMessage.getSequence()) < 0) {
                    lowestSequenceMessage = splitMessageEntity;
                }
            }

            // Step 4: Place the lowest sequence message in Kafka queue for processing
            if (lowestSequenceMessage != null) {
                log.info("Processing lowest sequence message: {} with sequence: {}", lowestSequenceMessage.getId(), lowestSequenceMessage.getSequence());
                try {
                    enqueueMessage(lowestSequenceMessage);
                    // Update the state of the dependent manager
                    dependencyManager.markMessageAsProcessed(lowestSequenceMessage);
                } catch (Exception e) {
                    log.error("Error processing message: {} with sequence: {}", lowestSequenceMessage.getId(), lowestSequenceMessage.getSequence(), e);
                    throw e;
                }
            }

            customResponseEntity.setApiToProcessResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 5: Post-process using JOLT
            String postProcessJolt = integrationEntity.getPostProcessJolt();
            if (postProcessJolt != null && !postProcessJolt.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.executePostProcessing(splitMessage, postProcessJolt, integrationEntity.getPostProcessApi(),integrationEntity.getTransformationType()))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessJoltResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 6: Post-process API, if applicable
            String postProcessApi = integrationEntity.getPostProcessApi();
            if (postProcessApi != null && !postProcessApi.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(postProcessApi, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessApiResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 7: Call pass handler, if everything succeeds
            String passHandlerUrl = integrationEntity.getPassHandler();
            if (passHandlerUrl != null && !passHandlerUrl.isEmpty()) {
                String passHandlerResponse = processorRegistry.callApi(passHandlerUrl, messageBody);
                customResponseEntity.setPassHandlerResponse(passHandlerResponse);
            }

            // Step 8: Check for dependent messages and place the next one in the queue
         /*   String splitIdentifier = (String) message.get("splitIdentifier");

            if (splitIdentifier != null) {
                Optional<SplitMessageEntity> dependentMessageOpt = splitMessageRepository.findById(splitIdentifier);

                dependentMessageOpt.ifPresent(dependentMessage -> {
                    // Mark as processed if found
                    dependencyManager.markMessageAsProcessed(dependentMessage);

                    // Step 8: Check for pending dependent messages and place the next one in the queue
                    checkAndProcessNextDependentMessage(dependentMessage.getParentIdentifier());
                });
            }*/
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

            customResponseEntity.setStatus("Pass");
            responseRepository.save(customResponseEntity);


          /*  // Proceed with enqueueing messages and post-processing
            SplitMessageEntity highestSequenceMessage = getHighestSequenceMessage(integrationEntity.getIdentifier());
            if (highestSequenceMessage != null) {
                enqueueMessage(highestSequenceMessage);
                dependencyManager.markMessageAsProcessed(highestSequenceMessage);
            }

            customResponseEntity.setApiToProcessResponse(objectMapper.writeValueAsString(highestSequenceMessage));
            responseRepository.save(customResponseEntity);*/

        } catch (Exception e) {
            e.printStackTrace();
            customResponseEntity.setStatusMessage(e.getMessage());
            // Invoke failHandler in case of an exception
            try {
                String failHandlerUrl = integrationEntity.getFailHandler();
                if (failHandlerUrl != null && !failHandlerUrl.isEmpty()) {
                    String failHandlerResponse = processorRegistry.callApi(failHandlerUrl, messageBody);
                    customResponseEntity.setFailHandlerResponse(failHandlerResponse);
                }
                customResponseEntity.setStatus("Fail");
                responseRepository.save(customResponseEntity);
            } catch (Exception failHandlerException) {
                failHandlerException.printStackTrace();
            }
        }
    }

    private SplitMessageEntity createSplitMessageEntityFromXml(String xmlMessage, String sequence, String identifier) {
        SplitMessageEntity splitMessageEntity = new SplitMessageEntity();
        splitMessageEntity.setId(UUID.randomUUID().toString());
        splitMessageEntity.setParentIdentifier(identifier);
        splitMessageEntity.setSequence(sequence);
        splitMessageEntity.setMessageBody(xmlMessage);  // Store as JSON after conversion if needed
        splitMessageEntity.setProcessed(false);
        splitMessageEntity.setProcessedAsXml(true);
        splitMessageEntity.setCreatedDateTime(LocalDateTime.now());
        return splitMessageEntity;
    }

    private void enqueueMessage(SplitMessageEntity splitMessageEntity) {
        boolean sendMessageNode = false;
        WebClient webClient = webClientBuilder.build();
        if (splitMessageEntity.isProcessedAsXml()) {
            try {
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
                throw new RuntimeException("Error enqueuing split XML messages", e);
            }
        } else {
            /*webClient.post()
                    .uri(integrationServiceUrl + "/enqueue-message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(splitMessageEntity.getMessageBody())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();  // Blocks the thread until the request is done*/
            /*try {
                // Parse the message body
                JsonNode messageNode = objectMapper.readTree(splitMessageEntity.getMessageBody());
                JsonNode messageContent = messageNode.get("message");
                if (messageContent != null && messageContent.fields().hasNext()) {
                    Map.Entry<String, JsonNode> firstEntry = messageContent.fields().next();
                    JsonNode firstNode = firstEntry.getValue();
                    if (firstNode.isArray()) {
                        // If the first node is an array, enqueue each item separately
                        for (JsonNode message : firstNode) {
                            webClient.post()
                                    .uri(integrationServiceUrl + "/enqueue-message")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(objectMapper.writeValueAsString(message))
                                    .retrieve()
                                    .bodyToMono(String.class)
                                    .block();
                        }
                    } else {
                        throw new RuntimeException("Unsupported message format: first node is not an array");
                    }
                } else {
                    // Treat as a single message
                    webClient.post()
                            .uri(integrationServiceUrl + "/enqueue-message")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(messageNode)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();  // Blocks the thread until the request is done
                }
            } catch (Exception e) {
                throw new RuntimeException("Error enqueuing message", e);
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

    private SplitMessageEntity getHighestSequenceMessage(String parentIdentifier) {
        List<SplitMessageEntity> unprocessedMessages = splitMessageRepository.findByParentIdentifierAndProcessedOrderBySequenceAsc(parentIdentifier, false);
        return unprocessedMessages.isEmpty() ? null : unprocessedMessages.get(0);
    }

    private String getSequenceForSplitIdentifier(IntegrationEntity integrationEntity, String splitIdentifier) {
        for (IntegrationEntity.SplitDefinition splitDef : integrationEntity.getExpectedSplits()) {
            if (splitDef.getSplitIdentifier().equals(splitIdentifier)) {
                return splitDef.getSequence();
            }
        }
        return "0";
    }
    // Helper method to check for dependent messages and process them
    private void checkAndProcessNextDependentMessage(String parentIdentifier) {
        List<SplitMessageEntity> unprocessedMessages = splitMessageRepository.findByParentIdentifierAndProcessedOrderBySequenceAsc(parentIdentifier, false);

        // Process the next message in the queue, which has the lowest sequence number
        if (!unprocessedMessages.isEmpty()) {
            SplitMessageEntity nextMessage = unprocessedMessages.get(0);  // Pick the first unprocessed message
            enqueueMessage(nextMessage);  // Place the next message in the queue for processing
            dependencyManager.markMessageAsProcessed(nextMessage);  // Mark the message as processed
        }
    }
    // Helper method to compare sequences
    private int compareSequence(String seq1, String seq2) {
        return seq1.compareTo(seq2);  // String-based comparison, may need to change based on actual sequence format
    }

    public boolean deleteSplitMessages(Integer hours, Integer minutes, Integer seconds) {
        Query latestRecordQuery = new Query()
                .with(Sort.by(Sort.Direction.DESC, "created_date_time"))
                .limit(1);

        SplitMessageEntity latestRecord = mongoTemplate.findOne(latestRecordQuery, SplitMessageEntity.class);

        if (latestRecord == null || latestRecord.getCreatedDateTime() == null) {
            return false; // No records found, nothing to delete
        }

        LocalDateTime cutoffDate = latestRecord.getCreatedDateTime();
        if (hours != null) {
            cutoffDate = cutoffDate.minusHours(hours);
        }
        if (minutes != null) {
            cutoffDate = cutoffDate.minusMinutes(minutes);
        }
        if (seconds != null) {
            cutoffDate = cutoffDate.minusSeconds(seconds);
        }

        // Delete records where createdDateTime is older than the cutoff
        Query deleteQuery = new Query(Criteria.where("created_date_time").lte(cutoffDate));

        DeleteResult result = mongoTemplate.remove(deleteQuery, SplitMessageEntity.class);

        return result.getDeletedCount() > 0; // Returns true if any records were deleted
    }

}


/*package com.rits.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SplitMessageProcessingService {

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
    private final ObjectMapper objectMapper ;  // For JSON handling

    private final WebClient.Builder webClientBuilder;  // For web client requests

    @Autowired
    public SplitMessageProcessingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = new ObjectMapper();
    }

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        customResponseEntity.setInput(messageBody);

        try {
            // Convert input messageBody to Map
            Map<String, Object> message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});

            // Step 1: Pre-process using JOLT
            String preprocessJolt = integrationEntity.getPreprocessJolt();
            if (preprocessJolt == null || preprocessJolt.isEmpty()) {
                throw new RuntimeException("Pre-process JOLT configuration is missing");
            }

            List<Map<String, Object>> splitMessages = processorRegistry.executeSplitPreProcessing(message, preprocessJolt, integrationEntity.getPreprocessApi());

            customResponseEntity.setPreprocessJoltResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 2: Pre-process API, if applicable
            String preprocessApi = integrationEntity.getPreprocessApi();
            if (preprocessApi != null && !preprocessApi.isEmpty() && splitMessages != null) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(preprocessApi, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPreprocessApiResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 3: Save each message in SplitMessageEntity and determine the highest sequence
            SplitMessageEntity highestSequenceMessage = null;
            String parentId = UUID.randomUUID().toString();
            //store the parent in splitmessages with processed true.
            SplitMessageEntity parentMessageEntity = new SplitMessageEntity();
            parentMessageEntity.setId(parentId); // Set unique UUID for the message
            parentMessageEntity.setParentIdentifier(parentId);
            parentMessageEntity.setSplitIdentifier(integrationEntity.getIdentifier());
            parentMessageEntity.setMessageBody(objectMapper.writeValueAsString(messageBody));
            parentMessageEntity.setSequence("00");  // Set sequence from IntegrationEntity's expectedSplits
            parentMessageEntity.setProcessed(true);

            splitMessageRepository.save(parentMessageEntity);

            for (Map<String, Object> splitMessage : splitMessages) {
                String splitIdentifier = (String) splitMessage.get("identifier");
                String splitUid = UUID.randomUUID().toString();
                // Retrieve the sequence from expectedSplits using the splitIdentifier
                String sequence = getSequenceForSplitIdentifier(integrationEntity, splitIdentifier);

                // Create and save SplitMessageEntity with a unique ID (UUID)
                SplitMessageEntity splitMessageEntity = new SplitMessageEntity();
                splitMessageEntity.setId(splitUid); // Set unique UUID for the message
                splitMessageEntity.setParentIdentifier(parentId);
                splitMessageEntity.setSplitIdentifier(splitIdentifier);
                // Append parentIdentifier and splitIdentifier to the splitMessage before saving it
                splitMessage.put("parentIdentifier", parentId);
                splitMessage.put("splitIdentifier", splitUid);
                splitMessageEntity.setMessageBody(objectMapper.writeValueAsString(splitMessage));
                splitMessageEntity.setSequence(sequence);  // Set sequence from IntegrationEntity's expectedSplits
                splitMessageEntity.setProcessed(false);

                splitMessageRepository.save(splitMessageEntity);

                // Identify the highest sequence message
                if (highestSequenceMessage == null || compareSequence(splitMessageEntity.getSequence(), highestSequenceMessage.getSequence()) > 0) {
                    highestSequenceMessage = splitMessageEntity;
                }
            }

            // Step 4: Place the highest sequence message in Kafka queue for processing
            if (highestSequenceMessage != null) {
                enqueueMessage(highestSequenceMessage);
                // Update the state of the dependent manager
                dependencyManager.markMessageAsProcessed(highestSequenceMessage);
            }

            customResponseEntity.setApiToProcessResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 5: Post-process using JOLT
            String postProcessJolt = integrationEntity.getPostProcessJolt();
            if (postProcessJolt != null && !postProcessJolt.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.executePostProcessing(splitMessage, postProcessJolt, null))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessJoltResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 6: Post-process API, if applicable
            String postProcessApi = integrationEntity.getPostProcessApi();
            if (postProcessApi != null && !postProcessApi.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(postProcessApi, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessApiResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 7: Call pass handler, if everything succeeds
            String passHandlerUrl = integrationEntity.getPassHandler();
            if (passHandlerUrl != null && !passHandlerUrl.isEmpty()) {
                String passHandlerResponse = processorRegistry.callApi(passHandlerUrl, messageBody);
                customResponseEntity.setPassHandlerResponse(passHandlerResponse);
            }

            // Step 8: Check for dependent messages and place the next one in the queue
            String splitIdentifier = (String) message.get("splitIdentifier");

            if (splitIdentifier != null) {
                Optional<SplitMessageEntity> dependentMessageOpt = splitMessageRepository.findById(splitIdentifier);

                dependentMessageOpt.ifPresent(dependentMessage -> {
                    // Mark as processed if found
                    dependencyManager.markMessageAsProcessed(dependentMessage);

                    // Step 8: Check for pending dependent messages and place the next one in the queue
                    checkAndProcessNextDependentMessage(dependentMessage.getParentIdentifier());
                });
            }
            customResponseEntity.setStatus("Pass");
            responseRepository.save(customResponseEntity);

        } catch (Exception e) {
            e.printStackTrace();
            customResponseEntity.setStatusMessage(e.getMessage());
            // Invoke failHandler in case of an exception
            try {
                String failHandlerUrl = integrationEntity.getFailHandler();
                if (failHandlerUrl != null && !failHandlerUrl.isEmpty()) {
                    String failHandlerResponse = processorRegistry.callApi(failHandlerUrl, messageBody);
                    customResponseEntity.setFailHandlerResponse(failHandlerResponse);
                }
                customResponseEntity.setStatus("Fail");
                // Save the response entity
                responseRepository.save(customResponseEntity);
            } catch (Exception failHandlerException) {
                failHandlerException.printStackTrace();
            }
        }
    }

    // Helper method to enqueue the message into Kafka using the web client
    private void enqueueMessage(SplitMessageEntity splitMessageEntity) {
        WebClient webClient = webClientBuilder.build();
        webClient.post()
                .uri(integrationServiceUrl+"/enqueue-message")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(splitMessageEntity.getMessageBody())
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Blocks the thread until the request is done
    }

    // Helper method to check for dependent messages and process them
    private void checkAndProcessNextDependentMessage(String parentIdentifier) {
        List<SplitMessageEntity> unprocessedMessages = splitMessageRepository.findByParentIdentifierAndProcessedOrderBySequenceAsc(parentIdentifier, false);

        // Process the next message in the queue, which has the lowest sequence number
        if (!unprocessedMessages.isEmpty()) {
            SplitMessageEntity nextMessage = unprocessedMessages.get(0);  // Pick the first unprocessed message
            enqueueMessage(nextMessage);  // Place the next message in the queue for processing
            dependencyManager.markMessageAsProcessed(nextMessage);  // Mark the message as processed
        }
    }
    // Retrieve the sequence for a given split identifier from expectedSplits in IntegrationEntity
    private String getSequenceForSplitIdentifier(IntegrationEntity integrationEntity, String splitIdentifier) {
        for (IntegrationEntity.SplitDefinition splitDef : integrationEntity.getExpectedSplits()) {
            if (splitDef.getSplitIdentifier().equals(splitIdentifier)) {
                return splitDef.getSequence();  // Return the sequence for the split identifier
            }
        }
        return "0";  // Default sequence if not found
    }

    // Helper method to compare sequences
    private int compareSequence(String seq1, String seq2) {
        return seq1.compareTo(seq2);  // String-based comparison, may need to change based on actual sequence format
    }
}*/
/*
second old Code.
package com.rits.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.processing.ProcessorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SplitMessageProcessingService {

    @Autowired
    private ProcessorRegistry processorRegistry;

    @Autowired
    private ResponseRepository responseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();  // For JSON handling

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        customResponseEntity.setInput(messageBody);

        try {
            // Convert input messageBody to Map
            Map<String, Object> message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});

            // Step 1: Pre-process using JOLT
            String preprocessJolt = integrationEntity.getPreprocessJolt();
            if (preprocessJolt == null || preprocessJolt.isEmpty()) {
                throw new RuntimeException("Pre-process JOLT configuration is missing");
            }

            List<Map<String, Object>> splitMessages = processorRegistry.executeSplitPreProcessing(message, preprocessJolt, integrationEntity.getPreprocessApi());

            customResponseEntity.setPreprocessJoltResponse(objectMapper.writeValueAsString(splitMessages));

            // Save response entity after pre-processing
            responseRepository.save(customResponseEntity);

            // Step 2: Pre-process API, if applicable
            String preprocessApi = integrationEntity.getPreprocessApi();
            if (preprocessApi != null && !preprocessApi.isEmpty() && splitMessages != null) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(preprocessApi, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPreprocessApiResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 3: Process with apiToProcess URL
            String apiToProcess = integrationEntity.getApiToProcess();
            if (apiToProcess != null && !apiToProcess.isEmpty()) {
                for (Map<String, Object> splitMessage : splitMessages) {
                    processorRegistry.callApi(apiToProcess, splitMessage);
                }
            }

            customResponseEntity.setApiToProcessResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 4: Post-process using JOLT
            String postProcessJolt = integrationEntity.getPostProcessJolt();
            if (postProcessJolt != null && !postProcessJolt.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.executePostProcessing(splitMessage, postProcessJolt, null))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessJoltResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 5: Post-process API, if applicable
            String postProcessApi = integrationEntity.getPostProcessApi();
            if (postProcessApi != null && !postProcessApi.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(postProcessApi, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessApiResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 6: Call pass handler, if everything succeeds
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
}*/

/*
package com.rits.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.integration.model.CustomResponseEntity;
import com.rits.integration.model.IntegrationEntity;
import com.rits.integration.repository.ResponseRepository;
import com.rits.processing.ProcessorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SplitMessageProcessingService {

    @Autowired
    private ProcessorRegistry processorRegistry;

    @Autowired
    private ResponseRepository responseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();  // For JSON handling

    public void processMessage(String messageBody, IntegrationEntity integrationEntity) {
        CustomResponseEntity customResponseEntity = new CustomResponseEntity();
        customResponseEntity.setInput(messageBody);

        try {
            // Convert messageBody to Map<String, Object>
            Map<String, Object> message = objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});

            // Step 1: Pre-process using JOLT
            String preprocessJolt = integrationEntity.getPreprocessJolt();
            if (preprocessJolt == null || preprocessJolt.isEmpty()) {
                throw new RuntimeException("Pre-process JOLT configuration is missing");
            }

            List<Map<String, Object>> splitMessages = processorRegistry.executeSplitPreProcessing(
                    message,
                    preprocessJolt,
                    integrationEntity.getPreprocessApi());

            customResponseEntity.setPreprocessJoltResponse(objectMapper.writeValueAsString(splitMessages));

            // Save response entity after pre-processing
            responseRepository.save(customResponseEntity);

            // Step 2: Pre-process API, if applicable
            String preprocessApi = integrationEntity.getPreprocessApi();
            if (preprocessApi != null && !preprocessApi.isEmpty() && splitMessages != null) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(preprocessApi, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPreprocessApiResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 3: Process with apiToProcess URL
            String apiToProcess = integrationEntity.getApiToProcess();
            if (apiToProcess != null && !apiToProcess.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(apiToProcess, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setApiToProcessResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 4: Post-process using JOLT
            String postProcessJolt = integrationEntity.getPostProcessJolt();
            if (postProcessJolt != null && !postProcessJolt.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.executePostProcessing(splitMessage, postProcessJolt, null))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessJoltResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 5: Post-process API, if applicable
            String postProcessApi = integrationEntity.getPostProcessApi();
            if (postProcessApi != null && !postProcessApi.isEmpty()) {
                splitMessages = splitMessages.stream()
                        .map(splitMessage -> processorRegistry.callApi(postProcessApi, splitMessage))
                        .collect(Collectors.toList());
            }

            customResponseEntity.setPostProcessApiResponse(objectMapper.writeValueAsString(splitMessages));
            responseRepository.save(customResponseEntity);

            // Step 6: Call pass handler, if everything succeeds
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
*/
