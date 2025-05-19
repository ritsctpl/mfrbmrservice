package com.rits.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MessagePublisherService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishMessage(String identifier, Object message) {
        kafkaTemplate.send(identifier, message);  // Dynamically route to Kafka topic
    }
}
