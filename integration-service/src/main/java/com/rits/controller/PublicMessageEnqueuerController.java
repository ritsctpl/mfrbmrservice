package com.rits.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rits.event.MessagePublisherService;
import com.rits.event.WebClientCallEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/public/app/v1/integration-service")
@RequiredArgsConstructor
public class PublicMessageEnqueuerController {
    private final ApplicationEventPublisher eventPublisher;
    @Autowired
    private MessagePublisherService messagePublisherService;



    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON handling
    private final XmlMapper xmlMapper = new XmlMapper(); // For XML handling
    @Value("${productionLog-service.uri}/save")
    private String productionLogUrl;
    @PostMapping(value = "/enqueue-message", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> enqueueMessage(@RequestBody String requestBody, @RequestHeader(value = HttpHeaders.CONTENT_TYPE) String contentType) {
        Map<String, Object> messageBody;

        try {
            // Handle XML and JSON input based on Content-Type
            if (MediaType.APPLICATION_XML_VALUE.equals(contentType)) {
                // Convert XML string to Map and ensure consistency in structure
                messageBody = convertXmlToMap(requestBody);
                messageBody = ensureConsistentFormat(messageBody);
            } else if (MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
                // Convert JSON string to Map
                messageBody = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});
            } else {
                return ResponseEntity.status(415).body("{\"error\": \"Unsupported Media Type\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid input format\"}");
        }

        // Validate the request body and extract the identifier
        if (!messageBody.containsKey("identifier") || messageBody.get("identifier") == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Identifier is required\"}");
        }

        String identifier = messageBody.get("identifier").toString();

        // Publish message to Kafka
        messagePublisherService.publishMessage(identifier, messageBody);

        // Return success response
        return ResponseEntity.ok("{\"status\": \"Message sent to Kafka queue: " + identifier + "\"}");
    }

    @PostMapping(value = "/testconnection", consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.TEXT_PLAIN_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> requestBody) {
        // Log the received message (optional)
        System.out.println("Received message: " + requestBody);

        // Return a response indicating everything is okay
        return ResponseEntity.ok("{\"status\": \"ok to go\"}");
    }

    // JSON handling
    @PostMapping(value = "/testconnection/json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receiveJsonMessage(@RequestBody Map<String, Object> requestBody) {
        // Log the received message (optional)
        System.out.println("Received JSON message: " + requestBody);
        eventPublisher.publishEvent(new WebClientCallEvent(this, productionLogUrl, requestBody));
        return ResponseEntity.ok("{\"status\": \"ok to go\"}");
    }
    @PostMapping(value = "/sapMeConnection/json", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sapMeConnection(@RequestBody Map<String, Object> requestBody) {
        // Log the received message (optional)
        System.out.println("Received JSON message: " + requestBody);

        // Extract identifier from the request body
        String identifier = (String) requestBody.get("identifier");
        if (identifier == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Identifier is missing in the request body.\"}");
        }

        // Publish the message
        try {
            messagePublisherService.publishMessage(identifier, requestBody);
        } catch (Exception e) {
            // Log the error and return a failure response
            System.err.println("Error while publishing message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to publish message.\"}");
        }

        return ResponseEntity.ok("{\"status\": \"ok to go\"}");
    }

    @PostMapping(value = "/sapMeConnection/json/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sapMeConnectionJsonList(@RequestBody List<Map<String, Object>> requestBodyList) throws JsonProcessingException {
        // Log the received messages
        System.out.println("Received JSON messages: " + requestBodyList);

        List<String> errors = new ArrayList<>();

        // Process each request in the list
        for (Map<String, Object> requestBody : requestBodyList) {
            String identifier = (String) requestBody.get("identifier");
            if (identifier == null) {
                errors.add("Identifier is missing in one of the requests");
                continue;
            }

            try {
                messagePublisherService.publishMessage(identifier, requestBody);
            } catch (Exception e) {
                System.err.println("Error while publishing message: " + e.getMessage());
                errors.add("Failed to publish message for identifier: " + identifier);
            }
        }

        // Return appropriate response based on errors
        if (!errors.isEmpty()) {
            String errorResponse = "{\"errors\": " + new ObjectMapper().writeValueAsString(errors) + "}";
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(errorResponse);
        }

        return ResponseEntity.ok("{\"status\": \"all messages processed successfully\"}");
    }

    // Plain text handling
    @PostMapping(value = "/testconnection/text", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receiveTextMessage(@RequestBody String requestBody) {
        // Log the received message (optional)
        System.out.println("Received text message: " + requestBody);

        return ResponseEntity.ok("{\"status\": \"ok to go\"}");
    }

    // Method to convert XML to Map
    private Map<String, Object> convertXmlToMap(String xml) {
        try {
            return xmlMapper.readValue(xml, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error converting XML to Map", e);
        }
    }

    // Utility method to ensure lists are in the correct format
    private Map<String, Object> ensureConsistentFormat(Map<String, Object> message) {
        String[] possibleListFields = {"certificationList", "subStepList", "activityHookList", "operationCustomDataList"};
        for (String field : possibleListFields) {
            if (message.containsKey(field)) {
                Object value = message.get(field);
                if (!(value instanceof List)) {
                    // If the value is not a list, wrap it in a list
                    List<Object> listValue = new ArrayList<>();
                    listValue.add(value);
                    message.put(field, listValue);
                }
            }
        }
        return message;
    }
}
