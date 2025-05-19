package com.rits.integration.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "split_messages")
@Data
public class SplitMessageEntity {

    @Id
    private String id;  // Unique ID for the split message
    private String parentIdentifier; // Identifier for the parent message or process
    private String splitIdentifier;  // Unique identifier for this specific split message
    private String sequence;         // Sequence number to maintain ordered processing
    private boolean processed;       // Flag to mark whether this message has been processed
    private String messageBody;      // Original message content in JSON or XML format
    private boolean useKafkaStreams; // Flag to indicate whether Kafka Streams are used for processing

    private boolean processedAsXml;
    private Integer hours;
    private Integer minutes;
    private Integer seconds;


    @CreatedDate
    @Field("created_date_time")
    private LocalDateTime createdDateTime; // Date and time when the message was created

    // Constructors, Getters, and Setters

    public SplitMessageEntity() {
    }

    public SplitMessageEntity(String parentIdentifier, String splitIdentifier, String sequence, String messageBody, boolean useKafkaStreams) {
        this.parentIdentifier = parentIdentifier;
        this.splitIdentifier = splitIdentifier;
        this.sequence = sequence;
        this.processed = false; // Initially set to false, indicating unprocessed status
        this.messageBody = messageBody;
        this.useKafkaStreams = useKafkaStreams;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public boolean isUseKafkaStreams() {
        return useKafkaStreams;
    }

    public void setUseKafkaStreams(boolean useKafkaStreams) {
        this.useKafkaStreams = useKafkaStreams;
    }

    public boolean isProcessedAsXml() {
        return processedAsXml;
    }

    public void setProcessedAsXml(boolean processedAsXml) {
        this.processedAsXml = processedAsXml;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
}
