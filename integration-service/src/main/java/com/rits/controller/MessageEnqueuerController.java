package com.rits.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rits.event.MessagePublisherService;
import com.rits.integration.service.IntegrationService;
import com.rits.service.MongoDeduplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/v1/integration-service")
public class MessageEnqueuerController {

    @Autowired
    private MessagePublisherService messagePublisherService;

    /*@Autowired
    private IntegrationService integrationService;*/
    @Autowired
    private MongoDeduplicationService deduplicationService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON handling
    private final XmlMapper xmlMapper = new XmlMapper(); // For XML handling

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

        /*// Validate identifier against integration service
        boolean isValidIdentifier = integrationService.isValidOrderIdentifier(identifier);
        if (!isValidIdentifier) {
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid identifier. No matching identifier for the order download.\"}");
        }*/

        // Validate identifier
        if (!messageBody.containsKey("identifier") || messageBody.get("identifier") == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Identifier is required\"}");
        }

        String identifierMap = messageBody.get("identifier").toString();

        // 🔐 Generate secure, hash-based message ID
        String messageId = generateHashedMessageId(identifierMap, messageBody);

        // ✅ Deduplication Check
        if (deduplicationService.isDuplicate(messageId)) {
            return ResponseEntity.status(409).body("{\"error\": \"Duplicate message detected. Skipping enqueue.\"}");
        }


        // Publish message to Kafka
        messagePublisherService.publishMessage(identifier, messageBody);

        // Return success response
        return ResponseEntity.ok("{\"status\": \"Message sent to Kafka queue: " + identifier + "\"}");
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

    private String generateHashedMessageId(String identifier, Map<String, Object> messageBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true); // ensure consistent key order
            String canonicalJson = mapper.writeValueAsString(messageBody);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalJson.getBytes(StandardCharsets.UTF_8));

            String base64Hash = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return identifier + "-" + base64Hash;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate message ID", e);
        }
    }

}


/*
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rits.event.MessagePublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/v1/integration-service")
public class MessageEnqueuerController {

    @Autowired
    private MessagePublisherService messagePublisherService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON handling
    private final XmlMapper xmlMapper = new XmlMapper(); // For XML handling

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

*/
