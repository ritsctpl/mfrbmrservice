package com.rits.kafkapojo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "topic_configurations")
public class TopicConfiguration {
    @Id
    private String id;
    private String topicName;
    private boolean active; // Whether the topic is enabled
    private String apiUrl;  // The API endpoint to call when a message is received
}