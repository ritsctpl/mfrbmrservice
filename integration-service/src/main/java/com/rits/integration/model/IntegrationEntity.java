package com.rits.integration.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "integration_entities")  // This defines the collection name
public class IntegrationEntity {

    @Id
    private String id;
    private String site;
    private String identifier;          // Unique identifier
    private String type;                // Type (simple, split, merge)
    private String operationType;       // New field for operation type (CREATE, UPDATE, DELETE)
    private String messageId;           // The message content
    private String preprocessJolt;      // XSLT for pre-processing
    private String preprocessApi;       // API URL for pre-processing
    private String apiToProcess;        // API URL to process
    private String postProcessJolt;     // XSLT for post-processing
    private String postProcessApi;      // API URL for post-processing
    private String passHandler;         // Success handler
    private String failHandler;         // Failure handler
    private List<SplitDefinition> expectedSplits; // List of split definitions
    private boolean processSplitXslt;
    private String preprocessXslt;
    private String transformationType;  // Transformation type (JOLT/JSONata)

    // Optionally add parentIdentifier and splitIdentifier fields
    private String parentIdentifier;     // Track parent identifier if needed
    private String splitIdentifier;      // Track split identifier if needed
    private LocalDateTime createdDateTime; // Date and time when the message was created
    public List<SplitDefinition> getExpectedSplits() {
        return expectedSplits;
    }

    public void setExpectedSplits(List<SplitDefinition> expectedSplits) {
        this.expectedSplits = expectedSplits;
    }

    // This method will return all unique Kafka topics (identifiers)
    public static List<String> getAllUniqueIdentifiers(List<IntegrationEntity> entities) {
        return entities.stream()
                .map(IntegrationEntity::getIdentifier)
                .distinct()
                .collect(Collectors.toList());  // Compatible with Java 8 and beyond
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getPreprocessJolt() {
        return preprocessJolt;
    }

    public void setPreprocessJolt(String preprocessJolt) {
        this.preprocessJolt = preprocessJolt;
    }

    public String getPreprocessApi() {
        return preprocessApi;
    }

    public void setPreprocessApi(String preprocessApi) {
        this.preprocessApi = preprocessApi;
    }

    public String getApiToProcess() {
        return apiToProcess;
    }

    public void setApiToProcess(String apiToProcess) {
        this.apiToProcess = apiToProcess;
    }

    public String getPostProcessJolt() {
        return postProcessJolt;
    }

    public void setPostProcessJolt(String postProcessJolt) {
        this.postProcessJolt = postProcessJolt;
    }

    public String getPostProcessApi() {
        return postProcessApi;
    }

    public void setPostProcessApi(String postProcessApi) {
        this.postProcessApi = postProcessApi;
    }

    public String getPassHandler() {
        return passHandler;
    }

    public void setPassHandler(String passHandler) {
        this.passHandler = passHandler;
    }

    public String getFailHandler() {
        return failHandler;
    }

    public void setFailHandler(String failHandler) {
        this.failHandler = failHandler;
    }

    public boolean isProcessSplitXslt() {
        return processSplitXslt;
    }

    public void setProcessSplitXslt(boolean processSplitXslt) {
        this.processSplitXslt = processSplitXslt;
    }

    public boolean getProcessSplitXslt(){
    return this.processSplitXslt;
    }

    public String getPreprocessXslt() {
        return preprocessXslt;
    }

    public void setPreprocessXslt(String preprocessXslt) {
        this.preprocessXslt = preprocessXslt;
    }

    public String getParentIdentifier() {
        return parentIdentifier;
    }

    public void setParentIdentifier(String parentIdentifier) {
        this.parentIdentifier = parentIdentifier;
    }

    public String getSplitIdentifier() {
        return splitIdentifier;
    }

    public void setSplitIdentifier(String splitIdentifier) {
        this.splitIdentifier = splitIdentifier;
    }

    public String getTransformationType() {
        return transformationType;
    }

    public void setTransformationType(String transformationType) {
        this.transformationType = transformationType;
    }
    public String getSite() { return site; }

    public void setSite(String site) { this.site = site; }


    // Define the SplitDefinition class as a static inner class
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SplitDefinition {
        private String splitIdentifier;   // Unique identifier for each split
        private String sequence;// Sequence number to maintain order (optional)
        private String processJoltXslt;

        public String getSplitIdentifier() {
            return splitIdentifier;
        }

        public void setSplitIdentifier(String splitIdentifier) {
            this.splitIdentifier = splitIdentifier;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }


        public String getProcessJoltXslt() {
            return processJoltXslt;
        }

        public void setProcessJoltXslt(String processJoltXslt) {
            this.processJoltXslt = processJoltXslt;
        }
    }
}

/*
package com.rits.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "integration_entities")  // This defines the collection name
public class IntegrationEntity {

    @Id
    private String id;
    private String identifier;          // Unique identifier
    private String type;                // Type (simple, split, merge)
    private String operationType;       // New field for operation type (CREATE, UPDATE, DELETE)
    private String messageId;           // The message content
    private String preprocessJolt;      // XSLT for pre-processing
    private String preprocessApi;       // API URL for pre-processing
    private String apiToProcess;        // API URL to process
    private String postProcessJolt;     // XSLT for post-processing
    private String postProcessApi;      // API URL for post-processing
    private String passHandler;         // Success handler
    private String failHandler;         // Failure handler
    private List<SplitDefinition> expectedSplits;


    public List<SplitDefinition> getExpectedSplits() {
        return expectedSplits;
    }

    public void setExpectedSplits(List<SplitDefinition> expectedSplits) {
        this.expectedSplits = expectedSplits;
    }

    // This method will return all unique Kafka topics (identifiers)
    public static List<String> getAllUniqueIdentifiers(List<IntegrationEntity> entities) {
        return entities.stream()
                .map(IntegrationEntity::getIdentifier)
                .distinct()
                .collect(Collectors.toList());  // Compatible with Java 8 and beyond
    }
}
*/

/*
package com.rits.integration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data                   // Lombok annotation that automatically generates getters, setters, toString, equals, and hashCode methods
@AllArgsConstructor      // Generates a constructor with all fields
@NoArgsConstructor       // Generates a default constructor
@Document(collection = "integration_entities")  // This defines the collection name
public class IntegrationEntity {

    @Id
    private String id;

    private String identifier;          // Unique identifier
    private String type;                // Type (simple, split, merge)
    private String messageId;             // The message content
    private String preprocessJolt;      // XSLT for pre-processing
    private String preprocessApi;       // API URL for pre-processing
    private String apiToProcess;        // API URL to process
    private String postProcessJolt;     // XSLT for post-processing
    private String postProcessApi;      // API URL for post-processing
    private String passHandler;
    private String failHandler;

}
*/
