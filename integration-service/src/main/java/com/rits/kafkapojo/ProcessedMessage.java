package com.rits.kafkapojo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "processed_messages")
public class ProcessedMessage {
    @Id
    private String messageId;
    private String topicName;
    private LocalDateTime processedAt;
    private String message;
    private String status; // PENDING, PROCESSED, FAILED
    private String errorMessage; // Optional, to store error details
    private Integer hours;
    private Integer minutes;
    private Integer seconds;
}
